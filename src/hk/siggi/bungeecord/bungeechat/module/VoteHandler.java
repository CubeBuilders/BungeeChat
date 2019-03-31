package hk.siggi.bungeecord.bungeechat.module;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import java.util.UUID;

public interface VoteHandler {

	public void handleVote(UUID uuid, BungeeChat plugin) throws Exception;
}
