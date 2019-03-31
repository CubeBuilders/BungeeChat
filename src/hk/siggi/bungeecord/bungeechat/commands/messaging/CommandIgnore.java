package hk.siggi.bungeecord.bungeechat.commands.messaging;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.NicknameCache;
import hk.siggi.bungeecord.bungeechat.PlayerNameHandler;
import hk.siggi.bungeecord.bungeechat.UUIDCache;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandIgnore extends Command implements TabExecutor {

	private final BungeeChat plugin;

	public CommandIgnore(BungeeChat plugin) {
		super("ignore", null, "ignorelist");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender cs, String[] strings) {
		if (!(cs instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer pl = (ProxiedPlayer) cs;
		PlayerAccount playerInfo = plugin.getPlayerInfo(pl.getUniqueId());
		PlayerNameHandler playerNameHandler = plugin.getPlayerNameHandler();
		String command = strings.length >= 1 ? strings[0] : "";
		if (pl.hasPermission("hk.siggi.bungeechat.ignorenotallowed") && !command.equals("removeall")) {
			if (!playerInfo.getIgnores().isEmpty()) {
				playerInfo.clearIgnores();
			}
			pl.sendMessage(plugin.unify(plugin.processChat(null, "&4As a staff member, you are not allowed to ignore players.")));
			return;
		}
		switch (command) {
			case "add": {
				if (strings.length >= 2) {
					UUID target = playerNameHandler.getPlayerByName(strings[1]);
					if (target == null) {
						pl.sendMessage(plugin.unify(plugin.processChat(null, "&6Unknown user " + strings[1])));
					} else {
						String name = playerNameHandler.getNameByPlayer(target);
						playerInfo.addIgnore(target);
						pl.sendMessage(plugin.unify(plugin.processChat(null, "&6Added &b" + name + " &6to your ignore list.")));
					}
				} else {
					pl.sendMessage(plugin.unify(plugin.processChat(null, "&6Usage:")));
					pl.sendMessage(plugin.unify(plugin.processChat(null, "&b /ignorelist add [playername] &6- Add a player to your ignore list.")));
				}
			}
			break;
			case "remove": {
				if (strings.length >= 2) {
					UUID target = playerNameHandler.getPlayerByName(strings[1]);
					if (target == null) {
						pl.sendMessage(plugin.unify(plugin.processChat(null, "&6Unknown user " + strings[1])));
					} else {
						String name = playerNameHandler.getNameByPlayer(target);
						playerInfo.removeIgnore(target);
						pl.sendMessage(plugin.unify(plugin.processChat(null, "&6Removed &b" + name + " &6from your ignore list.")));
					}
				} else {
					pl.sendMessage(plugin.unify(plugin.processChat(null, "&6Usage:")));
					pl.sendMessage(plugin.unify(plugin.processChat(null, "&b /ignorelist remove [playername] &6- Remove a player from your ignore list.")));
				}
			}
			break;
			case "removeall": {
				playerInfo.clearIgnores();
				pl.sendMessage(plugin.unify(plugin.processChat(null, "&6Your ignore list has been cleared.")));
			}
			break;
			case "list": {

			}
			break;
			default: {
				pl.sendMessage(plugin.unify(plugin.processChat(null, "&6Usage:")));
				pl.sendMessage(plugin.unify(plugin.processChat(null, "&b /ignorelist add [playername] &6- Add a player to your ignore list.")));
				pl.sendMessage(plugin.unify(plugin.processChat(null, "&b /ignorelist remove [playername] &6- Remove a player from your ignore list.")));
				pl.sendMessage(plugin.unify(plugin.processChat(null, "&b /ignorelist removeall &6- Empty your ignore list.")));
				pl.sendMessage(plugin.unify(plugin.processChat(null, "&b /ignorelist list &6- See who is on your ignore list.")));
			}
			break;
		}
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender cs, String[] strings) {
		List<String> suggestions = new LinkedList<>();
		if (!(cs instanceof ProxiedPlayer)) {
			return suggestions;
		}
		String lastStrLowerCase = strings[strings.length - 1].toLowerCase();
		ProxiedPlayer pl = (ProxiedPlayer) cs;
		Consumer<String> addSuggestion = (suggestion) -> {
			if (suggestion.toLowerCase().startsWith(lastStrLowerCase)) {
				suggestions.add(suggestion);
			}
		};
		UUIDCache uc = plugin.getUUIDCache();
		NicknameCache nc = plugin.getNicknameCache();
		PlayerNameHandler playerNameHandler = plugin.getPlayerNameHandler();
		Consumer<UUID> addUUID = (suggestion) -> {
			String name = uc.getNameFromUUID(suggestion);
			String nick = nc.getNickname(suggestion);
			if (name != null) {
				addSuggestion.accept(name);
			}
			if (nick != null) {
				addSuggestion.accept(nick);
			}
		};
		if (strings.length == 1) {
			addSuggestion.accept("add");
			addSuggestion.accept("remove");
			addSuggestion.accept("removeall");
			addSuggestion.accept("list");
		} else if (strings.length == 2) {
			switch (strings[0]) {
				case "add": {
					suggestions.addAll(playerNameHandler.autocompletePlayers(lastStrLowerCase));
				}
				break;
				case "remove": {
					PlayerAccount acc = plugin.getPlayerInfo(pl.getUniqueId());
					for (UUID uuid : acc.getIgnores()) {
						addUUID.accept(uuid);
					}
				}
				break;
			}
		}
		return suggestions;
	}

}
