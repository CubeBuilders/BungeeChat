package hk.siggi.bungeecord.bungeechat.ontime;

import java.util.UUID;

public final class OnTimeSessionRecord {

	/**
	 * The player's UUID.
	 */
	public final UUID player;
	/**
	 * The server the player is connected to.
	 */
	public final String server;
	/**
	 * The time the player logged in.
	 */
	public final long login;
	/**
	 * The time the player logged out. If the player has not logged out, this is -1.
	 */
	public final long logout;

	OnTimeSessionRecord(UUID player, String server, long login, long logout) {
		this.player = player;
		this.server = server;
		this.login = login;
		this.logout = logout;
	}

	/**
	 * Calculates the total amount of time the player was logged in for this session.
	 * @return total time logged in, in milliseconds.
	 */
	public long getTimeLoggedIn() {
		return (logout == -1L ? System.currentTimeMillis() : logout) - login;
	}
}
