package net.cubebuilders.user;

import java.util.UUID;

public class NameHistory {

	private UUID uuid = null;
	private String name = null;
	private long changedToAt = 0L;

	public NameHistory(UUID uuid, String name, long time) {
		this.uuid = uuid;
		this.name = name;
		this.changedToAt = time;
	}

	void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	public UUID getUUID() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public long getTime() {
		return changedToAt;
	}

	public static NameHistory fromJson(String json) {
		return CBUser.nameHistoryFromJson(json);
	}

	public static NameHistory[] fromJsonArray(String json) {
		return CBUser.nameHistoryArrayFromJson(json);
	}

	public static String toJsonArray(NameHistory[] history, boolean prettyPrinting) {
		return CBUser.nameHistoryToJson(history, prettyPrinting);
	}

	public String toJson(boolean prettyPrinting) {
		return CBUser.nameHistoryToJson(this, prettyPrinting);
	}
}
