package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import java.util.Collection;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandSetGroup extends Command {

	public final BungeeChat plugin;

	public CommandSetGroup(BungeeChat plugin) {
		super("setgroup", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if (!p.hasPermission("hk.siggi.bungeechat.setgroup")) {
				TextComponent no = new TextComponent("You can't do that.");
				no.setColor(ChatColor.RED);
				MessageSender.sendMessage(sender, no);
				return;
			}
		}
		if (args.length == 0) {
			TextComponent usage1 = new TextComponent("Usage: /setgroup [player] [group,group2,group3]");
			usage1.setColor(ChatColor.GOLD);
			MessageSender.sendMessage(sender, usage1);
			TextComponent usage2 = new TextComponent("Usage: /setgroup [player] fake [group,group2,group3]");
			usage2.setColor(ChatColor.GOLD);
			MessageSender.sendMessage(sender, usage2);
			return;
		}
		ProxiedPlayer target = plugin.getProxy().getPlayer(args[0]);
		if (target == null) {
			TextComponent no = new TextComponent("Player not found.");
			no.setColor(ChatColor.RED);
			MessageSender.sendMessage(sender, no);
			return;
		}
		Collection<String> currentGroupsCollection = target.getGroups();
		String[] currentGroups = currentGroupsCollection.toArray(new String[currentGroupsCollection.size()]);
		if (args.length == 1) {
			for (String group : currentGroups) {
				MessageSender.sendMessage(sender, group);
			}
			return;
		}
		boolean fake = false;
		String groupsStr = args[1];
		if (args.length > 2) {
			if (args[1].equalsIgnoreCase("fake")) {
				fake = true;
				groupsStr = args[2];
			}
		}
		String[] groups = groupsStr.split(",");
		if (fake) {
			BungeeChat.getSession(target).setFakeGroups(groups);
			TextComponent ok = new TextComponent("Set fake groups.");
			ok.setColor(ChatColor.GOLD);
			MessageSender.sendMessage(sender, ok);
		} else {
			target.removeGroups(currentGroups);
			target.addGroups(groups);
			BungeeChat.getSession(target).updateBungeePermissionCache();
			plugin.sendInfoUpdate(target, target.getServer());
			TextComponent ok = new TextComponent("Groups updated.");
			ok.setColor(ChatColor.GOLD);
			MessageSender.sendMessage(sender, ok);
		}
	}
}
