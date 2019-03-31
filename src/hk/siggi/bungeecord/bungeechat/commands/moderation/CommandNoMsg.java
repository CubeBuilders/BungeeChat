package hk.siggi.bungeecord.bungeechat.commands.moderation;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import hk.siggi.bungeecord.bungeechat.util.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandNoMsg extends Command {

	public final BungeeChat plugin;

	public CommandNoMsg(BungeeChat plugin) {
		super("nomsg", null);
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
		if (info.isNoMsg()) {
			info.setNoMsg(false);
			sender.sendMessage("Incoming PMs are enabled again now.");
		} else {
			info.setNoMsg(true);
			sender.sendMessage("Incoming PMs are disabled. It re-enables by typing the command again, or by being offline for at least one hour.");
		}
	}
}
