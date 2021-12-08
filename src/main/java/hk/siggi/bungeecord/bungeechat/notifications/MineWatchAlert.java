package hk.siggi.bungeecord.bungeechat.notifications;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import hk.siggi.bungeecord.bungeechat.UUIDCache;
import hk.siggi.bungeecord.bungeechat.event.MineWatchEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class MineWatchAlert extends NotificationTrigger<MineWatchEvent, String> {

	public static final List<String> allOres;

	static {
		allOres = new ArrayList<>();
		allOres.addAll(Arrays.asList(new String[]{
			"iron",
			"gold",
			"coal",
			"lapis",
			"diamond",
			"redstone",
			"emerald",
			"quartz"
		}));
	}

	private final String server;
	private String[] ores = null;

	public MineWatchAlert(Notifications notifications, JsonObject trigger) {
		super(notifications, trigger);
		JsonElement serverElement = trigger.get("server");
		JsonElement oresElement = trigger.get("ores");
		String serverStr;
		try {
			serverStr = serverElement.getAsString();
		} catch (Exception e) {
			serverStr = null;
		}
		if (serverStr == null) {
			this.server = null;
		} else {
			this.server = serverStr;
		}
		try {
			if (oresElement != null && oresElement.isJsonArray()) {
				List<String> theOres = new LinkedList<>();
				JsonArray oresArray = oresElement.getAsJsonArray();
				for (JsonElement element : oresArray) {
					if (element != null) {
						theOres.add(element.getAsString());
					}
				}
				if (!theOres.isEmpty()) {
					ores = theOres.toArray(new String[theOres.size()]);
				}
			}
		} catch (Exception e) {
		}
	}

	public MineWatchAlert(Notifications notifications, UUID triggerID, boolean sendSMS, boolean sendProwl, UUID notifyee, int activations, int maxActivations, String server) {
		super(notifications, triggerID, sendSMS, sendProwl, notifyee, activations, maxActivations);
		this.server = server;
	}

	public void setOres(String[] ores) {
		this.ores = ores;
		notifications.saveData();
	}

	@Override
	public String getTrigger() {
		return server;
	}

	@Override
	public void performAction(MineWatchEvent event) {
		check:
		if (ores != null && ores.length > 0) {
			String oreToMatch = event.getOre();
			for (String ore : ores) {
				if (ore.equalsIgnoreCase(oreToMatch)) {
					break check;
				}
			}
			return;
		}
		UUIDCache uuidCache = notifications.getPlugin().getUUIDCache();
		int count = event.getCount();
		String countStr = count >= 100000 ? "100000+" : (count + "x");
		send("MineWatch", uuidCache.getNameFromUUID(event.getPlayer().getUniqueId())
				+ "\n"
				+ countStr + " " + event.getOre()
				+ "\n"
				+ "light level: " + event.getLightLevel()
				+ "\n"
				+ "("
				+ event.getServer().getName()
				+ ":" + event.getWorld()
				+ ", " + event.getX()
				+ ", " + event.getY()
				+ ", " + event.getZ()
				+ ")");
	}

	@Override
	public boolean matchesTrigger(String server) {
		if (this.server == null) {
			return true;
		}
		if (server == null) {
			return false;
		}
		return this.server.equals(server);
	}

	@Override
	public JsonObject serialize() {
		JsonObject object = super.serialize();
		if (server != null) {
			object.addProperty("server", server);
		}
		if (ores != null && ores.length > 0) {
			JsonArray oresList = new JsonArray();
			for (String ore : ores) {
				oresList.add(new JsonPrimitive(ore));
			}
			object.add("ores", oresList);
		}
		return object;
	}

	@Override
	public String toString() {
		String serverStr = (server == null ? "<any>" : server);
		String oresStr = "";
		if (ores != null) {
			for (String ore : ores) {
				oresStr += ", " + ore;
			}
		}
		return "MineWatchAlert: " + serverStr + oresStr + (getSendSMS() ? ", sms" : "") + (getSendProwl() ? ", prowl" : "") + (", activations=" + getActivations()) + (maxActivations > 0 ? ("/" + maxActivations) : "");
	}

}
