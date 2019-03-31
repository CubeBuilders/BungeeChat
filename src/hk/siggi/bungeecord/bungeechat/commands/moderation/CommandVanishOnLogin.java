package hk.siggi.bungeecord.bungeechat.commands.moderation;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import hk.siggi.bungeecord.bungeechat.util.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandVanishOnLogin extends Command {

	public final BungeeChat plugin;

	public CommandVanishOnLogin(BungeeChat plugin) {
		super("vol", null);
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
		if (info.getVanishOnLogin()) {
			info.setVanishOnLogin(false);
			sender.sendMessage("Vanish on Login disabled.");
		} else {
			info.setVanishOnLogin(true);
			sender.sendMessage("Vanish on Login enabled.");
		}
	}
}
