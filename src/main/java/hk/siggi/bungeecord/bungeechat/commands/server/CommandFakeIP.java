package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.geolocation.Geolocation;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandFakeIP extends Command {

	private final BungeeChat plugin;

	public CommandFakeIP(BungeeChat plugin) {
		super("fakeip", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender cs, String[] strings) {
		if (!(cs instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer p = (ProxiedPlayer) cs;
		if (!p.hasPermission("hk.siggi.bungeechat.fakeip")) {
			return;
		}
		String newIP = strings[0];
		Geolocation newGeolocation = plugin.getGeolocation(newIP);
		if (newGeolocation != null) {
			plugin.setGeolocation(p, newGeolocation);
		}
	}

}
