package hk.siggi.bungeecord.bungeechat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class AprilFools2019 implements Listener {

	private final BungeeChat plugin;

	public AprilFools2019(BungeeChat plugin) {
		this.plugin = plugin;
	}

	private final Set<UUID> prankedUsers = new HashSet<>();
	private final Set<UUID> saidAprilFools = new HashSet<>();

	@EventHandler
	public void login(LoginEvent event) {
		if (System.currentTimeMillis() >= 1554181200000L) {
			// after 1am April 2nd Eastern Time stop playing the prank
			return;
		}
		UUID uuid = event.getConnection().getUniqueId();
		if (prankedUsers.contains(uuid)) {
			if (saidAprilFools.contains(uuid)) {
				return;
			}
			saidAprilFools.add(uuid);
			plugin.getScheduler().schedule(plugin, () -> {
				plugin.unify(plugin.processChat(null, "&dAPRIL FOOLS! HAHA Betcha you freaked out for a second thinking you were really perm banned!"));
			}, 5000, TimeUnit.MILLISECONDS);
			return;
		}
		prankedUsers.add(uuid);
		event.setCancelled(true);
		TextComponent kickMessage = plugin.unify(plugin.processChat(null, "Due to the recent passing of Articles 11 & 13 in the European Union, we have &4permanently banned&r all IP's from all European Union countries, including your IP address, from joining CubeBuilders. We apologize for any inconvenience this may cause. Thanks for choosing CubeBuilders! <3"));
		event.setCancelReason(kickMessage);
		event.getConnection().disconnect(kickMessage);
	}
}
