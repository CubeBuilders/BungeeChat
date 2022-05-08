package hk.siggi.bungeecord.bungeechat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hk.siggi.bungeecord.bungeechat.chat.ChatController;
import hk.siggi.bungeecord.bungeechat.chat.handler.ChatHandler;
import hk.siggi.bungeecord.bungeechat.chat.handler.PublicChatHandler;
import hk.siggi.bungeecord.bungeechat.commands.server.CommandImpersonate;
import hk.siggi.bungeecord.bungeechat.geolocation.Geolocation;
import hk.siggi.bungeecord.bungeechat.permissionloader.PermissionLoader;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount.ChatPrefixType;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.processChat;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.unify;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import net.cubebuilders.user.CBUser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.packet.Chat;

public class PlayerSession {

	public final InetSocketAddress address;
	private UUID uuid;
	public boolean alertedMCBans;
	public String teamSpeakVerifyUID = null;
	public String teamSpeakVerifyCode = null;
	public boolean teamSpeakOnline = false;
	public CBUser user = null;
	public long recentMute = 0L;
	public long recentBan = 0L;
	public LoginResult.Property skinProperty = null;
	public long loginTime = 0L;
	public long ontimeOnLogin = 0L;
	public long timeInLast2Weeks = 0L;
	public volatile boolean showingAfkTimer = false;
	public volatile int afkKickTimer = 86400;
	public volatile int afkTime = 0;
	private WeakReference<ProxiedPlayer> playerRef = null;
	public String clientBrand = "";
	public boolean didMineChatTeleport = false;
	public boolean isMineChat = false;

	private final List<Long> chatTimes = new LinkedList<>();

	public void addChatTime() {
		chatTimes.add(System.currentTimeMillis());
	}

	public int getChatCount(long maxAge) {
		long now = System.currentTimeMillis();
		long oldest = now - maxAge;
		long delete = now - 60000L;
		try {
			int count = 0;
			for (Iterator<Long> it = chatTimes.iterator(); it.hasNext();) {
				long t = it.next();
				if (t < delete) {
					it.remove();
				} else if (t >= oldest) {
					count += 1;
				}
			}
			return count;
		} catch (Exception e) {
		}
		return 0;
	}

	public boolean invalidatedOnTime;
	public Geolocation geolocation;

	public String changeEmailTo = null;
	public boolean emailNeedsConfirmation = false;
	public boolean didResendEmail = false;

	public String discordStatus = null;

	public UUID getUUID() {
		return uuid;
	}

	public void setUUID(UUID uuid, CommandImpersonate.ImpersonationLock lock) {
		if (lock == null) {
			throw new NullPointerException();
		}
		this.uuid = uuid;
		account = null;
	}

	private ChatHandler chatHandler = null;
	boolean loggingOut = false;

	public long lastSpeedReport = 0L;
	public long highSpeedXZ = 0L;
	public long highSpeedY = 0L;
	public boolean groupTabBypass;

	public boolean isConfirmedFullClient() {
		if (clientBrand.isEmpty()) {
			return false;
		} else if (clientBrand.equalsIgnoreCase("MineChat")) {
			return false;
		}
		return true;
	}

	public ChatPrefixType getChatPrefixType(ChatPrefixType cpt) {
		if (cpt != ChatPrefixType.AUTO) {
			return cpt;
		}
		if (ontimeOnLogin > 14400000L) {
			return ChatPrefixType.COMPACT;
		}
		return ChatPrefixType.CLASSIC;
	}

	public ProxiedPlayer getPlayer() {
		if (playerRef == null) {
			ProxiedPlayer p = BungeeChat.getInstance().getProxy().getPlayer(uuid);
			if (p != null) {
				if (p.getAddress().equals(address)) {
					playerRef = new WeakReference<>(p);
				}
			}
		}
		if (playerRef == null) {
			return null;
		}
		return playerRef.get();
	}

	public boolean isValid() {
		ProxiedPlayer p = getPlayer();
		if (p != null) {
			return BungeeChat.getInstance().getProxy().getPlayer(p.getUniqueId()) == p;
		}
		if (playerRef == null) {
			return System.currentTimeMillis() - created <= 120000L;
		} else {
			return false;
		}
	}

