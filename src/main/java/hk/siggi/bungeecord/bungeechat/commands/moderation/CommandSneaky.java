package hk.siggi.bungeecord.bungeechat.commands.moderation;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import hk.siggi.bungeecord.bungeechat.util.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandSneaky extends Command {

	public final BungeeChat plugin;

	public CommandSneaky(BungeeChat plugin) {
		super("sneaky", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		String name;
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = ((ProxiedPlayer) sender);
			name = player.getName();
			if (!player.hasPermission("hk.siggi.bungeechat.vanish")) {
				player.sendMessage(Util.randomNotPermittedMessage());
				return;
			}
		} else {
			sender.sendMessage("This command can only be used in-game.");
			return;
		}
		PlayerAccount info = plugin.getPlayerInfo(((ProxiedPlayer) sender).getUniqueId());
		if (info.isSneaky()) {
			info.setSneaky(false);
			sender.sendMessage("You are no longer sneaky.");
		} else {
			info.setSneaky(true);
			sender.sendMessage("You are now sneaky.");
		}
	}
}
