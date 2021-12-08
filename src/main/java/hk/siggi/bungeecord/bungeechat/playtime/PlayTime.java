package hk.siggi.bungeecord.bungeechat.playtime;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.md_5.bungee.api.plugin.Listener;

public class PlayTime {

	final BungeeChat plugin;
	private final File dataFolder;
	private final PlayTimeListener listener;

	public PlayTime(BungeeChat plugin) {
		this.plugin = plugin;
		this.dataFolder = new File(plugin.getDataFolder(), "playtime");
		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}
		this.listener = new PlayTimeListener(this);
	}

	public Listener getListener() {
		return listener;
	}

	private final Map<UUID, PlayTimePlayer> onlinePlayerMap = new HashMap<>();
	private final ReentrantReadWriteLock onlinePlayerMapLock = new ReentrantReadWriteLock();
	private final Lock onlinePlayerMapReadLock = onlinePlayerMapLock.readLock();
	private final Lock onlinePlayerMapWriteLock = onlinePlayerMapLock.writeLock();

	private final Map<UUID, WeakReference<PlayTimePlayer>> playerMap = new HashMap<>();
	private final ReentrantReadWriteLock playerMapLock = new ReentrantReadWriteLock();
	private final Lock playerMapReadLock = playerMapLock.readLock();
	private final Lock playerMapWriteLock = playerMapLock.writeLock();

	File getSessionRecordDataFolder() {
		return dataFolder;
	}

	void loggedIn(UUID uuid, String server, String playerIP) {
		PlayTimePlayer p;
		onlinePlayerMapReadLock.lock();
		try {
			p = onlinePlayerMap.get(uuid);
		} finally {
			onlinePlayerMapReadLock.unlock();
		}
		if (p != null) {
			p.loggedIn(server, playerIP);
			return;
		}
		onlinePlayerMapWriteLock.lock();
		try {
			p = getPlayer(uuid);
			p.loggedIn(server, playerIP);
			onlinePlayerMap.put(uuid, p);
		} finally {
			onlinePlayerMapWriteLock.unlock();
		}
	}

	void loggedOut(UUID uuid) {
		PlayTimePlayer p;
		onlinePlayerMapWriteLock.lock();
		try {
			p = onlinePlayerMap.remove(uuid);
		} finally {
			onlinePlayerMapWriteLock.unlock();
		}
		if (p != null) {
			p.loggedOut();
		}
	}

	public PlayTimePlayer getPlayer(UUID uuid) {
		playerMapReadLock.lock();
		try {
			WeakReference<PlayTimePlayer> ref = playerMap.get(uuid);
			if (ref != null) {
				PlayTimePlayer result = ref.get();
				if (result != null) {
					return result;
				}
			}
		} finally {
			playerMapReadLock.unlock();
		}
		playerMapWriteLock.lock();
		try {
			for (Iterator<Map.Entry<UUID, WeakReference<PlayTimePlayer>>> it
					= playerMap.entrySet().iterator();
					it.hasNext();) {
				Map.Entry<UUID, WeakReference<PlayTimePlayer>> entry = it.next();
				WeakReference<PlayTimePlayer> value = entry.getValue();
				if (value == null || value.get() == null) {
					it.remove();
				}
			}
			PlayTimePlayer result;
			WeakReference<PlayTimePlayer> ref = playerMap.get(uuid);
			if (ref == null || (result = ref.get()) == null) {
				result = new PlayTimePlayer(this, uuid);
				playerMap.put(uuid, new WeakReference<>(result));
			}
			return result;
		} finally {
			playerMapWriteLock.unlock();
		}
	}
}
