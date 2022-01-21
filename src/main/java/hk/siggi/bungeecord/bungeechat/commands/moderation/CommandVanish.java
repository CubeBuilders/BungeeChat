package hk.siggi.bungeecord.bungeechat.commands.moderation;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.util.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandVanish extends Command {
	public final BungeeChat plugin;
	public CommandVanish(BungeeChat plugin) {
		super("vanish", null, "v");
		this.plugin = plugin;
	}
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer player = (ProxiedPlayer) sender;
		if (player.hasPermission("hk.siggi.bungeechat.vanish")) {
			plugin.vanish(player, args);
		} else {
			MessageSender.sendMessage(player, Util.randomNotPermittedMessage());
		}
	}
}
