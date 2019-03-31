package hk.siggi.bungeecord.bungeechat.player;

import java.util.UUID;

public class MCBan {

	public final UUID player;
	public final String reason;
	public final String server;
	public final String prosecutor;

	public MCBan(UUID player, String reason, String server, String prosecutor) {
		this.player = player;
		this.reason = reason;
		this.server = server;
		this.prosecutor = prosecutor;
	}

	private MCBan() {
		this.player = null;
		this.reason = this.server = this.prosecutor = null;
	}
}
