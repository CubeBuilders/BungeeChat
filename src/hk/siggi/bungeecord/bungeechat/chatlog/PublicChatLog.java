package hk.siggi.bungeecord.bungeechat.chatlog;

public class PublicChatLog extends ChatLogLine {

	PublicChatLog(String message, ChatLogUser sender, String server, long time) {
		super(message, sender, time);
		this.server = server;
	}
	public final String server;

	@Override
	public boolean isPlayerLikelyInvolved(String player) {
		return player.equalsIgnoreCase(sender.username);
	}

	@Override
	public String toString() {
		// time:Public-server:sender:message
		return time+":Public-"+server+":"+sender.toString()+":"+message;
	}
}
