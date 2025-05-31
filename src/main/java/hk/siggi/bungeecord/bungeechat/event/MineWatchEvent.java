package hk.siggi.bungeecord.bungeechat.event;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class MineWatchEvent extends Event {

	private final ProxiedPlayer player;
	private final ServerInfo server;
	private final String world;
	private final int x;
	private final int y;
	private final int z;
	private final String ore;
	private final int count;
	private final int lightLevel;

	public MineWatchEvent(ProxiedPlayer p, ServerInfo server, String world, int x, int y, int z, String ore, int count, int lightLevel) {
		this.player = p;
		this.server = server;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.ore = ore;
		this.count = count;
		this.lightLevel = lightLevel;
	}

	public ProxiedPlayer getPlayer() {
		return player;
	}

	public ServerInfo getServer() {
		return server;
	}

	public String getWorld() {
		return world;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public String getOre() {
		return ore;
	}

	public int getCount() {
		return count;
	}

	public int getLightLevel() {
		return lightLevel;
	}
}
