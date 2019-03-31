package net.cubebuilders.user;

import java.util.UUID;
import net.cubebuilders.user.Punishment.PunishmentAction;
import net.cubebuilders.user.Punishment.PunishmentStatus;

public class PunishmentHistory {
	/**
	 * The staff member who made a change.
	 */
	public final UUID user;
	/**
	 * The time the change was made.
	 */
	public final long time;
	/**
	 * The start time.
	 */
	public final long startTime;
	/**
	 * The action.
	 */
	public final PunishmentAction action;
	/**
	 * The length of punishment.
	 */
	public final long length;
	/**
	 * The reason for punishment displayed to the punished user.
	 */
	public final String reason;
	/**
	 * The status.
	 */
	public final PunishmentStatus status;
	/**
	 * If the punishment is cancelled.
	 */
	public final boolean cancelled;
	/**
	 * The reason for editing.
	 */
	public final String editReason;
	
	public PunishmentHistory(UUID user, long time, long startTime, PunishmentAction action, long length, String reason, PunishmentStatus status, boolean cancelled, String editReason) {
		this.user = user;
		this.time = time;
		this.startTime = startTime;
		this.action = action;
		this.length = length;
		this.reason = reason;
		this.status = status;
		this.cancelled = cancelled;
		this.editReason = editReason;
	}
	
	public PunishmentHistory(PunishmentHistory history, String editReason) {
		this.user = history.user;
		this.time = history.time;
		this.startTime = history.startTime;
		this.action = history.action;
		this.length = history.length;
		this.reason = history.reason;
		this.status = history.status;
		this.cancelled = history.cancelled;
		this.editReason = editReason;
	}
}
