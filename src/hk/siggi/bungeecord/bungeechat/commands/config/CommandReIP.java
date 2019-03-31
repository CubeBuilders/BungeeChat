package hk.siggi.bungeecord.bungeechat.commands.config;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandReIP extends Command {
	public final BungeeChat plugin;
	public CommandReIP(BungeeChat plugin) {
		super("reip", null);
		this.plugin = plugin;
	}
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ProxiedPlayer) {
			sender.sendMessage("This command can only be used from the console.");
			return;
		}
		String server = args[0];
		String ip = args[1];
		int port = Integer.parseInt(args[2]);
		sender.sendMessage("Updating " + server + " to " + ip + ":" + port);
		plugin.reIP(server, ip, port);
		plugin.updateReIP(server, ip, port);
	}
}
