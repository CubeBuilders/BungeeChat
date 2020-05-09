package hk.siggi.bungeecord.bungeechat;

import hk.siggi.bungeecord.bungeechat.ontime.OnTime;
import hk.siggi.bungeecord.bungeechat.ontime.OnTimePlayer;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class PlayerNameHandler {

	private final BungeeChat plugin;

	public PlayerNameHandler(BungeeChat plugin) {
		this.plugin = plugin;
	}

	/**
	 * Get a player's display name.
	 *
	 * @param pl The player
	 * @return The display name of the player
	 */
	public String getNameByPlayer(ProxiedPlayer pl) {
		return getNameByPlayer(pl.getUniqueId());
	}

	/**
	 * Get a player's display name from their UUID.
	 *
	 * @param uuid The UUID of the player
	 * @return The display name of the player
	 */
	public String getNameByPlayer(UUID uuid) {
		PlayerAccount acc = plugin.getPlayerInfo(uuid);
		String nickname = acc.getNickname();
		if (nickname != null) {
			return BungeeChat.NICK_PREFIX + nickname;
		}
		return plugin.getUUIDCache().getNameFromUUID(uuid);
	}

	/**
	 * Get a player by their username or nickname. Priority matching with
	 * original name first, if a player with the original name isn't found, it
	 * searches for a player with a matching nickname. If the passed name starts
	 * with *, will skip regular names entirely and only match against a
	 * nickname.
	 *
	 * @param name The name to match
	 * @return The UUID of the matched player
	 */
	public UUID getPlayerByName(String name) {
		UUID uuidFromName;
		if (name.startsWith(BungeeChat.NICK_PREFIX)) {
			name = name.substring(1);
			uuidFromName = null;
		} else {
			uuidFromName = plugin.getUUIDCache().getUUIDFromName(name);
		}
		return uuidFromName == null ? plugin.getNicknameCache().getUserByNickname(name) : uuidFromName;
	}

	/**
	 * Get a player by their username or nickname. Priority matching with
	 * original name first, if a player with the original name isn't found, it
	 * searches for a player with a matching nickname. If the passed name starts
	 * with *, will skip regular names entirely and only match against a
	 * nickname.
	 *
	 * @param name The name to match
	 * @param allowPartialMatch If true, will match partially against online
	 * players.
	 * @return The UUID of the matched player
	 */
	public UUID getPlayerByName(String name, boolean allowPartialMatch) {
		UUID uuidFromName = getPlayerByName(name);
		if (uuidFromName != null || !allowPartialMatch) {
			return uuidFromName;
		}
		name = name.toLowerCase();
		for (ProxiedPlayer pl : plugin.getProxy().getPlayers()) {
			String playerName = pl.getName().toLowerCase();
			String playerNick = plugin.getPlayerInfo(pl.getUniqueId()).getNickname();
			if (playerNick != null) {
				playerNick = playerNick.toLowerCase();
			}
			if (playerName.contains(name) || (playerNick != null && playerNick.contains(name))) {
				if (uuidFromName != null) {
					return null;
				}
				uuidFromName = pl.getUniqueId();
			}
		}
		return uuidFromName;
	}

	/**
	 * Get up to 100 player names for autocompletion. Priority matching with
	 * nicknames first, then original names. If the passed name starts with *,
	 * will skip regular names entirely and only match against a nickname.
	 *
	 * @param name The name to match
	 * @return List of names that match
	 */
	public List<String> autocompletePlayers(String name) {
		return PlayerNameHandler.this.autocompletePlayers(name, null);
	}

	/**
	 * Same as {@link #autocompletePlayers(java.lang.String)} but allows you to
	 * limit which users get matched.
	 *
	 * @param name The name to match
	 * @param allowedUsers Predicate to restrict which users get added to the
	 * list
	 * @return List of names that match
	 */
	public List<String> autocompletePlayers(String name, Predicate<UUID> allowedUsers) {
		boolean skipNonNicks = false;
		if (name.startsWith(BungeeChat.NICK_PREFIX)) {
			skipNonNicks = true;
			name = name.substring(1);
		}
		int limit = 100;
		List<String> matchedNames = new LinkedList<>();
		UUIDCache uc = plugin.getUUIDCache();
		NicknameCache nc = plugin.getNicknameCache();
		matchedNames.addAll(nc.getUsersWithNameStartingWith(name, allowedUsers == null ? limit : 0));
		if (allowedUsers == null) {
			limit -= matchedNames.size();
		} else {
			matchedNames.removeIf((n) -> !allowedUsers.test(nc.getUserByNickname(n)));
		}
		if (!skipNonNicks && ((allowedUsers == null && limit > 0)
				|| (allowedUsers != null && matchedNames.size() < limit))) {
			uc.getPlayersWithNamesStartingWith(name, allowedUsers == null ? limit : 0).forEach(
					(UUID u) -> {
						if (allowedUsers == null || allowedUsers.test(u)) {
							matchedNames.add(uc.getNameFromUUID(u));
						}
					}
			);
		}
		if (allowedUsers != null) {
			while (matchedNames.size() > limit) {
				matchedNames.remove(matchedNames.size() - 1);
			}
		}
		matchedNames.sort(sortByRecentlyOnline(name));
		return matchedNames;
	}

	public List<String> autocompleteOnlinePlayers(String name) {
		return autocompleteOnlinePlayers(name, null);
	}

	public List<String> autocompleteOnlinePlayers(String name, Predicate<ProxiedPlayer> allowedPlayers) {
		name = name.toLowerCase();
		int limit = 100;
		List<String> usernames = new LinkedList<>();
		List<String> nicknames = new LinkedList<>();

		for (ProxiedPlayer pl : plugin.getProxy().getPlayers()) {
			if (allowedPlayers != null && !allowedPlayers.test(pl)) {
				continue;
			}
			String username = pl.getName();
			if (username.toLowerCase().startsWith(name)) {
				usernames.add(username);
			}
			String nickname = plugin.getPlayerInfo(pl.getUniqueId()).getNickname();
			if (nickname != null && nickname.toLowerCase().startsWith(name)) {
				nicknames.add(nickname);
			}
		}

		List<String> allNames = new LinkedList<>();
		allNames.addAll(nicknames);
		allNames.addAll(usernames);
		while (allNames.size() > limit) {
			allNames.remove(allNames.size() - 1);
		}
		allNames.sort(sortByRecentlyOnline(name));
		return allNames;
	}

	private Comparator<String> sortByRecentlyOnline(String typedName) {
		return (String o1, String o2) -> {if(typedName!=null){
			if (o1.equalsIgnoreCase(o2)) {
				return 0;
			} else if (o1.equalsIgnoreCase(typedName)) {
				return -1;
			} else if (o2.equalsIgnoreCase(typedName)) {
				return 1;
			}}
			UUID u1 = getPlayerByName(o1);
			UUID u2 = getPlayerByName(o2);
			ProxiedPlayer p1 = u1 == null ? null : plugin.getProxy().getPlayer(u1);
			ProxiedPlayer p2 = u2 == null ? null : plugin.getProxy().getPlayer(u2);
			if (p1 != null && p2 == null) {
				return -1;
			} else if (p1 == null && p2 != null) {
				return 1;
			}
			long now = System.currentTimeMillis();
			OnTimePlayer otp1 = OnTime.getInstance().getPlayer(u1);
			OnTimePlayer otp2 = OnTime.getInstance().getPlayer(u2);
			long lastLogin1 = otp1.getLastOnline();
			long lastLogin2 = otp2.getLastOnline();
			if (lastLogin1 == -1L && lastLogin2 == -1) {
				return o1.toLowerCase().compareTo(o2.toLowerCase());
			} else if (lastLogin1 == -1L) {
				lastLogin1 = now;
			} else if (lastLogin2 == -1L) {
				lastLogin2 = now;
			}
			if (lastLogin1 > lastLogin2) {
				return -1;
			} else if (lastLogin2 > lastLogin1) {
				return 1;
			} else {
				return o1.toLowerCase().compareTo(o2.toLowerCase());
			}
		};
	}
}
