package hk.siggi.bungeecord.bungeechat.playtime;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayTimeListener implements Listener {

	private final PlayTime playTime;

	PlayTimeListener(PlayTime playTime) {
		this.playTime = playTime;
	}

	@EventHandler
	public void join(ServerConnectedEvent event) {
		playTime.loggedIn(
				event.getPlayer().getUniqueId(),
				event.getServer().getInfo().getName(),
				event.getPlayer().getAddress().getAddress().getHostAddress()
		);
	}

	@EventHandler
	public void leave(PlayerDisconnectEvent event) {
		playTime.loggedOut(
				event.getPlayer().getUniqueId()
		);
	}
}
