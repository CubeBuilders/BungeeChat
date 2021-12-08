package hk.siggi.bungeecord.bungeechat.notifications;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.event.MineWatchEvent;
import hk.siggi.bungeecord.bungeechat.event.PlayerSpeedingEvent;
import hk.siggi.bungeecord.bungeechat.event.PunishmentIssuedEvent;
import hk.siggi.bungeecord.bungeechat.util.Util;
import static hk.siggi.bungeecord.bungeechat.util.Util.tryClose;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public final class Notifications implements Listener {
	
	private static final Gson gson;
	private static final Map<String, Class<? extends NotificationTrigger>> triggerTypes = new HashMap<>();
	private static final Map<Class<? extends NotificationTrigger>, String> triggerClasses = new HashMap<>();
	
	BungeeChat getPlugin() {
		return bc;
	}
	
	private static <T extends NotificationTrigger> void registerType(Class<T> clazz) {
		String name = clazz.getName();
		name = name.substring(name.lastIndexOf(".") + 1);
		triggerTypes.put(name, clazz);
		triggerClasses.put(clazz, name);
	}
	
	public void copyTypesTo(Map<String, Class<? extends NotificationTrigger>> map) {
		for (String tt : triggerTypes.keySet()) {
			map.put(tt, triggerTypes.get(tt));
		}
	}
	
	static {
		gson = new GsonBuilder().setPrettyPrinting().create();
		registerType(LoginAlert.class);
		registerType(PunishmentAlert.class);
		registerType(MineWatchAlert.class);
		registerType(SpeedAlert.class);
	}
	
	private final BungeeChat bc;
	private final File file;
	private final File fileAtomicSave;
	
	public Notifications(BungeeChat bc, File f) {
		this.bc = bc;
		this.file = f;
		this.fileAtomicSave = new File(f.getPath() + ".sav");
		reloadData();
	}
	private final List<NotificationTrigger> triggers = new LinkedList<>();
	
	public void reloadData() {
		if (!file.exists()) {
			if (fileAtomicSave.exists()) {
				fileAtomicSave.renameTo(file);
			} else {
				return;
			}
		}
		triggers.clear();
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int c;
			byte[] b = new byte[4096];
			while ((c = in.read(b, 0, b.length)) != -1) {
				baos.write(b, 0, c);
			}
			String str = baos.toString();
			JsonParser parser = new JsonParser();
			JsonElement parse = parser.parse(str);
			if (parse.isJsonArray()) {
				JsonArray array = parse.getAsJsonArray();
				for (JsonElement element : array) {
					if (element.isJsonObject()) {
						try {
							JsonObject obj = element.getAsJsonObject();
							JsonElement typeElement = obj.get("type");
							if (typeElement.isJsonNull()) {
								continue;
							}
							String type = typeElement.getAsString();
							Class<? extends NotificationTrigger> classType = triggerTypes.get(type);
							if (classType == null) {
								continue;
							}
							Constructor<? extends NotificationTrigger> constructor = classType.getDeclaredConstructor(Notifications.class, JsonObject.class);
							NotificationTrigger t = constructor.newInstance(this, obj);
							triggers.add(t);
						} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (IOException | JsonSyntaxException e) {
		} finally {
			tryClose(in);
		}
	}
	
	public void saveData() {
		FileOutputStream out = null;
		boolean savedSuccess = false;
		try {
			out = new FileOutputStream(fileAtomicSave);
			JsonArray array = new JsonArray();
			for (NotificationTrigger t : triggers) {
				JsonObject obj = t.serialize();
				obj.addProperty("type", triggerClasses.get(t.getClass()));
				array.add(obj);
			}
			String json = gson.toJson(array);
			out.write(json.getBytes());
			out.flush();
			savedSuccess = true;
		} catch (Exception e) {
		} finally {
			Util.tryClose(out);
			if (savedSuccess) {
				file.delete();
				fileAtomicSave.renameTo(file);
			}
		}
	}
	
	public void addTrigger(NotificationTrigger trigger) {
		if (triggers.contains(trigger)) {
			return;
		}
		triggers.add(trigger);
		saveData();
	}
	
	public void deleteTrigger(NotificationTrigger trigger) {
		triggers.remove(trigger);
		saveData();
	}
	
	@SuppressWarnings("SuspiciousToArrayCall")
	public <P extends NotificationTrigger> P[] getTriggers(Class<P> type, Object triggerObject) {
		List<NotificationTrigger> triggerList = new LinkedList<>();
		for (NotificationTrigger trigger : triggers) {
			if (type.isAssignableFrom(trigger.getClass())) {
				if (trigger.matchesTrigger(triggerObject)) {
					triggerList.add(trigger);
				}
			}
		}
		P[] arr = (P[]) Array.newInstance(type, triggerList.size());
		triggerList.toArray(arr);
		return arr;
	}
	
	public NotificationTrigger[] getByNotifyee(UUID notifyee) {
		if (notifyee == null) {
			throw new NullPointerException();
		}
		List<NotificationTrigger> triggerList = new LinkedList<>();
		for (NotificationTrigger trigger : triggers) {
			if (trigger.getNotifyee().equals(notifyee)) {
				triggerList.add(trigger);
			}
		}
		return triggerList.toArray(new NotificationTrigger[triggerList.size()]);
	}
	
	private <T extends NotificationTrigger> void performAction(Class<T> clazz, Object event, Object triggerObject) {
		T[] t = getTriggers(clazz, triggerObject);
		for (T tt : t) {
			if (tt.hasExpired()) {
				deleteTrigger(tt);
				continue;
			}
			tt.performAction(event);
			tt.incrementActivations();
			if (tt.hasExpired()) {
				deleteTrigger(tt);
			}
		}
	}
	
	@EventHandler
	public void playerLoggedIn(PostLoginEvent event) {
		performAction(LoginAlert.class, event, event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void punishmentIssued(PunishmentIssuedEvent event) {
		performAction(PunishmentAlert.class, event, event.getPunishment().getIssuedBy());
	}
	
	@EventHandler
	public void mineWatch(MineWatchEvent event) {
		performAction(MineWatchAlert.class, event, event.getServer().getName());
	}
	@EventHandler
	public void playerSpeeding(PlayerSpeedingEvent event) {
		performAction(SpeedAlert.class, event, null);
	}
}
