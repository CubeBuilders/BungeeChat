package hk.siggi.bungeecord.bungeechat.notifications;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hk.siggi.bungeecord.bungeechat.event.PlayerSpeedingEvent;
import java.util.UUID;

public class SpeedAlert extends NotificationTrigger<PlayerSpeedingEvent, String> {

	private final boolean firstOnly;

	public SpeedAlert(Notifications notifications, JsonObject trigger) {
		super(notifications, trigger);
		JsonElement firstOnlyElement = trigger.get("firstOnly");
		boolean firstOnlyB;
		try {
			firstOnlyB = firstOnlyElement.getAsBoolean();
		} catch (Exception e) {
			firstOnlyB = false;
		}
		this.firstOnly = firstOnlyB;
	}

	public SpeedAlert(Notifications notifications, UUID triggerID, boolean sendSMS, boolean sendProwl, UUID notifyee, int activations, int maxActivations, boolean firstOnly) {
		super(notifications, triggerID, sendSMS, sendProwl, notifyee, activations, maxActivations);
		this.firstOnly = firstOnly;
	}

	@Override
	public String getTrigger() {
		return null;
	}

	@Override
	public void performAction(PlayerSpeedingEvent event) {
		if (firstOnly) {
			if (!event.isFirst()) {
				return;
			}
		}
		send("Speed", event.getPlayer().getName() + " speed3D: " + shorten(event.getSpeed3D()) + " bps, speedXZ: " + shorten(event.getSpeedXZ()) + " bps, speedY: " + shorten(event.getSpeedY()) + " bps" + (event.isFlying() ? ", flying" : "") + (event.isGliding() ? ", gliding" : "") + ", gamemode: " + (event.getGameMode()));
	}

	private String shorten(double d) {
		d = Math.round(d * 100.0) / 100.0;
		return Double.toString(d);
	}

	@Override
	public JsonObject serialize() {
		JsonObject object = super.serialize();
		object.addProperty("firstOnly", firstOnly);
		return object;
	}

	@Override
	public String toString() {
		return "SpeedAlert: firstOnly=" + firstOnly + (getSendSMS() ? ", sms" : "") + (getSendProwl() ? ", prowl" : "") + (", activations=" + getActivations()) + (maxActivations > 0 ? ("/" + maxActivations) : "");
	}
}
