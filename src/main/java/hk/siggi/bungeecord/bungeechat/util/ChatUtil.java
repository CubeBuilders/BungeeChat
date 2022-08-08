package hk.siggi.bungeecord.bungeechat.util;

import java.util.ArrayList;
import java.util.List;
import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.chat.ProcessedChat;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ChatUtil {

	private ChatUtil() {
		// not meant to be instantiated
	}

	public static String stripChatCodes(String text) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) == '&' && text.length() > i + 1 && isChatCode(text.charAt(i + 1))) {
				i += 1;
				continue;
			}
			sb.append(text.charAt(i));
		}
		return sb.toString();
	}

	public static boolean isChatCode(char character) {
		return (character >= '0' && character <= '9')
				|| (character >= 'A' && character <= 'F')
				|| (character >= 'a' && character <= 'f')
				|| (character >= 'K' && character <= 'O')
				|| (character >= 'k' && character <= 'o')
				|| (character == 'R')
				|| (character == 'r');
	}

	public static ArrayList<BaseComponent> processChat(ProxiedPlayer sender, String text) {
		ProcessedChat process = BungeeChat.getInstance().getChatController().process(sender, text, false);
		return process.uncensored;
	}

	public static TextComponent unify(List<BaseComponent> components) {
		TextComponent component = new TextComponent("");
		component.addExtra(new TextComponent(""));
		for (BaseComponent c : components) {
			component.addExtra(c);
		}
		return component;
	}
}
