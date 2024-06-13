package hk.siggi.bungeecord.bungeechat;

import hk.siggi.bungeecord.bungeechat.event.MineWatchEvent;
import hk.siggi.bungeecord.bungeechat.event.PlayerSpeedingEvent;
import hk.siggi.bungeecord.bungeechat.util.DiscordBotAPI;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class EventDiscordRelay implements Listener {
	private final BungeeChat plugin;
	public EventDiscordRelay(BungeeChat plugin) {
		this.plugin = plugin;
	}

	private static final TimeZone gmt = TimeZone.getTimeZone("GMT");
	private static String timeToString(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
		sdf.setTimeZone(gmt);
		return sdf.format(new Date(time)) + "-GMT";
	}

	private static String doubleToString(double value) {
		return Double.toString(Math.round(value * 100.0) / 100.0);
	}

	@EventHandler
	public void mineWatch(MineWatchEvent event) {
		if (!event.getServer().getName().equals("factions") && !event.getServer().getName().equals("survival")) {
			return;
		}
		if (!event.getOre().equals("diamond") && !event.getOre().equals("emerald")) {
			return;
		}
		DiscordBotAPI.sendMessage("hack-watch",
				event.getPlayer().getName()
						+ " found " + event.getCount() + "x " + event.getOre()
						+ "\nServer: " + event.getServer().getName()
						+ "\nTime: " + timeToString(System.currentTimeMillis()),
				false);
	}

	@EventHandler
	public void playerSpeeding(PlayerSpeedingEvent event) {
		if (event.getGameMode() == 1 || event.getGameMode() == 3) return; // ignore speeding for creative mode and spectator mode
		DiscordBotAPI.sendMessage("hack-watch",
				event.getPlayer().getName() + " is speeding!"
						+ "\nSpeed: " + doubleToString(event.getSpeed3D()) + " bps"
						+ "\nVertical speed: " + doubleToString(event.getSpeedY()) + " bps"
						+ "\nServer: " + event.getPlayer().getServer().getInfo().getName()
						+ "\nTime: " + timeToString(System.currentTimeMillis()),
				false);
	}
}
