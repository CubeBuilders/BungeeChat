package hk.siggi.bungeecord.bungeechat.commands.moderation;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandStream extends Command {

	public final BungeeChat plugin;

	public CommandStream(BungeeChat plugin) {
		super("stream", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		String name;
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = ((ProxiedPlayer) sender);
			name = player.getName();
		} else {
			sender.sendMessage("This command can only be used in-game.");
			return;
		}
		PlayerAccount info = plugin.getPlayerInfo(((ProxiedPlayer) sender).getUniqueId());
		if (info.isStreamModeActive()) {
			info.setStreamMode(false);
			sender.sendMessage("Stream mode has been disabled. (nospy is still disabled)");
		} else {
			long expiry = System.currentTimeMillis() + 4200000L;
			info.setStreamMode(true);
			info.setNoSpy(true);
			sender.sendMessage("Stream mode and nospy has been enabled.");
		}
	}
}
