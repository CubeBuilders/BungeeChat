package hk.siggi.bungeecord.bungeechat.notifications;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hk.siggi.bungeecord.bungeechat.BungeeChat;
import static hk.siggi.bungeecord.bungeechat.util.Util.notNull;
import static hk.siggi.bungeecord.bungeechat.util.Util.uuidFromString;
import java.util.UUID;

public abstract class NotificationTrigger<E, T> {

	protected final Notifications notifications;
	protected final UUID triggerID;
	private boolean sendSMS;
	private boolean sendProwl;
	protected final UUID notifyee;
	private int activations;
	protected final int maxActivations;

	NotificationTrigger(Notifications notifications, JsonObject trigger) {
		this.notifications = notifications;
		String triggerIDStr = trigger.get("triggerID").getAsString();
		triggerID = uuidFromString(triggerIDStr);

		JsonElement sendSMSElement = trigger.get("sendSMS");
		if (sendSMSElement != null && sendSMSElement.isJsonPrimitive()) {
			sendSMS = sendSMSElement.getAsBoolean();
		}

		JsonElement sendProwlElement = trigger.get("sendProwl");
		if (sendProwlElement != null && sendProwlElement.isJsonPrimitive()) {
			sendProwl = sendProwlElement.getAsBoolean();
		}

		String notifyeeStr = trigger.get("notifyee").getAsString();
		notifyee = uuidFromString(notifyeeStr);

		JsonElement activationsElement = trigger.get("activations");
		if (activationsElement != null && activationsElement.isJsonPrimitive()) {
			activations = activationsElement.getAsInt();
		} else {
			activations = 0;
		}

		JsonElement maxActivationsElement = trigger.get("maxActivations");
		if (maxActivationsElement != null && maxActivationsElement.isJsonPrimitive()) {
			maxActivations = maxActivationsElement.getAsInt();
		} else {
			maxActivations = 0;
		}
	}

	NotificationTrigger(Notifications notifications, UUID triggerID, boolean sendSMS, boolean sendProwl, UUID notifyee, int activations, int maxActivations) {
		if (!notNull(notifications, triggerID, notifyee)) {
			throw new NullPointerException();
		}
		this.notifications = notifications;
		this.triggerID = triggerID;
		this.sendSMS = sendSMS;
		this.sendProwl = sendProwl;
		this.notifyee = notifyee;
		this.activations = activations;
		this.maxActivations = maxActivations;
	}

	public final UUID getTriggerID() {
		return triggerID;
	}

	public final boolean getSendSMS() {
		return sendSMS;
	}

	public final void setSendSMS(boolean sendSMS) {
		this.sendSMS = sendSMS;
		notifications.saveData();
	}

	public final boolean getSendProwl() {
		return sendProwl;
	}

	public final void setSendProwl(boolean sendProwl) {
		this.sendProwl = sendProwl;
		notifications.saveData();
	}

	public final UUID getNotifyee() {
		return notifyee;
	}

	public final int getActivations() {
		return activations;
	}

	public final int getMaxActivations() {
		return maxActivations;
	}

	public final void incrementActivations() {
		activations += 1;
		notifications.saveData();
	}

	public final boolean hasExpired() {
		return (maxActivations > 0 && activations >= maxActivations);
	}

	public abstract T getTrigger();

	public abstract void performAction(E event);

	public JsonObject serialize() {
		JsonObject object = new JsonObject();
		object.addProperty("triggerID", triggerID.toString());
		if (sendSMS) {
			object.addProperty("sendSMS", true);
		}
		if (sendProwl) {
			object.addProperty("sendProwl", true);
		}
		object.addProperty("notifyee", notifyee.toString());
		object.addProperty("activations", activations);
		if (maxActivations != 0) {
			object.addProperty("maxActivations", maxActivations);
		}
		return object;
	}

	protected final void send(String header, String message) {
		BungeeChat bc = BungeeChat.getInstance();
		if (sendSMS) {
			bc.text(notifyee, header + ": " + message);
		}
		if (sendProwl) {
			bc.prowl(notifyee, header, message);
		}
	}

	public boolean matchesTrigger(T trigger) {
		T myTrigger = getTrigger();
		if (trigger == myTrigger) {
			return true;
		}
		if (trigger == null || myTrigger == null) {
			return false;
		}
		return trigger.equals(myTrigger);
	}
}
