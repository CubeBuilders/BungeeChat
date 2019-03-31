package hk.siggi.bungeecord.bungeechat.event;

import lombok.Getter;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class MineWatchEvent extends Event {

	@Getter
	private final ProxiedPlayer player;
	@Getter
	private final ServerInfo server;
	@Getter
	private final String world;
	@Getter
	private final int x;
	@Getter
	private final int y;
	@Getter
	private final int z;
	@Getter
	private final String ore;
	@Getter
	private final int count;
	@Getter
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
}
