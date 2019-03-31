package hk.siggi.bungeecord.bungeechat.chat.handler;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.chat.ChatController;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PublicChatHandler implements ChatHandler {
	private final ChatController controller;
	public PublicChatHandler(ChatController controller) {
		this.controller = controller;
	}

	@Override
	public void sendChat(ProxiedPlayer sender, String message) {
		controller.publicChat(sender, message);
	}
	
	@Override
	public List<BaseComponent> getDisplay() {
		return BungeeChat.getInstance().processChat(null, "&ePublic");
	}
}
