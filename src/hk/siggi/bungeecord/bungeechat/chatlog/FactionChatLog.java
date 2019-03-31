package hk.siggi.bungeecord.bungeechat.chatlog;

import java.util.List;

public class FactionChatLog extends ChatLogLine {

	FactionChatLog(String message, ChatLogUser sender, String server, List<ChatLogUser> witnesses, long time) {
		super(message, sender, time);
		this.server = server;
		this.witnesses = witnesses;
	}
	public final String server;
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
		// time:Faction-[server]:sender:witnesses:message
		StringBuilder sb = new StringBuilder();
		sb.append(Long.toString(time)).append(":Faction-");
		sb.append(server);
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
