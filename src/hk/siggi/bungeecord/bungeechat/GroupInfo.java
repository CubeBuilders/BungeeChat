package hk.siggi.bungeecord.bungeechat;

import static hk.siggi.bungeecord.bungeechat.BungeeChat.pencil;
import static hk.siggi.bungeecord.bungeechat.BungeeChat.staroutline;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import static hk.siggi.bungeecord.bungeechat.util.Util.assertNotNull;
import static hk.siggi.bungeecord.bungeechat.util.Util.tryClose;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.cubebuilders.user.NameHistory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class GroupInfo {

	private long lastModified = 0L;
	private long lastCheck = 0L;
	private final BungeeChat plugin;
	private final File file;
	private final List<Rank> memberRanks = new ArrayList<>();
	private final List<Rank> staffRanks = new ArrayList<>();

	public GroupInfo(BungeeChat plugin, File file) {
		this.plugin = plugin;
		this.file = file;
	}

	private void loadIfNeeded() {
		long now = System.currentTimeMillis();
		if ((now < lastCheck || now - lastCheck > 60000L)
				&& (file.lastModified() == lastModified || loadData())) {
			lastCheck = now;
		}
	}

	private boolean loadData() {
		FileInputStream fis = null;
		long lm = lastModified;
		boolean loadSuccess = false;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis = new FileInputStream(file)));
			lastModified = file.lastModified();
			String line;
			while ((line = reader.readLine()) != null) {
				try {
					int idx = line.indexOf("#");
					if (idx >= 0) {
						line = line.substring(0, idx);
					}
					line = line.trim();
					while (line.contains("\t\t")) {
						line = line.replace("\t\t", "\t");
					}
					String[] pieces = line.split("\t");
					if (pieces[0].equalsIgnoreCase("member") || pieces[0].equals("staff")) {
						String id = pieces[1];
						String display = pieces[2];
						String color = pieces[3];
						String minicolor = color;
						if (pieces.length > 4) {
							minicolor = pieces[4];
						}
						(pieces[0].equals("staff") ? staffRanks : memberRanks).add(new Rank(id, display, ChatColor.valueOf(color.toUpperCase()), ChatColor.valueOf(minicolor.toUpperCase())));
					}
				} catch (Exception e) {
				}
			}
			loadSuccess = true;
		} catch (Exception e) {
		} finally {
			tryClose(fis);
		}
		lastModified = lm;
		return loadSuccess;
	}
	
	public Collection<String> getGroups(ProxiedPlayer player) {
			Collection<String> groups;
			{
				String[] groupsArray = BungeeChat.getSession(player).getFakeGroups();
				if (groupsArray == null) {
					groups = player.getGroups();
				} else {
					groups = Arrays.asList(groupsArray);
				}
			}
			return groups;
	}
	
	public boolean isStaff(ProxiedPlayer player) {
		boolean staff = false;
		boolean hiddenStaff = false;
		Collection<String> groups = getGroups(player);
		for (String group : groups) {
			if (group.equalsIgnoreCase("hiddenstaff")) hiddenStaff=true;
			for (Rank rank : staffRanks) {
				if (rank.id.equals(group))staff=true;
			}
		}
		return staff && !hiddenStaff;
	}

	public ChatColor getColor(ProxiedPlayer player) {
		int memberRankID = -1;
		int staffRankID = -1;
		{
			int memberSize = memberRanks.size();
			int staffSize = staffRanks.size();
			Collection<String> groups = getGroups(player);
			for (String group : groups) {
				if (group.equalsIgnoreCase("hiddenstaff")) {
					staffRankID = -2;
					continue;
				}
				for (int i = 0; i < memberSize; i++) {
					if (group.equalsIgnoreCase(memberRanks.get(i).id)) {
						memberRankID = Math.max(memberRankID, i);
					}
				}
				if (staffRankID >= -1) {
					for (int i = 0; i < staffSize; i++) {
						if (group.equalsIgnoreCase(staffRanks.get(i).id)) {
							staffRankID = Math.max(staffRankID, i);
						}
					}
				}
			}
			if (staffRankID < -1) {
				staffRankID = -1;
			}
		}
		Rank memberRank = (memberRankID >= 0 && memberRankID < memberRanks.size() ? memberRanks.get(memberRankID) : null);
		Rank staffRank = (staffRankID >= 0 && staffRankID < staffRanks.size() ? staffRanks.get(staffRankID) : null);

		if (staffRank != null) {
			return staffRank.color;
		} else if (memberRank != null) {
			return memberRank.color;
		} else {
			return ChatColor.GRAY;
		}
	}

	public List<TextComponent> usernameComponent(ProxiedPlayer player, boolean miniPrefix, boolean showPrefix, boolean allowDoublePrefix, boolean ignoreCustomPrefix) {
		loadIfNeeded();

		PlayerAccount a = plugin.getPlayerInfo(player.getUniqueId());

		String customPrefix = ignoreCustomPrefix ? null : a.getChatNamePrefix();
		String customSuffix = ignoreCustomPrefix ? null : a.getChatNameSuffix();

		if (customPrefix != null || customSuffix != null) {
			miniPrefix = false;
			showPrefix = true;
			allowDoublePrefix = false;
		}

		String nickname = a.getNickname();
		NameHistory[] history = a.getNameHistory();
		String previousName = null;
		if (history.length >= 2) {
			NameHistory current = history[history.length - 1];
			NameHistory previous = history[history.length - 2];
			long now = System.currentTimeMillis();
			long timeChanged = current.getTime();
			long timeSinceChange = now - timeChanged;
			if (timeSinceChange < (60L * 60L * 24L * 30L * 1000L)) {
				previousName = previous.getName();
			}
		}
		
		if (previousName != null && plugin.isNameBanned(previousName)) {
			previousName = null;
		}

		int memberRankID = -1;
		int staffRankID = -1;
		{
			int memberSize = memberRanks.size();
			int staffSize = staffRanks.size();
			Collection<String> groups = getGroups(player);
			for (String group : groups) {
				if (group.equalsIgnoreCase("hiddenstaff")) {
					staffRankID = -2;
					continue;
				}
				for (int i = 0; i < memberSize; i++) {
					if (group.equalsIgnoreCase(memberRanks.get(i).id)) {
						memberRankID = Math.max(memberRankID, i);
					}
				}
				if (staffRankID >= -1) {
					for (int i = 0; i < staffSize; i++) {
						if (group.equalsIgnoreCase(staffRanks.get(i).id)) {
							staffRankID = Math.max(staffRankID, i);
						}
					}
				}
			}
			if (staffRankID < -1) {
				staffRankID = -1;
			}
		}
		Rank memberRank = (memberRankID >= 0 && memberRankID < memberRanks.size() ? memberRanks.get(memberRankID) : null);
		Rank staffRank = (staffRankID >= 0 && staffRankID < staffRanks.size() ? staffRanks.get(staffRankID) : null);

		String hoverText = "";
		try {
			if (memberRank != null) {
				hoverText += memberRank.color + memberRank.display + " ";
			}
			if (staffRank != null) {
				hoverText += staffRank.color + staffRank.display + " ";
			}
			hoverText += ChatColor.RESET;
		} catch (Exception e) {
		}
		String realName = player.getName();
		String realNameWithPencil = realName + (previousName == null ? "" : pencil);
		hoverText += realNameWithPencil;
		if (nickname != null) {
			if (nickname.equals(player.getName())) {
				a.setNickname(nickname = null);
			} else {
				hoverText = hoverText + (hoverText.equals("") ? "" : "\n") + "Real Name: " + realNameWithPencil;
			}
		}
		String displayName = (nickname == null ? realNameWithPencil : ("*" + nickname));
		TextComponent nameComponent = new TextComponent(((miniPrefix && staffRank != null) ? staroutline : "") + displayName);
		if (previousName != null) {
			hoverText = hoverText + (hoverText.equals("") ? "" : "\n") + "Previous Name: " + previousName;
		}
		String fingerprint = player.getUniqueId().toString().toUpperCase();
		fingerprint = fingerprint.substring(0, 8);
		hoverText = hoverText + (hoverText.equals("") ? "" : "\n") + "Player Fingerprint: " + fingerprint;
		nameComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(hoverText)}));
		if (miniPrefix) {
			if (staffRank != null) {
				nameComponent.setColor(staffRank.miniColor);
			} else if (memberRank != null) {
				nameComponent.setColor(memberRank.miniColor);
			}
		}

		if (showPrefix) {
			if (customPrefix != null || customSuffix != null) {
				if (customPrefix == null) {
					customPrefix = "";
				}
				if (customSuffix == null) {
					customSuffix = "";
				}
				nameComponent.setText(customPrefix + nameComponent.getText() + customSuffix);
				return Arrays.asList(new TextComponent[]{nameComponent});
			}
			ArrayList<TextComponent> componentList = new ArrayList<>();
			TextComponent memberRankComponent = null;
			TextComponent staffRankComponent = null;
			TextComponent spacer = new TextComponent(" ");
			TextComponent spacer2 = new TextComponent(" ");
			if (staffRank != null) {
				staffRankComponent = new TextComponent(staffRank.display);
				staffRankComponent.setColor(staffRank.color);
			}
			if (memberRank != null) {
				memberRankComponent = new TextComponent(memberRank.display);
				memberRankComponent.setColor(memberRank.color);
			}
			if (staffRankComponent != null) {
				if (allowDoublePrefix && memberRank != null) {
					componentList.add(memberRankComponent);
					componentList.add(spacer);
				}
				componentList.add(staffRankComponent);
			} else {
				componentList.add(memberRankComponent);
			}
			componentList.add(spacer2);
			componentList.add(nameComponent);
			return componentList;
		}
		return Arrays.asList(new TextComponent[]{nameComponent});
	}

	private static final class Rank {

		public final String id;
		public final String display;
		public final ChatColor color;
		public final ChatColor miniColor;

		public Rank(String id, String display, ChatColor color) {
			this(id, display, color, color);
		}

		public Rank(String id, String display, ChatColor color, ChatColor miniColor) {
			assertNotNull(id, "id");
			assertNotNull(display, "display");
			assertNotNull(color, "color");
			assertNotNull(miniColor, "miniColor");
			this.id = id;
			this.display = display;
			this.color = color;
			this.miniColor = miniColor;
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof Rank) {
				return equals((Rank) other);
			}
			return false;
		}

		public boolean equals(Rank other) {
			return this.id.equals(other.id)
					&& this.display.equals(other.display)
					&& this.color.equals(other.color)
					&& this.miniColor.equals(other.miniColor);
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 67 * hash + id.hashCode();
			hash = 67 * hash + display.hashCode();
			hash = 67 * hash + color.ordinal();
			hash = 67 * hash + miniColor.ordinal();
			return hash;
		}
	}
}
