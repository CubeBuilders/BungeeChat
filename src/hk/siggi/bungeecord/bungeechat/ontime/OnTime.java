package hk.siggi.bungeecord.bungeechat.ontime;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.PlayerSession;
import java.io.File;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public final class OnTime implements Listener {

	final BungeeChat plugin;

	private final ConcurrentHashMap<UUID, OnTimePlayer> playerMap = new ConcurrentHashMap<>();

	private static OnTime instance = null;

	/**
	 * Get the OnTime instance.
	 *
	 * @return The main OnTime instance
	 */
	public static OnTime getInstance() {
		return instance;
	}
	private File sessionRecordDataFolder;

	@SuppressWarnings("LeakingThisInConstructor")
	public OnTime(BungeeChat plugin) {
		this.plugin = plugin;
		instance = this;
	}

	File getSessionRecordDataFolder() {
		if (sessionRecordDataFolder != null) {
			return sessionRecordDataFolder;
		}
		sessionRecordDataFolder = new File(plugin.getDataFolder(), "sessionrecord");
		if (!sessionRecordDataFolder.exists()) {
			sessionRecordDataFolder.mkdirs();
		}
		return sessionRecordDataFolder;
	}

	/**
	 * Called by BungeeCord's Event system.
	 *
	 * @param event
	 */
	@EventHandler
	public void join(ServerConnectedEvent event) {
		ProxiedPlayer player = event.getPlayer();
		String targetServer = event.getServer().getInfo().getName();
		OnTimePlayer p;
		UUID uuid = player.getUniqueId();
		PlayerSession session = plugin.getSession(player);
		if (session.invalidatedOnTime) {
			return;
		}
		synchronized (playerMap) {
			p = playerMap.get(uuid);
			if (p == null) {
				playerMap.put(uuid, p = new OnTimePlayer(uuid));
			}
		}
		p.recordLogin(targetServer);
	}

	/**
	 * Called by BungeeCord's Event system.
	 *
	 * @param event
	 */
	@EventHandler
	public void leave(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		OnTimePlayer p;
		UUID uuid = player.getUniqueId();
		synchronized (playerMap) {
			p = playerMap.get(uuid);
			if (p != null) {
				p.recordLogout();
				playerMap.remove(uuid);
			}
		}
	}

	public void invalidateSession(UUID uuid) {
		OnTimePlayer p;
		synchronized (playerMap) {
			p = playerMap.get(uuid);
			if (p != null) {
				playerMap.remove(uuid);
			}
		}
		ProxiedPlayer pp = plugin.getProxiedPlayer(uuid);
		if (pp != null) {
			plugin.getSession(pp).invalidatedOnTime = true;
		}
	}

	/**
	 * Called when shutting down the server.
	 */
	public void serverShuttingDown() {
		ArrayList<UUID> uuids = new ArrayList<>();
		synchronized (playerMap) {
			uuids.addAll(playerMap.keySet());
			for (UUID uuid : uuids) {
				OnTimePlayer p = playerMap.get(uuid);
				if (p != null) {
					p.recordLogout();
					playerMap.remove(uuid);
				}
			}
		}
	}

	/**
	 * Get a player by UUID
	 *
	 * @param uuid Player's UUID
	 * @return OnTimePlayer for the passed UUID
	 */
	public OnTimePlayer getPlayer(UUID uuid) {
		OnTimePlayer p;
		synchronized (playerMap) {
			p = playerMap.get(uuid);
		}
		if (p == null) {
			p = new OnTimePlayer(uuid);
		}
		return p;
	}

	/**
	 * Trim record list so that we only have records that overlap the specified
	 * time frame.
	 *
	 * @param records Record list to trim
	 * @param start Earliest record to include
	 * @param end Latest record to include
	 * @return Trimmed record list
	 */
	public static OnTimeSessionRecord[] trim(OnTimeSessionRecord[] records, long start, long end) {
		ArrayList<OnTimeSessionRecord> list = new ArrayList<OnTimeSessionRecord>();
		for (OnTimeSessionRecord record : records) {
			if ((record.logout >= start || record.logout == -1L) && record.login < end) {
				list.add(record);
			}
		}
		return list.toArray(new OnTimeSessionRecord[list.size()]);
	}

	/**
	 * Count how many logins there are in the record list.
	 *
	 * @param records Record list to look through
	 * @return Number of times the user logged in.
	 */
	public static int getLoginCount(OnTimeSessionRecord[] records) {
		if (records.length == 0) {
			return 0;
		}
		if (records.length == 1) {
			return 1;
		}
		int loginCount = 1;
		for (int i = 1; i < records.length; i++) {
			if (records[i - 1].logout != records[i].login) {
				loginCount += 1;
			}
		}
		return loginCount;
	}

	/**
	 * Get total time a player has logged in by looking at the records.
	 *
	 * @param records Records to look at.
	 * @return Total time player was online in milliseconds.
	 */
	public static long getTotalTimeLoggedIn(OnTimeSessionRecord[] records) {
		return getTotalTimeLoggedIn(records, null);
	}

	/**
	 * Get total time a player has logged in by looking at the records.
	 *
	 * @param records Records to look at.
	 * @param server Only look for records matching the passed server.
	 * @return Total time player was online in milliseconds.
	 */
	public static long getTotalTimeLoggedIn(OnTimeSessionRecord[] records, String server) {
		long time = 0L;
		for (OnTimeSessionRecord record : records) {
			if (server == null || record.server.equals(server)) {
				time += record.getTimeLoggedIn();
			}
		}
		return time;
	}

	/**
	 * Get total time a player has logged in by looking at the records.
	 *
	 * @param records Records to look at.
	 * @param start Ignore session before this time.
	 * @param end Ignore session after this time.
	 * @return Total time player was online in milliseconds.
	 */
	public static long getTotalTimeLoggedIn(OnTimeSessionRecord[] records, long start, long end) {
		return getTotalTimeLoggedIn(records, start, end, null);
	}

	/**
	 * Get total time a player has logged in by looking at the records.
	 *
	 * @param records Records to look at.
	 * @param start Ignore session before this time.
	 * @param end Ignore session after this time.
	 * @param server Only look for records matching the passed server.
	 * @return Total time player was online in milliseconds.
	 */
	public static long getTotalTimeLoggedIn(OnTimeSessionRecord[] records, long start, long end, String server) {
		long time = 0L;
		long now = System.currentTimeMillis();
		for (OnTimeSessionRecord record : records) {
			if (server == null || record.server.equals(server)) {
				if ((record.logout == -1L || record.logout > start) && record.login < end) {
					long login = record.login;
					long logout = record.logout;
					if (logout == -1L) {
						logout = now;
					}
					if (login < start) {
						login = start;
					}
					if (logout > end) {
						logout = end;
					}
					time += logout - login;
				}
			}
		}
		return time;
	}
}