	private final long created;

	public PlayerSession(InetSocketAddress address, UUID uuid) {
		this.address = address;
		created = System.currentTimeMillis();
		this.uuid = uuid;
	}

	public void sendHotbarMessage(String message) {
		sendHotbarMessage(TextComponent.fromLegacyText(message));
	}

	public void sendHotbarMessage(BaseComponent message) {
		sendHotbarMessageRaw(ComponentSerializer.toString(message));
	}

	public void sendHotbarMessage(BaseComponent... message) {
		sendHotbarMessageRaw(ComponentSerializer.toString(message));
	}

	public void sendHotbarMessage0(BaseComponent message) {
		sendHotbarMessageRaw0(ComponentSerializer.toString(message));
	}

	public void sendHotbarMessage0(BaseComponent... message) {
		sendHotbarMessageRaw0(ComponentSerializer.toString(message));
	}

	public void sendHotbarMessageRaw(String message) {
		lastHotbar = System.currentTimeMillis();
		sendHotbarMessageRaw0(message);
	}

	private void sendHotbarMessageRaw0(String message) {
		Chat c = new Chat(message);
		c.setPosition((byte) 2);
		ProxiedPlayer p = getPlayer();
		if (p != null) {
			p.unsafe().sendPacket(c);
		}
	}

	public void playSound(String soundName, float volume, float pitch, int delay) {
		ProxiedPlayer p = getPlayer();
		Server server = p.getServer();
		try {
			ByteArrayOutputStream dataS = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(dataS);
			data.writeUTF("Sound");
			data.writeUTF(soundName);
			data.writeFloat(volume);
			data.writeFloat(pitch);
			data.writeInt(delay);
			data.flush();
			server.sendData("BungeeCord", dataS.toByteArray());
		} catch (Exception e) {
		}
	}

	public void lightningStrike() {
		ProxiedPlayer p = getPlayer();
		Server server = p.getServer();
		try {
			ByteArrayOutputStream dataS = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(dataS);
			data.writeUTF("LightningStrike");
			data.flush();
			server.sendData("BungeeCord", dataS.toByteArray());
		} catch (Exception e) {
		}
	}

	public void openReporter() {
		ProxiedPlayer p = getPlayer();
		Server server = p.getServer();
		try {
			ByteArrayOutputStream dataS = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(dataS);
			data.writeUTF("OpenReporter");
			data.flush();
			server.sendData("BungeeCord", dataS.toByteArray());
		} catch (Exception e) {
		}
	}

	public void openPunisher(UUID targetPlayer, boolean allowTroll, boolean allowMute, boolean allowBan) {
		ProxiedPlayer p = getPlayer();
		Server server = p.getServer();
		try {
			ByteArrayOutputStream dataS = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(dataS);
			data.writeUTF("OpenPunisher");
			data.writeUTF(targetPlayer.toString());

			String name = BungeeChat.getInstance().getUUIDCache().getNameFromUUID(targetPlayer);
			if (name == null) {
				name = targetPlayer.toString();
			}
			data.writeUTF(name);

			String skin = null;
			String skinSignature = null;
			PlayerAccount info = BungeeChat.getInstance().getPlayerInfo(targetPlayer);
			if (info != null) {
				skin = info.getCustomSkin();
				skinSignature = info.getCustomSkinSignature();
				if (skin == null || skinSignature == null) {
					skin = info.getMojangSkin();
					skinSignature = info.getMojangSkinSignature();
				}
			}
			data.writeUTF(skin == null ? "null" : skin);
			data.writeUTF(skinSignature == null ? "null" : skinSignature);

			data.writeBoolean(allowTroll);
			data.writeBoolean(allowMute);
			data.writeBoolean(allowBan);
			data.flush();
			server.sendData("BungeeCord", dataS.toByteArray());
		} catch (Exception e) {
		}
	}

