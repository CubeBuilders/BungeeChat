package hk.siggi.bungeecord.bungeechat.chat.string;

import net.md_5.bungee.api.ChatColor;

import java.util.Objects;

public final class ChatCharacter {

	public final char character;
	public final ChatColor colorCode;
	public final boolean bold;
	public final boolean italic;
	public final boolean underline;
	public final boolean strike;
	public final boolean magic;
	public final String link;
	public final String tooltip;

	public ChatCharacter(char character, ChatColor colorCode, boolean bold, boolean italic, boolean underline, boolean strike, boolean magic, String link, String tooltip) {
		this.character = character;
		this.colorCode = colorCode;
		this.bold = bold;
		this.italic = italic;
		this.underline = underline;
		this.strike = strike;
		this.magic = magic;
		this.link = link;
		this.tooltip = tooltip;
	}

	public ChatCharacter(char character, ChatCharacter copyFormat) {
		this(
				character,
				copyFormat.colorCode,
				copyFormat.bold,
				copyFormat.italic,
				copyFormat.underline,
				copyFormat.strike,
				copyFormat.magic,
				copyFormat.link,
				copyFormat.tooltip
		);
	}

	public ChatCharacter setCharacter(char c) {
		if (character == c) {
			return this;
		}
		return new ChatCharacter(c, colorCode, bold, italic, underline, strike, magic, link, tooltip);
	}

	public ChatCharacter setColorCode(ChatColor c) {
		if (colorCode == c) {
			return this;
		}
		return new ChatCharacter(character, c, bold, italic, underline, strike, magic, link, tooltip);
	}

	public ChatCharacter setBold(boolean b) {
		if (bold == b) {
			return this;
		}
		return new ChatCharacter(character, colorCode, b, italic, underline, strike, magic, link, tooltip);
	}

	public ChatCharacter setItalic(boolean i) {
		if (italic == i) {
			return this;
		}
		return new ChatCharacter(character, colorCode, bold, i, underline, strike, magic, link, tooltip);
	}

	public ChatCharacter setUnderline(boolean u) {
		if (underline == u) {
			return this;
		}
		return new ChatCharacter(character, colorCode, bold, italic, u, strike, magic, link, tooltip);
	}

	public ChatCharacter setStrike(boolean s) {
		if (strike == s) {
			return this;
		}
		return new ChatCharacter(character, colorCode, bold, italic, underline, s, magic, link, tooltip);
	}

	public ChatCharacter setMagic(boolean m) {
		if (magic == m) {
			return this;
		}
		return new ChatCharacter(character, colorCode, bold, italic, underline, strike, m, link, tooltip);
	}

	public ChatCharacter setLink(String l) {
		if (equals(link, l)) {
			return this;
		}
		return new ChatCharacter(character, colorCode, bold, italic, underline, strike, magic, l, tooltip);
	}

	public ChatCharacter setTooltip(String t) {
		if (equals(tooltip, t)) {
			return this;
		}
		return new ChatCharacter(character, colorCode, bold, italic, underline, strike, magic, link, t);
	}

	public boolean formatMatches(ChatCharacter o) {
		return this.colorCode == o.colorCode
				&& this.bold == o.bold
				&& this.italic == o.italic
				&& this.underline == o.underline
				&& this.strike == o.strike
				&& this.magic == o.magic
				&& equals(this.link, o.link)
				&& equals(this.tooltip, o.tooltip);
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (other instanceof ChatCharacter) {
			ChatCharacter o = (ChatCharacter) other;
			return this.character == o.character
					&& this.colorCode == o.colorCode
					&& this.bold == o.bold
					&& this.italic == o.italic
					&& this.underline == o.underline
					&& this.strike == o.strike
					&& this.magic == o.magic
					&& equals(this.link, o.link)
					&& equals(this.tooltip, o.tooltip);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		int booleanField = (this.bold ? 16 : 0) + (this.italic ? 8 : 0) + (this.underline ? 4 : 0)
				+ (this.strike ? 2 : 0) + (this.magic ? 1 : 0);
		hash = 29 * hash + this.character;
		hash = 29 * hash + this.colorCode.hashCode();
		hash = 29 * hash + booleanField;
		hash = 29 * hash + Objects.hash(link);
		hash = 29 * hash + Objects.hash(tooltip);
		return hash;
	}

	private <O> boolean equals(O o1, O o2) {
		if (o1 == null) {
			return o2 == null;
		} else if (o2 == null) {
			return false;
		}
		return o1.equals(o2);
	}
}
