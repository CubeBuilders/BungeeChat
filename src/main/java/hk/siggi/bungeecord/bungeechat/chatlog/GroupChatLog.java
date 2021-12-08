package hk.siggi.bungeecord.bungeechat.chatlog;

import java.util.List;
import java.util.UUID;

public class GroupChatLog extends ChatLogLine {

	GroupChatLog(String message, String groupName, UUID groupUUID, ChatLogUser sender, List<ChatLogUser> witnesses, long time) {
		super(message, sender, time);
		this.groupName = groupName;
		this.groupUUID = groupUUID;
		this.witnesses = witnesses;
	}
	public final String groupName;
	public final UUID groupUUID;
	private final List<ChatLogUser> witnesses;

	public ChatLogUser[] getWitnesses() {
		return witnesses.toArray(new ChatLogUser[witnesses.size()]);
	}

	@Override
	public boolean isPlayerLikelyInvolved(String player) {
		return player.equalsIgnoreCase(sender.username) || containsWitness(player);
	}

	private boolean containsWitness(String player) {
		for (ChatLogUser a : witnesses) {
			if (a.username != null && a.username.equals(player)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		// time:Group-groupName/groupID:sender:witnesses:message
		StringBuilder sb = new StringBuilder();
		sb.append(Long.toString(time)).append(":Group-");
		sb.append(groupName).append("/").append(groupUUID.toString().replace("-", "").toLowerCase());
		sb.append(":");
		sb.append(sender.toString());
		for (int i = 0; i < witnesses.size(); i++) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append(witnesses.get(i).toString());
		}
		sb.append(":");
		sb.append(message);
		return sb.toString();
	}
}
