package hk.siggi.bungeecord.bungeechat.commands.messaging;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import net.cubebuilders.user.NameHistory;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandRealName extends Command implements TabExecutor {

	private final BungeeChat plugin;

	public CommandRealName(BungeeChat plugin) {
		super("realname", null, "names");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(plugin.unify(plugin.processChat(null, "&6Usage: &b/realname [playername]")));
			return;
		}
		String n = args[0];
		UUID u = plugin.getPlayerNameHandler().getPlayerByName(n);
		String realName = plugin.getUUIDCache().getNameFromUUID(u);
		sender.sendMessage(plugin.unify(plugin.processChat(null, "&6Real name: &b" + realName)));
		List<String> prevNames = new LinkedList<>();
		for (NameHistory history : plugin.getPlayerInfo(u).getNameHistory()) {
			String nn = history.getName();
			if (!plugin.isNameBanned(nn) && !prevNames.contains(nn)) {
				prevNames.add(nn);
			}
		}
		if (!prevNames.isEmpty()) {
		StringBuilder nameList = new StringBuilder();
		for (String prevName : prevNames) {
			if (nameList.length() != 0) {
				nameList.append("&6, &b");
			}
			nameList.append(prevName);
		}
		String result = "&6Previous names: &b" + (nameList.toString()) + "&6.";
		sender.sendMessage(plugin.unify(plugin.processChat(null, result)));
		}
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		String lastLower = args[args.length - 1].toLowerCase();
		List<String> list = new LinkedList<>();
		if (args.length == 1) {
			list.addAll(plugin.getPlayerNameHandler().autocompletePlayers(lastLower));
		}
		return list;
	}

}
