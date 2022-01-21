package hk.siggi.bungeecord.bungeechat.commands.server;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import hk.siggi.bungeecord.bungeechat.BungeeChat;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import hk.siggi.bungeecord.bungeechat.MessageSender;
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
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandServer extends Command implements TabExecutor {

	public final BungeeChat plugin;
	private byte[] serverlist;

	public CommandServer(BungeeChat plugin) {
		super("server");
		this.plugin = plugin;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] b = new byte[4096];
			int c = 0;
			FileInputStream in = new FileInputStream(new File(plugin.getDataFolder(), "serverlist.txt"));
			while ((c = in.read(b, 0, b.length)) != -1) {
				baos.write(b, 0, c);
			}
			serverlist = baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			serverlist = new byte[0];
		}
	}

	public boolean contains(String server, String[] serverListing) {
		for (int i = 0; i < serverListing.length; i++) {
			if (serverListing[i].equalsIgnoreCase(server)) {
				return true;
			}
		}
		return false;
	}

	public String getHumanReadableName(String serverId) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(serverlist)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.contains("#")) {
					line = line.substring(0, line.indexOf("#"));
				}
				int x = line.indexOf("=");
				if (x == -1) {
					continue;
				}
				String key = line.substring(0, x).trim();
				String val = line.substring(x + 1).trim();
				if (key.equalsIgnoreCase(serverId)) {
					return val;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
			}
		}
		return serverId;
	}

	public String[] getServerList() {
		ArrayList serverList = new ArrayList();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(serverlist)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.contains("#")) {
					line = line.substring(0, line.indexOf("#"));
				}
				int x = line.indexOf("=");
				if (x == -1) {
					continue;
				}
				String key = line.substring(0, x).trim();
				String val = line.substring(x + 1).trim();
				serverList.add(key);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
			}
		}
		return (String[]) serverList.toArray(new String[serverList.size()]);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer player = (ProxiedPlayer) sender;
		Map servers = ProxyServer.getInstance().getServers();
		String[] serverListing = getServerList();
		if (args.length == 0) {
			String currentServerHumanReadableName = getHumanReadableName(player.getServer().getInfo().getName());
			TextComponent youAreHere = new TextComponent("You are here: ");
			youAreHere.setColor(ChatColor.YELLOW);
			TextComponent youAreHereServer = new TextComponent(currentServerHumanReadableName);
			youAreHereServer.setColor(ChatColor.WHITE);
			youAreHere.addExtra(youAreHereServer);
			MessageSender.sendMessage(player, youAreHere);
			TextComponent serverList = new TextComponent("Click on a server to join it:");
			serverList.setColor(ChatColor.GOLD);
			MessageSender.sendMessage(player, serverList);
			for (int i = 0; i < serverListing.length; i++) {
				ServerInfo server = (ServerInfo) servers.get(serverListing[i]);
				if (server != null) {
					if (server.canAccess(player)) {
						String humanReadableName = getHumanReadableName(server.getName());
						TextComponent serverPrefix = new TextComponent("-> ");
						serverPrefix.setColor(ChatColor.WHITE);
						//int count = server.getPlayers().size();
						TextComponent serverTextComponent = new TextComponent(humanReadableName/* + " (" + count + " player" + (count == 1 ? "" : "s") + ")"*/);
						serverTextComponent.setColor(ChatColor.AQUA);
						serverTextComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to join " + humanReadableName)}));
						serverTextComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + server.getName()));
						serverPrefix.addExtra(serverTextComponent);
						MessageSender.sendMessage(player, serverPrefix);
					}
				}
			}
		} else {
			args[0] = args[0].toLowerCase();
			ServerInfo server = (ServerInfo) servers.get(args[0]);
			if (args[0].equals("lobby") && server == null) {
				server = (ServerInfo) servers.get("hub");
			}
			if (args[0].equals("personalspace") && server == null) {
				server = (ServerInfo) servers.get("creative");
			}
			if (server == null) {
				for (Object servO : servers.keySet()) {
					if (!(servO instanceof String)) {
						continue;
					}
					String serv = (String) servO;
					if (serv.toLowerCase().startsWith(args[0])) {
						if (serv.equals("skyblockold")) continue;
						ServerInfo serverA = (ServerInfo) servers.get(serv);
						if (contains(serverA.getName(), serverListing)) {
							server = serverA;
							break;
						}
					}
				}
			}
			if (server == null) {
				MessageSender.sendMessage(player, ProxyServer.getInstance().getTranslation("no_server", new Object[0]));
			} else if (!server.canAccess(player)) {
				MessageSender.sendMessage(player, ProxyServer.getInstance().getTranslation("no_server_permission", new Object[0]));
			} else if (!player.hasPermission("hk.siggi.bungeecord.logintohub.directaccessany") && !contains(server.getName(), serverListing)) {
				TextComponent serverList = new TextComponent("Direct access to this server denied.");
				serverList.setColor(ChatColor.RED);
				MessageSender.sendMessage(player, serverList);
			} else {
				player.connect(server);
			}
		}
	}

	@Override
	public Iterable<String> onTabComplete(final CommandSender sender, String[] args) {
		final String[] serverListing = getServerList();
		return args.length != 0 ? Collections.EMPTY_LIST : Iterables.transform(Iterables.filter(ProxyServer.getInstance().getServers().values(), new Predicate<ServerInfo>() {
			@Override
			public boolean apply(ServerInfo input) {
				return contains(input.getName(), serverListing);
			}
		}), (ServerInfo input) -> input.getName());
	}
}
