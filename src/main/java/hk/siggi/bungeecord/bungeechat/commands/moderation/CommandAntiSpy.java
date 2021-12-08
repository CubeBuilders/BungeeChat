package hk.siggi.bungeecord.bungeechat.commands.moderation;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import hk.siggi.bungeecord.bungeechat.util.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandAntiSpy extends Command {

	public final BungeeChat plugin;

	public CommandAntiSpy(BungeeChat plugin) {
		super("antispy", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		String name;
		boolean nolog = false;
		if (args.length > 0) {
			if (args[0].equals("nolog")) {
				nolog = true;
			}
		}
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = ((ProxiedPlayer) sender);
			name = player.getName();
			if (!player.hasPermission("hk.siggi.bungeechat.antispy")) {
				player.sendMessage(Util.randomNotPermittedMessage());
				return;
			}
		} else {
			sender.sendMessage("This command can only be used in-game.");
			return;
		}
		PlayerAccount info = plugin.getPlayerInfo(((ProxiedPlayer) sender).getUniqueId());
		if (nolog) {
			if (info.isNoLog()) {
				info.setNoLog(false);
				sender.sendMessage("Your private messages are now logged again.");
			} else {
				info.setNoLog(true);
				sender.sendMessage("Your private messages are now not logged. It will be logged again by typing the command again, or after being offline for at least one hour.");
			}
		} else {
			if (info.isAntiSpy()) {
				info.setAntiSpy(false);
				sender.sendMessage("Your private messages can now be spied on again.");
			} else {
				info.setAntiSpy(true);
				sender.sendMessage("Private messages now hidden on chatspy. It will be shown again when you type the command again, or after being offline for at least one hour.");
			}
		}
	}
}
