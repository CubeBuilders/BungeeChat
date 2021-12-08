package hk.siggi.bungeecord.bungeechat.chatlog;

public class PrivateChatLog extends ChatLogLine {
	PrivateChatLog(String message, ChatLogUser sender, ChatLogUser recipient, long time) {
		super(message, sender, time);
		this.recipient = recipient;
	}
	
	public final ChatLogUser recipient;
	
	@Override
	public boolean isPlayerLikelyInvolved(String player) {
		return player.equalsIgnoreCase(sender.username) || player.equalsIgnoreCase(recipient.username);
	}

	@Override
	public String toString() {
		// time:Private:sender:recipient:message
		return time+":Private:"+sender.toString()+":"+recipient.toString()+":"+message;
	}
}
