package hk.siggi.bungeecord.bungeechat.util;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Color {
	private static final Color[] preset;

	static {
		char[] chars = "0123456789abcdef".toCharArray();
		preset = new Color[16];
		for (int i = 0; i < 16; i++) {
			char code = chars[i];
			ChatColor chatColor = ChatColor.getByChar(code);
			java.awt.Color javaColor = chatColor.getColor();
			int value = javaColor.getRGB() & 0xffffff;
			String valueAsString = Integer.toString(value, 16).toLowerCase();
			while (valueAsString.length() < 6) valueAsString = "0" + valueAsString;
			preset[i] = new Color(new String(new char[]{code}), chatColor, "#" + valueAsString, value);
		}
	}

	/**
	 * The chat code used in strings.  For example, &d or &#cc00ff.
	 */
	public final String code;

	/**
	 * The BungeeCord Chat API color.
	 */
	public final ChatColor chatColor;

	/**
	 * The hex code for this color.
	 */
	public final String hexCode;

	/**
	 * The value of this color.
	 */
	public final int value;

	private Color(String code, ChatColor chatColor, String hexCode, int value) {
		this.code = "&" + code;
		this.chatColor = chatColor;
		this.hexCode = hexCode;
		this.value = value;
	}

	public RGB toRGB() {
		return new RGB((value >> 16) & 0xff, (value >> 8) & 0xff, value & 0xff);
	}

	public HSB toHSB() {
		return toRGB().toHSB();
	}

	@SuppressWarnings("deprecation")
	public static Color of(String string) {
		if (string.length() == 1) {
			return preset[Integer.parseInt(string, 16)];
		}
		if (string.startsWith("#") && string.length() == 7) {
			int value = Integer.parseInt(string.substring(1, 7), 16);
			String lower = string.toLowerCase();
			return new Color(lower, ChatColor.of(lower), lower, value);
		}
		return preset[ChatColor.of(string).ordinal()];
	}

	public static final class RGB {
		public final int r;
		public final int g;
		public final int b;

		public RGB(int r, int g, int b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}

		public HSB toHSB() {
			float[] hsb = java.awt.Color.RGBtoHSB(r, g, b, null);
			return new HSB(hsb[0], hsb[1], hsb[2]);
		}

		public Color toColor() {
			String valueAsString = Integer.toString((r << 16) | (g << 8) | (b), 16);
			while (valueAsString.length() < 6) valueAsString = "0" + valueAsString;
			return of("#" + valueAsString);
		}

		public List<RGB> gradient(int count, int r, int g, int b) {
			List<RGB> gradient = new ArrayList<>(count);
			gradient.add(this);
			int steps = count - 1;
			int diffR = r - this.r;
			int diffG = g - this.g;
			int diffB = b - this.b;
			for (int i = count - 1; i >= 0; i--) {
				gradient.add(new RGB(
						r - (diffR * i / steps),
						g - (diffG * i / steps),
						b - (diffB * i / steps)
				));
			}
			return gradient;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) return true;
			if (!(other instanceof RGB)) return false;
			RGB o = (RGB) other;
			return r == o.r && g == o.g && b == o.b;
		}

		@Override
		public int hashCode() {
			return Objects.hash(r, g, b);
		}
	}

	public static final class HSB {
		public final float h;
		public final float s;
		public final float b;

		public HSB(float h, float s, float b) {
			this.h = h;
			this.s = s;
			this.b = b;
		}

		public RGB toRGB() {
			int value = java.awt.Color.HSBtoRGB(h, s, b);
			int r = (value >> 16) & 0xff;
			int g = (value >> 8) & 0xff;
			int b = value & 0xff;
			return new RGB(r, g, b);
		}

		public Color toColor() {
			return toRGB().toColor();
		}

		public List<HSB> gradient(int count, float h, float s, float b) {
			List<HSB> gradient = new ArrayList<>(count);
			gradient.add(this);
			int steps = count - 1;
			float diffH = h - this.h;
			float diffS = s - this.s;
			float diffB = b - this.b;
			for (int i = count - 1; i >= 0; i--) {
				gradient.add(new HSB(
						h - (diffH * i / steps),
						s - (diffS * i / steps),
						b - (diffB * i / steps)
				));
			}
			return gradient;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof HSB))
				return false;
			HSB o = (HSB) other;
			return Float.compare(o.h, h) == 0 && Float.compare(o.s, s) == 0 && Float.compare(o.b, b) == 0;
		}

		@Override
		public int hashCode() {
			return Objects.hash(h, s, b);
		}
	}
}
