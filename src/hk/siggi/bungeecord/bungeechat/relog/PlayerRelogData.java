package hk.siggi.bungeecord.bungeechat.relog;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.ProxyServer;

public final class PlayerRelogData {

	private final UUID uuid;
	private final List<String> recentServers = new LinkedList<>();
	private long expiry;

	PlayerRelogData(UUID uuid) {
		this.uuid = uuid;
		resetExpiryTime();
	}

	public void setServer(String server) {
		if (recentServers.isEmpty() || !recentServers.get(0).equals(server)) {
			recentServers.add(0, server);
			int s;
			while ((s = recentServers.size()) > 3) {
				recentServers.remove(s - 1);
			}
		}
		resetExpiryTime();
	}

	public List<String> getRecentServers() {
		return recentServers;
	}

	void resetExpiryTime() {
		expiry = System.currentTimeMillis() + 1200000L;
	}

	public boolean hasExpired() {
		if (ProxyServer.getInstance().getPlayer(uuid) != null) {
			return false;
		}
		return expiry <= System.currentTimeMillis();
	}
}
