package hk.siggi.bungeecord.bungeechat.antiddos;

public class RecentChat {
	public final String line;
	public final long time;
	public RecentChat(String line, long time) {
		this.line = line;
		this.time = time;
	}
}
