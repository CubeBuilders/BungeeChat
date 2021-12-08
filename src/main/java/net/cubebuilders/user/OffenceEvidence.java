package net.cubebuilders.user;

import java.util.UUID;

public class OffenceEvidence {
	public enum EvidenceType {
		CHATLOG, REPLAY, SCREENSHOT;
		@Override
		public String toString() {
			switch (this) {
				case CHATLOG:
					return "Chatlog";
				case REPLAY:
					return "Replay";
				case SCREENSHOT:
					return "Screenshot";
			}
			return null;
		}
		public static EvidenceType fromString(String str) {
			if (str.equalsIgnoreCase("Chatlog")) {
				return CHATLOG;
			} else if (str.equalsIgnoreCase("Replay")) {
				return REPLAY;
			} else if (str.equalsIgnoreCase("Screenshot")) {
				return SCREENSHOT;
			}
			return null;
		}
	}
	public final EvidenceType type;
	public final long time;
	public final String url;
	public final UUID uuid;
	public final UUID addedBy;
	public OffenceEvidence(EvidenceType type, long time, String url, UUID uuid, UUID addedBy) {
		this.type = type;
		this.time = time;
		this.url = url;
		this.uuid = uuid;
		this.addedBy = addedBy;
	}
}
