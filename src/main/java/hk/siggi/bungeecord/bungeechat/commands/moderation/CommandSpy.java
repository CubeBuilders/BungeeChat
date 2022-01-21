package hk.siggi.bungeecord.bungeechat.commands.moderation;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import hk.siggi.bungeecord.bungeechat.util.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandSpy extends Command {

	public final BungeeChat plugin;

	public CommandSpy(BungeeChat plugin) {
		super("spy", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		String name;
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = ((ProxiedPlayer) sender);
			name = player.getName();
			if (!player.hasPermission("hk.siggi.bungeechat.spy") && !player.hasPermission("hk.siggi.bungeechat.antispy")) {
				MessageSender.sendMessage(player, Util.randomNotPermittedMessage());
				return;
			}
		} else {
			MessageSender.sendMessage(sender, "This command can only be used in-game.");
			return;
		}
		PlayerAccount info = plugin.getPlayerInfo(((ProxiedPlayer)sender).getUniqueId());
		boolean noSpy = info.isNoSpy();
		boolean noMsg = info.isNoMsg();
		boolean antiSpy = info.isAntiSpy();
		boolean noLog = info.isNoLog();
		boolean vanish = info.isVanished();
		boolean sneaky = info.isSneaky();
		String spySettings = "";
		if (noSpy) {
			spySettings += (spySettings.length() == 0 ? "" : ", ") + "NoSpy";
		}
		if (antiSpy) {
			spySettings += (spySettings.length() == 0 ? "" : ", ") + "AntiSpy";
		}
		if (noLog) {
			spySettings += (spySettings.length() == 0 ? "" : ", ") + "NoLog";
		}
		if (vanish) {
			spySettings += (spySettings.length() == 0 ? "" : ", ") + "Vanish";
		}
		if (noMsg) {
			spySettings += (spySettings.length() == 0 ? "" : ", ") + "NoMsg";
		}
		if (sneaky) {
			spySettings += (spySettings.length() == 0 ? "" : ", ") + "Sneaky";
		}
		MessageSender.sendMessage(sender, "Spy Settings: " + spySettings);
	}
}
