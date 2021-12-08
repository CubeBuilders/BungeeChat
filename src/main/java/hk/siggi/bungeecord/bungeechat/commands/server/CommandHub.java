package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import java.util.Map;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandHub extends Command {

	public final BungeeChat plugin;

	public CommandHub(BungeeChat plugin) {
		super("hub", null, "lobby");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer player = (ProxiedPlayer) sender;
		Map servers = ProxyServer.getInstance().getServers();
		ServerInfo server = (ServerInfo) servers.get("hub");
		if (server == null) {
			player.sendMessage("An error has occurred. :/");
			return;
		}
		player.connect(server);
	}
}
