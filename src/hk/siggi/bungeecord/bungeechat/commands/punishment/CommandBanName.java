package hk.siggi.bungeecord.bungeechat.commands.punishment;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandBanName extends Command implements TabExecutor {

	private final BungeeChat plugin;

	public CommandBanName(BungeeChat plugin) {
		super("banname", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!sender.hasPermission("hk.siggi.bungeechat.ban")) {
			sender.sendMessage(plugin.unify(plugin.processChat(null, "&4You don't have permission.")));
			return;
		}
		if (args.length == 0) {
			sender.sendMessage(plugin.unify(plugin.processChat(null, "&6Usage: &b/banname [username]")));
			sender.sendMessage(plugin.unify(plugin.processChat(null, "&6This command bans usernames, not accounts. The account owner can change their name to get out of this ban. This can be used to ban inappropriate usernames from joining.")));
			return;
		}
		UUID issuer = BungeeChat.console;
		String issuerName = "CONSOLE";
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			issuer = p.getUniqueId();
			issuerName = p.getName();
		}
		UUID targetUUID = plugin.getPlayerNameHandler().getPlayerByName(args[0]);
		if (plugin.isNameBanned(args[0])) {
			sender.sendMessage(plugin.unify(plugin.processChat(null, "&b" + args[0] + "&6 is already banned.")));
			return;
		}
		String targetName;
		if (targetUUID == null) {
			boolean bypass = false;
			if (args.length < 2 || !args[1].equalsIgnoreCase("force")) {
				sender.sendMessage(plugin.unify(plugin.processChat(null, "&6Unknown name: &b" + args[0] + "&6. Type &b/banname " + args[0] + " force&6 to force ban.")));
				return;
			}
			targetName = args[0];
		} else {
			String nick = plugin.getNicknameCache().getNickname(targetUUID);
			if (nick != null && nick.equals(args[0])) {
				targetName = nick;
			} else
			targetName = plugin.getPlayerNameHandler().getNameByPlayer(targetUUID);
		}
		sender.sendMessage(plugin.unify(plugin.processChat(null, "&6Banning username &b" + args[0] + "&6. (If they're currently online, you can either /kick, or issue a regular /ban) (if this is a nickname ban, you have to remove their nick as well manually)")));
		plugin.banName(targetName, issuer, issuerName);
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		List<String> list = new LinkedList<>();
		if (args.length == 1) {
			plugin.getUUIDCache().getPlayersWithNamesStartingWith(args[0], 100);
		}
		return list;
	}

}
