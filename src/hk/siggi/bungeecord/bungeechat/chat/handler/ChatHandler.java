package hk.siggi.bungeecord.bungeechat.chat.handler;

import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public interface ChatHandler {
	public void sendChat(ProxiedPlayer sender, String message);
	public List<BaseComponent> getDisplay();
}
