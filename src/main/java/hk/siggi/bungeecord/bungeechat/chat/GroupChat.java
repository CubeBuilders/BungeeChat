package hk.siggi.bungeecord.bungeechat.chat;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.PlayerSession;
import hk.siggi.bungeecord.bungeechat.chat.handler.ChatHandler;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.processChat;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.unify;
import hk.siggi.bungeecord.bungeechat.util.Util;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class GroupChat implements ChatHandler {

	GroupChat(ChatController controller, UUID groupUUID) {
		this.controller = controller;
		this.groupUUID = groupUUID;
		creationDate = lastActivity = System.currentTimeMillis();
	}

	@Override
	public void sendChat(ProxiedPlayer sender, String message) {
		BungeeChat plugin = controller.bungeechat;
		PlayerSession session = BungeeChat.getSession(sender);
		BungeeChat bungeechat = BungeeChat.getInstance();
		boolean bypassIgnore = sender.hasPermission("hk.siggi.bungeechat.ignoreexempt");
		if (controller.rateLimitChat(sender)) {
			return;
		}
		if (session.user.isMuted()) {
			BungeeChat.getInstance().youAreMuted(sender, session.user);
			return;
		}
		if (permissionNode != null) {
			whitelisted = false;
		}
		boolean allowedToSend = !deleted && (!isWhitelistedOrPermissionNode() || isMember(sender) || isJoined(sender) || hasPermission(sender)) && !isBanned(sender);
		if (!allowedToSend) {
			if (session.getChatHandler() == this) {
				session.setChatHandler(null, false); // revert to Public Chat
				sender.sendMessage(unify(processChat(null, "&cYou can't send messages to this chat. Your default chat has been changed to &ePublic Chat&c.")));
			} else {
				sender.sendMessage(unify(processChat(null, "&cYou can't send messages to this chat.")));
			}
			return;
		} else if (!isJoined(sender)) {
			joined.add(sender.getUniqueId());
		}

		final PlayerAccount info = controller.bungeechat.getPlayerInfo(sender.getUniqueId());
		final boolean imSilentMuted = info.isSilentMuted();

		ProcessedChat chat = controller.process(sender, message, !censorDisabled);

		List<BaseComponent> groupPrefix = getDisplay();
		groupPrefix.add(new TextComponent(" "));

		List<TextComponent> shortPrefix = controller.bungeechat.getGroupInfo().usernameComponent(sender, true, false, false, false);

		TextComponent colon = new TextComponent(" \u00bb ");
		colon.setColor(ChatColor.GRAY);

		StringBuilder recipientSb = new StringBuilder();

		for (UUID recipientUUID : joined) {
			ProxiedPlayer recipient = BungeeChat.getProxiedPlayer(recipientUUID);
			if (recipient == null) {
				continue;
			}
			PlayerAccount targetPlayer = controller.bungeechat.getPlayerInfo(recipient.getUniqueId());
			if (!bypassIgnore && targetPlayer.isIgnoring(sender.getUniqueId())) {
				continue;
			}
			if (!imSilentMuted || targetPlayer.isSilentMuted() || recipient.hasPermission("hk.siggi.bungeechat.silentmute")) {
				TextComponent chatMessage = new TextComponent("");
				controller.bungeechat.addAll(chatMessage, groupPrefix);
				controller.bungeechat.addAll(chatMessage, shortPrefix);
				chatMessage.addExtra(colon);
				if (targetPlayer.getChatCensor()) {
					if (targetPlayer.getChatCensorSemi()) {
						controller.bungeechat.addAll(chatMessage, chat.semiCensored);
					} else {
						controller.bungeechat.addAll(chatMessage, chat.censored);
					}
				} else {
					controller.bungeechat.addAll(chatMessage, chat.uncensored);
				}
				recipient.sendMessage(chatMessage);
			}
			if (recipientSb.length() != 0) {
				recipientSb.append(",");
			}
			recipientSb.append(recipient.getName());
			recipientSb.append("/");
			recipientSb.append(Util.uuidToString(recipient.getUniqueId()));
		}
		session.addChatTime();
		if (!doNotLog) {
			controller.bungeechat.logChat("Group-" + getCanonicalName() + "/" + Util.uuidToString(getUUID()) + ":" + sender.getName() + "/" + Util.uuidToString(sender.getUniqueId()) + ":" + (recipientSb.toString()) + ":" + message);
		}
		messageCount += 1;
		lastActivity = System.currentTimeMillis();
		sync();
	}

	public boolean allowJoining(ProxiedPlayer player) {
		UUID uuid = player.getUniqueId();
		if (banned.contains(uuid)) {
			return false;
		}
		if (permissionNode != null) {
			return player.hasPermission(permissionNode);
		} else {
			if (whitelisted) {
				return owner.equals(uuid) || members.contains(uuid);
			} else {
				return true;
			}
		}
	}

	@Override
	public List<BaseComponent> getDisplay() {
		String str = "&f[G]&a" + name;
		if (hidePrefix) {
			if (name.startsWith("&")) {
				str = name;
			} else {
				str = "&a" + name;
			}
		}
		List<BaseComponent> c = processChat(null, str);
		return c;
	}

	private boolean deleted = false;
	private final ChatController controller;
	private final UUID groupUUID;
	String name = null;
	private UUID owner = null;
	private boolean whitelisted = true;
	private final List<UUID> moderators = new LinkedList<>();
	private final List<UUID> members = new LinkedList<>();
	private final List<UUID> banned = new LinkedList<>();
	private final List<UUID> joined = new LinkedList<>();
	private long creationDate;
	private long lastActivity;
	private int messageCount = 0;
	private String permissionNode = null;
	private boolean doNotLog = false;
	private boolean antiBypass = false;
	private boolean hidePrefix = false;
	private boolean censorDisabled = false;

	public boolean canJoin(ProxiedPlayer player) {
		if (permissionNode != null) {
			return player.hasPermission(name);
		} else {
			if (whitelisted) {
				if (!members.contains(player.getUniqueId())) {
					return false;
				}
			}
			return true;
		}
	}

	public boolean isOwner(ProxiedPlayer player) {
		return owner != null && player.getUniqueId().equals(owner);
	}

	public boolean isModerator(ProxiedPlayer player) {
		return isModerator(player.getUniqueId());
	}

	public boolean isModerator(UUID uuid) {
		return (owner != null && owner.equals(uuid)) || moderators.contains(uuid);
	}

	public boolean isMember(ProxiedPlayer player) {
		UUID uuid = player.getUniqueId();
		return (owner != null && owner.equals(uuid)) || members.contains(uuid);
	}
	
	public boolean hasPermission(ProxiedPlayer player) {
		return permissionNode != null && player.hasPermission(permissionNode);
	}

	public boolean isBanned(ProxiedPlayer player) {
		return banned.contains(player.getUniqueId());
	}

	public boolean isJoined(ProxiedPlayer player) {
		return joined.contains(player.getUniqueId());
	}

	public UUID getUUID() {
		return groupUUID;
	}

	public String getName() {
		return name;
	}

	public String getCanonicalName() {
		return ChatController.canonicalName(name);
	}

	public UUID getOwner() {
		return owner;
	}

	public boolean isWhitelisted() {
		return whitelisted;
	}

	public boolean isWhitelistedOrPermissionNode() {
		return whitelisted || (permissionNode != null);
	}

	public List<UUID> getModerators() {
		List<UUID> list = new LinkedList<>();
		list.addAll(moderators);
		return list;
	}

	public List<UUID> getMembers() {
		List<UUID> list = new LinkedList<>();
		list.addAll(members);
		return list;
	}

	public List<UUID> getBanned() {
		List<UUID> list = new LinkedList<>();
		list.addAll(banned);
		return list;
	}

	public List<UUID> getUsers() {
		List<UUID> list = new LinkedList<>();
		list.addAll(joined);
		return list;
	}

	public long getLastActivity() {
		return lastActivity;
	}

	public boolean setName(String newName) {
		try {
			return controller.renameChat(this, newName);
		} finally {
			sync();
		}
	}

	public void setOwner(UUID newOwner) {
		this.owner = newOwner;
		sync();
	}

	public void setWhitelist(boolean whitelisted) {
		this.whitelisted = whitelisted;
		sync();
	}

	public void addModerator(UUID user) {
		if (!moderators.contains(user)) {
			moderators.add(user);
			sync();
		}
	}

	public void addMember(UUID user) {
		if (!members.contains(user)) {
			members.add(user);
			sync();
		}
	}

	public void addBanned(UUID user) {
		if (!banned.contains(user)) {
			banned.add(user);
			sync();
		}
	}

	public void addUser(UUID user) {
		if (!joined.contains(user)) {
			joined.add(user);
			sync();
		}
	}

	public void removeModerator(UUID user) {
		moderators.remove(user);
		sync();
	}

	public void removeMember(UUID user) {
		members.remove(user);
		sync();
	}

	public void removeBanned(UUID user) {
		banned.remove(user);
		sync();
	}

	public void removeUser(UUID user) {
		joined.remove(user);
		sync();
	}

	void delete() {
		deleted = true;
		try {
			getFile().delete();
		} catch (Exception e) {
		}
	}

	private File getFile() {
		File chatDir = new File(BungeeChat.getInstance().getDataFolder(), "groupchats");
		if (!chatDir.exists()) {
			chatDir.mkdirs();
		}
		return new File(chatDir, Util.uuidToString(groupUUID) + ".txt");
	}

	private long lastSync = 0L;

	private final Object syncLock = new Object();
	private boolean syncing = false;

	private void sync() {
		synchronized (syncLock) {
			if (syncing) {
				return;
			}
			try {
				syncing = true;
				if (deleted) {
					return;
				}
				if (permissionNode != null) {
					members.clear();
				}
				File f = getFile();
				if (f.exists() && f.lastModified() > lastSync) {
					String n = name;
					load();
					String n2 = name;
					if (n != null && n2 != null && !n.equals(n2)) {
						name = n;
						setName(n2);
					}
				} else {
					save();
				}
			} finally {
				syncing = false;
			}
		}
	}

	void save() {
		if (deleted) {
			return;
		}
		FileOutputStream fos = null;
		File f = getFile();
		try {
			fos = new FileOutputStream(f);
			writeLine(fos, "creationdate=" + creationDate);
			writeLine(fos, "lastactivity=" + lastActivity);
			writeLine(fos, "name=" + name);
			writeLine(fos, "owner=" + Util.uuidToString(owner));
			for (UUID user : joined) {
				writeLine(fos, "joined=" + Util.uuidToString(user));
			}
			for (UUID user : members) {
				writeLine(fos, "member=" + Util.uuidToString(user));
			}
			for (UUID user : moderators) {
				writeLine(fos, "moderator=" + Util.uuidToString(user));
			}
			for (UUID user : banned) {
				writeLine(fos, "banned=" + Util.uuidToString(user));
			}
			writeLine(fos, "messagecount=" + messageCount);
			writeLine(fos, "whitelisted=" + (whitelisted ? "1" : "0"));
			if (permissionNode != null) {
				writeLine(fos, "permissionnode=" + permissionNode);
			}
			if (antiBypass) {
				writeLine(fos, "antibypass=1");
			}
			if (doNotLog) {
				writeLine(fos, "donotlog=1");
			}
			if (hidePrefix) {
				writeLine(fos, "hideprefix=1");
			}
			if (censorDisabled) {
				writeLine(fos, "censordisabled=1");
			}
		} catch (Exception e) {
		} finally {
			tryClose(fos);
		}
		if (f.exists()) {
			lastSync = f.lastModified();
		} else {
			lastSync = System.currentTimeMillis();
		}
	}

	void load() {
		lastSync = System.currentTimeMillis();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(getFile())));
			joined.clear();
			members.clear();
			banned.clear();
			moderators.clear();
			for (String line; (line = reader.readLine()) != null;) {
				int eqPos = line.indexOf("=");
				if (eqPos == -1) {
					continue;
				}
				String key = line.substring(0, eqPos);
				String val = line.substring(eqPos + 1);
				switch (key) {
					case "creationdate":
						creationDate = Long.parseLong(val);
						break;
					case "lastactivity":
						lastActivity = Long.parseLong(val);
						break;
					case "joined":
						joined.add(Util.uuidFromString(val));
						break;
					case "member":
						members.add(Util.uuidFromString(val));
						break;
					case "moderator":
						moderators.add(Util.uuidFromString(val));
						break;
					case "banned":
						banned.add(Util.uuidFromString(val));
						break;
					case "name":
						name = val;
						break;
					case "owner":
						owner = Util.uuidFromString(val);
						break;
					case "messagecount":
						messageCount = Integer.parseInt(val);
						break;
					case "whitelisted":
						whitelisted = val.equals("1");
						break;
					case "permissionnode":
						permissionNode = val;
						break;
					case "antibypass":
						antiBypass = val.equals("1");
						break;
					case "donotlog":
						doNotLog = val.equals("1");
						break;
					case "hideprefix":
						hidePrefix = val.equals("1");
						break;
					case "censordisabled":
						censorDisabled = val.equals("1");
						break;
				}
			}
		} catch (Exception e) {
		} finally {
			tryClose(reader);
		}
	}

	private void tryClose(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
			}
		}
	}

	private void writeLine(FileOutputStream fos, String string) throws IOException {
		fos.write(string.getBytes());
		fos.write("\r\n".getBytes());
	}

	public String getPermissionNode() {
		return permissionNode;
	}

	public int getMessageCount() {
		return messageCount;
	}

	public boolean hasAntiBypass() {
		return antiBypass;
	}

	public boolean isNotLogged() {
		return doNotLog;
	}

	public boolean isCensorDisabled() {
		return censorDisabled;
	}
}
