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
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import hk.siggi.bungeecord.bungeechat.util.TimeUtil;
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
	private final File serverGroupsFile;

	public CommandList(BungeeChat plugin) {
		super("glist", null, "list", "playerlist", "online", "who");
		this.plugin = plugin;
		this.serverGroupsFile = new File(plugin.getDataFolder(), "servergroups.txt");
	}

	public boolean isHidden(ProxiedPlayer p) {
		return plugin.isVanished(p);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(serverGroupsFile));
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
				SortedSet<ProxiedPlayer> players = new TreeSet<>(sortAlphabetically);
				for (int i = 0; i < serverList.length; i++) {
					if (i == 0) {
						primaryServer = serverList[i];
					}
					ServerInfo server = (ServerInfo) servers.get(serverList[i]);
					if (server == null) {
						continue;
					}
					if (server.canAccess(sender)) {
						players.addAll(server.getPlayers());
					}
				}
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
				for (ProxiedPlayer p : players) {
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
						if (session.isAfk()) {
							TextComponent afk = new TextComponent("[AFK]");
							afk.setColor(ChatColor.GRAY);
							TextComponent hoverText1 = new TextComponent(p.getName());
							TextComponent hoverText2 = new TextComponent(" has been AFK for " + TimeUtil.timeToString(((long) session.afkTime) * 1000L, 2, true) + ".");
							afk.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{hoverText1, hoverText2}));
							playersHere.addExtra(afk);
						}
					}
					if (session != null) {
						String brand = session.clientBrand;
						if (brand.equals("")) {
							brand = "Unknown";
						}
						TextComponent brandText = new TextComponent((didHoverText ? "\n" : "") + "Client brand: " + brand);
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

		} catch (Exception e) {
			e.printStackTrace();
			MessageSender.sendMessage(sender, "An error has occurred. :/");
		}
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

	public final Comparator<ProxiedPlayer> sortAlphabetically = new Comparator<ProxiedPlayer>() {

		@Override
		public int compare(ProxiedPlayer p1, ProxiedPlayer p2) {
			return p1.getDisplayName().toLowerCase().compareTo(p2.getDisplayName().toLowerCase());
		}
	};
}
