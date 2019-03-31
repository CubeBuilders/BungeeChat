package hk.siggi.bungeecord.bungeechat.notifications;

import com.google.gson.JsonObject;
import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.UUIDCache;
import static hk.siggi.bungeecord.bungeechat.util.Util.notNull;
import static hk.siggi.bungeecord.bungeechat.util.Util.uuidFromString;
import java.util.UUID;
import net.md_5.bungee.api.event.PostLoginEvent;

public final class LoginAlert extends NotificationTrigger<PostLoginEvent,UUID> {

	private final UUID trigger;

	LoginAlert(Notifications notifications, JsonObject trigger) {
		super(notifications, trigger);
		String triggerStr = trigger.get("trigger").getAsString();
		this.trigger = uuidFromString(triggerStr);
	}

	public LoginAlert(Notifications notifications, UUID triggerID, boolean sendSMS, boolean sendProwl, UUID notifyee, int activations, int maxActivations, UUID trigger) {
		super(notifications, triggerID, sendSMS, sendProwl, notifyee, activations, maxActivations);
		if (!notNull(trigger)) {
			throw new NullPointerException();
		}
		this.trigger = trigger;
	}

	@Override
	public UUID getTrigger() {
		return trigger;
	}

	@Override
	public void performAction(PostLoginEvent event) {
		BungeeChat bc = BungeeChat.getInstance();
		UUIDCache uuidCache = bc.getUUIDCache();
		String name = uuidCache.getNameFromUUID(trigger);
		send("Login Alert", name + " is now online");
	}

	@Override
	public JsonObject serialize() {
		JsonObject object = super.serialize();
		object.addProperty("trigger", trigger.toString());
		return object;
	}

	@Override
	public String toString() {
		UUIDCache uuidCache = notifications.getPlugin().getUUIDCache();
		return "LoginAlert: "+ uuidCache.getNameFromUUID(trigger) + (getSendSMS() ? ", sms":"") + (getSendProwl()?", prowl":"") + (", activations="+getActivations()) + (maxActivations>0?("/"+maxActivations):"");
	}
}