	public void openPunishmentSetup(UUID targetPlayer, String offence, String preselectedType,
			long preselectedLength, boolean allowTroll, boolean allowMute, boolean allowBan) {
		ProxiedPlayer p = getPlayer();
		Server server = p.getServer();
		try {
			ByteArrayOutputStream dataS = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(dataS);
			data.writeUTF("OpenPunishmentSetup");
			data.writeUTF(targetPlayer.toString());

			String name = BungeeChat.getInstance().getUUIDCache().getNameFromUUID(targetPlayer);
			if (name == null) {
				name = targetPlayer.toString();
			}
			data.writeUTF(name);

			String skin = null;
			String skinSignature = null;
			PlayerAccount info = BungeeChat.getInstance().getPlayerInfo(targetPlayer);
			if (info != null) {
				skin = info.getCustomSkin();
				skinSignature = info.getCustomSkinSignature();
				if (skin == null || skinSignature == null) {
					skin = info.getMojangSkin();
					skinSignature = info.getMojangSkinSignature();
				}
			}
			data.writeUTF(skin == null ? "null" : skin);
			data.writeUTF(skinSignature == null ? "null" : skinSignature);

			data.writeUTF(offence);
			data.writeUTF(preselectedType);
			data.writeLong(preselectedLength);

			data.writeBoolean(allowTroll);
			data.writeBoolean(allowMute);
			data.writeBoolean(allowBan);
			data.flush();
			server.sendData("BungeeCord", dataS.toByteArray());
		} catch (Exception e) {
		}
	}

	private PlayerAccount account = null;

	public PlayerAccount getPlayerInfo() {
		if (account == null) {
			ProxiedPlayer p = getPlayer();
			if (p != null) {
				account = BungeeChat.getInstance().getPlayerInfo(p.getUniqueId());
			}
		}
		return account;
	}

	public void tick() {
		// TICK ONCE PER SECOND! This isn't Minecraft server, remember? It's BungeeCord.
		// Tick length was decided by Siggi.

		long now = System.currentTimeMillis();

		ProxiedPlayer p = getPlayer();
		if (p == null) {
			return;
		}

		BungeeChat bc = BungeeChat.getInstance();
		PlayerAccount playerInfo = bc.getPlayerInfo(p.getUniqueId());

		if (vanishCheck > 0) {
			vanishCheck -= 1;
			if (vanishCheck == 0) {
				boolean vanishPermission = getPlayer().hasPermission("hk.siggi.bungeechat.vanish");
				if (!vanishPermission) {
					if (bc.isVanished(p)) {
						bc.setVanished(p, false);
						fakeJoin(null);
					}
					if (playerInfo.getVanishOnLogin()) {
						playerInfo.setVanishOnLogin(false);
					}
				}
			}
		}

		if (afkTime == 0 && showingAfkTimer) {
			showingAfkTimer = false;
			TextComponent a = new TextComponent("");
			TextComponent wb = new TextComponent("§6Welcome back!");
			wb.setColor(ChatColor.GOLD);
			a.addExtra(wb);
			sendHotbarMessage(a);
		}

		afkTime += 1;

		int timeToAFKKick = afkKickTimer - afkTime;
		if ((afkTime >= 120 && timeToAFKKick <= 3600) || timeToAFKKick <= 60) {
			int seconds = timeToAFKKick % 60;
			int minutes = (timeToAFKKick - seconds) / 60;
			boolean playSound = false;
			boolean playSound2 = false;
			if (timeToAFKKick == 120 || timeToAFKKick == 60
					|| timeToAFKKick == 30 || timeToAFKKick == 15
					|| timeToAFKKick == 10 || timeToAFKKick <= 7) {
				playSound = true;
			}
			if (timeToAFKKick <= 3) {
				playSound2 = true;
			}
			if (timeToAFKKick < 0) {
				TextComponent kickedForIdling = new TextComponent("");
				TextComponent msg = new TextComponent("You have been kicked for idling.");
				kickedForIdling.addExtra(msg);
				getPlayer().disconnect(kickedForIdling);
			} else {
				showingAfkTimer = true;
				kickMessage(minutes + ":" + (seconds < 10 ? "0" : "") + seconds, playSound, playSound2);
			}
		}

		if (!showingAfkTimer && now - lastHotbar > 3500L) {
			TextComponent a = new TextComponent("");
			Consumer<List<BaseComponent>> addComponents = (components) -> {
				List<BaseComponent> ex = a.getExtra();
				if (ex != null && !ex.isEmpty()) {
					TextComponent aa = new TextComponent(ChatColor.YELLOW + " | ");
					aa.setColor(ChatColor.YELLOW);
					a.addExtra(aa);
				}
				for (BaseComponent component : components) {
					a.addExtra(component);
				}
			};
			Consumer<BaseComponent> addComponent = (component) -> {
				addComponents.accept(Arrays.asList(new BaseComponent[]{component}));
			};
			ChatHandler handler = getChatHandler();
			if (!(handler instanceof PublicChatHandler)) {
				List<BaseComponent> display = handler.getDisplay();
				addComponents.accept(display);
			}
			if (getPlayerInfo().isVanished()) {
				TextComponent v = new TextComponent(ChatColor.GREEN + "Vanished");
				v.setColor(ChatColor.GREEN);
				addComponent.accept(v);
			}
			for (Map.Entry<String, String> hotbarMessage : hotbarMessages.entrySet()) {
				TextComponent ee = new TextComponent(ChatColor.GREEN + hotbarMessage.getValue());
				ee.setColor(ChatColor.GREEN);
				addComponent.accept(ee);
			}
			for (Map.Entry<String, String> hotbarMessage : bungeeHotbarMessages.entrySet()) {
				TextComponent ee = new TextComponent(ChatColor.GREEN + hotbarMessage.getValue());
				ee.setColor(ChatColor.GREEN);
				addComponent.accept(ee);
			}
			boolean emptyHotbar = a.getExtra().isEmpty();
			if (!emptyHotbar || !lastEmptyHotbar) {
				sendHotbarMessage0(a);
			}
			lastEmptyHotbar = emptyHotbar;
		} else {
			lastEmptyHotbar = true;
		}

		getPlayerInfo().updateTimers();

		setupAfterLogin();
	}

