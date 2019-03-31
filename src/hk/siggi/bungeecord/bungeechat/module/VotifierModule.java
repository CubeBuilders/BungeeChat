package hk.siggi.bungeecord.bungeechat.module;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import net.md_5.bungee.api.plugin.Listener;

public interface VotifierModule extends Listener {
	public void setBungeeChat(BungeeChat plugin);
}
