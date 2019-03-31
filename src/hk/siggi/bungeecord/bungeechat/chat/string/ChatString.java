package hk.siggi.bungeecord.bungeechat.chat.string;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatString implements CharSequence {

	private final List<ChatCharacter> characters = new ArrayList<>();

	public ChatString() {
	}

	public ChatString(String rawChat) {
		this(rawChat, false);
	}

	public ChatString(String rawChat, boolean processFormatting) {
		this(rawChat, processFormatting, processFormatting, processFormatting, processFormatting, processFormatting);
	}

	public ChatString(String rawChat, boolean allowColor, boolean allowFormat, boolean allowMagic, boolean allowLinks, boolean allowCommandLinks) {
		boolean wasColorCode = false;

		char codePrefix = '&';

		char prevChar = '\0';

		char colorCode = 'f';
		char colorCodeOutsideLink = 'f';
		boolean bold = false;
		boolean italic = false;
		boolean underline = false;
		boolean strike = false;
		boolean magic = false;
		boolean boldOutsideLink = false;
		boolean italicOutsideLink = false;
		boolean underlineOutsideLink = false;
		boolean strikeOutsideLink = false;
		boolean magicOutsideLink = false;
		String link = null;
		String tooltip = null;
		String tooltipOutsideLink = null;
		boolean isFormatCode = false;
		char[] chars = rawChat.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (i > 0) {
				prevChar = chars[i - 1];
			}
			if ((c == '&' || c == (char) 0xA7) && chars.length > i + 1) {
				wasColorCode = true;
				codePrefix = c;
				continue;
			} else if (wasColorCode) {
				wasColorCode = false;
				if (isFormatCode(c)) {
					boolean deniedFormat = false;
					c = Character.toLowerCase(c);
					if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')) {
						if (allowColor) {
							colorCode = c;
						} else {
							deniedFormat = true;
						}
					}
					if (c == 'k') {
						if (allowMagic) {
							magic = true;
						} else {
							deniedFormat = true;
						}
					}
					if (c >= 'l' && c <= 'o') {
						if (allowFormat) {
							if (c == 'l') {
								bold = true;
							}
							if (c == 'm') {
								strike = true;
							}
							if (c == 'n') {
								underline = true;
							}
							if (c == 'o') {
								italic = true;
							}
						} else {
							deniedFormat = true;
						}
					}
					if (c == 'r') {
						if (allowColor || allowFormat || allowMagic) {
							bold = italic = underline = strike = magic = false;
							colorCode = (link == null ? 'f' : 'b');
						} else {
							deniedFormat = true;
						}
					}
					if (deniedFormat) {
						characters.add(new ChatCharacter(codePrefix, colorCode, bold, italic, underline, strike, magic, link, tooltip));
					} else {
						continue;
					}
				}
			}
			if (c == '\\') {
				if (i + 1 < chars.length) {
					char nextChar = chars[i + 1];
					switch (nextChar) {
						case '\\':
						case '&':
						case '<':
						case '>':
							i += 1;
							characters.add(new ChatCharacter(nextChar, colorCode, bold, italic, underline, strike, magic, link, tooltip));
							continue;
					}
				}
			}
			if (c == '<' && link == null) {
				int originalI = i;
				try {
					int startOfLink = i + 1;
					int endOfLink = rawChat.indexOf(">", startOfLink);
					int firstCharAfterLink = endOfLink + 1;
					String theLink = rawChat.substring(startOfLink, endOfLink);
					boolean hasText = false;
					if (chars.length > firstCharAfterLink) {
						int startOfText = firstCharAfterLink + 1;
						int endOfText = startOfText - 1;
						do {
							endOfText = rawChat.indexOf(">", endOfText + 1);
						} while (endOfText != -1 && chars[endOfText - 1] == '\\');
						if (endOfText != -1) {
							hasText = true;
						}
					}
					if (validateLink(theLink) && (theLink.startsWith("/") ? allowCommandLinks : allowLinks)) {
						if (hasText) {
							colorCodeOutsideLink = colorCode;
							boldOutsideLink = bold;
							italicOutsideLink = italic;
							underlineOutsideLink = underline;
							strikeOutsideLink = strike;
							magicOutsideLink = magic;
							tooltipOutsideLink = tooltip;

							colorCode = 'b';
							bold = italic = underline = strike = magic = false;
							link = theLink;
							tooltip = theLink;
							i = firstCharAfterLink;
						} else {
							characters.add(new ChatCharacter('\0', 'b', false, false, false, false, false, theLink, theLink.startsWith("/") ? "Click to run this command!" : "Click to visit this page!"));
							i = endOfLink;
						}
						continue;
					}
				} catch (Exception e) {
				}
				i = originalI;
			}
			if (c == '>' && link != null) {
				colorCode = colorCodeOutsideLink;
				bold = boldOutsideLink;
				italic = italicOutsideLink;
				underline = underlineOutsideLink;
				strike = strikeOutsideLink;
				magic = magicOutsideLink;
				tooltip = tooltipOutsideLink;
				link = null;
				continue;
			}
			characters.add(new ChatCharacter(c, colorCode, bold, italic, underline, strike, magic, link, tooltip));
		}
	}

	public ChatCharacter get(int position) {
		return characters.get(position);
	}

	public ChatString set(int position, char c) {
		characters.set(position, characters.get(position).setCharacter(c));
		return this;
	}

	public ChatString set(int position, ChatCharacter c) {
		characters.set(position, c);
		return this;
	}

	public ChatString insertAt(int position, ChatString string) {
		characters.addAll(position, string.characters);
		return this;
	}

	public ChatString insertAt(int position, String text, boolean copyFormatAfter) {
		ChatCharacter copyFormat;
		if (characters.size() > 0) {
			if (position == 0) {
				copyFormatAfter = true;
			} else if (position == characters.size()) {
				copyFormatAfter = false;
			}
			if (copyFormatAfter) {
				copyFormat = characters.get(position);
			} else {
				copyFormat = characters.get(position - 1);
			}
		} else {
			copyFormat = new ChatCharacter('\0', 'f', false, false, false, false, false, null, null);
		}
		List<ChatCharacter> newCharacters = new LinkedList<>();
		char[] chars = text.toCharArray();
		for (char c : chars) {
			newCharacters.add(new ChatCharacter(c, copyFormat));
		}
		characters.addAll(position, newCharacters);
		return this;
	}

	public ChatString replaceText(int position, ChatString string) {
		int len = string.length();
		int minStringLength = position + len;
		if (characters.size() < minStringLength) {
			throw new ArrayIndexOutOfBoundsException();
		}
		for (int i = 0; i < len; i++) {
			characters.set(position + i, string.characters.get(i));
		}
		return this;
	}

	public ChatString replaceText(int position, String text) {
		int len = text.length();
		int minStringLength = position + len;
		if (characters.size() < minStringLength) {
			throw new ArrayIndexOutOfBoundsException();
		}
		for (int i = 0; i < len; i++) {
			characters.set(position + i, characters.get(position + i).setCharacter(text.charAt(i)));
		}
		return this;
	}

	public ChatString delete(int startPos, int endPos) {
		int len = endPos - startPos;
		if (startPos < 0 || endPos > characters.size() || len < 0) {
			throw new ArrayIndexOutOfBoundsException();
		}
		for (int i = 0; i < len; i++) {
			characters.remove(startPos);
		}
		return this;
	}

	public ChatString substring(int startPos, int endPos) {
		int len = endPos - startPos;
		if (startPos < 0 || endPos > characters.size() || len < 0) {
			throw new ArrayIndexOutOfBoundsException();
		}
		ChatString n = new ChatString();
		for (int i = 0; i < len; i++) {
			n.characters.add(characters.get(i + startPos));
		}
		return n;
	}

	public ChatString append(String text) {
		return insertAt(characters.size(), text, false);
	}

	public ChatString setBold(boolean b) {
		for (int i = 0; i < characters.size(); i++) {
			characters.set(i, characters.get(i).setBold(b));
		}
		return this;
	}

	public ChatString setItalic(boolean ii) {
		for (int i = 0; i < characters.size(); i++) {
			characters.set(i, characters.get(i).setItalic(ii));
		}
		return this;
	}

	public ChatString setUnderline(boolean u) {
		for (int i = 0; i < characters.size(); i++) {
			characters.set(i, characters.get(i).setUnderline(u));
		}
		return this;
	}

	public ChatString setStrike(boolean s) {
		for (int i = 0; i < characters.size(); i++) {
			characters.set(i, characters.get(i).setStrike(s));
		}
		return this;
	}

	public ChatString setMagic(boolean m) {
		for (int i = 0; i < characters.size(); i++) {
			characters.set(i, characters.get(i).setMagic(m));
		}
		return this;
	}

	public ChatString setLink(String l) {
		for (int i = 0; i < characters.size(); i++) {
			characters.set(i, characters.get(i).setLink(l));
		}
		return this;
	}

	public ChatString setTooltip(String t) {
		for (int i = 0; i < characters.size(); i++) {
			characters.set(i, characters.get(i).setTooltip(t));
		}
		return this;
	}

	public ChatString setColor(ChatColor color) {
		char c = getColorCode(color);
		for (int i = 0; i < characters.size(); i++) {
			characters.set(i, characters.get(i).setColorCode(c));
		}
		return this;
	}

	public String toRawString() {
		char[] aa = new char[characters.size()];
		for (int i = 0; i < aa.length; i++) {
			aa[i] = characters.get(i).character;
		}
		return new String(aa);
	}

	@Override
	public int length() {
		return characters.size();
	}

	@Override
	public String toString() {
		return toUnformattedString();
	}

	/**
	 * Get the raw text with no format codes.
	 *
	 * @return raw text with no format codes.
	 */
	public String toUnformattedString() {
		StringBuilder sb = new StringBuilder();
		int a = characters.size();
		for (int i = 0; i < a; i++) {
			ChatCharacter cc = characters.get(i);
			if (cc.link != null && cc.character == '\0') {
				sb.append(cc.link);
			} else {
				sb.append(characters.get(i).character);
			}
		}
		return sb.toString();
	}

	/**
	 * Get the text with format codes.
	 *
	 * @return the text with format codes.
	 */
	public String toFormattedString() {
		char prefixChar = '&';
		char colorCode = 'f';
		boolean bold = false;
		boolean italic = false;
		boolean underline = false;
		boolean strike = false;
		boolean magic = false;
		char colorCodeOutsideLink = 'f';
		boolean boldOutsideLink = false;
		boolean italicOutsideLink = false;
		boolean underlineOutsideLink = false;
		boolean strikeOutsideLink = false;
		boolean magicOutsideLink = false;
		String link = null;
		StringBuilder sb = new StringBuilder();
		for (ChatCharacter c : characters) {
			if (c.link == null) {
				if (link != null) {
					sb.append('>');
					colorCode = colorCodeOutsideLink;
					bold = boldOutsideLink;
					italic = italicOutsideLink;
					underline = underlineOutsideLink;
					strike = strikeOutsideLink;
					magic = magicOutsideLink;
					link = null;
				}
			} else {
				if (link != null) {
					if (!link.equals(c.link)) {
						sb.append('>');
						link = null;
						colorCode = colorCodeOutsideLink;
						bold = boldOutsideLink;
						italic = italicOutsideLink;
						underline = underlineOutsideLink;
						strikeOutsideLink = strike;
						magic = magicOutsideLink;
					}
				}
				if (link == null) {
					sb.append('<').append(c.link).append('>');
					if (c.character != '\0') {
						sb.append('<');
						link = c.link;
						colorCodeOutsideLink = colorCode;
						boldOutsideLink = bold;
						italicOutsideLink = italic;
						underlineOutsideLink = underline;
						strikeOutsideLink = strike;
						magicOutsideLink = magic;
						colorCode = 'b';
						bold = italic = underline = strike = magic = false;
					}
				}
			}
			if ((bold && !c.bold)
					|| (italic && !c.italic)
					|| (underline && !c.underline)
					|| (strike && !c.strike)
					|| (magic && !c.magic)) {
				bold = italic = underline = strike = magic = false;
				colorCode = link == null ? 'b' : 'f';
			}
			if (c.bold && !bold) {
				sb.append(prefixChar).append('l');
				bold = true;
			}
			if (c.italic && !italic) {
				sb.append(prefixChar).append('o');
				italic = true;
			}
			if (c.underline && !underline) {
				sb.append(prefixChar).append('n');
				underline = true;
			}
			if (c.strike && !strike) {
				sb.append(prefixChar).append('m');
				strike = true;
			}
			if (c.magic && !magic) {
				sb.append(prefixChar).append('k');
				magic = true;
			}
			if (colorCode != c.colorCode) {
				sb.append(prefixChar).append(c.colorCode);
				colorCode = c.colorCode;
			}
			sb.append(c.character);
		}
		if (link != null) {
			sb.append('>');
		}
		return sb.toString();
	}

	public List<TextComponent> toTextComponents() {
		List<TextComponent> result = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		ChatCharacter prev = null;
		for (ChatCharacter c : characters) {
			if (prev != null && !c.formatMatches(prev)) {
				result.add(build(sb.toString(), prev));
				sb.delete(0, sb.length());
			}
			if (c.link != null && c.character == '\0') {
				if (sb.length() > 0) {
					result.add(build(sb.toString(), prev));
					sb.delete(0, sb.length());
				}
				result.add(build(c.link, c));
				prev = null;
				continue;
			} else {
				sb.append(c.character);
			}
			prev = c;
		}
		if (sb.length() > 0) {
			result.add(build(sb.toString(), prev));
		}
		return result;
	}

	private TextComponent build(String text, ChatCharacter c) {
		TextComponent tc = new TextComponent(text);
		if (c != null) {
			tc.setBold(c.bold);
			tc.setItalic(c.italic);
			tc.setUnderlined(c.underline);
			tc.setStrikethrough(c.strike);
			tc.setObfuscated(c.magic);
			tc.setColor(getChatColor(c.colorCode));
			if (c.link != null) {
				String link = c.link;
				if (link.startsWith("/")) {
					String commandString = link;
					boolean suggest = false;
					if (link.endsWith("...")) {
						suggest = true;
						commandString = link.substring(0, link.length() - 3);
					}
					tc.setClickEvent(new ClickEvent(
							suggest
									? ClickEvent.Action.SUGGEST_COMMAND
									: ClickEvent.Action.RUN_COMMAND,
							commandString
					));
				} else if (link.startsWith("[")) {
					// do nothing
				} else {
					if (!link.startsWith("http://") && !link.startsWith("https://")) {
						int pos = link.indexOf("/");
						if (pos == -1) {
							pos = link.length();
						}
						String site = link.substring(0, pos);
						if (isHSTS(site)) {
							link = "https://" + link;
						} else {
							link = "http://" + link;
						}
					}
					tc.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
				}
			}
			if (c.tooltip != null) {
				TextComponent t = new TextComponent(c.tooltip);
				tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{t}));
			}
		}
		return tc;
	}

	public TextComponent toTextComponent() {
		TextComponent b = new TextComponent("");
		appendTo(b);
		return b;
	}

	public void appendTo(BaseComponent bc) {
		for (TextComponent t : toTextComponents()) {
			bc.addExtra(t);
		}
	}

	public static boolean isFormatCode(char c) {
		switch (c) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case 'A':
			case 'a':
			case 'B':
			case 'b':
			case 'C':
			case 'c':
			case 'D':
			case 'd':
			case 'E':
			case 'e':
			case 'F':
			case 'f':
			case 'R':
			case 'r':
			case 'K':
			case 'k':
			case 'L':
			case 'l':
			case 'M':
			case 'm':
			case 'N':
			case 'n':
			case 'O':
			case 'o':
				return true;
			default:
				return false;
		}
	}

	private static boolean validateLink(String link) {
		if (link.startsWith("http://") || link.startsWith("https://")
				|| link.startsWith("/")) {
			return true;
		}
		if (link.contains("/")) {
			return link.substring(0, link.indexOf("/")).contains(".");
		} else {
			return link.contains(".");
		}
	}

	private char getColorCode(ChatColor col) {
		switch (col) {
			case BLACK:
				return '0';
			case DARK_BLUE:
				return '1';
			case DARK_GREEN:
				return '2';
			case DARK_AQUA:
				return '3';
			case DARK_RED:
				return '4';
			case DARK_PURPLE:
				return '5';
			case GOLD:
				return '6';
			case GRAY:
				return '7';
			case DARK_GRAY:
				return '8';
			case BLUE:
				return '9';
			case GREEN:
				return 'a';
			case AQUA:
				return 'b';
			case RED:
				return 'c';
			case LIGHT_PURPLE:
				return 'd';
			case YELLOW:
				return 'e';
			case WHITE:
			default:
				return 'f';
		}
	}

	private ChatColor getChatColor(char colorCode) {
		switch (colorCode) {
			case '0':
				return ChatColor.BLACK;
			case '1':
				return ChatColor.DARK_BLUE;
			case '2':
				return ChatColor.DARK_GREEN;
			case '3':
				return ChatColor.DARK_AQUA;
			case '4':
				return ChatColor.DARK_RED;
			case '5':
				return ChatColor.DARK_PURPLE;
			case '6':
				return ChatColor.GOLD;
			case '7':
				return ChatColor.GRAY;
			case '8':
				return ChatColor.DARK_GRAY;
			case '9':
				return ChatColor.BLUE;
			case 'a':
				return ChatColor.GREEN;
			case 'b':
				return ChatColor.AQUA;
			case 'c':
				return ChatColor.RED;
			case 'd':
				return ChatColor.LIGHT_PURPLE;
			case 'e':
				return ChatColor.YELLOW;
			case 'f':
			default:
				return ChatColor.WHITE;
		}
	}

	private static final Set<String> hstsSites = new HashSet<>();

	static {
		hstsSites.add("cubebuilders.net");
		hstsSites.add("siggi.io");
		hstsSites.add("spotify.com");
		hstsSites.add("youtube.com");
		hstsSites.add("youtu.be");
		hstsSites.add("facebook.com");
		hstsSites.add("google.com");
		hstsSites.add("twitter.com");
		hstsSites.add("instagram.com");
		hstsSites.add("snapchat.com");
		hstsSites.add("vimeo.com");
		hstsSites.add("reddit.com");
	}

	private static boolean isHSTS(String site) {
		site = "." + site.toLowerCase();
		do {
			site = site.substring(site.indexOf(".") + 1);
			if (hstsSites.contains(site)) {
				return true;
			}
		} while (site.contains("."));
		return false;
	}
	
	public static String prependProtocol(String url){
		if (url.startsWith("http://") || url.startsWith("https://")) {return url;}
		int domainEnd = url.indexOf("/");
		if(domainEnd==-1){domainEnd=url.length();}
		String domain = url.substring(0,domainEnd);
		if (isHSTS(domain)){return "https://"+url;}else{return "http://"+url;}
	}

	@Override
	public char charAt(int index) {
		return characters.get(index).character;
	}

	@Override
	public ChatString subSequence(int start, int end) {
		return substring(start, end);
	}

	@Override
	@SuppressWarnings({"CloneDoesntDeclareCloneNotSupportedException", "CloneDoesntCallSuperClone"})
	public ChatString clone() {
		ChatString newC = new ChatString();
		newC.characters.addAll(characters);
		return newC;
	}
}
