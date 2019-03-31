package hk.siggi.bungeecord.bungeechat.commands.moderation;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import hk.siggi.bungeecord.bungeechat.util.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandNoSpy extends Command {

	public final BungeeChat plugin;

	public CommandNoSpy(BungeeChat plugin) {
		super("nospy", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		String name;
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = ((ProxiedPlayer) sender);
			name = player.getName();
			if (!player.hasPermission("hk.siggi.bungeechat.spy")) {
				player.sendMessage(Util.randomNotPermittedMessage());
				return;
			}
		} else {
			sender.sendMessage("This command can only be used in-game.");
			return;
		}
		PlayerAccount info = plugin.getPlayerInfo(((ProxiedPlayer) sender).getUniqueId());
		if (info.isNoSpy()) {
			info.setNoSpy(false);
			sender.sendMessage("You now receive spy messages again.");
		} else {
			info.setNoSpy(true);
			sender.sendMessage("Spy messages hidden. They will be shown again after you type the command again, or if you are offline for at least one hour.");
		}
	}
}
