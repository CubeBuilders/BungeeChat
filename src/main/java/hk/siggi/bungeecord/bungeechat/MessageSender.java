package hk.siggi.bungeecord.bungeechat;

import hk.siggi.bungeecord.bungeechat.util.ChatUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class MessageSender {
	public static void sendMessage(CommandSender player, String message) {
		sendMessage(player, null, message);
	}
	public static void sendMessage(CommandSender player, UUID sender, String message) {
		sendMessage(player, sender, ChatUtil.unify(ChatUtil.processChat(null, message)));
	}
	public static void sendMessage(CommandSender player, BaseComponent message) {
		sendMessage(player, null, message);
	}
	public static void sendMessage(CommandSender player, UUID sender, BaseComponent message) {
		if (player instanceof ProxiedPlayer) {
			List<Runnable> reverters = new LinkedList<>();
			replaceWithAutoLoginLinks((ProxiedPlayer) player, reverters, message);
			if (sender != null) {
				((ProxiedPlayer) player).sendMessage(sender, message);
			} else {
				player.sendMessage(message);
			}
			for (Runnable reverter : reverters) {
				reverter.run();
			}
		} else {
			player.sendMessage(message);
		}
	}

	private static void replaceWithAutoLoginLinks(ProxiedPlayer player, List<Runnable> reverters, BaseComponent component) {
		ClickEvent clickEvent = component.getClickEvent();
		if (clickEvent != null) {
			ClickEvent.Action action = clickEvent.getAction();
			if (action == ClickEvent.Action.OPEN_URL) {
				String link = clickEvent.getValue();
				if (link.startsWith("http://") || link.startsWith("https://")) {
					String generatedLink = generateAutoLoginLink(player, link);
					if (!generatedLink.equals(link)) {
						component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, generatedLink));
						reverters.add(0, () -> component.setClickEvent(clickEvent));
					}
				}
			}
		}
		List<BaseComponent> extras = component.getExtra();
		if (extras != null) {
			for (BaseComponent extra : extras) {
				replaceWithAutoLoginLinks(player, reverters, extra);
			}
		}
	}

	public static String generateAutoLoginLink(ProxiedPlayer player, String link) {
		String autoLoginToken = BungeeChat.getSession(player).user.getUserData().autoLoginToken;
		if (autoLoginToken == null)
			return link;
		String escapedLink;
		try {
			String cbwebsitePrefix = "https://cubebuilders.net/";
			if (link.startsWith(cbwebsitePrefix)) {
				link = link.substring(cbwebsitePrefix.length());
			} else if (link.startsWith("https://")) {
				link = "https/" + link.substring(8);
			} else if (link.startsWith("http://")) {
				link = "http/" + link.substring(7);
			} else {
				return link;
			}
			escapedLink = URLEncoder.encode(link, "UTF-8").replace("%2F", "/");
		} catch (Exception e) {
			return link;
		}
		return "https://cubebuilders.net/link/u=" + (player.getUniqueId().toString().replace("-","")) + "/" + autoLoginToken + "/" + escapedLink;
	}
}
