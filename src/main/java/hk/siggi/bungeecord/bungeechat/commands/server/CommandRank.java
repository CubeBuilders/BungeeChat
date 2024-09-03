package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.util.APIUtil;
import hk.siggi.bungeecord.bungeechat.util.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

public class CommandRank extends Command {

	private final BungeeChat plugin;

	public CommandRank(BungeeChat plugin) {
		super("rank");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] split) {
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = (ProxiedPlayer) sender;
			if (!player.hasPermission("hk.siggi.bungeecord.setrank")) {
				MessageSender.sendMessage(sender, ChatColor.RED + "You don't have permission to do this!");
				return;
			}
		}
		if (split.length < 3) {
			MessageSender.sendMessage(sender, ChatColor.RED + "Usage: /rank [add|del] [name|uuid] [rank]");
			return;
		}
		UUID user;
		if (split[1].length() > 16) {
			user = Util.uuidFromString(split[1]);
		} else {
			user = plugin.getPlayerNameHandler().getPlayerByName(split[1]);
		}
		String correctName = plugin.getPlayerNameHandler().getNameByPlayer(user);
		if (correctName == null) {
			correctName = split[1];
		}
		if (user == null) {
			BaseComponent noExist = new TextComponent("Player " + split[1] + " does not exist");
			noExist.setColor(ChatColor.RED);
			MessageSender.sendMessage(sender, noExist);
			return;
		}
		String rank = split[2];
		switch (split[0]) {
			case "add":
				APIUtil.addRank(user, rank);
				MessageSender.sendMessage(sender, ChatColor.AQUA + "Adding rank " + rank + " to " + correctName + ".");
				break;
			case "del":
				APIUtil.delRank(user, rank);
				MessageSender.sendMessage(sender, ChatColor.AQUA + "Deleting rank " + rank + " from " + correctName + ".");
				break;
			default:
				MessageSender.sendMessage(sender, ChatColor.RED + "Unknown subcommand: " + split[0]);
				break;
		}
	}
}
