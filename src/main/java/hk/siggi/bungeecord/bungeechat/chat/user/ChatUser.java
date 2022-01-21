package hk.siggi.bungeecord.bungeechat.chat.user;

import java.util.UUID;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ChatUser {

	private final ChatUsers users;
	private final UUID uuid;

	public ChatUser(ChatUsers users, UUID uuid) {
		this.users = users;
		this.uuid = uuid;
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public ProxiedPlayer getPlayer() {
		return users.plugin.getProxy().getPlayer(uuid);
	}

	public boolean isOnline() {
		return isInGame() || isOnWebChat();
	}

	public boolean isInGame() {
		ProxiedPlayer pl = getPlayer();
		return (pl != null && pl.isConnected());
	}

	public boolean isOnWebChat() {
		return users.webChat.isUserOnline(uuid);
	}

	public void sendMessage(BaseComponent component) {
		ProxiedPlayer pl = getPlayer();
		if (pl != null) {
			MessageSender.sendMessage(pl, component);
		}
		users.webChat.send(uuid, component);
	}
}