	private int vanishCheck = 0;
	private boolean didSetupAfterLogin = false;

	private void setupAfterLogin() {
		if (loggingOut || didSetupAfterLogin) {
			return;
		}
		didSetupAfterLogin = true;
		ProxiedPlayer p = getPlayer();
		if (p == null) {
			didSetupAfterLogin = false;
			return;
		}
		PlayerAccount pi = getPlayerInfo();
		if (pi == null) {
			didSetupAfterLogin = false;
			return;
		}
		vanishCheck = 3;
		if (pi.getVanishOnLogin()) {
			BungeeChat.getInstance().setVanished(p, true);
		} else {
			BungeeChat.getInstance().setVanished(p, pi.isVanished());
		}
		Server server = p.getServer();
		if (server == null) {
			didSetupAfterLogin = false;
			return;
		}
		ServerInfo info = server.getInfo();
		if (info != null) {
			travelTo(info);
		}
	}

	private void kickMessage(String timeLeftStr, boolean sound1, boolean sound2) {
		TextComponent textComponent = new TextComponent("");
		TextComponent timeUntilYouAreKicked = new TextComponent("§6Time left before you are kicked for idling: ");
		timeUntilYouAreKicked.setColor(ChatColor.GOLD);
		TextComponent timeLeft = new TextComponent("§b" + timeLeftStr);
		timeLeft.setColor(ChatColor.AQUA);
		textComponent.addExtra(timeUntilYouAreKicked);
		textComponent.addExtra(timeLeft);
		sendHotbarMessage(textComponent);
		if (sound1) {
			playSound("BLOCK_NOTE_BLOCK_PLING", 1.0f, 0.5f, 0);
		}
		if (sound2) {
			playSound("BLOCK_NOTE_BLOCK_PLING", 1.0f, 0.5f, 10);
		}
	}

	private ChatHandler nullHandler() {
		return new PublicChatHandler(BungeeChat.getInstance().getChatController());
	}

	public ChatHandler getChatHandler() {
		if (chatHandler == null) {
			chatHandler = nullHandler();
		}
		return chatHandler;
	}

