package hk.siggi.bungeecord.bungeechat.notifications;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import static hk.siggi.bungeecord.bungeechat.BungeeChat.getSpecialUser;
import hk.siggi.bungeecord.bungeechat.UUIDCache;
import hk.siggi.bungeecord.bungeechat.event.PunishmentIssuedEvent;
import hk.siggi.bungeecord.bungeechat.util.TimeUtil;
import static hk.siggi.bungeecord.bungeechat.util.Util.uuidFromString;
import java.util.UUID;
import net.cubebuilders.user.Punishment;
import net.cubebuilders.user.Punishment.PunishmentAction;

public class PunishmentAlert extends NotificationTrigger<PunishmentIssuedEvent, UUID> {

	private final UUID issuer;

	public PunishmentAlert(Notifications notifications, JsonObject trigger) {
		super(notifications, trigger);
		JsonElement issuerElement = trigger.get("issuer");
		String issuerStr;
		try {
			issuerStr = issuerElement.getAsString();
		} catch (Exception e) {
			issuerStr = null;
		}
		if (issuerStr == null) {
			this.issuer = null;
		} else {
			this.issuer = uuidFromString(issuerStr);
		}
	}

	public PunishmentAlert(Notifications notifications, UUID triggerID, boolean sendSMS, boolean sendProwl, UUID notifyee, int activations, int maxActivations, UUID issuer) {
		super(notifications, triggerID, sendSMS, sendProwl, notifyee, activations, maxActivations);
		this.issuer = issuer;
	}

	@Override
	public UUID getTrigger() {
		return issuer;
	}

	@Override
	public void performAction(PunishmentIssuedEvent event) {
		Punishment punishment = event.getPunishment();
		
		UUIDCache uuidCache = notifications.getPlugin().getUUIDCache();
		
		String issuerName = getSpecialUser(punishment.getIssuedBy());
		if (issuerName == null) {
			issuerName = uuidCache.getNameFromUUID(punishment.getIssuedBy());
		}
		String receiver = uuidCache.getNameFromUUID(punishment.getIssuedTo());
		PunishmentAction punishmentAction = punishment.getAction();
		String punishmentType = punishmentAction.toString().toLowerCase();
		boolean permanent = punishment.getLength() == -1;
		String banLengthAsString = permanent ? null : TimeUtil.timeToString(punishment.getLength());
		String reason = punishment.getReason();
		
		String txt = issuerName + " is issuing a " + (permanent ? "permanent " : "") + punishmentType + " to " + receiver + ((permanent || punishmentAction == PunishmentAction.WARNING) ? "" : (" for " + banLengthAsString)) + ".";
		txt += "\n";
		txt += "Reason: " + reason;
		
		send("Punishment", txt);
	}

	@Override
	public boolean matchesTrigger(UUID issuer) {
		if (this.issuer == null) {
			return true;
		}
		if (issuer == null) {
			return false;
		}
		return this.issuer.equals(issuer);
	}

	@Override
	public JsonObject serialize() {
		JsonObject object = super.serialize();
		if (issuer != null) {
			object.addProperty("issuer", issuer.toString());
		}
		return object;
	}

	@Override
	public String toString() {
		UUIDCache uuidCache = notifications.getPlugin().getUUIDCache();
		String user = (issuer == null ? "<anyone>" : uuidCache.getNameFromUUID(issuer));
		return "PunishmentAlert: " + user + (getSendSMS() ? ", sms" : "") + (getSendProwl() ? ", prowl" : "") + (", activations="+getActivations()) + (maxActivations>0?("/"+maxActivations):"");
	}
}
