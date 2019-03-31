package hk.siggi.bungeecord.bungeechat.event;

import lombok.Getter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class PlayerSpeedingEvent extends Event {

	@Getter
	private final ProxiedPlayer player;
	@Getter
	private final double speed3D, speedXZ, speedY;
	@Getter private final boolean flying, gliding, first;
	@Getter private final int gameMode;

	public PlayerSpeedingEvent(ProxiedPlayer player, double speed3D, double speedXZ, double speedY, boolean flying, boolean gliding, int gameMode, boolean first) {
		this.player = player;
		this.speed3D = speed3D;
		this.speedXZ = speedXZ;
		this.speedY = speedY;
		this.flying = flying;
		this.gliding = gliding;
		this.gameMode = gameMode;
		this.first = first;
	}
}
