package hk.siggi.bungeecord.bungeechat.relog;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.AbstractReconnectHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class RelogHandler extends AbstractReconnectHandler implements Listener {

	private final BungeeChat plugin;
	private final Map<UUID, PlayerRelogData> table = new HashMap<>();

	public RelogHandler(BungeeChat plugin) {
		this.plugin = plugin;
	}

	private PlayerRelogData get(ProxiedPlayer p) {
		return get(p.getUniqueId());
	}

	private PlayerRelogData get(UUID uuid) {
		PlayerRelogData prd = table.get(uuid);
		if (prd == null) {
			table.put(uuid, prd = new PlayerRelogData(uuid));
		}
		return prd;
	}

	private void cleanExpired() {
		for (Iterator<PlayerRelogData> it = table.values().iterator(); it.hasNext();) {
			PlayerRelogData prd = it.next();
			if (prd.hasExpired()) {
				it.remove();
			}
		}
	}

	@EventHandler
	public void serverChanged(ServerConnectedEvent event) {
		synchronized (table) {
			get(event.getPlayer()).setServer(event.getServer().getInfo().getName());
		}
	}

	@EventHandler
	public void playerQuit(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		synchronized (table) {
			get(player).resetExpiryTime();
		}
	}

	@Override
	protected ServerInfo getStoredServer(ProxiedPlayer pp) {
		synchronized (table) {
			cleanExpired();
			PlayerRelogData prd = get(pp);
			for (String s : prd.getRecentServers()) {
				ServerInfo si = ProxyServer.getInstance().getServerInfo(s);
				if (si != null) {
					return si;
				}
			}
		}
		return null;
	}

	@Override
	public void setServer(ProxiedPlayer paramProxiedPlayer) {
	}

	@Override
	public void save() {
	}

	@Override
	public void close() {
	}
}
