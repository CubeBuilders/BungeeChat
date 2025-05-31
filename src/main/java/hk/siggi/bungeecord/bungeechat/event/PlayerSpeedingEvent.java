package hk.siggi.bungeecord.bungeechat.event;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class PlayerSpeedingEvent extends Event {

	private final ProxiedPlayer player;
	private final double speed3D, speedXZ, speedY;
	private final boolean flying, gliding, first;
	private final int gameMode;

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

	public ProxiedPlayer getPlayer() {
		return player;
	}

	public double getSpeed3D() {
		return speed3D;
	}

	public double getSpeedXZ() {
		return speedXZ;
	}

	public double getSpeedY() {
		return speedY;
	}

	public boolean isFlying() {
		return flying;
	}

	public boolean isGliding() {
		return gliding;
	}

	public boolean isFirst() {
		return first;
	}

	public int getGameMode() {
		return gameMode;
	}
}
