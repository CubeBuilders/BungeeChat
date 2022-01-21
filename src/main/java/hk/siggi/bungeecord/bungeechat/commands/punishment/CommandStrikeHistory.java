package hk.siggi.bungeecord.bungeechat.commands.punishment;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class CommandStrikeHistory extends Command {
	public final BungeeChat plugin;
	public CommandStrikeHistory(BungeeChat plugin) {
		super("strikehistory", null);
		this.plugin = plugin;
	}
	@Override
	public void execute(CommandSender sender, String[] args) {
		BaseComponent commandRemovedMessage = new TextComponent("This command has been removed.");
		commandRemovedMessage.setColor(ChatColor.AQUA);
		MessageSender.sendMessage(sender, commandRemovedMessage);
		return;
	}
}
