package hk.siggi.bungeecord.bungeechat.commands.punishment;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.util.Util;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandOldTemporaryBan extends Command {
	public final BungeeChat plugin;
	public CommandOldTemporaryBan(BungeeChat plugin) {
		super("tempban", null);
		this.plugin = plugin;
	}
	@Override
	public void execute(CommandSender sender, String[] args) {
		UUID issuer;
		String issuerName;
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = ((ProxiedPlayer) sender);
			issuer = player.getUniqueId();
			issuerName = player.getName();
			if (!player.hasPermission("hk.siggi.bungeechat.ban")) {
				MessageSender.sendMessage(player, Util.randomNotPermittedMessage());
				return;
			}
		} else {
			issuer = BungeeChat.console;
			issuerName = "<Console>";
		}
		if (args.length < 2) {
			BaseComponent usage = new TextComponent("This command has been removed, instead use: ");
			usage.setColor(ChatColor.AQUA);
			BaseComponent extra = new TextComponent("/ban <name> <duration> <reason>");
			extra.setColor(ChatColor.WHITE);
			usage.addExtra(extra);
			MessageSender.sendMessage(sender, usage);
			return;
		}
	}
}
