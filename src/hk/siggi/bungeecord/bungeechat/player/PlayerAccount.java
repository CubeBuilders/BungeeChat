package hk.siggi.bungeecord.bungeechat.player;

import com.google.gson.Gson;
import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.util.Util;
import static hk.siggi.bungeecord.bungeechat.util.Util.copyCreationDate;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.cubebuilders.user.CBUser;
import net.cubebuilders.user.NameHistory;
import net.cubebuilders.user.UserDonation;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class PlayerAccount {

	private static final long timerLength = 14400000L;
	private final ArrayList<Punishment> punishments = new ArrayList<>();
	private final ArrayList<Mail> mail = new ArrayList<>();
	private final Set<UUID> ignoredUsers = new HashSet<>();
	private NameHistory[] previousNames = new NameHistory[0];
	private final UUID player;
	private long muteTime = 0L;
	private long muteExpires = 0L;
	private String muteReason = null;
	private long banTime = 0L;
	private long banExpires = 0L;
	private String banReason = null;
	private boolean chatCensor = true;
	private boolean chatCensorSemi = false;
	private long lastShownChatCensorMessage = 0;
	private String nickname = null;
	private boolean mcBansPBanned = false;
	private boolean mcBansMod = false;
	private float mcBansRep = 10.0f;
	private MCBan[] mcBanList = new MCBan[0];
	private boolean mcBansExempt = false;
	private boolean bypassIPBan = false;
	private boolean silentMuted = false;
	private ChatPrefixType chatPrefixType = ChatPrefixType.AUTO;
	private boolean mineChatGiftOnNextLogin;
	private boolean gaveMineChatGift;
	private String chatNamePrefix = null;
	private String chatNameSuffix = null;
	private long chatNamePrefixSuffixForcedUntil = 0L;
	private String chatNamePrefixSuffixForcedMessage = null;
	private boolean deleteTitleWhenForcedExpires = false;
	private String prowlApiKey = null;
	private long lastVoted = 0L;
	private String staffSpot = null;
	private String rank = null;
	private long rankExpires = 0L;
	private boolean alertsOn = true;
	private boolean messagesOn = true;
	private boolean mailOn = true;
	private long lastMailAlert = 0L;

	private String mojangSkin = null;
	private String mojangSkinSignature = null;
	private String customSkin = null;
	private String customSkinSignature = null;

	private long streamModeExpires = 0L;
	private long vanishExpires = 0L;
	private boolean vanishOnLogin = false;
	private long noSpyExpires = 0L;
	private long noMsgExpires = 0L;
	private long antiSpyExpires = 0L;
	private long noLogExpires = 0L;
	private long sneakyExpires = 0L;

	private String customNameBanMessage = null;
	private String phoneNumber = null;
	private long fakeLastOnline = 0L;

	private boolean disableCapsFilter = false;

	public void setMojangSkin(String skin, String signature) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			mojangSkin = skin;
			mojangSkinSignature = signature;
			save();
		}
	}

	public void setCustomSkin(String skin, String signature) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			customSkin = skin;
			customSkinSignature = signature;
			save();
		}
	}

	public String getMojangSkin() {
		return mojangSkin;
	}

	public String getMojangSkinSignature() {
		return mojangSkinSignature;
	}

	public String getCustomSkin() {
		return customSkin;
	}

	public String getCustomSkinSignature() {
		return customSkinSignature;
	}

	public void setProwlApiKey(String prowlApiKey) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.prowlApiKey = prowlApiKey;
			save();
		}
	}

	public String getProwlApiKey() {
		return prowlApiKey;
	}

	public void setBypassIPBan(boolean bypassIPBan) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.bypassIPBan = bypassIPBan;
			save();
		}
	}

	public boolean getBypassIPBan() {
		return bypassIPBan;
	}

	public void setMCBans(boolean mcBansPBanned, boolean mcBansMod, float mcBansRep, MCBan[] mcBanList) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.mcBansPBanned = mcBansPBanned;
			this.mcBansMod = mcBansMod;
			this.mcBansRep = mcBansRep;
			MCBan[] b = new MCBan[mcBanList.length];
			System.arraycopy(mcBanList, 0, b, 0, b.length);
			this.mcBanList = b;
			save();
		}
	}

	public boolean isMCBansPBanned() {
		return mcBansPBanned;
	}

	public boolean isMCBansMod() {
		return mcBansMod;
	}

	public float getMCBansRep() {
		return mcBansRep;
	}

	public int getMCBanCount() {
		return mcBanList.length;
	}

	public boolean isSilentMuted() {
		return silentMuted;
	}

	public void setSilentMuted(boolean silentMuted) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.silentMuted = silentMuted;
			save();
		}
	}

	public ChatPrefixType getChatPrefixType() {
		return chatPrefixType;
	}

	public void setChatPrefixType(ChatPrefixType chatPrefixType) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.chatPrefixType = chatPrefixType;
			save();
		}
	}

	public void setMCBansExempt(boolean mcBansExempt) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.mcBansExempt = mcBansExempt;
			save();
		}
	}

	public boolean isMCBansExempt() {
		return mcBansExempt;
	}

	public MCBan[] getMCBanList() {
		MCBan[] b = new MCBan[mcBanList.length];
		for (int i = 0; i < b.length; i++) {
			b[i] = mcBanList[i];
		}
		return b;
	}

	public NameHistory[] getNameHistory() {
		NameHistory[] hista = previousNames;
		NameHistory[] hist = new NameHistory[previousNames.length];
		for (int i = 0; i < hist.length; i++) {
			hist[i] = hista[i];
		}
		return hist;
	}

	public void setNameHistory(NameHistory[] newHistory) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			NameHistory[] hist = new NameHistory[newHistory.length];
			for (int i = 0; i < hist.length; i++) {
				hist[i] = newHistory[i];
			}
			previousNames = hist;
			save();
		}
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.nickname = nickname;
			save();
		}
	}

	private static File getPlayerFile(UUID player) {
		File file = new File(BungeeChat.getInstance().getDataFolder(), "playerdata");
		if (!file.isDirectory()) {
			if (file.exists()) {
				file.delete();
			}
			file.mkdirs();
		}
		return new File(file, player.toString().toLowerCase().replaceAll("-", "") + ".json");
	}

	private static File getPlayerFileAtomicSave(UUID player) {
		File file = new File(BungeeChat.getInstance().getDataFolder(), "playerdata");
		if (!file.isDirectory()) {
			if (file.exists()) {
				file.delete();
			}
			file.mkdirs();
		}
		return new File(file, player.toString().toLowerCase().replaceAll("-", "") + ".json.sav");
	}

	private static File getPlayerFileTxt(UUID player) {
		File file = new File(BungeeChat.getInstance().getDataFolder(), "playerdata");
		if (!file.isDirectory()) {
			if (file.exists()) {
				file.delete();
			}
			file.mkdirs();
		}
		return new File(file, player.toString().toLowerCase().replaceAll("-", "") + ".txt");
	}

	private static File getBackupPlayerTxt(UUID player) {
		File file = new File(BungeeChat.getInstance().getDataFolder(), "oldplayerdata");
		if (!file.isDirectory()) {
			if (file.exists()) {
				file.delete();
			}
			file.mkdirs();
		}
		return new File(file, player.toString().toLowerCase().replaceAll("-", "") + ".txt");
	}

	private PlayerAccount() {
		this.player = null;
	}

	public PlayerAccount(UUID player) {
		this.player = player;
		refresh();
	}

	public Mail[] getMail() {
		return mail.toArray(new Mail[mail.size()]);
	}

	public void sendMail(UUID from, String message, boolean alertRecipient, boolean alertSender) {
		long now = System.currentTimeMillis();
		if (from == null) {
			from = BungeeChat.console;
		}
		if (message == null) {
			return;
		}
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			mail.add(new Mail(now, from, player, message));
			save();
		}
		if (alertRecipient) {
			String senderName;
			if (from.equals(BungeeChat.console)) {
				senderName = "Server";
			} else {
				senderName = BungeeChat.getInstance().getPlayerNameHandler().getNameByPlayer(from);
			}
			if (getCurrentRank() == null) {
				if (now - getLastMailAlert() > 43200000L) {
					BungeeChat.getInstance().text(player, "You have new mail waiting for you on CubeBuilders! Login to CubeBuilders in-game and type /mail read to read! Upgrade to Plus to send & receive mail on your phone at https://cubebuilders.net/store (Msg & Data rates may apply)");
				}
			} else {
				BungeeChat.getInstance().text(player, "Mail from " + senderName + ": " + message + " (reply: MAIL " + (senderName.startsWith("*") ? senderName.substring(1) : senderName) + " [message])");
				if (getMail().length >= getMaxMail()) {
					BungeeChat.getInstance().text(player, "Your Mail box is full! To continue receiving mail, please login to CubeBuilders and type /mail clear");
				}
			}
			if (getMail().length >= getMaxMail()) {
				BungeeChat.getInstance().prowl(player, "Mail", "Your Mail box is full! To continue receiving mail, please login to CubeBuilders and type /mail clear");
			}
			BungeeChat.getInstance().prowl(player, "Mail", "Mail from " + senderName + ": " + message);
			ProxiedPlayer recipient = BungeeChat.getInstance().getProxy().getPlayer(player);

			if (recipient != null) {
				final BaseComponent youGotMail = new TextComponent("You received mail! ");
				youGotMail.setColor(ChatColor.GOLD);
				BaseComponent toRead = new TextComponent("To read, type /mail read");
				toRead.setColor(ChatColor.AQUA);
				toRead.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Or click here to read!")}));
				toRead.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mail read"));
				youGotMail.addExtra(toRead);
				recipient.sendMessage(youGotMail);
			}
		}
		if (alertSender) {
			try {
				ProxiedPlayer sender = BungeeChat.getInstance().getProxy().getPlayer(from);
				if (sender != null) {
					BaseComponent ms = new TextComponent("Mail sent to " + BungeeChat.getInstance().getPlayerNameHandler().getNameByPlayer(player) + ": ");
					ms.setColor(ChatColor.GOLD);
					BaseComponent extra = new TextComponent(message);
					extra.setColor(ChatColor.WHITE);
					ms.addExtra(extra);
					sender.sendMessage(ms);
				}
			} catch (Exception e) {
			}
		}
		BungeeChat bc = BungeeChat.getInstance();
		String fromName = from.equals(BungeeChat.console) ? "Server" : bc.getUUIDCache().getNameFromUUID(from);
		bc.logChat("Mail:" + fromName + "/" + Util.uuidToString(from) + ":" + bc.getUUIDCache().getNameFromUUID(player) + "/" + Util.uuidToString(player) + ":" + message);
	}

	public void clearMail() {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			mail.clear();
			save();
		}
	}

	public boolean getChatCensor() {
		return chatCensor;
	}

	public void setChatCensor(boolean censor) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			chatCensor = censor;
			save();
		}
	}

	public boolean getChatCensorSemi() {
		return chatCensorSemi;
	}

	public void setChatCensorSemi(boolean censorSemi) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			chatCensorSemi = censorSemi;
			save();
		}

	}

	public long getLastShownChatCensorMessage() {
		return lastShownChatCensorMessage;
	}

	public void setLastShownChatCensorMessage(long lastShown) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			lastShownChatCensorMessage = lastShown;
			save();
		}
	}

	public boolean getMineChatGiftOnNextLogin() {
		return mineChatGiftOnNextLogin;
	}

	public void setMineChatGiftOnNextLogin(boolean gift) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			mineChatGiftOnNextLogin = gift;
			save();
		}
	}

	public boolean gaveMineChatGift() {
		return gaveMineChatGift;
	}

	public void setGaveMineChatGift(boolean given) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			gaveMineChatGift = given;
			save();
		}
	}

	public long getStreamModeExpiry() {
		return streamModeExpires;
	}

	private void setStreamModeExpiry(long expiry) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			streamModeExpires = expiry;
			save();
		}
	}

	public long getVanishExpiry() {
		return vanishExpires;
	}

	private void setVanishExpiry(long expiry) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			vanishExpires = expiry;
			save();
		}
	}

	public boolean getVanishOnLogin() {
		return vanishOnLogin;
	}

	public void setVanishOnLogin(boolean vanishOnLogin) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.vanishOnLogin = vanishOnLogin;
			save();
		}
	}

	public long getNoSpyExpiry() {
		return noSpyExpires;
	}

	private void setNoSpyExpiry(long expiry) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			noSpyExpires = expiry;
			save();
		}
	}

	public long getNoMsgExpiry() {
		return noMsgExpires;
	}

	private void setNoMsgExpiry(long expiry) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			noMsgExpires = expiry;
			save();
		}
	}

	public long getAntiSpyExpiry() {
		return antiSpyExpires;
	}

	private void setAntiSpyExpiry(long expiry) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			antiSpyExpires = expiry;
			save();
		}
	}

	public long getNoLogExpiry() {
		return noLogExpires;
	}

	private void setNoLogExpiry(long expiry) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			noLogExpires = expiry;
			save();
		}
	}

	public long getSneakyExpiry() {
		return sneakyExpires;
	}

	private void setSneakyExpiry(long expiry) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			sneakyExpires = expiry;
			save();
		}
	}

	public boolean isStreamModeActive() {
		return (streamModeExpires > System.currentTimeMillis());
	}

	public boolean isVanished() {
		return (vanishExpires > System.currentTimeMillis());
	}

	public boolean isNoSpy() {
		return (noSpyExpires > System.currentTimeMillis());
	}

	public boolean isNoMsg() {
		return (noMsgExpires > System.currentTimeMillis());
	}

	public boolean isAntiSpy() {
		return (antiSpyExpires > System.currentTimeMillis());
	}

	public boolean isNoLog() {
		return (noLogExpires > System.currentTimeMillis());
	}

	public boolean isSneaky() {
		return (sneakyExpires > System.currentTimeMillis());
	}

	public void setStreamMode(boolean active) {
		setStreamModeExpiry(active ? (System.currentTimeMillis() + timerLength) : 0L);
	}

	public void setVanished(boolean active) {
		setVanishExpiry(active ? (System.currentTimeMillis() + timerLength) : 0L);
	}

	public void setNoSpy(boolean active) {
		setNoSpyExpiry(active ? (System.currentTimeMillis() + timerLength) : 0L);
	}

	public void setNoMsg(boolean active) {
		setNoMsgExpiry(active ? (System.currentTimeMillis() + timerLength) : 0L);
	}

	public void setAntiSpy(boolean active) {
		setAntiSpyExpiry(active ? (System.currentTimeMillis() + timerLength) : 0L);
	}

	public void setNoLog(boolean active) {
		setNoLogExpiry(active ? (System.currentTimeMillis() + timerLength) : 0L);
	}

	public void setSneaky(boolean active) {
		setSneakyExpiry(active ? (System.currentTimeMillis() + timerLength) : 0L);
	}

	public void addIgnore(UUID ignore) {
		if (ignore == null) {
			return;
		}
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			ignoredUsers.add(ignore);
			save();
		}
	}

	public void removeIgnore(UUID ignore) {
		if (ignore == null) {
			return;
		}
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			ignoredUsers.remove(ignore);
			save();
		}
	}

	public boolean isIgnoring(UUID player) {
		return ignoredUsers.contains(player);
	}

	public void clearIgnores() {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			ignoredUsers.clear();
			save();
		}
	}

	public Set<UUID> getIgnores() {
		return Collections.unmodifiableSet(ignoredUsers);
	}

	public long getLastVoted() {
		return lastVoted;
	}

	public void setLastVoted(long lastVoted) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.lastVoted = lastVoted;
			save();
		}
	}

	public final void refresh() {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
		}
	}

	private void load() {
		if (player == null) {
			return;
		}
		try {
			File fileToRead = getPlayerFile(player);
			if (!fileToRead.exists()) {
				File atomic = getPlayerFileAtomicSave(player);
				if (!atomic.exists()) {
					return;
				} else {
					atomic.renameTo(getPlayerFile(player));
				}
			}
			Gson gson = Serialization.getGson();
			PlayerAccount acc = gson.fromJson(new FileReader(fileToRead), PlayerAccount.class);
			Serialization.copyFields(acc, this);
			return;
		} catch (Exception e) {
		}
		// <editor-fold defaultstate="collapsed" desc="Old Load Code">
		try {
			muteTime = 0L;
			muteExpires = 0L;
			muteReason = null;
			banTime = 0L;
			banExpires = 0L;
			banReason = null;
			chatCensor = true;
			chatCensorSemi = false;
			lastShownChatCensorMessage = 0L;
			nickname = null;
			mcBansPBanned = false;
			mcBansExempt = false;
			mcBansMod = false;
			mcBansRep = 10.0f;
			mineChatGiftOnNextLogin = false;
			gaveMineChatGift = false;
			punishments.clear();
			mail.clear();
			ignoredUsers.clear();
			mojangSkin = null;
			mojangSkinSignature = null;
			customSkin = null;
			customSkinSignature = null;
			streamModeExpires = 0L;
			vanishExpires = 0L;
			vanishOnLogin = false;
			noSpyExpires = 0L;
			noMsgExpires = 0L;
			antiSpyExpires = 0L;
			noLogExpires = 0L;
			sneakyExpires = 0L;
			chatNamePrefix = null;
			chatNameSuffix = null;
			chatNamePrefixSuffixForcedMessage = null;
			chatNamePrefixSuffixForcedUntil = 0L;
			deleteTitleWhenForcedExpires = false;
			prowlApiKey = null;
			lastVoted = 0L;
			staffSpot = null;
			rank = null;
			rankExpires = 0L;
			alertsOn = true;
			messagesOn = true;
			mailOn = true;
			lastMailAlert = 0L;
			customNameBanMessage = null;
			phoneNumber = null;
			fakeLastOnline = 0L;
			ArrayList<NameHistory> nameHistory = new ArrayList<>();
			ArrayList<MCBan> mcBans = new ArrayList<>();
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(getPlayerFileTxt(player))));
				String version = reader.readLine();
				if (version.equals("ProfileVersion=0")) {
					String line;
					while ((line = reader.readLine()) != null) {
						int index = line.indexOf("=");
						if (index == -1) {
							continue;
						}
						String key = line.substring(0, index);
						String val = line.substring(index + 1);
						if (key.equalsIgnoreCase("MuteTime")) {
							muteTime = Long.parseLong(val);
						} else if (key.equalsIgnoreCase("MuteExpires")) {
							muteExpires = Long.parseLong(val);
						} else if (key.equalsIgnoreCase("MuteReason")) {
							muteReason = val;
						} else if (key.equalsIgnoreCase("BanTime")) {
							banTime = Long.parseLong(val);
						} else if (key.equalsIgnoreCase("BanExpires")) {
							banExpires = Long.parseLong(val);
						} else if (key.equalsIgnoreCase("BanReason")) {
							banReason = val;
						} else if (key.equalsIgnoreCase("SilentMuted")) {
							silentMuted = val.equals("1");
						} else if (key.equalsIgnoreCase("ChatPrefixType")) {
							try {
								chatPrefixType = ChatPrefixType.valueOf(val);
							} catch (Exception e) {
							}
						} else if (key.equalsIgnoreCase("Punishment")) {
							String[] pieces = val.split("\\|");
							if (pieces.length != 5) {
								continue;
							}
							Punishment punishment = new Punishment(Punishment.PunishmentAction.fromString(pieces[0]), Long.parseLong(pieces[1]), Long.parseLong(pieces[2]), pieces[3], Util.uuidFromString(pieces[4]), player);
							punishments.add(punishment);
						} else if (key.equalsIgnoreCase("Mail")) {
							String[] pieces = val.split("\\|", 2);
							if (pieces.length != 2) {
								continue;
							}
							Mail myMail = new Mail(0L, Util.uuidFromString(pieces[0]), player, pieces[1]);
							mail.add(myMail);
						} else if (key.equalsIgnoreCase("PrivateMail")) {
							String[] pieces = val.split("\\|", 3);
							if (pieces.length != 3) {
								continue;
							}
							Mail myMail = new Mail(Long.parseLong(pieces[0]), Util.uuidFromString(pieces[1]), player, pieces[2]);
							mail.add(myMail);
						} else if (key.equalsIgnoreCase("ChatCensor")) {
							chatCensor = val.equals("1");
						} else if (key.equalsIgnoreCase("ChatCensorSemi")) {
							chatCensorSemi = val.equals("1");
						} else if (key.equalsIgnoreCase("LastShownChatCensorMessage")) {
							lastShownChatCensorMessage = Long.parseLong(val);
						} else if (key.equalsIgnoreCase("Name")) {
							try {
								String pieces[] = val.split(",");
								String name = pieces[0];
								long time = Long.parseLong(pieces[1]);
								nameHistory.add(new NameHistory(player, name, time));
							} catch (Exception e) {
							}
						} else if (key.equalsIgnoreCase("Nickname")) {
							nickname = val;
						} else if (key.equalsIgnoreCase("MCBansPBanned")) {
							mcBansPBanned = val.equals("1");
						} else if (key.equalsIgnoreCase("MCBansMod")) {
							mcBansMod = val.equals("1");
						} else if (key.equalsIgnoreCase("MCBansRep")) {
							mcBansRep = Float.parseFloat(val);
						} else if (key.equalsIgnoreCase("MCBansExempt")) {
							mcBansExempt = val.equals("1");
						} else if (key.equalsIgnoreCase("MCBan")) {
							try {
								String pieces[] = val.split("\\$");
								String reason = pieces[0];
								String server = pieces[1];
								String prosecutor = pieces[2];
								mcBans.add(new MCBan(player, reason, server, prosecutor));
							} catch (Exception e) {
							}
						} else if (key.equalsIgnoreCase("GiveMineChatGiftOnNextLogin")) {
							mineChatGiftOnNextLogin = val.equals("1");
						} else if (key.equalsIgnoreCase("GaveMineChatGift")) {
							gaveMineChatGift = val.equals("1");
						} else if (key.equalsIgnoreCase("BypassIPBan")) {
							bypassIPBan = val.equals("1");
						} else if (key.equalsIgnoreCase("MojangSkin")) {
							mojangSkin = val;
						} else if (key.equalsIgnoreCase("MojangSkinSignature")) {
							mojangSkinSignature = val;
						} else if (key.equalsIgnoreCase("CustomSkin")) {
							customSkin = val;
						} else if (key.equalsIgnoreCase("CustomSkinSignature")) {
							customSkinSignature = val;
						} else if (key.equalsIgnoreCase("StreamMode")) {
							streamModeExpires = Long.parseLong(val);
						} else if (key.equalsIgnoreCase("VanishMode")) {
							vanishExpires = Long.parseLong(val);
						} else if (key.equalsIgnoreCase("VanishOnLogin")) {
							vanishOnLogin = val.equals("1");
						} else if (key.equalsIgnoreCase("NoSpyMode")) {
							noSpyExpires = Long.parseLong(val);
						} else if (key.equalsIgnoreCase("NoMsgMode")) {
							noMsgExpires = Long.parseLong(val);
						} else if (key.equalsIgnoreCase("AntiSpyMode")) {
							antiSpyExpires = Long.parseLong(val);
						} else if (key.equalsIgnoreCase("NoLogMode")) {
							noLogExpires = Long.parseLong(val);
						} else if (key.equalsIgnoreCase("SneakyMode")) {
							sneakyExpires = Long.parseLong(val);
						} else if (key.equalsIgnoreCase("ChatNamePrefix")) {
							chatNamePrefix = val;
						} else if (key.equalsIgnoreCase("ChatNameSuffix")) {
							chatNameSuffix = val;
						} else if (key.equalsIgnoreCase("ChatNamePrefixSuffixForcedUntil")) {
							chatNamePrefixSuffixForcedUntil = Long.parseLong(val);
						} else if (key.equalsIgnoreCase("ChatNamePrefixSuffixForcedMessage")) {
							chatNamePrefixSuffixForcedMessage = val;
						} else if (key.equalsIgnoreCase("DeleteTitleWhenForcedExpires")) {
							deleteTitleWhenForcedExpires = val.equals("1");
						} else if (key.equalsIgnoreCase("ProwlApiKey")) {
							prowlApiKey = val;
						} else if (key.equalsIgnoreCase("Ignore")) {
							ignoredUsers.add(UUID.fromString(val));
						} else if (key.equalsIgnoreCase("LastVoted")) {
							lastVoted = Long.parseLong(val);
						} else if (key.equalsIgnoreCase("Staff")) {
							staffSpot = val;
						} else if (key.equalsIgnoreCase("Rank")) {
							rank = val;
						} else if (key.equalsIgnoreCase("RankExpires")) {
							rankExpires = Long.parseLong(val);
						} else if (key.equalsIgnoreCase("AlertsOn")) {
							alertsOn = val.equals("1");
						} else if (key.equalsIgnoreCase("MessagesOn")) {
							messagesOn = val.equals("1");
						} else if (key.equalsIgnoreCase("MailOn")) {
							mailOn = val.equals("1");
						} else if (key.equalsIgnoreCase("LastMailAlert")) {
							lastMailAlert = Long.parseLong(val);
						} else if (key.equalsIgnoreCase("CustomNameBanMessage")) {
							customNameBanMessage = val.replace("\\r", "\r").replace("\\n", "\n").replace("\\\\", "\\");
						} else if (key.equalsIgnoreCase("PhoneNumber")) {
							phoneNumber = val;
						} else if (key.equalsIgnoreCase("FakeLastOnline")) {
							fakeLastOnline = Long.parseLong(val);
						}
					}
				}
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (Exception e) {
					}
				}
			}
			previousNames = nameHistory.toArray(new NameHistory[nameHistory.size()]);
			mcBanList = mcBans.toArray(new MCBan[mcBans.size()]);
		} catch (Exception e) {
		}
		// </editor-fold>
	}

	private void save() {
		Gson gson = Serialization.getPrettyGson();
		String json = gson.toJson(this);
		File playerFile = getPlayerFile(player);
		File atomicFile = getPlayerFileAtomicSave(player);
		try (FileWriter writer = new FileWriter(atomicFile)) {
			writer.write(json);
		} catch (Exception e) {
			return;
		}
		copyCreationDate(playerFile, atomicFile);
		playerFile.delete();
		atomicFile.renameTo(playerFile);
		File f = getPlayerFileTxt(player);
		if (f.exists()) {
			File oldStuff = getBackupPlayerTxt(player);
			f.renameTo(oldStuff);
		}
		// <editor-fold defaultstate="collapsed" desc="Old Save Code">
		if (true) {
			return;
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(getPlayerFileTxt(player));
			fos.write("ProfileVersion=0\n".getBytes());
			fos.write(("ChatCensor=" + (chatCensor ? "1" : "0") + "\n").getBytes());
			fos.write(("ChatCensorSemi=" + (chatCensorSemi ? "1" : "0") + "\n").getBytes());
			fos.write(("LastShownChatCensorMessage=" + lastShownChatCensorMessage + "\n").getBytes());
			for (NameHistory nh : previousNames) {
				fos.write(("Name=" + nh.getName() + "," + Long.toString(nh.getTime()) + "\n").getBytes());
			}
			if (nickname != null) {
				fos.write(("Nickname=" + nickname + "\n").getBytes());
			}
			if (bypassIPBan) {
				fos.write("BypassIPBan=1\n".getBytes());
			}
			if (mineChatGiftOnNextLogin) {
				fos.write("GiveMineChatGiftOnNextLogin=1\n".getBytes());
			}
			if (gaveMineChatGift) {
				fos.write("GaveMineChatGift=1\n".getBytes());
			}
			fos.write(("MCBansPBanned=" + (mcBansPBanned ? "1" : "0") + "\n").getBytes());
			fos.write(("MCBansMod=" + (mcBansMod ? "1" : "0") + "\n").getBytes());
			fos.write(("MCBansRep=" + Float.toString(mcBansRep) + "\n").getBytes());
			fos.write(("MCBansExempt=" + (mcBansExempt ? "1" : "0") + "\n").getBytes());
			for (MCBan ban : mcBanList) {
				fos.write(("MCBan=" + (ban.reason) + "$" + (ban.server) + "$" + (ban.prosecutor) + "\n").getBytes());
			}
			if (muteTime != 0L) {
				fos.write(("MuteTime=" + muteTime + "\n").getBytes());
			}
			if (muteExpires != 0L) {
				fos.write(("MuteExpires=" + muteExpires + "\n").getBytes());
			}
			if (muteReason != null) {
				fos.write(("MuteReason=" + muteReason + "\n").getBytes());
			}
			if (banTime != 0L) {
				fos.write(("BanTime=" + banTime + "\n").getBytes());
			}
			if (banExpires != 0L) {
				fos.write(("BanExpires=" + banExpires + "\n").getBytes());
			}
			if (banReason != null) {
				fos.write(("BanReason=" + banReason + "\n").getBytes());
			}
			if (silentMuted) {
				fos.write(("SilentMuted=" + (silentMuted ? "1" : "0") + "\n").getBytes());
			}
			fos.write(("ChatPrefixType=" + chatPrefixType.name() + "\n").getBytes());
			for (Punishment punishment : punishments) {
				fos.write(("Punishment=" + punishment.action.toString() + "|" + punishment.time + "|" + punishment.length + "|" + punishment.reason + "|" + (punishment.issuedBy.toString().toLowerCase().replaceAll("-", "")) + "\n").getBytes());
			}
			for (Mail myMail : mail) {
				fos.write(("PrivateMail=" + myMail.date + "|" + (myMail.from.toString().toLowerCase().replaceAll("-", "")) + "|" + myMail.message + "\n").getBytes());
			}
			if (mojangSkin != null) {
				fos.write(("MojangSkin=" + mojangSkin + "\n").getBytes());
			}
			if (mojangSkinSignature != null) {
				fos.write(("MojangSkinSignature=" + mojangSkinSignature + "\n").getBytes());
			}
			if (customSkin != null) {
				fos.write(("CustomSkin=" + customSkin + "\n").getBytes());
			}
			if (customSkinSignature != null) {
				fos.write(("CustomSkinSignature=" + customSkinSignature + "\n").getBytes());
			}
			if (streamModeExpires > 0L) {
				fos.write(("StreamMode=" + streamModeExpires + "\n").getBytes());
			}
			if (vanishExpires > 0L) {
				fos.write(("VanishMode=" + vanishExpires + "\n").getBytes());
			}
			if (vanishOnLogin) {
				fos.write(("VanishOnLogin=" + (vanishOnLogin ? "1" : "0") + "\n").getBytes());
			}
			if (noSpyExpires > 0L) {
				fos.write(("NoSpyMode=" + noSpyExpires + "\n").getBytes());
			}
			if (noMsgExpires > 0L) {
				fos.write(("NoMsgMode=" + noMsgExpires + "\n").getBytes());
			}
			if (antiSpyExpires > 0L) {
				fos.write(("AntiSpyMode=" + antiSpyExpires + "\n").getBytes());
			}
			if (noLogExpires > 0L) {
				fos.write(("NoLogMode=" + noLogExpires + "\n").getBytes());
			}
			if (sneakyExpires > 0L) {
				fos.write(("SneakyMode=" + sneakyExpires + "\n").getBytes());
			}
			if (chatNamePrefix != null) {
				fos.write(("ChatNamePrefix=" + chatNamePrefix + "\n").getBytes());
			}
			if (chatNameSuffix != null) {
				fos.write(("ChatNameSuffix=" + chatNameSuffix + "\n").getBytes());
			}
			if (chatNamePrefixSuffixForcedUntil != 0L) {
				fos.write(("ChatNamePrefixSuffixForcedUntil=" + chatNamePrefixSuffixForcedUntil + "\n").getBytes());
			}
			if (chatNamePrefixSuffixForcedMessage != null) {
				fos.write(("ChatNamePrefixSuffixForcedMessage=" + chatNamePrefixSuffixForcedMessage + "\n").getBytes());
			}
			if (deleteTitleWhenForcedExpires) {
				fos.write(("DeleteTitleWhenForcedExpires=" + (deleteTitleWhenForcedExpires ? "1" : "0") + "\n").getBytes());
			}
			if (prowlApiKey != null) {
				fos.write(("ProwlApiKey=" + prowlApiKey + "\n").getBytes());
			}
			for (UUID ignoredUser : ignoredUsers) {
				fos.write(("Ignore=" + ignoredUser.toString() + "\n").getBytes());
			}
			if (lastVoted > 0L) {
				fos.write(("LastVoted=" + lastVoted + "\n").getBytes());
			}
			if (staffSpot != null) {
				fos.write(("Staff=" + staffSpot + "\n").getBytes());
			}
			if (rank != null) {
				fos.write(("Rank=" + rank + "\n").getBytes());
			}
			if (rankExpires > 0L) {
				fos.write(("RankExpires=" + rankExpires + "\n").getBytes());
			}
			fos.write(("AlertsOn=" + (alertsOn ? "1" : "0") + "\n").getBytes());
			fos.write(("MessagesOn=" + (messagesOn ? "1" : "0") + "\n").getBytes());
			fos.write(("MailOn=" + (mailOn ? "1" : "0") + "\n").getBytes());
			if (lastMailAlert > 0) {
				fos.write(("LastMailAlert=" + lastMailAlert + "\n").getBytes());
			}
			if (customNameBanMessage != null) {
				fos.write(("CustomNameBanMessage=" + (customNameBanMessage.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n")) + "\n").getBytes());
			}
			if (phoneNumber != null) {
				fos.write(("PhoneNumber=" + phoneNumber + "\n").getBytes());
			}
			if (fakeLastOnline > 0L) {
				fos.write(("FakeLastOnline=" + fakeLastOnline + "\n").getBytes());
			}
		} catch (Exception e) {
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
		// </editor-fold>
	}

	public void forceResave() {
		synchronized (BungeeChat.getInstance().fsLock) {
			save();
		}
	}

	@Deprecated
	public Punishment[] getPunishments() {
		return punishments.toArray(new Punishment[punishments.size()]);
	}

	private void updateTimer(long now, Supplier<Long> get, Consumer<Long> set, Consumer<Boolean> set2) {
		long exp = get.get();
		if (exp != 0L) {
			long timeUntilExpire = exp - now;
			if (timeUntilExpire < 0L) {
				set.accept(0L);
			} else if (timeUntilExpire < timerLength) {
				set2.accept(true);
			}
		}
	}

	public void updateTimers() {
		long now = System.currentTimeMillis();
		updateTimer(now, this::getStreamModeExpiry, this::setStreamModeExpiry, this::setStreamMode);
		updateTimer(now, this::getVanishExpiry, this::setVanishExpiry, this::setVanished);
		updateTimer(now, this::getNoSpyExpiry, this::setNoSpyExpiry, this::setNoSpy);
		updateTimer(now, this::getNoMsgExpiry, this::setNoMsgExpiry, this::setNoMsg);
		updateTimer(now, this::getAntiSpyExpiry, this::setAntiSpyExpiry, this::setAntiSpy);
		updateTimer(now, this::getNoLogExpiry, this::setNoLogExpiry, this::setNoLog);
		updateTimer(now, this::getSneakyExpiry, this::setSneakyExpiry, this::setSneaky);
	}

	public void setChatNamePrefix(String chatNamePrefix) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.chatNamePrefix = chatNamePrefix;
			save();
		}
	}

	public String getChatNamePrefix() {
		return chatNamePrefix;
	}

	public void setChatNameSuffix(String chatNameSuffix) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.chatNameSuffix = chatNameSuffix;
			save();
		}
	}

	public String getChatNameSuffix() {
		return chatNameSuffix;
	}

	public void setChatNamePrefixSuffixForcedUntil(long chatNamePrefixSuffixForcedUntil) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.chatNamePrefixSuffixForcedUntil = chatNamePrefixSuffixForcedUntil;
			save();
		}
	}

	public long getChatNamePrefixSuffixForcedUntil() {
		return chatNamePrefixSuffixForcedUntil;
	}

	public void setChatNamePrefixSuffixForcedMessage(String chatNamePrefixSuffixForcedMessage) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.chatNamePrefixSuffixForcedMessage = chatNamePrefixSuffixForcedMessage;
			save();
		}
	}

	public String getChatNamePrefixSuffixForcedMessage() {
		return chatNamePrefixSuffixForcedMessage;
	}

	public long getRankExpires() {
		return rankExpires;
	}

	public String getStaffSpot() {
		return staffSpot;
	}

	public String getRank() {
		return rank;
	}

	public String getCurrentRank() {
		return rankExpires > System.currentTimeMillis() ? rank : null;
	}

	public void updateFromCBUser(CBUser user) {
		setNameHistory(user.getUserData().nameHistory);
		UserDonation latestRank = user.getUserData().getLatestRank();
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			staffSpot = user.getUserData().staffRank;
			if (latestRank != null) {
				rank = latestRank.rank;
				rankExpires = latestRank.endTime;
			}
			save();
		}
	}

	public void setAlertsOn(boolean alertsOn) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.alertsOn = alertsOn;
			save();
		}
	}

	public void setMessagesOn(boolean messagesOn) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.messagesOn = messagesOn;
			save();
		}
	}

	public void setMailOn(boolean mailOn) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.mailOn = mailOn;
			save();
		}
	}

	public boolean isAlertsOn() {
		return alertsOn;
	}

	public boolean isMessagesOn() {
		return messagesOn;
	}

	public boolean isMailOn() {
		return mailOn;
	}

	public void setLastMailAlert(long lastMailAlert) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.lastMailAlert = lastMailAlert;
			save();
		}
	}

	public long getLastMailAlert() {
		return lastMailAlert;
	}

	public int getMaxMail() {
		if (getCurrentRank() == null) {
			return 20;
		} else {
			return 60;
		}
	}

	public UUID getPlayerUUID() {
		return player;
	}

	public String getCustomNameBanMessage() {
		return customNameBanMessage;
	}

	public void setCustomNameBanMessage(String customNameBanMessage) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.customNameBanMessage = customNameBanMessage;
			save();
		}
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getFormattedPhoneNumber() {
		if (phoneNumber.startsWith("+1")) {
			return "+1 (" + phoneNumber.substring(2, 5) + ") " + phoneNumber.substring(5, 8) + "-" + phoneNumber.substring(8);
		}
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.phoneNumber = phoneNumber;
			save();
		}
	}

	public long getFakeLastOnline() {
		return fakeLastOnline;
	}

	public void setFakeLastOnline(long fakeLastOnline) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.fakeLastOnline = fakeLastOnline;
			save();
		}
	}

	public void setDisableCapsFilter(boolean disableCapsFilter) {
		synchronized (BungeeChat.getInstance().fsLock) {
			load();
			this.disableCapsFilter = disableCapsFilter;
			save();
		}
	}

	public boolean getDisableCapsFilter() {
		return disableCapsFilter;
	}

	public enum ChatPrefixType {

		AUTO, CLASSIC, COMPACT;
	}
}
