package hk.siggi.bungeecord.bungeechat;

import hk.siggi.bungeecord.bungeechat.event.MineWatchEvent;
import hk.siggi.bungeecord.bungeechat.event.PlayerSpeedingEvent;
import hk.siggi.bungeecord.bungeechat.util.DiscordBotAPI;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class EventDiscordRelay implements Listener {
	private final BungeeChat plugin;
	public EventDiscordRelay(BungeeChat plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void mineWatch(MineWatchEvent event) {
		if (!event.getServer().getName().equals("factions") && !event.getServer().getName().equals("survival")) {
			return;
		}
		if (!event.getOre().equals("diamond") && !event.getOre().equals("emerald")) {
			return;
		}
		DiscordBotAPI.sendMessage("hack-watch", event.getPlayer().getName() + " found " + event.getCount() + "x " + event.getOre() + "\nServer: " + event.getServer().getName(), false);
	}

	@EventHandler
	public void playerSpeeding(PlayerSpeedingEvent event) {
		if (event.getGameMode() == 1 || event.getGameMode() == 3) return; // ignore speeding for creative mode and spectator mode
		DiscordBotAPI.sendMessage("hack-watch", event.getPlayer().getName() + " is speeding! Speed: " + event.getSpeed3D() + " bps, Vertical speed: " + event.getSpeedY(), false);
	}
}
