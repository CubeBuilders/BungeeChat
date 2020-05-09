package hk.siggi.bungeecord.bungeechat;

import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import hk.siggi.bungeecord.bungeechat.util.Util;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class NicknameCache {

	private final HashMap<UUID, String> nicknamesByUUID = new HashMap<>();
	private final HashMap<String, UUID> uuidByNicknames = new HashMap<>();
	private boolean finishedLoadingNicknames = false;

	void startLoadingNicknames() {
		new Thread(this::loadNicknames).start();
	}

	private void loadNicknames() {
		try {
			File[] playerFiles = new File(BungeeChat.getInstance().getDataFolder(), "playerdata").listFiles();
			if (playerFiles == null) {
				return;
			}
			long now = System.currentTimeMillis();
			for (File playerFile : playerFiles) {
				try {
					String uuidString = playerFile.getName();
					if (uuidString.contains(".")) {
						uuidString = uuidString.substring(0, uuidString.indexOf("."));
					}
					UUID uuid = Util.uuidFromString(uuidString);
					PlayerAccount player = new PlayerAccount(uuid);
					String nickname = player.getNickname();
					if (nickname != null) {
						nicknamesByUUID.put(uuid, nickname);
						uuidByNicknames.put(nickname.toLowerCase(), uuid);
					}
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
		} finally {
			finishedLoadingNicknames = true;
		}
	}

	public void setNicknameCache(UUID player, String nickname) {
		String get = nicknamesByUUID.get(player);
		if (get != null) {
			uuidByNicknames.remove(get.toLowerCase());
		}
		if (nickname == null) {
			nicknamesByUUID.remove(player);
		} else {
			nicknamesByUUID.put(player, nickname);
			uuidByNicknames.put(nickname.toLowerCase(), player);
		}
	}

	public String getNickname(UUID user) {
		return nicknamesByUUID.get(user);
	}

	public UUID getUserByNickname(String nickname) {
		return uuidByNicknames.get(nickname.toLowerCase());
	}

	public boolean isNicknameUsed(String nickname) {
		return uuidByNicknames.containsKey(nickname);
	}

	public boolean nicknamesLoaded() {
		return finishedLoadingNicknames;
	}

	public List<String> getUsersWithNameStartingWith(String nickname) {
		return getUsersWithNameStartingWith(nickname, 0);
	}

	public List<String> getUsersWithNameStartingWith(String nickname, int limit) {
		List<String> nicks = new LinkedList<>();
		nickname = nickname.toLowerCase();
		for (String nick : nicknamesByUUID.values()) {
			if (nick.toLowerCase().startsWith(nickname)) {
				nicks.add(nick);
				if (limit > 0 && nicks.size() >= limit) {
					break;
				}
			}
		}
		return nicks;
	}
}
