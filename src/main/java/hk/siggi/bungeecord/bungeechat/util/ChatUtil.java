package hk.siggi.bungeecord.bungeechat.util;

import java.util.ArrayList;
import java.util.List;
import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.chat.ProcessedChat;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
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
		if (false)
		return processChat(sender, text, false);
		ProcessedChat process = BungeeChat.getInstance().getChatController().process(sender, text, false);
		return process.uncensored;
	}

	private static ArrayList<BaseComponent> processChat(ProxiedPlayer sender, String text, boolean isInsideLink) {
		ArrayList<BaseComponent> componentArray = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		char[] chars = text.toCharArray();
		boolean allowColor = sender == null ? true : sender.hasPermission("hk.siggi.bungeechat.chat.color");
		boolean allowFormat = sender == null ? true : sender.hasPermission("hk.siggi.bungeechat.chat.format");
		boolean allowMagic = sender == null ? true : sender.hasPermission("hk.siggi.bungeechat.chat.magic");
		boolean allowLinks = sender == null ? true : sender.hasPermission("hk.siggi.bungeechat.chat.link");
		boolean allowCommandLinks = sender == null ? true : sender.hasPermission("hk.siggi.bungeechat.chat.commandlink");
		boolean allowNonWebLinks = sender == null ? true : sender.hasPermission("hk.siggi.bungeechat.chat.nonweblink");
		boolean bypassBlacklist = sender == null;
		ChatColor color = isInsideLink ? ChatColor.AQUA : ChatColor.WHITE;
		boolean magic = false;
		boolean bold = false;
		boolean strike = false;
		boolean underline = false;
		boolean italic = false;
		if (isInsideLink) {
			allowLinks = allowCommandLinks = allowNonWebLinks = false;
		}
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '\\' && chars.length > i + 1 && (chars[i + 1] == '\\' || chars[i + 1] == '&' || chars[i + 1] == '<')) {
				i += 1;
				builder.append(chars[i]);
				continue;
			}
			if (chars[i] == '&' && chars.length > i + 1 && isChatCode(chars[i + 1])) {
				i += 1;
				if (builder.length() != 0) {
					String finalString = builder.toString();
					TextComponent textComponent = new TextComponent(finalString);
					textComponent.setObfuscated(magic);
					textComponent.setBold(bold);
					textComponent.setStrikethrough(strike);
					textComponent.setUnderlined(underline);
					textComponent.setItalic(italic);
					textComponent.setColor(color);
					componentArray.add(textComponent);
					builder = new StringBuilder();
				}
				switch (chars[i]) {
					case '0':
						color = ChatColor.BLACK;
						break;
					case '1':
						color = ChatColor.DARK_BLUE;
						break;
					case '2':
						color = ChatColor.DARK_GREEN;
						break;
					case '3':
						color = ChatColor.DARK_AQUA;
						break;
					case '4':
						color = ChatColor.DARK_RED;
						break;
					case '5':
						color = ChatColor.DARK_PURPLE;
						break;
					case '6':
						color = ChatColor.GOLD;
						break;
					case '7':
						color = ChatColor.GRAY;
						break;
					case '8':
						color = ChatColor.DARK_GRAY;
						break;
					case '9':
						color = ChatColor.BLUE;
						break;
					case 'A':
					case 'a':
						color = ChatColor.GREEN;
						break;
					case 'B':
					case 'b':
						color = ChatColor.AQUA;
						break;
					case 'C':
					case 'c':
						color = ChatColor.RED;
						break;
					case 'D':
					case 'd':
						color = ChatColor.LIGHT_PURPLE;
						break;
					case 'E':
					case 'e':
						color = ChatColor.YELLOW;
						break;
					case 'F':
					case 'f':
						color = ChatColor.WHITE;
						break;
					case 'K':
					case 'k':
						magic = true;
						break;
					case 'L':
					case 'l':
						bold = true;
						break;
					case 'M':
					case 'm':
						strike = true;
						break;
					case 'N':
					case 'n':
						underline = true;
						break;
					case 'O':
					case 'o':
						italic = true;
						break;
					case 'R':
					case 'r':
						color = isInsideLink ? ChatColor.AQUA : ChatColor.WHITE;
						magic = false;
						bold = false;
						strike = false;
						underline = false;
						italic = false;
						break;
				}
				if (!allowColor) {
					color = ChatColor.WHITE;
				}
				if (!allowFormat) {
					bold = false;
					strike = false;
					underline = false;
					italic = false;
				}
				if (!allowMagic) {
					magic = false;
				}
				continue;
			}
			if (chars[i] == '<' && allowLinks) {
				int originalI = i;
				if (builder.length() != 0) {
					String finalString = builder.toString();
					TextComponent textComponent = new TextComponent(finalString);
					textComponent.setObfuscated(magic);
					textComponent.setBold(bold);
					textComponent.setStrikethrough(strike);
					textComponent.setUnderlined(underline);
					textComponent.setItalic(italic);
					textComponent.setColor(color);
					componentArray.add(textComponent);
					builder = new StringBuilder();
				}
				int startLinkPos = i + 1;
				int endLinkPos = -1;
				for (int j = i; endLinkPos == -1 && j < chars.length; j++) {
					if (chars[j] == '>') {
						endLinkPos = j;
					}
				}
				if (endLinkPos != -1) {
					i = endLinkPos;
					int startTextPos = -1;
					int endTextPos = -1;
					if (chars.length > i + 1 && chars[i + 1] == '<') {
						startTextPos = i + 2;
						for (int j = i + 2; endTextPos == -1 && j < chars.length; j++) {
							if (chars[j] == '>') {
								endTextPos = j;
							}
						}
						i = endTextPos;
					}
					String link = new String(chars, startLinkPos, endLinkPos - startLinkPos);
					String lowerLink = link.toLowerCase();
					boolean linkBlacklisted = !bypassBlacklist && Util.isChatLinkBlacklisted(link);
					if ((link.startsWith("/") || link.contains(":")) && ((!link.startsWith("/") || allowCommandLinks) && (lowerLink.startsWith("http://") || lowerLink.startsWith("https://") || allowNonWebLinks))) {
						if (linkBlacklisted) {
							link = "[blacklisted website]";
						}
						if (startTextPos < endTextPos) {
							String linkText = new String(chars, startTextPos, endTextPos - startTextPos);
							if (allowColor) {
								linkText = "&b" + linkText;
							}
							ArrayList<BaseComponent> linkComponents = processChat(sender, linkText, true);
							for (BaseComponent c : linkComponents) {
								if (!allowColor) {
									c.setColor(ChatColor.AQUA);
								}
								if (link.startsWith("/")) {
									c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(link)}));
									if (link.endsWith("...")) {
										c.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, link.substring(0, link.length() - 3)));
									} else {
										c.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, link));
									}
								} else if (link.equals("[blacklisted website]")) {
									c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("This link is blacklisted")}));
								} else {
									c.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(link)}));
									c.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
								}
								componentArray.add(c);
							}
						} else {
							String linkText = link;
							TextComponent linkTextComponent = new TextComponent(linkText);
							linkTextComponent.setColor(ChatColor.AQUA);
							if (link.startsWith("/")) {
								linkTextComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to run command")}));
								if (link.endsWith("...")) {
									linkTextComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, link.substring(0, link.length() - 3)));
								} else {
									linkTextComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, link));
								}
							} else if (link.equals("[blacklisted website]")) {
								linkTextComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("This link is blacklisted")}));
							} else {
								String title;
								if (sender != null && Util.isChatLinkWhitelisted(link) && (title = Util.getTitle(link)) != null) {
									linkTextComponent.setText("\u21e8" + title);
									linkTextComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(link)}));
									linkTextComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
								} else {
									linkTextComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to visit page")}));
									linkTextComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
								}
							}
							componentArray.add(linkTextComponent);
						}
						continue;
					}
				}
				i = originalI;
			}
			builder.append(chars[i]);
		}
		if (builder.length() != 0) {
			String finalString = builder.toString();
			TextComponent textComponent = new TextComponent(finalString);
			textComponent.setObfuscated(magic);
			textComponent.setBold(bold);
			textComponent.setStrikethrough(strike);
			textComponent.setUnderlined(underline);
			textComponent.setItalic(italic);
			textComponent.setColor(color);
			componentArray.add(textComponent);
		}
		return componentArray;
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