	public void setChatHandler(ChatHandler chatHandler, boolean tellPlayer) {
		if (chatHandler == null) {
			chatHandler = nullHandler();
		}
		this.chatHandler = chatHandler;
		if (tellPlayer) {
			ProxiedPlayer p = playerRef.get();
			if (p == null) {
				return;
			}
			BungeeChat bc = BungeeChat.getInstance();
			List<BaseComponent> msg1 = new ArrayList<>();
			msg1.addAll(processChat(null, "&9Your default chat is now "));
			msg1.addAll(chatHandler.getDisplay());
			msg1.addAll(processChat(null, "&9."));
			MessageSender.sendMessage(p, unify(msg1));
			if (!(chatHandler instanceof PublicChatHandler)) {
				MessageSender.sendMessage(p, "&9To return to Public chat, type &6/pub&9.");
			}
		}
	}

	public long getPlayTime() {
		return ontimeOnLogin + (System.currentTimeMillis() - loginTime);
	}

	private long lastHotbar = 0L;
	private boolean lastEmptyHotbar = true;

	public long lastCustomHotbarMessage() {
		return lastHotbar;
	}

	public boolean allowHotbarMessage() {
		return !showingAfkTimer;
	}

	private ServerInfo currentServer = null;

	public void travelTo(ServerInfo newServer) {
		publicChatGroup = null;
		setupAfterLogin();
		ServerInfo oldServer = currentServer;
		this.currentServer = newServer;
		PlayerAccount pi = getPlayerInfo();
		if (!pi.isVanished() && !pi.isSneaky()) {
			travelMessage(oldServer, newServer);
		}
	}

	public void fakeJoin(ServerInfo info) {
		travelMessage(info, false, currentServer, true);
	}

	public void fakeLeave(ServerInfo info) {
		travelMessage(currentServer, true, info, false);
	}

	private void travelMessage(ServerInfo serverFrom, ServerInfo serverTo) {
		travelMessage(serverFrom, true, serverTo, true);
	}

	private void travelMessage(ServerInfo serverFrom, boolean sendToFrom, ServerInfo serverTo, boolean sendToTo) {
		ProxiedPlayer p = getPlayer();
		if (p == null) {
			return;
		}
		String serverFromId = serverFrom == null ? null : serverFrom.getName();
		String serverToId = serverTo == null ? null : serverTo.getName();
		String serverFromName = serverNameFor(serverFromId);
		String serverToName = serverNameFor(serverToId);
		if (Objects.equals(serverFromName, serverToName)) {
			return;
		}
		UUID myUuid = p.getUniqueId();
		PlayerAccount myAccount = BungeeChat.getInstance().getPlayerInfo(myUuid);
		boolean imIgnoreExempt = p.hasPermission("hk.siggi.bungeechat.ignoreexempt");
		if (BungeeChat.getInstance().isGlobalPublicChat()) {
			if (serverToName == null || serverToName.equals("the game")) serverToName = serverToId;
			StringBuilder sb = new StringBuilder();
			sb.append(p.getName());
			if (serverToName == null) {
				sb.append(" left.");
			} else {
				if (serverFrom == null) {
					sb.append(" joined in ");
				} else {
					sb.append(" went to ");
				}
				sb.append(serverToName);
				sb.append(".");
			}
			TextComponent msg = new TextComponent("");
			TextComponent m = new TextComponent(sb.toString());
			m.setColor(ChatColor.AQUA);
			msg.addExtra(m);
			MessageSender.sendMessage(p, msg);
			for (ProxiedPlayer pl : BungeeChat.getInstance().getProxy().getPlayers()) {
				if (pl == p) {
					continue;
				}
				if (ChatController.isHidden(
						myAccount.isIgnoring(pl.getUniqueId()),
						imIgnoreExempt,
						BungeeChat.getInstance().getPlayerInfo(pl.getUniqueId()).isIgnoring(myUuid),
						pl.hasPermission("hk.siggi.bungeechat.ignoreexempt")
				)) {
					continue;
				}
				MessageSender.sendMessage(pl, msg);
			}
			return;
		}
		if (sendToFrom && serverFrom != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(p.getName());
			if (serverTo == null) {
				sb.append(" left.");
			} else {
				if (serverToName.equals("the game")) {
					sb.append(" left.");
				} else {
					sb.append(" went to ");
					sb.append(serverToName);
					sb.append(".");
				}
			}
			TextComponent msg = new TextComponent("");
			TextComponent m = new TextComponent(sb.toString());
			m.setColor(ChatColor.AQUA);
			msg.addExtra(m);
			for (ProxiedPlayer pl : serverFrom.getPlayers()) {
				if (pl == p) {
					continue;
				}
				if (ChatController.isHidden(
						myAccount.isIgnoring(pl.getUniqueId()),
						imIgnoreExempt,
						BungeeChat.getInstance().getPlayerInfo(pl.getUniqueId()).isIgnoring(myUuid),
						pl.hasPermission("hk.siggi.bungeechat.ignoreexempt")
				)) {
					continue;
				}
				MessageSender.sendMessage(pl, msg);
			}
		}
		if (sendToTo && serverTo != null) {
			StringBuilder sb = new StringBuilder();
			sb.append(p.getName());
			if (serverFrom == null) {
				sb.append(" joined.");
			} else {
				if (serverFromName.equals("the game")) {
					sb.append(" joined.");
				} else {
					sb.append(" arrived from ");
					sb.append(serverFromName);
					sb.append(".");
				}
			}
			TextComponent msg = new TextComponent("");
			TextComponent m = new TextComponent(sb.toString());
			m.setColor(ChatColor.AQUA);
			msg.addExtra(m);
			MessageSender.sendMessage(p, msg);
			for (ProxiedPlayer pl : serverTo.getPlayers()) {
				if (pl == p) {
					continue;
				}
				if (ChatController.isHidden(
						myAccount.isIgnoring(pl.getUniqueId()),
						imIgnoreExempt,
						BungeeChat.getInstance().getPlayerInfo(pl.getUniqueId()).isIgnoring(myUuid),
						pl.hasPermission("hk.siggi.bungeechat.ignoreexempt")
				)) {
					continue;
				}
				MessageSender.sendMessage(pl, msg);
			}
		}
	}

