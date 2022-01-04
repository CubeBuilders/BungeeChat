package hk.siggi.bungeecord.bungeechat.player;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.util.TimeUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.UUID;

/**
 *
 * @author Siggi
 * @deprecated Use net.cubebuilders.user.Punishment instead!
 */
@Deprecated
public class Punishment {

	public enum PunishmentAction {

		WARNING, MUTE, BAN, UNMUTE, UNBAN, STRIKE;

		@Override
		public String toString() {
			switch (this) {
				case WARNING:
					return "Warning";
				case MUTE:
					return "Mute";
				case BAN:
					return "Ban";
				case UNMUTE:
					return "Unmute";
				case UNBAN:
					return "Unban";
				case STRIKE:
					return "Strike";
			}
			return null;
		}

		public static PunishmentAction fromString(String action) {
			if (action.equalsIgnoreCase("Warning")) {
				return WARNING;
			}
			if (action.equalsIgnoreCase("Mute")) {
				return MUTE;
			}
			if (action.equalsIgnoreCase("Ban")) {
				return BAN;
			}
			if (action.equalsIgnoreCase("Unmute")) {
				return UNMUTE;
			}
			if (action.equalsIgnoreCase("Unban")) {
				return UNBAN;
			}
			if (action.equalsIgnoreCase("Strike")) {
				return STRIKE;
			}
			return null;
		}
	}
	private static final TimeZone timeZone;
	private static final SimpleDateFormat dateFormat;
	private static final SimpleDateFormat dateShortFormat;

	static {
		timeZone = TimeZone.getTimeZone("GMT+8:00");
		dateFormat = new SimpleDateFormat("dd/MM/yyyy h:mm:ss a");
		dateFormat.setTimeZone(timeZone);
		dateShortFormat = new SimpleDateFormat("dd/MM/yyyy");
		dateShortFormat.setTimeZone(timeZone);
	}
	public final PunishmentAction action;
	public final long time;
	public final long length;
	public final String reason;
	public final UUID issuedBy;
	public final UUID issuedTo;
	private String cachedIssueDate = null;
	private String cachedShortIssueDate = null;
	private String cachedLength = null;

	private Punishment() {
		this.action = null;
		this.time = 0L;
		this.length = 0L;
		this.reason = null;
		this.issuedBy = null;
		this.issuedTo = null;
	}

	public Punishment(PunishmentAction action, long time, long length, String reason, UUID issuedBy, UUID issuedTo) {
		if (reason.contains("|")) {
			reason = reason.replaceAll("\\|", "");
		}
		this.action = action;
		this.time = time;
		this.length = length;
		this.reason = reason;
		this.issuedBy = issuedBy;
		this.issuedTo = issuedTo;
	}

	public String getIssueDate() {
		if (cachedIssueDate != null) {
			return cachedIssueDate;
		}
		return cachedIssueDate = dateFormat.format(new Date(time)).replaceAll("AM", "am").replaceAll("PM", "pm");
	}

	public String getIssueDate(String timezone) {
		SimpleDateFormat df = (SimpleDateFormat) dateFormat.clone();
		df.setTimeZone(TimeZone.getTimeZone(timezone));
		return df.format(new Date(time)).replaceAll("AM", "am").replaceAll("PM", "pm");
	}

	public String getShortIssueDate() {
		if (cachedShortIssueDate != null) {
			return cachedShortIssueDate;
		}
		return cachedShortIssueDate = dateShortFormat.format(new Date(time)).replaceAll("AM", "am").replaceAll("PM", "pm");
	}

	public String getShortIssueDate(String timezone) {
		SimpleDateFormat df = (SimpleDateFormat) dateShortFormat.clone();
		df.setTimeZone(TimeZone.getTimeZone(timezone));
		return df.format(new Date(time)).replaceAll("AM", "am").replaceAll("PM", "pm");
	}

	public String getLength() {
		if (cachedLength != null) {
			return cachedLength;
		}
		if (action == PunishmentAction.WARNING || action == PunishmentAction.UNMUTE || action == PunishmentAction.UNBAN || action == PunishmentAction.STRIKE) {
			return cachedLength = "n/a";
		}
		if (length == 0L) {
			return cachedLength = "Permanent";
		}
		return cachedLength = TimeUtil.timeDifference(time, time + length);
	}

	public String getIssuedBy() {
		if (issuedBy.equals(BungeeChat.console)) {
			return "<Console>";
		}
		return BungeeChat.getInstance().getUUIDCache().getNameFromUUID(issuedBy);
	}

	public String getIssuedTo() {
		return BungeeChat.getInstance().getUUIDCache().getNameFromUUID(issuedTo);
	}
}
