package hk.siggi.bungeecord.bungeechat.commands.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.PlayerSession;
import hk.siggi.bungeecord.bungeechat.geolocation.Geolocation;
import hk.siggi.bungeecord.bungeechat.ontime.OnTime;
import hk.siggi.bungeecord.bungeechat.ontime.OnTimePlayer;
import hk.siggi.bungeecord.bungeechat.ontime.OnTimeSessionRecord;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import hk.siggi.bungeecord.bungeechat.player.Punishment;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandList extends Command {

	public final BungeeChat plugin;
	private byte[] servergroups;

	public CommandList(BungeeChat plugin) {
		super("glist", null, "list", "playerlist", "online", "who");
		this.plugin = plugin;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] b = new byte[4096];
			int c;
			FileInputStream in = new FileInputStream(new File(plugin.getDataFolder(), "servergroups.txt"));
			while ((c = in.read(b, 0, b.length)) != -1) {
				baos.write(b, 0, c);
			}
			servergroups = baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			servergroups = new byte[0];
		}
	}

	public boolean isHidden(ProxiedPlayer p) {
		return plugin.isVanished(p);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(servergroups)));
			String line;
			Map servers = ProxyServer.getInstance().getServers();
			int globalHiddenPlayerCount = 0;
			boolean canSeeHiddenPlayers = sender.hasPermission("hk.siggi.logintohub.seehiddenplayers");
			ProxiedPlayer myPlayer = null;
			PlayerSession mySession = null;
			if (sender instanceof ProxiedPlayer) {
				myPlayer = (ProxiedPlayer) sender;
				mySession = BungeeChat.getSession(myPlayer);
			}
			while ((line = reader.readLine()) != null) {
				String primaryServer = null;
				if (line.contains("#")) {
					line = line.substring(0, line.indexOf("#"));
				}
				int x = line.indexOf("=");
				if (x == -1) {
					continue;
				}
				String serverGroup = line.substring(0, x).trim();
				String serversString = line.substring(x + 1).trim();
				String additionalServers = getAdditionalServers(serverGroup);
				if (additionalServers != null) {
					serversString = serversString + (serversString.equals("") ? "" : ",") + additionalServers;
				}
				String[] serverList = serversString.split(",");
				ArrayList<PlayerInfo> players = new ArrayList<>();
				Geolocation myGeolocation = null;
				if (sender instanceof ProxiedPlayer) {
					myGeolocation = plugin.getGeolocation((ProxiedPlayer) sender);
				}
				Comparator<PlayerInfo> sorter = sortAlphabetically;
				if (args.length > 0) {
					if (sender.hasPermission("hk.siggi.bungeechat.seenip")) {
						try {
							myGeolocation = plugin.getGeolocation(plugin.getProxy().getPlayer(args[0]));
							sorter = sortByDistance;
						} catch (Exception e) {
						}
					}
				}
				for (int i = 0; i < serverList.length; i++) {
					if (i == 0) {
						primaryServer = serverList[i];
					}
					ServerInfo server = (ServerInfo) servers.get(serverList[i]);
					if (server == null) {
						continue;
					}
					if (server.canAccess(sender)) {
						for (ProxiedPlayer player : server.getPlayers()) {
							double distance = Double.MAX_VALUE;
							if (sorter == sortByDistance && myGeolocation != null) {
								Geolocation geolocation = plugin.getGeolocation(player);
								if (geolocation != null) {
									try {
										distance = distFrom(Double.parseDouble(myGeolocation.latitude),
												Double.parseDouble(myGeolocation.longitude),
												Double.parseDouble(geolocation.latitude),
												Double.parseDouble(geolocation.longitude));
									} catch (Exception e) {
									}
								}
							}
							players.add(new PlayerInfo(player, distance));
						}
					}
				}
				Collections.sort(players, sorter);
				String playerListString = Util.format(players, ChatColor.RESET + ", ");
				TextComponent playersHere = new TextComponent(serverGroup);
				playersHere.setColor(ChatColor.GREEN);
				TextComponent joinButton = new TextComponent(" [Join]");
				joinButton.setColor(ChatColor.AQUA);
				if (primaryServer != null) {
					joinButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to join " + serverGroup)}));
					joinButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + primaryServer));
				}
				playersHere.addExtra(joinButton);

				int actualPlayerCountInt = players.size();
				int playerCountInt = actualPlayerCountInt;

				TextComponent playerCount = new TextComponent(" (" + playerCountInt + "): ");
				playerCount.setColor(ChatColor.YELLOW);
				playersHere.addExtra(playerCount);
				boolean didList = false;
				for (int i = 0; i < players.size(); i++) {
					PlayerInfo info = players.get(i);
					ProxiedPlayer p = info.player;
					if (p == null) {
						continue;
					}
					TextComponent playerInfo = new TextComponent((plugin.getGroupInfo().isStaff(p) ? BungeeChat.staroutline : "") + p.getDisplayName());
					boolean retiredStaff = p.getGroups().contains("retired");

					TextComponent hover = new TextComponent("");
					boolean didHoverText = false;
					List<TextComponent> usernameComponent = plugin.getGroupInfo().usernameComponent(p, false, true, true, true);
					for (TextComponent c : usernameComponent) {
						hover.addExtra(c);
						didHoverText = true;
					}
					if (sender.hasPermission("hk.siggi.bungeecord.logintohub.directaccessany")) {
						String serv = p.getServer().getInfo().getName();
						TextComponent join = new TextComponent((didHoverText ? "\n" : "") + "At: " + serv + " (click to join)");
						hover.addExtra(join);
						didHoverText = true;
						playerInfo.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + serv));
					}
					if (isHidden(p)) {
						playerCountInt -= 1;
						globalHiddenPlayerCount += 1;
						if (canSeeHiddenPlayers) {
							TextComponent pVanished = new TextComponent((didHoverText ? "\n" : "") + "Vanished");
							hover.addExtra(pVanished);
							pVanished.setColor(ChatColor.GRAY);
							playerInfo.setStrikethrough(true);
							didHoverText = true;
						} else {
							continue;
						}
					}
					if (retiredStaff) {
						TextComponent retired = new TextComponent((didHoverText ? "\n" : "") + "Retired Staff");
						hover.addExtra(retired);
						retired.setColor(ChatColor.GRAY);
						didHoverText = true;
					}
					if (sender.hasPermission("hk.siggi.bungeechat.seenip")) {
						Geolocation geolocation = plugin.getGeolocation(p);
						String geolocationString = null;
						if (geolocation != null) {
							geolocationString = geolocation.regionName + ", " + geolocation.countryName;
						}
						if (geolocationString != null) {
							TextComponent geolocationText = new TextComponent((didHoverText ? "\n" : "") + geolocationString);
							hover.addExtra(geolocationText);
							geolocationText.setColor(ChatColor.GRAY);
							didHoverText = true;
						}
					}
					if (!didList) {
						didList = true;
					} else {
						TextComponent comma = new TextComponent(", ");
						comma.setColor(ChatColor.WHITE);
						playersHere.addExtra(comma);
					}
					playerInfo.setColor(plugin.getGroupInfo().getColor(p));
					PlayerSession session = BungeeChat.getSession(p);
					if (session != null) {
						if (session.isMineChat) {
							TextComponent mineChat = new TextComponent("[MC]");
							mineChat.setColor(ChatColor.BLUE);
							TextComponent hoverText1 = new TextComponent(p.getName());
							TextComponent hoverText2 = new TextComponent(" is connected using MineChat.");
							mineChat.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{hoverText1, hoverText2}));
							playersHere.addExtra(mineChat);
						}
						String discordStatus = session.discordStatus;
						if (discordStatus != null) {
							if (!discordStatus.toLowerCase().contains("minecraft") || discordStatus.toLowerCase().contains("story")) {
								TextComponent ds = new TextComponent("[S]");
								ds.setColor(ChatColor.BLUE);
								TextComponent hoverText = new TextComponent(discordStatus + "\nStatus provided by Discord");
								ds.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{hoverText}));
								playersHere.addExtra(ds);
							}
						}
					}
					if (session != null && !session.clientBrand.equalsIgnoreCase("vanilla")) {
						String brand = session.clientBrand;
						if (brand.equals("")) {
							brand = "Unknown";
						}
						TextComponent brandText = new TextComponent((didHoverText ? "\n" : "") + "Using client: " + brand);
						hover.addExtra(brandText);
						brandText.setColor(ChatColor.GRAY);
						didHoverText = true;
					}
					if (didHoverText) {
						playerInfo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hover}));
					}
					playersHere.addExtra(playerInfo);
				}
				if (playerCountInt != actualPlayerCountInt) {
					if (canSeeHiddenPlayers) {
						playerCount.setText(" (" + playerCountInt + "/" + actualPlayerCountInt + "): ");
					} else {
						playerCount.setText(" (" + playerCountInt + "): ");
					}
				}
				MessageSender.sendMessage(sender, playersHere);
			}
			int totalPlayers = ProxyServer.getInstance().getOnlineCount();
			TextComponent totalOnline = new TextComponent("Total Players Online: ");
			totalOnline.setColor(ChatColor.GOLD);
			TextComponent totalOnlineCount;
			if (canSeeHiddenPlayers && globalHiddenPlayerCount > 0) {
				totalOnlineCount = new TextComponent(Integer.toString(totalPlayers - globalHiddenPlayerCount) + "/" + Integer.toString(totalPlayers));
			} else {
				totalOnlineCount = new TextComponent(Integer.toString(totalPlayers - globalHiddenPlayerCount));
			}
			totalOnlineCount.setColor(ChatColor.WHITE);
			totalOnline.addExtra(totalOnlineCount);
			MessageSender.sendMessage(sender, totalOnline);

			if (sender.hasPermission("hk.siggi.bungeechat.punishmentalert")) {
				TextComponent probation = new TextComponent("");
				TextComponent probationText = new TextComponent("On Probation: ");
				probationText.setColor(ChatColor.GOLD);
				probation.addExtra(probationText);
				boolean addedProbation = false;

				Collection<ProxiedPlayer> playerCollection = plugin.getProxy().getPlayers();
				ProxiedPlayer[] players = playerCollection.toArray(new ProxiedPlayer[playerCollection.size()]);
				for (ProxiedPlayer player : players) {
					PlayerAccount info = plugin.getPlayerInfo(player.getUniqueId());
					Punishment[] punishments = info.getPunishments();
					int veryRecentWarnings = 0;
					int recentWarnings = 0;
					int veryRecentMutes = 0;
					int recentMutes = 0;
					int veryRecentBans = 0;
					int recentBans = 0;
					int totalWarnings = 0;
					int totalMutes = 0;
					int totalBans = 0;
					int mcBans = 0;
					long onTime = 0L;
					long now = System.currentTimeMillis();
					for (Punishment punishment : punishments) {
						long howLongAgo;
						if (punishment.action == Punishment.PunishmentAction.STRIKE
								|| punishment.action == Punishment.PunishmentAction.UNBAN
								|| punishment.action == Punishment.PunishmentAction.UNMUTE) {
							continue;
						}
						if (punishment.action == Punishment.PunishmentAction.WARNING) {
							howLongAgo = now - punishment.time;
						} else {
							howLongAgo = now - (punishment.time + punishment.length);
						}
						if (punishment.action == Punishment.PunishmentAction.WARNING) {
							totalWarnings += 1;
							if (howLongAgo < 3600L * 2L * 1000L) {
								veryRecentWarnings += 1;
							}
							if (howLongAgo < 3600L * 2L * 24L * 1000L) {
								recentWarnings += 1;
							}
						}
						if (punishment.action == Punishment.PunishmentAction.MUTE) {
							totalMutes += 1;
							if (howLongAgo < 3600L * 4L * 1000L) {
								veryRecentMutes += 1;
							}
							if (howLongAgo < 3600L * 7L * 24L * 1000L) {
								recentMutes += 1;
							}
						}
						if (punishment.action == Punishment.PunishmentAction.BAN) {
							totalBans += 1;
							if (howLongAgo < 3600L * 2L * 24L * 1000L) {
								veryRecentBans += 1;
							}
							if (howLongAgo < 3600L * 30L * 24L * 1000L) {
								recentBans += 1;
							}
						}
					}
					boolean moreThan24Hours = false;
					if (!info.isMCBansExempt()) {
						mcBans = info.getMCBanCount();
						if (mcBans > 0) {
							OnTimePlayer otp = OnTime.getInstance().getPlayer(info.getPlayerUUID());
							OnTimeSessionRecord[] sessionRecords = otp.getSessionRecords();
							long totalTime = 0L;
							for (OnTimeSessionRecord record : sessionRecords) {
								totalTime += record.getTimeLoggedIn();
								if (totalTime > 3600L * 24L * 1000L) {
									moreThan24Hours = true;
									continue;
								}
							}
						}
					}
					if (veryRecentWarnings > 1
							|| veryRecentMutes > 2
							|| veryRecentBans > 1
							|| recentWarnings > 5
							|| recentMutes > 8
							|| recentBans > 1
							|| (mcBans > 0 && !moreThan24Hours)) {
						if (addedProbation) {
							TextComponent comma = new TextComponent(", ");
							probation.addExtra(comma);
						} else {
							addedProbation = true;
						}
						TextComponent user = new TextComponent(plugin.getUUIDCache().getNameFromUUID(info.getPlayerUUID()));
						TextComponent hover = new TextComponent("Warnings: " + totalWarnings + "\nMutes: " + totalMutes + "\nBans: " + totalBans + "\nMCBans: " + mcBans + "\nClick for info");
						user.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{hover}));
						user.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/phistory " + plugin.getUUIDCache().getNameFromUUID(info.getPlayerUUID())));
						probation.addExtra(user);
					}
				}
				if (addedProbation) {
					MessageSender.sendMessage(sender, probation);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageSender.sendMessage(sender, "An error has occurred. :/");
		}
	}

	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 6371.0; // km (change to 3958.75 to get miles)
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double sindLat = Math.sin(dLat / 2);
		double sindLng = Math.sin(dLng / 2);
		double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
				* Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;

		return dist;
	}

	public void addAdditionalServer(String serverGroup, String server) {
		if (serverGroup == null || server == null) {
			return;
		}
		synchronized (additionalServers) {
			cleanAdditionalServers();
			ArrayList<String> additional = additionalServers.get(serverGroup);
			if (additional == null) {
				additionalServers.put(serverGroup, additional = new ArrayList<>());
			}
			if (!additional.contains(server)) {
				additional.add(server);
			}
		}
	}

	private final HashMap<String, ArrayList<String>> additionalServers = new HashMap<>();

	private String getAdditionalServers(String serverGroup) {
		synchronized (additionalServers) {
			cleanAdditionalServers();
			ArrayList<String> additional = additionalServers.get(serverGroup);
			if (additional == null) {
				return null;
			}
			if (additional.isEmpty()) {
				return null;
			}
			String result = additional.get(0);
			for (int i = 1; i < additional.size(); i++) {
				result += "," + additional.get(i);
			}
			return result;
		}
	}

	private void cleanAdditionalServers() {
		ArrayList<String> removeList = new ArrayList<>();
		for (String serverGroup : additionalServers.keySet()) {
			ArrayList<String> list = additionalServers.get(serverGroup);
			Iterator iter = list.iterator();
			while (iter.hasNext()) {
				String server = (String) iter.next();
				if (BungeeCord.getInstance().getServerInfo(server) == null) {
					iter.remove();
				}
			}
			if (list.isEmpty()) {
				removeList.add(serverGroup);
			}
		}
		for (String remove : removeList) {
			additionalServers.remove(remove);
		}
	}

	private class PlayerInfo {

		public final ProxiedPlayer player;
		public final double distance;

		public PlayerInfo(ProxiedPlayer player, double distance) {
			this.player = player;
			this.distance = distance;
		}
	}

	public final Comparator<PlayerInfo> sortAlphabetically = new Comparator<PlayerInfo>() {

		@Override
		public int compare(PlayerInfo p1, PlayerInfo p2) {
			return p1.player.getDisplayName().compareTo(p2.player.getDisplayName());
		}
	};

	public final Comparator<PlayerInfo> sortByDistance = new Comparator<PlayerInfo>() {

		@Override
		public int compare(PlayerInfo p1, PlayerInfo p2) {
			if (p1.distance == p2.distance) {
				return p1.player.getDisplayName().compareTo(p2.player.getDisplayName());
			}
			if (p1.distance > p2.distance) {
				return 1;
			}
			return -1;
		}
	};
}
