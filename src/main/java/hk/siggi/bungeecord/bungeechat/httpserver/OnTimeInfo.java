package hk.siggi.bungeecord.bungeechat.httpserver;

import java.awt.Color;

public class OnTimeInfo {

	public final String server;
	public final Color color;
	public long time = 0L;

	public OnTimeInfo(String server, Color color) {
		this.server = server;
		this.color = color;
	}
}
