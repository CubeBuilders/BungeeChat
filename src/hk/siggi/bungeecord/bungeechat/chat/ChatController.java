package hk.siggi.bungeecord.bungeechat.chat;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.PlayerSession;
import hk.siggi.bungeecord.bungeechat.chat.handler.ChatHandler;
import hk.siggi.bungeecord.bungeechat.chat.string.ChatCharacter;
import hk.siggi.bungeecord.bungeechat.chat.string.ChatString;
import hk.siggi.bungeecord.bungeechat.chat.string.patcher.ChatPatcher;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.processChat;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.unify;
import hk.siggi.bungeecord.bungeechat.util.Util;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public final class ChatController implements Listener {

	public final BungeeChat bungeechat;

	private final List<GroupChat> groupChatList = new LinkedList<>();
	private final Map<UUID, GroupChat> uuidGroupChatMap = new HashMap<>();
	private final Map<String, GroupChat> groupChatMap = new HashMap<>();

	public List<GroupChat> getChats() {
		doCleanup();
		List<GroupChat> chats = new LinkedList<>();
		chats.addAll(groupChatList);
		return chats;
	}

	void addChat(GroupChat chat) {
		groupChatList.add(chat);
		uuidGroupChatMap.put(chat.getUUID(), chat);
		groupChatMap.put(canonicalName(chat.getName()), chat);
	}

	public boolean renameChat(GroupChat chat, String newName) {
		GroupChat check = getChat(newName);
		if (check != null && chat != check) {
			return false;
		}
		groupChatMap.remove(canonicalName(chat.getName()));
		groupChatMap.put(canonicalName(newName), chat);
		chat.name = newName;
		return true;
	}

	void removeChat(GroupChat chat) {
		groupChatList.remove(chat);
		uuidGroupChatMap.remove(chat.getUUID());
		groupChatMap.remove(canonicalName(chat.getName()));
	}

	public GroupChat getChat(String name) {
		doCleanup();
		return groupChatMap.get(canonicalName(name));
	}

	public GroupChat getChat(UUID uuid) {
		doCleanup();
		return uuidGroupChatMap.get(uuid);
	}

	public static String canonicalName(String name) {
		name = name.toLowerCase();
		int l = name.length();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < l; i++) {
			char c = name.charAt(i);
			if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
				sb.append(c);
			}
			if (c == '&') {
				i += 1; // skip over colour codes.
			}
		}
		return sb.toString();
	}

	public static boolean isGroupNameAllowed(String name) {
		name = name.toLowerCase();
		int l = name.length();
		for (int i = 0; i < l; i++) {
			char c = name.charAt(i);
			if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '-' || c == '_') {
			} else {
				return false;
			}
		}
		return true;
	}

	public ChatController(BungeeChat bungeechat) {
		this.bungeechat = bungeechat;
		loadChats();
	}

	PlayerSession getSession(ProxiedPlayer p) {
		return BungeeChat.getSession(p);
	}

	@EventHandler
	public void playerChat(ChatEvent event) {
		Connection sender = event.getSender();
		if (!(sender instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer player = (ProxiedPlayer) sender;
		PlayerSession session = getSession(player);
		String message = event.getMessage();
		if (message.equalsIgnoreCase("/a") || message.toLowerCase().startsWith("/a ")) {
			message = "/g staffchat" + message.substring(2);
			event.setMessage(message);
		}
		if (message.equalsIgnoreCase("/pt")) {
			event.setCancelled(true);
			player.sendMessage("Usage: /pt [passthrough]");
			return;
		} else if (message.toLowerCase().startsWith("/pt ")) {
			// passthrough command
			if (player.hasPermission("hk.siggi.bungeechat.passthrough")) {
				event.setMessage(message.substring(4));
			} else {
				event.setCancelled(true);
				player.sendMessage("You don't have permission to use this command.");
			}
			return;
		} else if (message.startsWith("/")) {
			// it's a command, ignore it.
			return;
		}
		ChatHandler chatHandler = session.getChatHandler();
		doProcessChat(chatHandler, player, message);
		event.setCancelled(true);
	}

	//private final Pattern linkPattern = Pattern.compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)");
	private final Pattern linkPattern = Pattern.compile("(https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*))|([-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.(com?|org|net|edu|gov|us|ca|uk|eu|io|be)\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*))");

	public void doProcessChat(ChatHandler handler, ProxiedPlayer player, String message) {
		if (player.hasPermission("hk.siggi.bungeechat.chat.link")) {
			Matcher matcher = linkPattern.matcher(message);
			while (matcher.find()) {
				int firstPos = matcher.start();
				int lastPos = matcher.end();
				if (firstPos != 0 && message.charAt(firstPos - 1) == '<') {
					continue;
				}
				message = message.substring(0, firstPos) + "<" + message.substring(firstPos, lastPos) + ">" + message.substring(lastPos);
				matcher = linkPattern.matcher(message);
			}
		}
		String m = message;
		bungeechat.getScheduler().runAsync(bungeechat, () -> {
			handler.sendChat(player, m);
		});
	}

	public ProcessedChat process(ProxiedPlayer from, String message, boolean censor) {
		message = message.replace("<3", "❤");
		message = message.replace(":tableflip:", "(╯°□°）╯︵ ┻━┻");
		message = message.replace(":yuno:", "ლ(ಠ益ಠლ)");
		message = message.replace(":e_e:", "ಠ益ಠ");

		boolean allowColor = from == null ? true : from.hasPermission("hk.siggi.bungeechat.chat.color");
		boolean allowFormat = from == null ? true : from.hasPermission("hk.siggi.bungeechat.chat.format");
		boolean allowMagic = from == null ? true : from.hasPermission("hk.siggi.bungeechat.chat.magic");
		boolean allowLinks = from == null ? true : from.hasPermission("hk.siggi.bungeechat.chat.link");
		boolean allowCommandLinks = from == null ? true : from.hasPermission("hk.siggi.bungeechat.chat.commandlink");
		ChatString chatString = new ChatString(message, allowColor, allowFormat, allowMagic, allowLinks, allowCommandLinks);
		if (censor) {
			if (from != null) {
				boolean filter = true;
				if (from.hasPermission("hk.siggi.bungeechat.nofiltercaps")) {
					if (bungeechat.getPlayerInfo(from.getUniqueId()).getDisableCapsFilter()) {
						filter = false;
					}
				} else {
					PlayerAccount pi = bungeechat.getPlayerInfo(from.getUniqueId());
					if (pi.getDisableCapsFilter()) {
						pi.setDisableCapsFilter(false);
					}
				}
				if (filter) {
					filterCaps(chatString);
				}
				for (int i = 0; i < chatString.length(); i++) {
					ChatCharacter ch = chatString.get(i);
					if (ch.link != null) {
						String l = ch.link;
						if (!l.startsWith("/")) {
							if (Util.isChatLinkBlacklisted(l)) {
								ch = ch.setLink("[blacklisted]").setTooltip("This link is blacklisted");
								chatString.set(i, ch);
							} else if (ch.character == '\0' && Util.isChatLinkWhitelisted(l)) {
								String title = Util.getTitle(ChatString.prependProtocol(l));
								title = simplifyTitle(title);
								if (title != null) {
									String newText = "\u21E8" + title;
									chatString.insertAt(i + 1, newText, false);
									chatString.delete(i, i + 1);
									i += newText.length() - 1;
								}
							}
						}
					}
				}
			}
		}
		//ArrayList<BaseComponent> originalChatText = bungeechat.processChat(from, message);
		ArrayList<BaseComponent> originalChatText = new ArrayList<>();
		originalChatText.addAll(chatString.toTextComponents());
		ArrayList<BaseComponent> chatTextSemicensored;
		ArrayList<BaseComponent> chatTextCensored;
		if (censor) {
			chatTextCensored = bungeechat.censor(originalChatText, bungeechat.getChatCensor());
			if (originalChatText == chatTextCensored) {
				chatTextSemicensored = originalChatText;
			} else {
				chatTextSemicensored = new ArrayList<>();
				TextComponent hoverComponent = new TextComponent("\u29BF");
				hoverComponent.setColor(ChatColor.RED);
				hoverComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, originalChatText.toArray(new BaseComponent[originalChatText.size()])));
				chatTextSemicensored.add(hoverComponent);
				chatTextSemicensored.addAll(chatTextCensored);
			}
		} else {
			chatTextSemicensored = originalChatText;
			chatTextCensored = originalChatText;
		}
		if (chatTextCensored != originalChatText) {
			BungeeChat.getSession(from).showCensorMessage();
		}
		return new ProcessedChat(message, originalChatText, chatTextCensored, chatTextSemicensored);
	}

	public boolean rateLimitChat(ProxiedPlayer player) {
		PlayerSession session = getSession(player);
		if (player.hasPermission("hk.siggi.bungeechat.bypasschatratelimit")) {
			return false;
		}
		long playTime = session.getPlayTime();
		long maxAge;
		int maximumChats;
		if (playTime < (3600000L * 4L)) {
			maxAge = 6000L;
			maximumChats = 2;
		} else if (playTime < (3600000L * 8L)) {
			maxAge = 4000L;
			maximumChats = 3;
		} else {
			maxAge = 4000L;
			maximumChats = 4;
		}
		if (session.getChatCount(maxAge) >= maximumChats) {
			player.sendMessage(unify(processChat(null, "&cOh my gawd, you need to like take a chill pill! Slow down! :o")));
			return true;
		}
		return false;
	}

	public void publicChat(ProxiedPlayer from, String message) {
		PlayerSession session = getSession(from);
		if (rateLimitChat(from)) {
			return;
		}
		final PlayerAccount info = bungeechat.getPlayerInfo(from.getUniqueId());
		if (session.user.isMuted()) {
			bungeechat.youAreMuted(from, session.user);
			return;
		}
		final boolean imSilentMuted = info.isSilentMuted();

		List<ProxiedPlayer> recipients = new LinkedList<>();
		String publicChatGroup = session.getPublicChatGroup();
		bungeechat.getProxy().getPlayers().stream().forEach((p) -> {
			try {
				PlayerSession sess = getSession(p);
				if (sess.getPublicChatGroup().equals(publicChatGroup)) {
					recipients.add(p);
				}
			} catch (Exception e) {
			}
		});
		//ServerInfo serverInfo = from.getServer().getInfo();
		//String serverName = serverInfo.getName();
		//recipients.addAll(serverInfo.getPlayers());

		ProcessedChat chat = process(from, message, true);

		BaseComponent prefixComponent = null;

		List<TextComponent> shortPrefix;
		List<TextComponent> longPrefix;

		shortPrefix = bungeechat.getGroupInfo().usernameComponent(from, true, false, false, false);
		longPrefix = bungeechat.getGroupInfo().usernameComponent(from, false, true, false, false);
		TextComponent colon = new TextComponent(" \u00bb ");
		colon.setColor(ChatColor.GRAY);
		boolean bypassIgnore = from.hasPermission("hk.siggi.bungeechat.ignoreexempt");
		for (ProxiedPlayer recipient : recipients) {
			PlayerAccount targetPlayer = bungeechat.getPlayerInfo(recipient.getUniqueId());
			if (!bypassIgnore && targetPlayer.isIgnoring(from.getUniqueId())) {
				continue;
			}
			if (!imSilentMuted || targetPlayer.isSilentMuted() || recipient.hasPermission("hk.siggi.bungeechat.silentmute")) {
				PlayerSession targetPlayerSession = getSession(recipient);
				PlayerAccount.ChatPrefixType cpt = targetPlayerSession.getChatPrefixType(targetPlayer.getChatPrefixType());
				TextComponent chatMessage = new TextComponent("");
				if (prefixComponent != null) {
					chatMessage.addExtra(prefixComponent);
				}
				if (cpt == PlayerAccount.ChatPrefixType.COMPACT) {
					bungeechat.addAll(chatMessage, shortPrefix);
				} else {
					bungeechat.addAll(chatMessage, longPrefix);
				}
				chatMessage.addExtra(colon);
				if (targetPlayer.getChatCensor()) {
					if (targetPlayer.getChatCensorSemi()) {
						bungeechat.addAll(chatMessage, chat.semiCensored);
					} else {
						bungeechat.addAll(chatMessage, chat.censored);
					}
				} else {
					bungeechat.addAll(chatMessage, chat.uncensored);
				}
				recipient.sendMessage(chatMessage);
			}
		}
		bungeechat.logChat("Public-" + publicChatGroup + ":" + from.getName() + "/" + Util.uuidToString(from.getUniqueId()) + ":" + message);
		session.addChatTime();
	}

	public void sendPM(ProxiedPlayer from, ProxiedPlayer to, String message) {
		PlayerSession sessionFrom = getSession(from);
		PlayerSession sessionTo = getSession(to);
		if (rateLimitChat(from)) {
			return;
		}
		PlayerAccount fromAccount = bungeechat.getPlayerInfo(from.getUniqueId());
		if (sessionFrom.user.isMuted()) {
			bungeechat.youAreMuted(from, sessionFrom.user);
			return;
		}
		ProcessedChat chat = process(from, message, true);

		PlayerAccount toAccount = bungeechat.getPlayerInfo(to.getUniqueId());

		if (fromAccount.isIgnoring(to.getUniqueId())) {
			TextComponent baseFail = new TextComponent("");
			TextComponent cannotPM = new TextComponent("Cannot PM ");
			List<TextComponent> usernameComponents = bungeechat.getGroupInfo().usernameComponent(to, true, false, false, false);
			TextComponent because = new TextComponent(" because you are ignoring them.");
			cannotPM.setColor(ChatColor.RED);
			because.setColor(ChatColor.RED);
			baseFail.addExtra(cannotPM);
			bungeechat.addAll(baseFail, usernameComponents);
			baseFail.addExtra(because);
			return;
		}
		if (toAccount.isIgnoring(from.getUniqueId()) && !from.hasPermission("hk.siggi.bungeechat.ignoreexempt")) {
			TextComponent baseFail = new TextComponent("");
			TextComponent cannotPM = new TextComponent("Cannot PM ");
			List<TextComponent> usernameComponents = bungeechat.getGroupInfo().usernameComponent(to, true, false, false, false);
			TextComponent because = new TextComponent(" because you are on their ignore list.");
			cannotPM.setColor(ChatColor.RED);
			because.setColor(ChatColor.RED);
			baseFail.addExtra(cannotPM);
			bungeechat.addAll(baseFail, usernameComponents);
			baseFail.addExtra(because);
			return;
		}

		TextComponent baseFrom = new TextComponent("");
		TextComponent pmTo = new TextComponent("PM To ");
		pmTo.setColor(ChatColor.YELLOW);
		List<TextComponent> usernameComponents = bungeechat.getGroupInfo().usernameComponent(to, true, false, false, false);
		//extra.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/m " + to.getName() + " "));
		//extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to send " + to.getName() + " a message")}));
		baseFrom.addExtra(pmTo);
		bungeechat.addAll(baseFrom, usernameComponents);

		TextComponent baseTo = new TextComponent();
		TextComponent pmFrom = new TextComponent("PM From ");
		pmFrom.setColor(ChatColor.YELLOW);
		usernameComponents = bungeechat.getGroupInfo().usernameComponent(from, true, false, false, false);
		//extra.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/m " + from.getName() + " "));
		//extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to send " + from.getName() + " a message")}));
		baseTo.addExtra(pmFrom);
		bungeechat.addAll(baseTo, usernameComponents);

		TextComponent colon = new TextComponent(" \u00bb ");
		colon.setColor(ChatColor.GRAY);
		baseFrom.addExtra(colon);
		baseTo.addExtra(colon);

		if (fromAccount.getChatCensor()) {
			if (fromAccount.getChatCensorSemi()) {
				bungeechat.addAll(baseFrom, chat.semiCensored);
			} else {
				bungeechat.addAll(baseFrom, chat.censored);
			}
		} else {
			bungeechat.addAll(baseFrom, chat.uncensored);
		}
		if (toAccount.getChatCensor()) {
			if (toAccount.getChatCensorSemi()) {
				bungeechat.addAll(baseTo, chat.semiCensored);
			} else {
				bungeechat.addAll(baseTo, chat.censored);
			}
		} else {
			bungeechat.addAll(baseTo, chat.uncensored);
		}

		sessionFrom.playSound("BLOCK_NOTE_BLOCK_PLING", 1.0f, 2.0f, 0);
		from.sendMessage(baseFrom);
		if (!fromAccount.isSilentMuted() || toAccount.isSilentMuted() || to.hasPermission("hk.siggi.bungeechat.silentmute")) {
			to.sendMessage(baseTo);
			sessionTo.playSound("BLOCK_NOTE_BLOCK_PLING", 1.0f, 1.5f, 0);
			sessionTo.playSound("BLOCK_NOTE_BLOCK_PLING", 1.0f, 2.0f, 2);
		}

		if (sessionTo.user.isMuted()) {
			BaseComponent msg = new TextComponent("");
			BaseComponent name = new TextComponent(to.getName());
			BaseComponent xtra = new TextComponent(" is currently muted and cannot respond. For info, ");
			BaseComponent xtra2 = new TextComponent("/phistory " + to.getName());
			name.setColor(ChatColor.AQUA);
			xtra.setColor(ChatColor.GOLD);
			xtra2.setColor(ChatColor.AQUA);
			xtra2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/phistory " + to.getName()));
			xtra2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to see punishment history for this user.")}));
			msg.addExtra(name);
			msg.addExtra(xtra);
			msg.addExtra(xtra2);
			from.sendMessage(msg);
		}

		bungeechat.setLastMessage(from.getName(), to.getName());
		if (!fromAccount.isNoLog() && !toAccount.isNoLog()) {
			bungeechat.logChat("Private:" + from.getName() + "/" + Util.uuidToString(from.getUniqueId()) + ":" + to.getName() + "/" + Util.uuidToString(to.getUniqueId()) + ":" + message);
		}
		sessionFrom.addChatTime();
	}

	private void filterCaps(ChatString chatString) {
		String rawString = chatString.toRawString();
		List<OriginalStringInfo> maintainCaps = maintainCaps(rawString);

		boolean beginSentence = true;
		boolean insideWord = false;
		char[] c = rawString.toCharArray();
		char originalCharacter;
		char newCharacter;
		for (int i = 0; i < c.length; i++) {
			originalCharacter = newCharacter = c[i];
			if (originalCharacter >= 'A' && originalCharacter <= 'Z') { // Capital Letter
				if (insideWord) {
					newCharacter = originalCharacter;
					newCharacter += 32; // convert to lowercase
				}
				beginSentence = false;
				insideWord = true;
			} else if (originalCharacter >= 'a' && originalCharacter <= 'z') { // Lowercase Letter
				if (beginSentence) {
					newCharacter = originalCharacter;
					newCharacter -= 32; // convert to uppercase
				}
				beginSentence = false;
				insideWord = true;
			} else { // Space
				insideWord = false;
			}
			if (originalCharacter == '.' || originalCharacter == '?' || originalCharacter == '!') { // End Sentence
				beginSentence = true;
			}
			if (newCharacter != originalCharacter) {
				c[i] = newCharacter;
				chatString.replaceText(i, new String(new char[]{newCharacter}));
			}
		}

		revertCaps(maintainCaps, chatString);

		ChatPatcher patcher = new ChatPatcher(chatString);

		patcher.forEach("iPhone", (w) -> w.replaceWhole("iPhone"));
		patcher.forEach("iPad", (w) -> w.replaceWhole("iPad"));
		patcher.forEach("iPod", (w) -> w.replaceWhole("iPod"));
		patcher.forEach("iMac", (w) -> w.replaceWhole("iMac"));
		patcher.forEach("iOS", (w) -> w.replaceWhole("iOS"));
		patcher.forEach("IP", (w) -> w.replaceWhole("IP"));
		patcher.forEach("dont", (w) -> w.insert(3, "'"));
		patcher.forEach("cant", (w) -> w.insert(3, "'"));
		patcher.forEach("wont", (w) -> w.insert(3, "'"));
		patcher.forEach("wouldnt", (w) -> w.insert(6, "'"));
		patcher.forEach("couldnt", (w) -> w.insert(6, "'"));
		patcher.forEach("didnt", (w) -> w.insert(4, "'"));
		patcher.forEach("doesnt", (w) -> w.insert(5, "'"));
		patcher.forEach("im", (w) -> w.insert(1, "'"));
		patcher.forEach("I", (w) -> w.replaceWhole("I"));
		patcher.forEach("plz", (w) -> w.replace(2, "s").insert(3, "e").insert(2, "ea"));
		patcher.forEach("pls", (w) -> w.insert(3, "e").insert(2, "ea"));

		patcher.forEach("wat", (w) -> w.insert(1, "h"));
		patcher.forEach("wut", (w) -> w.insert(1, "h").replace(2, "a"));

		patcher.forEach("u", (w) -> w.insert(0, w.charAt(0) == 'U' ? "Yo" : "yo").replace(2, "u"));
		patcher.forEach("y", (w) -> w.insert(0, w.charAt(0) == 'Y' ? "Wh" : "wh").replace(2, "y"));

		patcher.forEach("iz", (w) -> w.replaceWhole(w.charAt(0) == 'I' ? "Is" : "is"));

		patcher.forEach("r", (w) -> w.insert(1, w.charAt(0) == 'R' ? "Are" : "are").delete(0, 1));

		patcher.forEach("is are", (w) -> w.replace(3, w.charAt(3) == 'A' ? "Our" : "our"));

		patcher.forEach("Siggi", (w) -> w.replace(0, "S"));
		patcher.forEach("Sigi", (w) -> w.replace(0, "S").insert(3, "g"));
		patcher.forEach("Siggi88", (w) -> w.replace(0, "S"));
		patcher.forEach("Sigi88", (w) -> w.replace(0, "S").insert(3, "g"));
		patcher.forEach("Siggi8", (w) -> w.replace(0, "S").insert(6, "8"));
		patcher.forEach("Sigi8", (w) -> w.replace(0, "S").insert(3, "g").insert(6, "8"));

		patcher.forEach("Cube Builders", (w) -> w.delete(4, 5));

		patcher.forEach("CubeBuilders", (w) -> w.replaceWhole("CubeBuilders"));
		patcher.forEach("CubeBuilders Girl", (w) -> w.delete(12, 13));
		patcher.forEach("CubeBuildersGirl", (w) -> w.replaceWhole("CubeBuildersGirl"));

		patcher.forEach("cubebuilders net", (w) -> w.replaceWhole("cubebuilders.net"));
		patcher.forEach("cubebuilders.net", (w) -> w.replaceWhole("cubebuilders.net"));

		patcher.forEach("Siggi.hk", (w) -> w.replaceWhole("Siggi.hk"));
		patcher.forEach("Siggi.io", (w) -> w.replaceWhole("Siggi.io"));

		patcher.forEach("hongkong", (w) -> w.insert(4, " "));
		patcher.forEach("Hong Kong", (w) -> w.replaceWhole("Hong Kong"));

		patcher.forEach("bang kok", (w) -> w.delete(4, 5));
		patcher.forEach("bangkok", (w) -> w.replaceWhole("Bangkok"));

		patcher.forEach("hypixel", (w) -> w.replace(0, "a diffe").insert(7, "rent dumb server"));
		patcher.forEach("hipixel", (w) -> w.replace(0, "a diffe").insert(7, "rent dumb server"));
		patcher.forEach("hypickel", (w) -> w.replace(0, "a differ").insert(8, "ent dumb server because im dumb and I keep tryna bypass the filter"));
		patcher.forEach("hipickel", (w) -> w.replace(0, "a differ").insert(8, "ent dumb server because im dumb and I keep tryna bypass the filter"));
		patcher.forEach("hipicel", (w) -> w.replace(0, "a diffe").insert(7, "rent dumb server because im dumb and I keep tryna bypass the filter"));
		patcher.forEach("hypicel", (w) -> w.replace(0, "a diffe").insert(7, "rent dumb server because im dumb and I keep tryna bypass the filter"));
		patcher.forEach("hipikel", (w) -> w.replace(0, "a diffe").insert(7, "rent dumb server because im dumb and I keep tryna bypass the filter"));
		patcher.forEach("hypikel", (w) -> w.replace(0, "a diffe").insert(7, "rent dumb server because im dumb and I keep tryna bypass the filter"));
		patcher.forEach("mineplex", (w) -> w.replace(0, "another ").insert(8, "dumb server"));

		patcher.forEach("Are.I.P", (w) -> w.delete(1, 3).replace(0, "R")); // replace Are.I.P with R.I.P

		// democrat -> demoncrat
		patcher.forEach("democrat", (w) -> w.insert(4, w.charAt(3) == 'O' ? "N" : "n"));
		patcher.forEach("democrats", (w) -> w.insert(4, w.charAt(3) == 'O' ? "N" : "n"));

		// liberal -> libtard
		patcher.forEach("liberal", (w) -> w.replace(3, "tard"));
		patcher.forEach("liberals", (w) -> w.replace(3, "tard"));

		// obama -> obummer
		patcher.forEach("obama", (w) -> w.replace(2, "u").replace(4, "e").insert(5, "r").insert(4, "m"));

		// fuck -> fluff
		patcher.forEach("fuck", (w) -> w.replace(1, "luf").insert(4, "f"));

		// fucker -> fluffy
		patcher.forEach("fucker", (w) -> w.replace(1, "luffy"));

		// fucking -> fluffy
		patcher.forEach("fucking", (w) -> w.replace(1, "luffy").delete(6, 7));
	}

	private List<OriginalStringInfo> maintainCaps(String string) {
		List<OriginalStringInfo> info = new LinkedList<>();

		maintainCaps(string, "xD", info); // laughing
		maintainCaps(string, "XD", info); // laughing
		maintainCaps(string, "TNT", info); // Trinitrotoluene
		maintainCaps(string, "LOL", info); // laughing out loud
		maintainCaps(string, "LMAO", info); // laughing my ass off
		maintainCaps(string, "RIP", info); // rest in peace
		maintainCaps(string, "US", info); // United States
		maintainCaps(string, "USA", info); // United States of America
		maintainCaps(string, "UK", info); // United Kingdom

		for (ProxiedPlayer pl : BungeeCord.getInstance().getPlayers()) {
			maintainCaps(string, pl.getName(), info);
		}

		return info;
	}

	private void maintainCaps(String haystack, String needle, List<OriginalStringInfo> currentList) {
		int pos = 0;
		int haystackLength = haystack.length();
		int needleLength = needle.length();
		int end = haystackLength - needleLength;
		while ((pos = haystack.indexOf(needle, pos)) != -1) {
			if ((pos == 0 || isWordBorderCharacter(haystack.charAt(pos - 1))) && (pos == end || isWordBorderCharacter(haystack.charAt(pos + needleLength)))) {
				currentList.add(new OriginalStringInfo(needle, pos));
			}
			pos += needleLength;
		}
	}

	private void revertCaps(List<OriginalStringInfo> ranges, ChatString chatString) {
		for (OriginalStringInfo range : ranges) {
			chatString.replaceText(range.getStart(), range.getOriginal());
		}
	}

	private void loadChats() {
		try {
			File[] ff = new File(bungeechat.getDataFolder(), "groupchats").listFiles();
			for (File f : ff) {
				String name = f.getName();
				if (name.endsWith(".txt")) {
					name = name.substring(0, name.length() - 4);
					if (name.length() == 32) {
						UUID uuid = Util.uuidFromString(name);
						GroupChat gc = new GroupChat(this, uuid);
						gc.load();
						addChat(gc);
					}
				}
			}
		} catch (Exception e) {
		}
	}

	public GroupChat createChat(String name, ProxiedPlayer player) {
		{
			GroupChat a = getChat(name);
			if (a != null) {
				return a;
			}
		}
		UUID uuid = null;
		while (getChat(uuid = UUID.randomUUID()) != null) {
		}
		GroupChat gc = new GroupChat(this, uuid);
		gc.name = name;
		if (player != null) {
			gc.setOwner(player.getUniqueId());
		}
		gc.save();
		addChat(gc);
		return gc;
	}

	public void deleteChat(GroupChat gc) {
		gc.delete();
		removeChat(gc);
	}

	private long lastCleanup = 0L;

	private void doCleanup() {
		long now = System.currentTimeMillis();
		long timeSinceCleanup = now - lastCleanup;
		if (timeSinceCleanup < 300000L) {
			return;
		}
		lastCleanup = now;
		for (GroupChat c : getChats()) {
			if (c.hasAntiBypass() || c.isNotLogged() || c.isCensorDisabled()) {
				continue;
			}
			int messageCount = c.getMessageCount();
			int memberCount = c.getMembers().size();
			int listenerCount = c.getUsers().size();
			int userCount = Math.max(memberCount, listenerCount);
			long expireTime = 86400000L * 2L; // 2 days
			if (userCount == 1) {
				expireTime = 3600000L; // 1 hour if only one user in the chat
			}
			if (messageCount >= 50) {
				if (userCount <= 2) {
					expireTime = 86400000L * 7L;
				} else {
					expireTime = 86400000L * 14L;
				}
			}
			if (messageCount >= 250 || userCount >= 4) {
				expireTime = 86400000L * 30L;
			}
			long lastActivity = c.getLastActivity();
			long timeSinceLastActivity = now - lastActivity;
			if (timeSinceLastActivity >= expireTime) {
				deleteChat(c);
			}
		}
	}

	private boolean isWordBorderCharacter(char c) {
		return c == ' ' || c == '.' || c == ',' || c == '?' || c == '!'
				|| c == '/' || c == '\\' || c == '|'
				|| c == ':' || c == ';'
				|| c == '"' || c == '\''
				|| c == '<' || c == '>' || c == '[' || c == ']'
				|| c == '{' || c == '}' || c == '(' || c == ')';
	}

	private final Pattern spotifyRegex = Pattern.compile("(.*), a song by (.*) on Spotify");

	private String simplifyTitle(String title) {
		if (title == null) {
			return null;
		}
		Matcher matcher = spotifyRegex.matcher(title);
		if (matcher.matches()) {
			return "Spotify: " + matcher.group(1) + " by " + matcher.group(2);
		}
		return title;
	}

	private class OriginalStringInfo {

		private final String original;
		private final int offset;

		public OriginalStringInfo(String original, int offset) {
			this.original = original;
			this.offset = offset;
		}

		public String getOriginal() {
			return original;
		}

		public int getStart() {
			return offset;
		}

		public int getEnd() {
			return offset + original.length();
		}
	}
}
