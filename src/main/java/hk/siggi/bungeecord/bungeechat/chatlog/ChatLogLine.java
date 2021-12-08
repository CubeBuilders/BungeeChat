package hk.siggi.bungeecord.bungeechat.chatlog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public abstract class ChatLogLine {

	ChatLogLine(String message, ChatLogUser sender, long time) {
		this.message = message;
		this.sender = sender;
		this.time = time;
	}
	public final String message;
	public final ChatLogUser sender;
	public final long time;

	public abstract boolean isPlayerLikelyInvolved(String str);

	private static final TimeZone timeZone;
	private static final SimpleDateFormat dateFormat;
	private static final SimpleDateFormat dateShortFormat;

	static {
		timeZone = TimeZone.getTimeZone("America/New_York");
		dateFormat = new SimpleDateFormat("yyyy-MM-dd h:mm:ss a");
		dateFormat.setTimeZone(timeZone);
		dateShortFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateShortFormat.setTimeZone(timeZone);
	}

	public String getDate() {
		return dateShortFormat.format(new Date(time));
	}

	public String getDateTime() {
		return dateFormat.format(new Date(time)).replaceAll("AM", "am").replaceAll("PM", "pm");
	}

	public String getDateTime(String timezone) {
		SimpleDateFormat df = (SimpleDateFormat) dateFormat.clone();
		df.setTimeZone(TimeZone.getTimeZone(timezone));
		return df.format(new Date(time)).replaceAll("AM", "am").replaceAll("PM", "pm");
	}

	@Override
	public abstract String toString();
}
