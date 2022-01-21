package hk.siggi.bungeecord.bungeechat.commands.punishment;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.util.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandPunish extends Command implements TabExecutor {

	public final BungeeChat plugin;

	public CommandPunish(BungeeChat plugin) {
		super("punish", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		UUID issuer;
		String issuerName;
		boolean allowTroll, allowMute, allowBan;
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = ((ProxiedPlayer) sender);
			issuer = player.getUniqueId();
			issuerName = player.getName();
			allowTroll = allowBan = player.hasPermission("hk.siggi.bungeechat.ban");
			allowMute = player.hasPermission("hk.siggi.bungeechat.mute");
			if (!allowTroll && !allowMute && !allowBan) {
				MessageSender.sendMessage(player, Util.randomNotPermittedMessage());
				return;
			}
		} else {
			issuer = BungeeChat.console;
			issuerName = "<Console>";
			MessageSender.sendMessage(sender, "ONLY CONSOLE NIGGUH!");
			return;
		}
		
		if (args.length < 1) {
			MessageSender.sendMessage(sender, "Usage: /punish [playername]");
			return;
		}
		String target = args[0];
		UUID targetUUID = plugin.getPlayerNameHandler().getPlayerByName(target);
		if (targetUUID == null) {
			MessageSender.sendMessage(sender, "Player not found!");
			return;
		}
		BungeeChat.getSession((ProxiedPlayer) sender).openPunisher(targetUUID, allowTroll, allowMute, allowBan);
		return;
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender cs, String[] strings) {
		List<String> list = new ArrayList<>();
		if (strings.length == 1) {
			list.addAll(plugin.getPlayerNameHandler().autocompleteOnlinePlayers(strings[0]));
		}
		return list;
	}
}
