package hk.siggi.bungeecord.bungeechat.event;

import java.util.UUID;
import net.cubebuilders.user.Punishment;
import net.md_5.bungee.api.plugin.Event;

public class PunishmentIssuedEvent extends Event {

	private final Punishment punishment;

	public PunishmentIssuedEvent(Punishment punishment) {
		this.punishment = punishment;
	}

	public Punishment getPunishment() {
		return punishment;
	}

	public UUID getIssuer() {
		return punishment.getIssuedBy();
	}

	public UUID getPlayer() {
		return punishment.getIssuedTo();
	}
}
