package hk.siggi.bungeecord.bungeechat.chatlog;

public class StaffChatLog extends ChatLogLine {
	StaffChatLog(String message, ChatLogUser sender, long time) {
		super(message, sender, time);
	}
	@Override
	public boolean isPlayerLikelyInvolved(String player) {
		return player.equalsIgnoreCase(sender.username);
	}

	@Override
	public String toString() {
		// time:StaffChat:sender:message
		return time+":StaffChat:"+sender.toString()+":"+message;
	}
}
