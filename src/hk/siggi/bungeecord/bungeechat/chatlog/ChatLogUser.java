package hk.siggi.bungeecord.bungeechat.chatlog;

import java.util.UUID;

public class ChatLogUser {

	/**
	 * The username of the player at the time of the chat message. This is not
	 * always the current username of the player.
	 */
	public final String username;
	/**
	 * The UUID of the player, if it is known at the time of the chat message,
	 * or null if it's not known.
	 */
	public final UUID uuid;

	public ChatLogUser(String username) {
		this(username, null);
	}

	public ChatLogUser(String username, UUID uuid) {
		this.username = username;
		this.uuid = uuid;
	}

	@Override
	public String toString() {
		if (uuid == null) {
			return username;
		} else {
			return username+"/"+(uuid.toString().replace("-","").toLowerCase());
		}
	}
}