	public void loggingOut() {
		loggingOut = true;
		travelTo(null);
	}

	private String serverNameFor(String serverId) {
		if (serverId == null) {
			return null;
		}
		if (serverId.startsWith("hub")) {
			return "the Lobby";
		}
		if (serverId.startsWith("factions")) {
			return "Factions" + (serverId.length() > 8 ? (" " + serverId.substring(8)) : (""));
		}
		if (serverId.startsWith("survival")) {
			return "Survival";
		}
		if (serverId.startsWith("creative")) {
			return "Creative";
		}
		if (serverId.startsWith("skyblockold")) {
			return "Skyblock Old";
		}
		if (serverId.startsWith("skyblock")) {
			return "Skyblock";
		}
		if (serverId.startsWith("skins")) {
			return "the Skin Wardrobe";
		}
		if (serverId.startsWith("minigames")) {
			return "the Minigames Lobby";
		}
		if (serverId.startsWith("colorshuffle")) {
			return "Color Shuffle" + (serverId.length() > 12 ? (" " + serverId.substring(12)) : (""));
		}
		if (serverId.startsWith("ffapvp")) {
			return "FFA PvP" + (serverId.length() > 6 ? (" " + serverId.substring(6)) : (""));
		}
		if (serverId.startsWith("mobarena")) {
			return "Mob Arena" + (serverId.length() > 8 ? (" " + serverId.substring(8)) : (""));
		}
		if (serverId.startsWith("quake")) {
			return "Quake" + (serverId.length() > 5 ? (" " + serverId.substring(5)) : (""));
		}
		if (serverId.startsWith("splurge")) {
			return "Splurge" + (serverId.length() > 7 ? (" " + serverId.substring(7)) : (""));
		}
		if (serverId.startsWith("survivalgames")) {
			return "Survival Games" + (serverId.length() > 13 ? (" " + serverId.substring(13)) : (""));
		}
		return "the game";
	}

