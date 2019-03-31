package hk.siggi.bungeecord.bungeechat.player;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import java.util.UUID;

public class Mail {

	public final UUID uuid;
	public final long date;
	public final UUID from;
	public final UUID to;
	public final String message;

	private Mail() {
		this.uuid = UUID.randomUUID();
		this.date = 0L;
		this.from = null;
		this.to = null;
		this.message = null;
	}

	public Mail(long date, UUID from, UUID to, String message) {
		this(UUID.randomUUID(), date, from, to, message);
	}

	public Mail(UUID uuid, long date, UUID from, UUID to, String message) {
		this.uuid = uuid;
		this.date = date;
		this.from = from;
		this.to = to;
		this.message = message.replaceAll("\\|", "");
	}

	public String getFrom() {
		if (from.equals(BungeeChat.console)) {
			return "<Console>";
		}
		return BungeeChat.getInstance().getUUIDCache().getNameFromUUID(from);
	}
}
