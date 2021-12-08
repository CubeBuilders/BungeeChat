package hk.siggi.bungeecord.bungeechat.chat.handler;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.processChat;
import hk.siggi.bungeecord.bungeechat.chat.ChatController;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PrivateChatHandler implements ChatHandler {

	private final ChatController controller;
	private final UUID destination;
	private String destinationName;
	private List<TextComponent> usernameComponent;

	public PrivateChatHandler(ChatController controller, ProxiedPlayer destination) {
		this.controller = controller;
		this.destination = destination.getUniqueId();
		this.destinationName = destination.getName();
		generateComponent(destination);
	}
	
	@Override
	public void sendChat(ProxiedPlayer sender, String message) {
		ProxiedPlayer dest;
		try {
			dest = controller.bungeechat.getProxy().getPlayer(destination);
			String n = dest.getName();
			if (!n.equals(destinationName)) {
				destinationName = dest.getName();
				generateComponent(dest);
			}
		} catch (Exception e) {
			dest = null;
		}
		if (dest == null) {
			BaseComponent msg = new TextComponent("");
			
			BaseComponent extra = new TextComponent("The player ");
			extra.setColor(ChatColor.RED);
			msg.addExtra(extra);
			
			extra = new TextComponent(destinationName);
			extra.setColor(ChatColor.WHITE);
			msg.addExtra(extra);
			
			extra = new TextComponent(" is currently offline.");
			extra.setColor(ChatColor.RED);
			msg.addExtra(extra);
			
			sender.sendMessage(msg);
			return;
		}
		controller.sendPM(sender, dest, message);
	}
	
	private void generateComponent(ProxiedPlayer destination) {
		usernameComponent = BungeeChat.getInstance().getGroupInfo().usernameComponent(destination, true, false, false, false);
	}
	
	@Override
	public List<BaseComponent> getDisplay() {
		List<BaseComponent> result = new ArrayList<>();
		result.addAll(processChat(null, "&ePM To "));
		result.addAll(usernameComponent);
		return result;
	}
}
