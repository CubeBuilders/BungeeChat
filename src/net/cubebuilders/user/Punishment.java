package net.cubebuilders.user;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class Punishment {

	public enum PunishmentAction {

		WARNING, MUTE, BAN;

		@Override
		public String toString() {
			switch (this) {
				case WARNING:
					return "Warning";
				case MUTE:
					return "Mute";
				case BAN:
					return "Ban";
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
			return null;
		}
	}

	public enum PunishmentStatus {

		ACTIVE, MISTAKE, FORGIVEN;

		@Override
		public String toString() {
			switch (this) {
				case ACTIVE:
					return "Active";
				case MISTAKE:
					return "Mistake";
				case FORGIVEN:
					return "Forgiven";
			}
			return null;
		}

		public static PunishmentStatus fromString(String action) {
			if (action.equalsIgnoreCase("Active")) {
				return ACTIVE;
			}
			if (action.equalsIgnoreCase("Mistake")) {
				return MISTAKE;
			}
			if (action.equalsIgnoreCase("Forgiven")) {
				return FORGIVEN;
			}
			return null;
		}
	}
	PunishmentAction action;
	String offence;
	PunishmentStatus status;
	ArrayList<OffenceEvidence> evidence;
	ArrayList<PunishmentHistory> history;
	private static final PunishmentHistory[] emptyHistory = new PunishmentHistory[0];
	long time;
	long startTime;
	long length;
	String reason;
	UUID issuedBy;
	UUID issuedTo;
	boolean cancelled = false;
	int appeal = -1;

	public Punishment(PunishmentAction action, String offence, long time, long startTime, long length, String reason, UUID issuedBy, UUID issuedTo) {
		if (issuedBy == null) {
			issuedBy = new UUID(0L, 0L);
		}
		this.action = action;
		this.offence = offence;
		this.time = time;
		this.startTime = startTime;
		this.length = length;
		this.reason = reason;
		this.status = PunishmentStatus.ACTIVE;
		this.issuedBy = issuedBy;
		this.issuedTo = issuedTo;
		history = new ArrayList<>();
		history.add(new PunishmentHistory(issuedBy, time, startTime, action, length, reason, status, cancelled, null));
	}

	public Punishment(Punishment punishment, String offence, PunishmentAction action, long startTime, long length, String reason, PunishmentStatus status, UUID editedBy, long editTime, String editReason, boolean cancelled) {
		if (editedBy == null) {
			editedBy = new UUID(0L, 0L);
		}
		this.action = action;
		this.offence = offence;
		this.time = punishment.time;
		this.startTime = startTime;
		this.length = length;
		this.reason = reason;
		this.status = status;
		this.issuedBy = punishment.issuedBy;
		this.issuedTo = punishment.issuedTo;
		this.cancelled = cancelled;
		this.appeal = punishment.appeal;
		history = new ArrayList<>();
		history.addAll(punishment.history);
		history.add(new PunishmentHistory(editedBy, editTime, startTime, action, length, reason, status, cancelled, editReason));
	}

	public PunishmentAction getAction() {
		return action;
	}

	public String getOffence() {
		return offence;
	}

	public PunishmentStatus getStatus() {
		return status;
	}

	public long getIssueDate() {
		return time;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getLength() {
		return length;
	}

	public String getReason() {
		return reason;
	}

	public UUID getIssuedBy() {
		return issuedBy;
	}

	public UUID getIssuedTo() {
		return issuedTo;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public PunishmentHistory[] getHistory() {
		return history.toArray(emptyHistory);
	}
	
	public int getAppeal() {
		return appeal;
	}
	
	public void setAppeal(int appeal) {
		this.appeal = appeal;
	}

	public String toJson(boolean prettyPrinting) {
		return CBUser.punishmentToJson(this, prettyPrinting);
	}

	public static Punishment fromJson(String json) {
		return CBUser.punishmentFromJson(json);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
		if (!(object instanceof Punishment)) {
			return false;
		}
		return equals((Punishment) object);
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 43 * hash + (int) (this.time ^ (this.time >>> 32));
		hash = 43 * hash + Objects.hashCode(this.issuedTo);
		return hash;
	}
	public boolean equals(Punishment other) {
		return this == other || (other.issuedTo.equals(issuedTo) && other.time == time);
	}
}
