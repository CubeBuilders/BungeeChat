package hk.siggi.bungeecord.bungeechat.player;

import java.util.HashMap;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;

public enum PlayerTitle {

	PRESIDENT(
			"president",
			ChatColor.LIGHT_PURPLE + "President " + ChatColor.WHITE,
			""
	),
	FIRST_LADY(
			"first_lady",
			ChatColor.LIGHT_PURPLE + "First Lady " + ChatColor.WHITE,
			""
	),
	PRINCE(
			"prince",
			ChatColor.YELLOW + "Prince " + ChatColor.WHITE,
			""
	),
	PRINCESS(
			"princess",
			ChatColor.LIGHT_PURPLE + "Princess " + ChatColor.WHITE,
			""
	),
	GENERAL(
			"general",
			ChatColor.GREEN + "General " + ChatColor.WHITE,
			""
	),
	LIEUTENANT(
			"general",
			ChatColor.BLUE + "Lieutenant " + ChatColor.WHITE,
			""
	),
	CHEATER(
			"cheater",
			ChatColor.RED + "Cheater " + ChatColor.WHITE,
			""
	),
	GRIEFER(
			"griefer",
			ChatColor.RED + "Griefer " + ChatColor.WHITE,
			""
	),
	BULLY(
			"bully",
			ChatColor.RED + "Bully " + ChatColor.WHITE,
			""
	),
	BAD_SPORT(
			"bad_sport",
			ChatColor.RED + "Bad Sport " + ChatColor.WHITE,
			""
	);

	private static final Map<String, PlayerTitle> byId = new HashMap<>();

	static {
		for (PlayerTitle title : values()) {
			byId.put(title.id.toLowerCase(), title);
		}
	}

	public static PlayerTitle getById(String id) {
		return byId.get(id.toLowerCase());
	}

	private final String id, prefix, suffix;

	private PlayerTitle(String id, String prefix, String suffix) {
		this.id = id;
		this.prefix = prefix;
		this.suffix = suffix;
	}

	public String getId() {
		return id;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}
}