	public void showCensorMessage() {
		ProxiedPlayer p = getPlayer();
		PlayerAccount playerInfo = getPlayerInfo();
		if (p == null || playerInfo == null) {
			return;
		}
		long now = System.currentTimeMillis();
		if ((now - playerInfo.getLastShownChatCensorMessage()) > (3600000L) && playerInfo.getChatCensor()) {
			playerInfo.setLastShownChatCensorMessage(now);
			TextComponent msg = new TextComponent("");
			TextComponent attentionGrabber = new TextComponent("HEY");
			TextComponent hey = new TextComponent(" Hey there! If you're one of those kids who love using curse words, try this command: ");
			TextComponent command = new TextComponent("/censor off");
			attentionGrabber.setColor(ChatColor.YELLOW);
			attentionGrabber.setObfuscated(true);
			hey.setColor(ChatColor.GOLD);
			command.setColor(ChatColor.AQUA);
			msg.addExtra(attentionGrabber);
			msg.addExtra(hey);
			msg.addExtra(command);
			MessageSender.sendMessage(p, msg);
		}
	}

	public String[] getFakeGroups() {
		return fakeGroups;
	}
	private String[] fakeGroups = null;

	public void setFakeGroups(String[] groups) {
		this.fakeGroups = groups;
	}

	private String publicChatGroup = null;

	public void setPublicChatGroup(String group) {
		this.publicChatGroup = group;
	}

	public String getPublicChatGroup() {
		try {
			if (publicChatGroup != null) {
				return publicChatGroup;
			}
			String serverName = getPlayer().getServer().getInfo().getName();
			String serverGroup = BungeeChat.getInstance().getServerPublicChatGroupController().getGroup(serverName);
			if (serverGroup != null) {
				return serverGroup;
			}
			return serverName;
		} catch (Exception e) {
			return null;
		}
	}

	private final Map<String, String> hotbarMessages = new LinkedHashMap<>();
	private final Map<String, String> bungeeHotbarMessages = new LinkedHashMap<>();

	public void addHotbar(String id, String message) {
		hotbarMessages.put(id, message);
	}

	public void removeHotbar(String id) {
		hotbarMessages.remove(id);
	}

	public void clearHotbar() {
		hotbarMessages.clear();
	}

	public void addBungeeHotbar(String id, String message) {
		bungeeHotbarMessages.put(id, message);
	}

	public void removeBungeeHotbar(String id) {
		bungeeHotbarMessages.remove(id);
	}

	public void clearBungeeHotbar() {
		bungeeHotbarMessages.clear();
	}

	private PermissionLoader bungeePermissions = null;
	private PermissionLoader bukkitPermissions = null;

	private static Map<String, Plugin> pluginmap = null;

	public static Map<String, Plugin> getPlugins() {
		if (pluginmap == null) {
			try {
				PluginManager pluginManager = BungeeChat.getInstance().getProxy().getPluginManager();
				Field declaredField = pluginManager.getClass().getDeclaredField("plugins");
				declaredField.setAccessible(true);
				pluginmap = (Map<String, Plugin>) declaredField.get(pluginManager);
			} catch (Exception e) {
			}
		}
		return pluginmap;
	}

	public boolean testPermission(String permission) {
		return (bungeePermissions != null && bungeePermissions.checkPermission(permission))
				|| (bukkitPermissions != null && bukkitPermissions.checkPermission(permission));
	}

	public void updateBungeePermissionCache() {
		PermissionLoader pl = BungeeChat.getInstance().getPermissionLoader();
		ProxiedPlayer player = getPlayer();
		Collection<String> bungeeGroups = player.getGroups();
		List<String> gr = new ArrayList<>();
		gr.addAll(bungeeGroups);
		List<String> bungeePlugins = new ArrayList<>();
		bungeePlugins.addAll(getPlugins().keySet());
		bungeePermissions = pl.get("bungee", gr, bungeePlugins);
	}

	public void updateBukkitPermissionCache(String serverName, List<String> groups, List<String> plugins) {
		PermissionLoader pl = BungeeChat.getInstance().getPermissionLoader();
		bukkitPermissions = pl.get(serverName, groups, plugins);
	}
}
