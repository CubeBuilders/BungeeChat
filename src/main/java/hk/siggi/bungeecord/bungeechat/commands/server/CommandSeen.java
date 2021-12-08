package hk.siggi.bungeecord.bungeechat.commands.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.PlayerSession;
import hk.siggi.bungeecord.bungeechat.geolocation.Geolocation;
import hk.siggi.bungeecord.bungeechat.iptoisp.IPtoISP;
import hk.siggi.bungeecord.bungeechat.ontime.OnTime;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import hk.siggi.bungeecord.bungeechat.util.TimeUtil;
import hk.siggi.bungeecord.bungeechat.util.Util;
import io.siggi.iphelper.IP;
import io.siggi.iphelper.IPv4;
import io.siggi.iphelper.IPv6;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandSeen extends Command implements TabExecutor {

	public final BungeeChat plugin;

	public CommandSeen(BungeeChat plugin) {
		super("seen", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			return;
		}
		if (!sender.hasPermission("hk.siggi.bungeechat.seen")) {
			BaseComponent message = new TextComponent("");
			BaseComponent notAvailable = new TextComponent("This feature is not available to you.");
			notAvailable.setColor(ChatColor.RED);
			message.addExtra(notAvailable);
			sender.sendMessage(message);
			return;
		}
		boolean maskIPAndLocation = false;
		ProxiedPlayer p = (ProxiedPlayer) sender;
		PlayerAccount account = plugin.getPlayerInfo(p.getUniqueId());
		PlayerSession session = BungeeChat.getSession(p);
		if (account.isStreamModeActive()) {
			maskIPAndLocation = true;
		}
		String entityToCheck = args[0];
		if (entityToCheck.contains(".") || entityToCheck.contains(":") || entityToCheck.contains("/")) {
			if (!sender.hasPermission("hk.siggi.bungeechat.seenip")) {
				BaseComponent message = new TextComponent("");
				BaseComponent notAvailable = new TextComponent("This feature is not available to you.");
				notAvailable.setColor(ChatColor.RED);
				message.addExtra(notAvailable);
				sender.sendMessage(message);
				return;
			}
			String ipAsStr = unmaskIP(entityToCheck);
			if (isIPInvalid(ipAsStr)) {
				BaseComponent message = new TextComponent("");
				BaseComponent notAvailable = new TextComponent("IP address is invalid. If you're entering a masked IP address from a stream session, the mapping may have expired.");
				notAvailable.setColor(ChatColor.RED);
				message.addExtra(notAvailable);
				sender.sendMessage(message);
				return;
			}
			try {
				IP ip = IP.getIP(ipAsStr);
				Iterable<UUID> playerList = plugin.getUUIDs(ip);
				ipAsStr = ip.toShortString();
				int blockSize = ip.getPrefixLength();
				boolean isSubnet = false;
				if (ip instanceof IPv4) {
					if (blockSize != 32) {
						isSubnet = true;
					}
					if (blockSize < 16) {
						BaseComponent message = new TextComponent("");
						BaseComponent msgA = new TextComponent("Minimum blocksize is /16 for searching IPv4 subnets.");
						msgA.setColor(ChatColor.RED);
						message.addExtra(msgA);
						sender.sendMessage(msgA);
						return;
					}
				} else if (ip instanceof IPv6) {
					if (blockSize != 128) {
						isSubnet = true;
					}
					if (blockSize < 32) {
						BaseComponent message = new TextComponent("");
						BaseComponent msgA = new TextComponent("Minimum blocksize is /32 for searching IPv6 subnets.");
						msgA.setColor(ChatColor.RED);
						message.addExtra(msgA);
						sender.sendMessage(msgA);
						return;
					}
				}
				Geolocation geolocation = null;
				if ((ip instanceof IPv4 && blockSize == 32) || (ip instanceof IPv6 && blockSize >= 64)) {
					geolocation = plugin.getGeolocation(ip.toString());
				}
				String location = null;
				if (geolocation != null) {
					location = (maskIPAndLocation ? "****" : geolocation.cityName) + ", " + geolocation.regionName + ", " + geolocation.countryName;
				}
				BaseComponent message = new TextComponent("");
				BaseComponent seenIP = new TextComponent("Seen IP address check");
				seenIP.setColor(ChatColor.BLUE);
				message.addExtra(seenIP);
				sender.sendMessage(message);

				if (maskIPAndLocation) {
					message = new TextComponent("");
					BaseComponent note = new TextComponent("Exit Stream Mode to unmask IP addresses");
					note.setColor(ChatColor.GOLD);
					message.addExtra(note);
					sender.sendMessage(message);
				}

				message = new TextComponent("");
				BaseComponent ipAddressIs = new TextComponent(isSubnet ? "Subnet: " : "IP Address: ");
				BaseComponent ipAddress = new TextComponent(maskIPAndLocation ? maskIP(ipAsStr) : ipAsStr);
				ipAddressIs.setColor(ChatColor.GREEN);
				ipAddress.setColor(ChatColor.AQUA);
				message.addExtra(ipAddressIs);
				message.addExtra(ipAddress);
				sender.sendMessage(message);

				if (ip instanceof IPv6 && blockSize > 64) {
					IP subnet64 = new IPv6(ip.getBytes(), 64);
					String subnet64Str = subnet64.toShortString();
					message = new TextComponent("");
					BaseComponent subnet64Is = new TextComponent(isSubnet ? "Broader Subnet: " : "Subnet: ");
					BaseComponent subnet64TC = new TextComponent(maskIPAndLocation ? maskIP(subnet64Str) : subnet64Str);
					subnet64Is.setColor(ChatColor.GREEN);
					subnet64TC.setColor(ChatColor.AQUA);
					subnet64TC.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to view other users in this subnet")}));
					subnet64TC.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/seen " + subnet64Str));
					message.addExtra(subnet64Is);
					message.addExtra(subnet64TC);
					sender.sendMessage(message);
				}

				if (location != null) {
					message = new TextComponent("");
					BaseComponent approximate = new TextComponent("Approximate Location: ");
					BaseComponent locationText = new TextComponent(location);
					approximate.setColor(ChatColor.GREEN);
					locationText.setColor(ChatColor.AQUA);
					message.addExtra(approximate);
					message.addExtra(locationText);
					sender.sendMessage(message);
				}

				if (!ip.toLongString().contains("/")) {
					IPtoISP ipToIsp = plugin.getIpToIsp();
					if (ipToIsp != null) {
						String isp = ipToIsp.getISP(ip.toShortString());
						if (isp != null) {
							message = new TextComponent("");
							BaseComponent ispIs = new TextComponent("ISP: ");
							BaseComponent ispTxt = new TextComponent(isp);
							ispIs.setColor(ChatColor.GREEN);
							ispTxt.setColor(ChatColor.AQUA);
							message.addExtra(ispIs);
							message.addExtra(ispTxt);
							sender.sendMessage(message);
						}
					}
				}

				message = new TextComponent("");
				BaseComponent players = new TextComponent("Players: ");
				players.setColor(ChatColor.GREEN);
				message.addExtra(players);
				boolean addedAPlayer = false;
				int playerCount = 0;
				for (UUID playerUUID : playerList) {
					if (addedAPlayer) {
						BaseComponent comma = new TextComponent(", ");
						message.addExtra(comma);
					} else {
						addedAPlayer = true;
					}
					String playerName = plugin.getUUIDCache().getNameFromUUID(playerUUID);
					if (playerName == null) {
						playerName = playerUUID.toString().replace("-", "").toLowerCase();
					}
					BaseComponent playerI = new TextComponent(playerName);
					playerI.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/seen " + playerName));
					playerI.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to see this player's IP addresses")}));
					message.addExtra(playerI);
					playerCount += 1;
				}
				((TextComponent) players).setText("Players (" + playerCount + "): ");
				sender.sendMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
				BaseComponent message = new TextComponent("An error has occurred! :/");
				message.setColor(ChatColor.RED);
				sender.sendMessage(message);
			}
		} else {
			UUID playerToCheck;
			if (entityToCheck.length() >= 32) {
				try {
					playerToCheck = Util.uuidFromString(entityToCheck);
				} catch (Exception e) {
					playerToCheck = null;
				}
			} else {
				playerToCheck = plugin.getPlayerNameHandler().getPlayerByName(entityToCheck);
			}
			if (playerToCheck == null) {
				if (entityToCheck.matches("[A-Za-z0-9_]{1,16}")) {
					try {
						URL url = new URL("http://127.0.0.1:8592/" + entityToCheck + "/latest");
						HttpURLConnection conn = (HttpURLConnection) url.openConnection();
						conn.setConnectTimeout(1000);
						conn.setReadTimeout(1000);
						InputStream in = conn.getInputStream();
						JsonElement parse = new JsonParser().parse(new InputStreamReader(in));
						if (parse instanceof JsonObject) {
							JsonObject root = (JsonObject)parse;
							String id = root.get("id").getAsString();
							playerToCheck = Util.uuidFromString(id);
						}
					} catch (Exception e) {
					}
				}
			}
			if (playerToCheck == null) {
				BaseComponent message = new TextComponent("");
				BaseComponent couldNotFind = new TextComponent("Could not find ");
				BaseComponent pName = new TextComponent(entityToCheck);
				BaseComponent checkAgain = new TextComponent(". Please check you spelled the name correctly.");
				couldNotFind.setColor(ChatColor.RED);
				pName.setColor(ChatColor.AQUA);
				checkAgain.setColor(ChatColor.RED);
				message.addExtra(couldNotFind);
				message.addExtra(pName);
				message.addExtra(checkAgain);
				sender.sendMessage(message);
				return;
			}
			String uuidStr = playerToCheck.toString().toLowerCase().replaceAll("-", "");
			String correctedName = plugin.getUUIDCache().getNameFromUUID(playerToCheck);
			if (correctedName == null) {
				correctedName = playerToCheck.toString().replace("-", "").toLowerCase();
			}
			ProxiedPlayer onlinePlayer = plugin.getProxy().getPlayer(playerToCheck);
			try {
				IPInfo recentIP = null;
				HashMap<String, IPInfo> ipMap = new HashMap<>();
				try {
					File file = new File(new File(plugin.getDataFolder(), "playerLoginRecords"), uuidStr + ".txt");
					BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
					String line;
					while ((line = reader.readLine()) != null) {
						if (!line.contains("/")) {
							continue;
						}
						String timeStr = line.substring(0, line.indexOf("/")).trim();
						long time = Long.parseLong(timeStr);
						String ip = line.substring(line.indexOf("/") + 1).trim();
						IPInfo info = ipMap.get(ip);
						if (info == null) {
							ipMap.put(ip, info = new IPInfo(ip));
						}
						recentIP = info;
						info.loginCount += 1;
						info.lastLogin = time;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				PlayerAccount targetUser = plugin.getPlayerInfo(playerToCheck);
				long fakeLastOnline = targetUser.getFakeLastOnline();
				if (p.hasPermission("hk.siggi.logintohub.seehiddenplayers")) {
					fakeLastOnline = 0L;
				}

				ArrayList<IPInfo> values = new ArrayList<>();
				values.addAll(ipMap.values());
				Collections.sort(values);
				BaseComponent message = new TextComponent("");
				BaseComponent seenIP = new TextComponent("Seen Player check");
				seenIP.setColor(ChatColor.BLUE);
				message.addExtra(seenIP);
				sender.sendMessage(message);
				message = new TextComponent("");
				BaseComponent playerIs = new TextComponent("Player: ");
				BaseComponent playerTxt = new TextComponent(correctedName);
				playerIs.setColor(ChatColor.GREEN);
				playerTxt.setColor(ChatColor.AQUA);
				message.addExtra(playerIs);
				message.addExtra(playerTxt);
				sender.sendMessage(message);
				lastSeen:
				if (onlinePlayer == null || fakeLastOnline > 0L) {
					long lastSeen = fakeLastOnline > 0L ? fakeLastOnline : OnTime.getInstance().getPlayer(playerToCheck).getLastOnline();
					if (lastSeen > 0L) {
						message = new TextComponent("");
						BaseComponent lastSeenT = new TextComponent("Last Seen: ");
						TimeZone tz = BungeeChat.getSession(p).user.getUserData().getTimeZone();
						BaseComponent lastSeenTimestamp = new TextComponent(plugin.formatDate(lastSeen, tz));
						BaseComponent lastSeenTime = new TextComponent(" (" + TimeUtil.timeToString(lastSeen - System.currentTimeMillis()) + " ago)");
						lastSeenT.setColor(ChatColor.GREEN);
						lastSeenTime.setColor(ChatColor.AQUA);
						message.addExtra(lastSeenT);
						message.addExtra(lastSeenTimestamp);
						message.addExtra(lastSeenTime);
						sender.sendMessage(message);
						break lastSeen;
					}
					// Mon, 13 Oct 2014 23:13:34.863 GMT
					// 14 October 2014
					message = new TextComponent("This player's last login is before 14 October 2014.");
					message.setColor(ChatColor.RED);
					sender.sendMessage(message);
				} else {
					message = new TextComponent("");
					BaseComponent lastSeen = new TextComponent("Last Seen: ");
					BaseComponent lastSeenNow;
					if (sender.hasPermission("hk.siggi.bungeecord.logintohub.directaccessany")) {
						lastSeenNow = new TextComponent("Online now at " + onlinePlayer.getServer().getInfo().getName());
						lastSeenNow.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to join")}));
						lastSeenNow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + onlinePlayer.getServer().getInfo().getName()));
					} else {
						lastSeenNow = new TextComponent("Online now");
					}
					lastSeen.setColor(ChatColor.GREEN);
					lastSeenNow.setColor(ChatColor.AQUA);
					message.addExtra(lastSeen);
					message.addExtra(lastSeenNow);
					sender.sendMessage(message);
				}

				if (targetUser.getPhoneNumber() != null && targetUser.isMailOn()) {
					BaseComponent phoneIsRegistered = new TextComponent("");
					BaseComponent phoneIsRegisteredMsg = new TextComponent("Phone Number Registered (receives /mail on their phone!)");
					phoneIsRegisteredMsg.setColor(ChatColor.GOLD);
					phoneIsRegistered.addExtra(phoneIsRegisteredMsg);
					p.sendMessage(phoneIsRegistered);
				}

				if (!sender.hasPermission("hk.siggi.bungeechat.seenip")) {
					return;
				}

				if (recentIP == null) {
					// Thu, 19 Mar 2015 22:10:46.993 GMT
					// 20 March 2014
					message = new TextComponent("IP address information is not available because this player's last login is before 20 March 2015.");
					message.setColor(ChatColor.RED);
					sender.sendMessage(message);
					return;
				}

				if (maskIPAndLocation) {
					message = new TextComponent("");
					BaseComponent note = new TextComponent("Exit Stream Mode to unmask IP addresses");
					note.setColor(ChatColor.GOLD);
					message.addExtra(note);
					sender.sendMessage(message);
				}

				message = new TextComponent("");
				BaseComponent ipIs = new TextComponent("IP Address: ");
				BaseComponent ipTxt = new TextComponent(maskIPAndLocation ? maskIP(recentIP.ip) : shorten(recentIP.ip));
				ipIs.setColor(ChatColor.GREEN);
				ipTxt.setColor(ChatColor.AQUA);
				ipTxt.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/seen " + recentIP.ip));
				ipTxt.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to see other players on this IP")}));
				message.addExtra(ipIs);
				message.addExtra(ipTxt);
				if (recentIP.ip.contains(":")) { // IPv6
					try {
						BaseComponent subnet = new TextComponent(" (/64)");
						IPv6 subnet64 = new IPv6(IP.getIP(recentIP.ip).getBytes(), 64);
						subnet.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/seen " + subnet64.toString()));
						subnet.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to see other players in this subnet")}));
						message.addExtra(subnet);
					} catch (Exception e) {
					}
				}
				sender.sendMessage(message);

				Geolocation geolocation = plugin.getGeolocation(recentIP.ip);
				if (geolocation != null) {
					message = new TextComponent("");
					BaseComponent locationIs = new TextComponent("Approximate Location: ");
					BaseComponent locationTxt = new TextComponent((maskIPAndLocation ? "****" : geolocation.cityName) + ", " + geolocation.regionName + ", " + geolocation.countryName);
					locationIs.setColor(ChatColor.GREEN);
					locationTxt.setColor(ChatColor.AQUA);
					message.addExtra(locationIs);
					message.addExtra(locationTxt);
					sender.sendMessage(message);
				}

				IPtoISP ipToIsp = plugin.getIpToIsp();
				if (ipToIsp != null) {
					String isp = ipToIsp.getISP(recentIP.ip);
					if (isp != null) {
						message = new TextComponent("");
						BaseComponent ispIs = new TextComponent("ISP: ");
						BaseComponent ispTxt = new TextComponent(isp);
						ispIs.setColor(ChatColor.GREEN);
						ispTxt.setColor(ChatColor.AQUA);
						message.addExtra(ispIs);
						message.addExtra(ispTxt);
						sender.sendMessage(message);
					}
				}

				message = new TextComponent("");
				BaseComponent prevIPs = new TextComponent("Other IPs: ");
				prevIPs.setColor(ChatColor.GREEN);
				message.addExtra(prevIPs);
				boolean addedAnIP = false;
				int totalIPs = values.size();
				int maxIPs = 8;
				for (int i = 0; i < Math.min(totalIPs, maxIPs); i++) {
					IPInfo info = values.get(i);
					if (addedAnIP) {
						BaseComponent comma = new TextComponent(", ");
						message.addExtra(comma);
					} else {
						addedAnIP = true;
					}
					BaseComponent ipAddressText = new TextComponent(maskIPAndLocation ? maskIP(info.ip) : shorten(info.ip));
					ipAddressText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to see other players on this IP")}));
					ipAddressText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/seen " + info.ip));
					message.addExtra(ipAddressText);
					if (info.ip.contains(":")) { // IPv6
						try {
							BaseComponent subnet = new TextComponent(" (/64)");
							IPv6 subnet64 = new IPv6(IP.getIP(info.ip).getBytes(), 64);
							subnet.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/seen " + subnet64.toString()));
							subnet.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to see other players in this subnet")}));
							message.addExtra(subnet);
						} catch (Exception e) {
						}
					}
				}
				if (totalIPs > maxIPs) {
					BaseComponent comma = new TextComponent(", ");
					message.addExtra(comma);
					int additional = (totalIPs - maxIPs);
					BaseComponent ipAddressText = new TextComponent("... " + additional + " more");
					message.addExtra(ipAddressText);
				}
				sender.sendMessage(message);
			} catch (Exception e) {
				e.printStackTrace();
				BaseComponent message = new TextComponent("An error has occurred! :/");
				message.setColor(ChatColor.RED);
				sender.sendMessage(message);
			}
		}
	}

	private static boolean isIPInvalid(String ip) {
		if (ip.contains("hidden")) {
			return true;
		}
		return isIPInvalid(IP.getIP(ip));
	}

	private static boolean isIPInvalid(IP ip) {
		for (IP i : illegalSubnets) {
			if (i.contains(ip)) {
				return true;
			}
		}
		return false;
	}
	private static final IP[] illegalSubnets = new IP[]{
		IP.getIP("0/8"),
		IP.getIP("10/8"),
		IP.getIP("100.64/10"),
		IP.getIP("169.254/16"),
		IP.getIP("172.16/12"),
		IP.getIP("192.0.0/24"),
		IP.getIP("192.0.2/24"),
		IP.getIP("192.168/16"),
		IP.getIP("198.51.100/24"),
		IP.getIP("203.0.113/24"),
		IP.getIP("224/4"),
		IP.getIP("240/4"),
		IP.getIP("255.255.255.255"),
		IP.getIP("::"),
		IP.getIP("::ff:0:0/96"),
		IP.getIP("100::/64"),
		IP.getIP("2001::/32"),
		IP.getIP("2001:10::/28"),
		IP.getIP("2001:20::/28"),
		IP.getIP("2001:db8::/32"),
		IP.getIP("2002::/16"),
		IP.getIP("fc00::/7"),
		IP.getIP("fe80::/10"),
		IP.getIP("ff00::/8")
	};

	private static final HashMap<IPv4, String> maskedIPv4s = new HashMap<>();
	private static final HashMap<String, IPv4> reverseMaskedIPv4s = new HashMap<>();

	private static final HashMap<String, String> maskedIPv6Subnets = new HashMap<>();
	private static final HashMap<String, String> reverseMaskedIPv6Subnets = new HashMap<>();

	private static final HashMap<String, String> maskedIPv6Devices = new HashMap<>();
	private static final HashMap<String, String> reverseMaskedIPv6Devices = new HashMap<>();

	private static String maskIP(String ip) {
		IP addrA = IP.getIP(ip);
		String result = "<MaskedIP (" + (addrA instanceof IPv4 ? "IPv4" : (addrA instanceof IPv6 ? "IPv6" : "?")) + ")>";
		if (addrA instanceof IPv4) {
			if (addrA.getPrefixLength() < 32) {
				return "<HiddenSubnet (IPv4)>";
			}
			IPv4 addr = (IPv4) addrA;
			result = maskedIPv4s.get(addr);
			while (result == null) {
				result = "hidden-ipv4:" + randomMaskString();
				if (reverseMaskedIPv4s.containsKey(result)) {
					result = null;
				} else {
					maskedIPv4s.put(addr, result);
					reverseMaskedIPv4s.put(result, addr);
				}
			}
		} else if (addrA instanceof IPv6) {
			IPv6 addr = (IPv6) addrA;
			IPv6 subnet = addr;
			if (addr.getPrefixLength() < 128 && addr.getPrefixLength() != 64) {
				return "<MaskedSubnet (IPv6)>";
			}
			subnet = new IPv6(addr.getBytes(), 64);
			String fullAddr = addr.toLongString();
			String subnetPart = fullAddr.substring(0, 19);
			String maskedSubnet = maskedIPv6Subnets.get(subnetPart);
			while (maskedSubnet == null) {
				maskedSubnet = "hidden-ipv6:" + randomMaskString();
				if (reverseMaskedIPv6Subnets.containsKey(maskedSubnet)) {
					maskedSubnet = null;
				} else {
					maskedIPv6Subnets.put(subnetPart, maskedSubnet);
					reverseMaskedIPv6Subnets.put(maskedSubnet, subnetPart);
				}
			}
			if (addr.getPrefixLength() == 128) {
				String devicePart = fullAddr.substring(20);
				String maskedDevice = maskedIPv6Devices.get(devicePart);
				while (maskedDevice == null) {
					maskedDevice = randomMaskString();
					if (reverseMaskedIPv6Devices.containsKey(maskedDevice)) {
						maskedDevice = null;
					} else {
						maskedIPv6Devices.put(devicePart, maskedDevice);
						reverseMaskedIPv6Devices.put(maskedDevice, devicePart);
					}
				}
				result = maskedSubnet + "/" + maskedDevice;
			} else {
				result = maskedSubnet;
			}
		}
		return result;
	}

	private static String unmaskIP(String ip) {
		try {
			String result = ip;
			if (ip.contains("v4")) {
				IP g = reverseMaskedIPv4s.get(ip);
				if (g != null) {
					result = g.toShortString();
				}
			} else if (ip.contains("v6")) {
				int colonPos = ip.indexOf(":");
				if (colonPos == -1) {
					return ip;
				}
				int slashPos = ip.indexOf("/", colonPos);
				if (slashPos == -1) {
					String subnet = reverseMaskedIPv6Subnets.get("hidden-ipv6:" + ip.substring(colonPos + 1));
					if (subnet != null) {
						result = IP.getIP(subnet + "/64").toShortString();
					}
				} else {
					String subnet = reverseMaskedIPv6Subnets.get("hidden-ipv6:" + ip.substring(colonPos + 1, slashPos));
					String device = reverseMaskedIPv6Devices.get(ip.substring(slashPos + 1));
					if (subnet != null && device != null) {
						result = IP.getIP(subnet + ":" + device).toShortString();
					}
				}
			}
			return result;
		} catch (Exception e) {
			return ip;
		}
	}

	private static String randomMaskString() {
		StringBuilder sb = new StringBuilder();
		char[] chars = "0123456789abcdef".toCharArray();
		while (sb.length() < 6) {
			sb.append(chars[(int) Math.floor(Math.random() * chars.length)]);
		}
		return sb.toString();
	}

	private static String shorten(String ip) {
		try {
			return IP.getIP(ip).toShortString();
		} catch (Exception e) {
			return ip;
		}
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		List<String> results = new LinkedList<>();
		if (args.length == 1) {
			String n = args[0];
			results.addAll(plugin.getPlayerNameHandler().autocompletePlayers(n));
		}
		return results;
	}

	private class IPInfo implements Comparable<IPInfo> {

		public String ip;
		public int loginCount = 0;
		public long lastLogin = 0L;

		public IPInfo(String ip) {
			this.ip = ip;
		}

		@Override
		public int compareTo(IPInfo other) {
			if (lastLogin == other.lastLogin) {
				return 0;
			}
			if (lastLogin < other.lastLogin) {
				return 1;
			}
			return -1;
		}
	}

	private class PlayerInfo implements Comparable<PlayerInfo> {

		public String playerName;
		public UUID uuid;
		public int loginCount;

		public PlayerInfo(String playerName, UUID uuid) {
			this.playerName = playerName;
			this.uuid = uuid;
			this.loginCount = 0;
		}

		@Override
		public int compareTo(PlayerInfo other) {
			if (other.loginCount == loginCount) {
				return playerName.compareTo(other.playerName);
			}
			return other.loginCount - loginCount;
		}
	}
}
