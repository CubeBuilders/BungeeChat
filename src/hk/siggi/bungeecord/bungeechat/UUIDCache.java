package hk.siggi.bungeecord.bungeechat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * Caches usernames for UUIDs. Not thread-safe, should only be called from the
 * main thread.
 *
 * @author Siggi
 */
public class UUIDCache {

	private final File dataFile;
	private final File dataFileAtomicSaveLocation;

	public UUIDCache(File dataFile) {
		this.dataFile = dataFile;
		this.dataFileAtomicSaveLocation = new File(dataFile.getPath() + ".sav");
	}

	private final Properties uuidToPlayer = new Properties();
	private final Properties playerToUuid = new Properties();

	/**
	 * Get a player's name from their UUID.
	 *
	 * @param uuid the player's UUID
	 * @return the player's name
	 */
	public String getNameFromUUID(UUID uuid) {
		if (uuid == null) {
			return null;
		}
		return getNameFromUUID(uuid.toString().toUpperCase());
	}

	private String getNameFromUUID(String uuid) {
		if (!didInitialLoad) {
			reloadData();
		}
		if (uuid == null) {
			return null;
		}
		return uuidToPlayer.getProperty(uuid);
	}

	/**
	 * Get a player's UUID from their name.
	 *
	 * @param player the player's name
	 * @return the player's UUID
	 */
	public UUID getUUIDFromName(String player) {
		if (!didInitialLoad) {
			reloadData();
		}
		String uuidStr = playerToUuid.getProperty(player.toLowerCase());
		if (uuidStr == null) {
			return null;
		}
		return UUID.fromString(uuidStr);
	}
	private boolean loadingUUIDs = false;

	public void storeToCache(String player, UUID uuid) {
		if (!didInitialLoad) {
			reloadData();
		}
		UUID uuidCheck = getUUIDFromName(player);
		String nameCheck = getNameFromUUID(uuid);
		if ((uuidCheck != null && uuidCheck.equals(uuid)) && (nameCheck != null && nameCheck.equals(player))) {
			return;
		}
		String oldName = uuidToPlayer.getProperty(uuid.toString().toUpperCase());
		String oldUUIDForNewName = playerToUuid.getProperty(player.toLowerCase());
		if (oldName != null) {
			playerToUuid.remove(oldName.toLowerCase());
		}
		if (oldUUIDForNewName != null) {
			uuidToPlayer.remove(oldUUIDForNewName.toUpperCase());
		}
		uuidToPlayer.setProperty(uuid.toString().toUpperCase(), player);
		playerToUuid.setProperty(player.toLowerCase(), uuid.toString());
		if (!loadingUUIDs && !preventSave) {
			saveData();
		}
	}

	private boolean didInitialLoad = false;
	private boolean preventSave = false;

	public void preventSaving() {
		preventSave = true;
	}

	public void resumeSaving() {
		preventSave = false;
		saveData();
	}

	public void reloadData() {
		didInitialLoad = true;
		uuidToPlayer.clear();
		playerToUuid.clear();
		try {
			loadingUUIDs = true;
			if (!dataFile.exists() && dataFileAtomicSaveLocation.exists()) {
				dataFileAtomicSaveLocation.renameTo(dataFile);
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile)));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("=")) {
					String key = line.substring(0, line.indexOf("="));
					String val = line.substring(line.indexOf("=") + 1);
					storeToCache(val, UUID.fromString(key));
				}
			}
		} catch (Exception e) {
		} finally {
			loadingUUIDs = false;
		}
	}

	private void saveData() {
		try {
			FileOutputStream out = new FileOutputStream(dataFileAtomicSaveLocation);
			Enumeration en = uuidToPlayer.propertyNames();
			while (en.hasMoreElements()) {
				String uuid = (String) en.nextElement();
				String player = uuidToPlayer.getProperty(uuid);
				out.write((uuid + "=" + player + "\n").getBytes());
			}
			dataFile.delete();
			dataFileAtomicSaveLocation.renameTo(dataFile);
		} catch (Exception e) {
		}
	}

	public List<UUID> getPlayersWithNamesStartingWith(String startingWith) {
		return getPlayersWithNamesStartingWith(startingWith, 0);
	}

	public List<UUID> getPlayersWithNamesStartingWith(String startingWith, int limit) {
		startingWith = startingWith.toLowerCase();
		LinkedList<UUID> uuids = new LinkedList<>();
		for (Map.Entry entry : uuidToPlayer.entrySet()) {
			UUID uuid = UUID.fromString((String) (entry.getKey()));
			String n = (String) (entry.getValue());
			if (n.toLowerCase().startsWith(startingWith)) {
				uuids.add(uuid);
				if (limit > 0 && uuids.size() >= limit) {
					break;
				}
			}
		}
		return uuids;
	}

	public Iterable<UUID> getUUIDs() {
		if (!didInitialLoad) {
			reloadData();
		}
		return () -> {
			Iterator it = uuidToPlayer.keySet().iterator();
			return new Iterator<UUID>() {

				@Override
				public boolean hasNext() {
					return it.hasNext();
				}

				@Override
				public UUID next() {
					return UUID.fromString((String) it.next());
				}
			};
		};
	}
}
