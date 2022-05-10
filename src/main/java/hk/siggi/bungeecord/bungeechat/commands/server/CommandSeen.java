package hk.siggi.bungeecord.bungeechat.commands.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.PlayerSession;
import hk.siggi.bungeecord.bungeechat.geolocation.Geolocation;
import hk.siggi.bungeecord.bungeechat.ontime.OnTime;
import hk.siggi.bungeecord.bungeechat.ontime.OnTimePlayer;
import hk.siggi.bungeecord.bungeechat.ontime.OnTimeSessionRecord;
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
			MessageSender.sendMessage(sender, message);
			return;
		}
		ProxiedPlayer p = (ProxiedPlayer) sender;
		PlayerAccount account = plugin.getPlayerInfo(p.getUniqueId());
		PlayerSession session = BungeeChat.getSession(p);
		String entityToCheck = args[0];
		if (entityToCheck.contains(".") || entityToCheck.contains(":") || entityToCheck.contains("/")) {
			if (!sender.hasPermission("hk.siggi.bungeechat.seenip")) {
				BaseComponent message = new TextComponent("");
				BaseComponent notAvailable = new TextComponent("This feature is not available to you.");
				notAvailable.setColor(ChatColor.RED);
				message.addExtra(notAvailable);
				MessageSender.sendMessage(sender, message);
				return;
			}
			if (account.isStreamModeActive()) {
				BaseComponent message = new TextComponent("");
				BaseComponent notAvailable = new TextComponent("Disable stream mode to lookup IP addresses.");
				notAvailable.setColor(ChatColor.RED);
				message.addExtra(notAvailable);
				MessageSender.sendMessage(sender, message);
				return;
			}
			String ipAsStr = entityToCheck;
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
						MessageSender.sendMessage(sender, msgA);
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
						MessageSender.sendMessage(sender, msgA);
						return;
					}
				}
				Geolocation geolocation = null;
				if ((ip instanceof IPv4 && blockSize == 32) || (ip instanceof IPv6 && blockSize >= 64)) {
					geolocation = plugin.getGeolocation(ip.toString());
				}
				String location = null;
				if (geolocation != null) {
					location = geolocation.cityName + ", " + geolocation.regionName + ", " + geolocation.countryName;
				}
				BaseComponent message = new TextComponent("");
				BaseComponent seenIP = new TextComponent("==== IP Address ====");
				seenIP.setColor(ChatColor.BLUE);
				message.addExtra(seenIP);
				MessageSender.sendMessage(sender, message);

				message = new TextComponent("");
				BaseComponent ipAddressIs = new TextComponent(isSubnet ? "Subnet: " : "IP Address: ");
				BaseComponent ipAddress = new TextComponent(ipAsStr);
				ipAddressIs.setColor(ChatColor.GREEN);
				ipAddress.setColor(ChatColor.AQUA);
				message.addExtra(ipAddressIs);
				message.addExtra(ipAddress);
				MessageSender.sendMessage(sender, message);

				if (ip instanceof IPv6 && blockSize > 64) {
					IP subnet64 = new IPv6(ip.getBytes(), 64);
					String subnet64Str = subnet64.toShortString();
					message = new TextComponent("");
					BaseComponent subnet64Is = new TextComponent(isSubnet ? "Broader Subnet: " : "Subnet: ");
					BaseComponent subnet64TC = new TextComponent(subnet64Str);
					subnet64Is.setColor(ChatColor.GREEN);
					subnet64TC.setColor(ChatColor.AQUA);
					subnet64TC.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to view other users in this subnet")}));
					subnet64TC.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/seen " + subnet64Str));
					message.addExtra(subnet64Is);
					message.addExtra(subnet64TC);
					MessageSender.sendMessage(sender, message);
				}

				if (location != null) {
					message = new TextComponent("");
					BaseComponent approximate = new TextComponent("Approximate Location: ");
					BaseComponent locationText = new TextComponent(location);
					approximate.setColor(ChatColor.GREEN);
					locationText.setColor(ChatColor.AQUA);
					message.addExtra(approximate);
					message.addExtra(locationText);
					MessageSender.sendMessage(sender, message);
					message = new TextComponent("");
					BaseComponent ispIs = new TextComponent("ISP: ");
					BaseComponent ispTxt = new TextComponent(geolocation.isp);
					ispIs.setColor(ChatColor.GREEN);
					ispTxt.setColor(ChatColor.AQUA);
					message.addExtra(ispIs);
					message.addExtra(ispTxt);
					MessageSender.sendMessage(sender, message);
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
				MessageSender.sendMessage(sender, message);
			} catch (Exception e) {
				e.printStackTrace();
				BaseComponent message = new TextComponent("An error has occurred! :/");
				message.setColor(ChatColor.RED);
				MessageSender.sendMessage(sender, message);
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
				MessageSender.sendMessage(sender, message);
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
				BaseComponent seenIP = new TextComponent("==== Player Activity ====");
				seenIP.setColor(ChatColor.BLUE);
				message.addExtra(seenIP);
				MessageSender.sendMessage(sender, message);
				message = new TextComponent("");
				BaseComponent playerIs = new TextComponent("Player: ");
				BaseComponent playerTxt = new TextComponent(correctedName);
				playerIs.setColor(ChatColor.GREEN);
				playerTxt.setColor(ChatColor.AQUA);
				message.addExtra(playerIs);
				message.addExtra(playerTxt);
				MessageSender.sendMessage(sender, message);
				TimeZone tz = BungeeChat.getSession(p).user.getUserData().getTimeZone();
				OnTimePlayer onTimePlayer = OnTime.getInstance().getPlayer(playerToCheck);
				OnTimeSessionRecord[] sessionRecords = onTimePlayer.getSessionRecords();
				long totalTimeLoggedIn = OnTime.getTotalTimeLoggedIn(sessionRecords);
				{
					long firstSeen = 0;
					if (sessionRecords.length != 0) {
						firstSeen = sessionRecords[0].login;
					}
					if (firstSeen > 0L) {
						message = new TextComponent("");
						BaseComponent firstSeenT = new TextComponent("First Seen: ");
						BaseComponent firstSeenTimestamp = new TextComponent(plugin.formatDate(firstSeen, tz));
						BaseComponent firstSeenTime = new TextComponent(" (" + TimeUtil.timeDifference(firstSeen, System.currentTimeMillis()) + " ago)");
						firstSeenT.setColor(ChatColor.GREEN);
						firstSeenTime.setColor(ChatColor.AQUA);
						message.addExtra(firstSeenT);
						message.addExtra(firstSeenTimestamp);
						message.addExtra(firstSeenTime);
						MessageSender.sendMessage(sender, message);
					}
				}
				lastSeen:
				if (onlinePlayer == null || fakeLastOnline > 0L) {
					long lastSeen = fakeLastOnline > 0L ? fakeLastOnline : onTimePlayer.getLastOnline();
					if (lastSeen > 0L) {
						message = new TextComponent("");
						BaseComponent lastSeenT = new TextComponent("Last Seen: ");
						BaseComponent lastSeenTimestamp = new TextComponent(plugin.formatDate(lastSeen, tz));
						BaseComponent lastSeenTime = new TextComponent(" (" + TimeUtil.timeDifference(lastSeen, System.currentTimeMillis()) + " ago)");
						lastSeenT.setColor(ChatColor.GREEN);
						lastSeenTime.setColor(ChatColor.AQUA);
						message.addExtra(lastSeenT);
						message.addExtra(lastSeenTimestamp);
						message.addExtra(lastSeenTime);
						MessageSender.sendMessage(sender, message);
						break lastSeen;
					}
					// Mon, 13 Oct 2014 23:13:34.863 GMT
					// 14 October 2014
					message = new TextComponent("This player's last login is before 14 October 2014.");
					message.setColor(ChatColor.RED);
					MessageSender.sendMessage(sender, message);
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
					MessageSender.sendMessage(sender, message);
				}
				if (totalTimeLoggedIn > 0L) {
					message = new TextComponent("");
					BaseComponent totalTimeOn = new TextComponent("Total Time Online: ");
					BaseComponent timeOn = new TextComponent(TimeUtil.timeToString(totalTimeLoggedIn, 4, true));
					totalTimeOn.setColor(ChatColor.GREEN);
					timeOn.setColor(ChatColor.AQUA);
					message.addExtra(totalTimeOn);
					message.addExtra(timeOn);
					MessageSender.sendMessage(sender, message);
				}

				if (targetUser.getPhoneNumber() != null && targetUser.isMailOn()) {
					BaseComponent phoneIsRegistered = new TextComponent("");
					BaseComponent phoneIsRegisteredMsg = new TextComponent("Phone Number Registered (receives /mail on their phone!)");
					phoneIsRegisteredMsg.setColor(ChatColor.GOLD);
					phoneIsRegistered.addExtra(phoneIsRegisteredMsg);
					MessageSender.sendMessage(p, phoneIsRegistered);
				}

				if (!sender.hasPermission("hk.siggi.bungeechat.seenip") || account.isStreamModeActive()) {
					return;
				}

				if (recentIP == null) {
					// Thu, 19 Mar 2015 22:10:46.993 GMT
					// 20 March 2014
					message = new TextComponent("IP address information is not available because this player's last login is before 20 March 2015.");
					message.setColor(ChatColor.RED);
					MessageSender.sendMessage(sender, message);
					return;
				}

				message = new TextComponent("");
				BaseComponent ipIs = new TextComponent("IP Address: ");
				BaseComponent ipTxt = new TextComponent(shorten(recentIP.ip));
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
				MessageSender.sendMessage(sender, message);

				Geolocation geolocation = plugin.getGeolocation(recentIP.ip);
				if (geolocation != null) {
					message = new TextComponent("");
					BaseComponent locationIs = new TextComponent("Approximate Location: ");
					BaseComponent locationTxt = new TextComponent(geolocation.cityName + ", " + geolocation.regionName + ", " + geolocation.countryName);
					locationIs.setColor(ChatColor.GREEN);
					locationTxt.setColor(ChatColor.AQUA);
					message.addExtra(locationIs);
					message.addExtra(locationTxt);
					MessageSender.sendMessage(sender, message);
					message = new TextComponent("");
					BaseComponent ispIs = new TextComponent("ISP: ");
					BaseComponent ispTxt = new TextComponent(geolocation.isp);
					ispIs.setColor(ChatColor.GREEN);
					ispTxt.setColor(ChatColor.AQUA);
					message.addExtra(ispIs);
					message.addExtra(ispTxt);
					MessageSender.sendMessage(sender, message);
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
					BaseComponent ipAddressText = new TextComponent(shorten(info.ip));
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
				MessageSender.sendMessage(sender, message);
			} catch (Exception e) {
				e.printStackTrace();
				BaseComponent message = new TextComponent("An error has occurred! :/");
				message.setColor(ChatColor.RED);
				MessageSender.sendMessage(sender, message);
			}
		}
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
