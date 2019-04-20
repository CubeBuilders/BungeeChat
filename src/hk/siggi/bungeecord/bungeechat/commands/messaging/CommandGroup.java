package hk.siggi.bungeecord.bungeechat.commands.messaging;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.processChat;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.unify;
import hk.siggi.bungeecord.bungeechat.NicknameCache;
import hk.siggi.bungeecord.bungeechat.PlayerNameHandler;
import hk.siggi.bungeecord.bungeechat.PlayerSession;
import hk.siggi.bungeecord.bungeechat.UUIDCache;
import hk.siggi.bungeecord.bungeechat.chat.ChatController;
import hk.siggi.bungeecord.bungeechat.chat.GroupChat;
import hk.siggi.bungeecord.bungeechat.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandGroup extends Command implements TabExecutor {

	public final BungeeChat plugin;

	public CommandGroup(BungeeChat plugin) {
		super("group", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer player = (ProxiedPlayer) sender;
		if (args[0].equals("tabbypass")) {
			PlayerSession session = plugin.getSession(player);
			if (session.groupTabBypass) {
				session.groupTabBypass = false;
				player.sendMessage(unify(processChat(null, "&6Tab Bypass is now disabled!")));
			} else if (player.hasPermission("hk.siggi.bungeechat.groupchatbypass")) {
				session.groupTabBypass = true;
				player.sendMessage(unify(processChat(null, "&6Tab Bypass is now enabled!")));
			} else {
				player.sendMessage(unify(processChat(null, "&4Tab bypass is not available for you.")));
			}
			return;
		}
		boolean allowSingleArg = false;
		if (args.length >= 1) {
			switch (args[0]) {
				case "list":
				case "listall":
					allowSingleArg = true;
					break;
			}
		}
		if (args.length >= 1 && args[0].equals("help")) {
			int page = 1;
			if (args.length >= 2) {
				try {
					page = Integer.parseInt(args[1]);
				} catch (Exception e) {
				}
			}
			player.sendMessage(unify(processChat(null, "&6Group Chat commands &e/group help " + page + " &6out of 3 pages.")));
			if (page == 1) {
				player.sendMessage(unify(processChat(null, "&e/group create [groupname] &f- Create a group")));
				player.sendMessage(unify(processChat(null, "&e/group delete [groupname] &f- Delete a group")));
				player.sendMessage(unify(processChat(null, "&e/group mute [groupname] &f- Mute a group chat")));
				player.sendMessage(unify(processChat(null, "&e/group unmute [groupname] &f- Unmute a group chat")));
				player.sendMessage(unify(processChat(null, "&e/group leave [groupname] &f- Leave a group")));
				player.sendMessage(unify(processChat(null, "&e/g [groupname] &f- Start chatting in a group")));
				player.sendMessage(unify(processChat(null, "&e/g [groupname] [message] &f- Send a single message to a group")));
			} else if (page == 2) {
				player.sendMessage(unify(processChat(null, "&e/group add [groupname] [username] &f- Add a user to a group")));
				player.sendMessage(unify(processChat(null, "&e/group mod [groupname] [username] &f- Make a user a moderator of a group")));
				player.sendMessage(unify(processChat(null, "&e/group unmod [groupname] [username] &f- Remove a user's moderator privileges")));
				player.sendMessage(unify(processChat(null, "&e/group kick [groupname] [username] &f- Kick a user from a group")));
			} else if (page == 3) {
				player.sendMessage(unify(processChat(null, "&e/group ban [groupname] [username] &f- Ban a user from a group")));
				player.sendMessage(unify(processChat(null, "&e/group unban [groupname] [username] &f- Unban a user from a group")));
				player.sendMessage(unify(processChat(null, "&e/group whitelist [groupname] [on/off] &f- Set whitelisting for a group")));
			}
			return;
		}
		boolean bypass = player.hasPermission("hk.siggi.bungeechat.groupchatbypass");
		if (args.length < (allowSingleArg ? 1 : 2)) {
			player.sendMessage(unify(processChat(null, "&6Group Chat (by Siggi)")));
			player.sendMessage(unify(processChat(null, "&6For help, type &e/group help")));
			if (bypass) {
				player.sendMessage(unify(processChat(null, "&6You are able to bypass group chat restrictions.")));
			}
			return;
		}

		ChatController controller = plugin.getChatController();
		String group = null;
		GroupChat gc = null;
		if (args.length >= 2 && !allowSingleArg) {
			group = args[1];
			gc = controller.getChat(group);
			if (gc != null) {
				if (gc.hasAntiBypass()) {
					bypass = false;
				}
			}
		}
		if (!allowSingleArg && !args[0].equals("create") && gc == null) {
			player.sendMessage(unify(processChat(null, "&cThat chat does not exist.")));
			return;
		}
		UUID playerUUID = player.getUniqueId();
		String targetPlayerName = null;
		UUID targetPlayer = null;
		PlayerNameHandler playerNameHandler = plugin.getPlayerNameHandler();
		switch (args[0]) {
			case "add":
			case "mod":
			case "unmod":
			case "kick":
			case "ban":
			case "unban": {
				targetPlayerName = args[2];
				targetPlayer = playerNameHandler.getPlayerByName(targetPlayerName);
				if (targetPlayer == null) {
					player.sendMessage(unify(processChat(null, "&cCould not find that player.")));
					return;
				} else {
					targetPlayerName = playerNameHandler.getNameByPlayer(targetPlayer);
				}
			}
			break;
		}

		switch (args[0]) {
			case "create": {
				if (gc != null) {
					player.sendMessage(unify(processChat(null, "&cA chat with that name already exists.")));
					return;
				}
				int numberOfChatsIOwn = 0;
				for (GroupChat chat : controller.getChats()) {
					if (chat.isOwner(player)) {
						numberOfChatsIOwn += 1;
					}
				}
				int maxChats = 3;
				if (!bypass && numberOfChatsIOwn >= maxChats) {
					player.sendMessage(unify(processChat(null, "&cYou already own " + numberOfChatsIOwn + " chats. (Maximum of " + maxChats + ")")));
				} else {
					if (ChatController.isGroupNameAllowed(group)) {
						if (group.length() > 16) {
							player.sendMessage(unify(processChat(null, "&cThat group chat name is too long.")));
						} else if (ChatController.canonicalName(group).length() < 3) {
							player.sendMessage(unify(processChat(null, "&cThat group chat name is too short.")));
						} else {
							GroupChat createChat = controller.createChat(args[1], player);
							player.sendMessage(unify(processChat(null, "&6You've created a new chat &b" + createChat.getName() + "&6. To send messages to it, &b/g " + createChat.getName() + " [msg]&6. To add friends, &b/group add " + createChat.getName() + " [friendname]&6.")));
						}
					} else {
						player.sendMessage(unify(processChat(null, "&cThat group chat name contains invalid characters.")));
					}
				}
			}
			break;
			case "delete": {
				if (bypass || gc.getOwner().equals(player.getUniqueId())) {
					controller.deleteChat(gc);
					player.sendMessage(unify(processChat(null, "&6You have deleted the chat &b" + gc.getName() + "&6.")));
				} else {
					player.sendMessage(unify(processChat(null, "&cYou don't have permission to do this.")));
				}
			}
			break;
			case "list": {
				boolean needCommaMember = false;
				boolean needCommaListening = false;
				TextComponent member = new TextComponent("");
				plugin.addAll(member, processChat(null, "&6Chats you are in: "));
				TextComponent comma = new TextComponent(", ");
				for (GroupChat c : controller.getChats()) {
					boolean ismember = c.isMember(player);
					boolean isjoined = c.isJoined(player);
					if (ismember || isjoined) {
						if (needCommaMember) {
							member.addExtra(comma);
						} else {
							needCommaMember = true;
						}
						ArrayList<BaseComponent> chatComponent = processChat(null, (c.isOwner(player) ? "&e" : "") + c.getName());
						ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/group info " + c.getCanonicalName());
						HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click for info")});
						plugin.addEventsToAll(chatComponent, click, hover);
						plugin.addAll(member, chatComponent);
						if (!isjoined) {
							chatComponent = processChat(null, " &4(muted)");
							click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/group unmute " + c.getCanonicalName());
							hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to unmute")});
							plugin.addEventsToAll(chatComponent, click, hover);
							plugin.addAll(member, chatComponent);
						}
					}
				}
				player.sendMessage(member);
			}
			break;
			case "listall": {
				List<GroupChat> chats = controller.getChats();
				Collections.sort(chats, orderChatsByLastActivity);
				int perPage = 8;
				int maxPage = (chats.size() + (perPage - 1)) / perPage;
				int curPage = 1;
				try {
					curPage = Integer.parseInt(args[1]);
				} catch (Exception e) {
				}
				int startAt = perPage * (curPage - 1);
				int endAt = Math.min(startAt + perPage, chats.size());
				player.sendMessage(unify(processChat(null, "&6All Group Chats (" + curPage + "/" + maxPage + ")")));
				for (int i = startAt; i < endAt; i++) {
					GroupChat c = chats.get(i);
					TextComponent chatInfo = new TextComponent("");
					ArrayList<BaseComponent> chatName = processChat(null, "&a" + c.getName());
					ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/group info " + c.getCanonicalName());
					HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click for more info")});
					plugin.addEventsToAll(chatName, click, hover);
					plugin.addAll(chatInfo, chatName);
					TextComponent comma = new TextComponent(", ");
					TextComponent openbracket = new TextComponent(" (");
					chatInfo.addExtra(openbracket);
					if (c.getOwner() != null) {
						TextComponent owner = new TextComponent(plugin.getPlayerNameHandler().getNameByPlayer(c.getOwner()));
						owner.setColor(ChatColor.AQUA);
						chatInfo.addExtra(owner);
						chatInfo.addExtra(comma);
					}
					if (c.isWhitelisted()) {
						TextComponent whitelist = new TextComponent("Whitelist");
						whitelist.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Whitelist is enabled on this chat")}));
						whitelist.setColor(ChatColor.YELLOW);
						chatInfo.addExtra(whitelist);
						chatInfo.addExtra(comma);
					} else if (c.getPermissionNode() != null) {
						TextComponent permissionBased = new TextComponent("Permission");
						permissionBased.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Permission based whitelist is enabled on this chat")}));
						permissionBased.setColor(ChatColor.GREEN);
						chatInfo.addExtra(permissionBased);
						chatInfo.addExtra(comma);
					} else {
						TextComponent notWhitelist = new TextComponent("Open");
						notWhitelist.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("This chat is open for anyone to join")}));
						notWhitelist.setColor(ChatColor.GREEN);
						chatInfo.addExtra(notWhitelist);
						chatInfo.addExtra(comma);
					}
					if (c.hasAntiBypass()) {
						TextComponent antibypass = new TextComponent("ABP");
						antibypass.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Anti bypass is enabled on this chat")}));
						antibypass.setColor(ChatColor.RED);
						chatInfo.addExtra(antibypass);
						chatInfo.addExtra(comma);
					}
					if (c.isNotLogged()) {
						TextComponent offTheRecord = new TextComponent("OTR");
						offTheRecord.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("This chat is off the record")}));
						offTheRecord.setColor(ChatColor.LIGHT_PURPLE);
						chatInfo.addExtra(offTheRecord);
						chatInfo.addExtra(comma);
					}
					if (c.isCensorDisabled()) {
						TextComponent noCensor = new TextComponent("NC-17");
						noCensor.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("The chat censor is disabled for this chat")}));
						noCensor.setColor(ChatColor.DARK_PURPLE);
						chatInfo.addExtra(noCensor);
						chatInfo.addExtra(comma);
					}
					TextComponent messageCount = new TextComponent(c.getMessageCount() + " msgs");
					messageCount.setColor(ChatColor.GOLD);
					chatInfo.addExtra(messageCount);
					TextComponent closeBracket = new TextComponent(")");
					chatInfo.addExtra(closeBracket);
					player.sendMessage(chatInfo);
				}
			}
			break;
			case "info": {
				player.sendMessage(unify(processChat(null, "&6Group " + gc.getName())));
				UUID ownerUUID = gc.getOwner();
				if (ownerUUID != null) {
					String ownerName = plugin.getPlayerNameHandler().getNameByPlayer(ownerUUID);
					if (ownerName != null) {
						player.sendMessage(unify(processChat(null, "&6Owner: &b" + ownerName)));
					}
				}
				StringBuilder moderators = new StringBuilder("&6Moderators: &f");
				boolean needComma = false;
				for (UUID uuid : gc.getModerators()) {
					if (uuid.equals(ownerUUID)) {
						continue;
					}
					String moderatorName = plugin.getPlayerNameHandler().getNameByPlayer(uuid);
					if (moderatorName != null) {
						if (needComma) {
							moderators.append(", ");
						} else {
							needComma = true;
						}
						moderators.append(moderatorName);
					}
				}
				player.sendMessage(unify(processChat(null, moderators.toString())));
				StringBuilder members = new StringBuilder("&6Members: &f");
				needComma = false;
				for (UUID uuid : gc.getMembers()) {
					if (uuid.equals(ownerUUID)) {
						continue;
					}
					String userName = plugin.getPlayerNameHandler().getNameByPlayer(uuid);
					if (userName != null) {
						if (needComma) {
							members.append(", ");
						} else {
							needComma = true;
						}
						members.append(userName);
					}
				}
				player.sendMessage(unify(processChat(null, members.toString())));
				StringBuilder listeners = new StringBuilder("&6Listening: &f");
				needComma = false;
				for (UUID uuid : gc.getUsers()) {
					String userName = plugin.getPlayerNameHandler().getNameByPlayer(uuid);
					if (userName != null) {
						if (needComma) {
							listeners.append(", ");
						} else {
							needComma = true;
						}
						listeners.append(userName);
					}
				}
				player.sendMessage(unify(processChat(null, listeners.toString())));
				StringBuilder banned = new StringBuilder("&6Banned: &f");
				needComma = false;
				for (UUID uuid : gc.getBanned()) {
					String userName = plugin.getPlayerNameHandler().getNameByPlayer(uuid);
					if (userName != null) {
						if (needComma) {
							banned.append(", ");
						} else {
							needComma = true;
						}
						banned.append(userName);
					}
				}
				player.sendMessage(unify(processChat(null, banned.toString())));
				if (gc.getPermissionNode() == null) {
					player.sendMessage(unify(processChat(null, "&6Whitelist: &f" + gc.isWhitelisted())));
				} else {
					player.sendMessage(unify(processChat(null, "&6Whitelist: &fPermission based")));
				}
				player.sendMessage(unify(processChat(null, "&6Messages sent in this group: &f" + gc.getMessageCount())));
				if (gc.hasAntiBypass()) {
					player.sendMessage(unify(processChat(null, "&6Note: Anti Bypass enabled for this chat.")));
				}
				if (gc.isNotLogged()) {
					player.sendMessage(unify(processChat(null, "&6Note: This chat is off the record.")));
				}
				if (gc.isCensorDisabled()) {
					player.sendMessage(unify(processChat(null, "&6Note: The censor is disabled for this chat.")));
				}
				TextComponent actions = new TextComponent("Actions:");
				if (gc.canJoin(player) && !gc.isJoined(player)) {
					ArrayList<BaseComponent> button = processChat(null, "&b [Unmute]");
					ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/group unmute " + gc.getCanonicalName());
					HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Unmute This Group")});
					plugin.addEventsToAll(button, click, hover);
					plugin.addAll(actions, button);
				}
				if (gc.isJoined(player)) {
					ArrayList<BaseComponent> button = processChat(null, "&b [Mute]");
					ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/group mute " + gc.getCanonicalName());
					HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Mute This Group")});
					plugin.addEventsToAll(button, click, hover);
					plugin.addAll(actions, button);
				}
				if (gc.isMember(player)) {
					ArrayList<BaseComponent> button = processChat(null, "&b [Leave]");
					ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/group leave " + gc.getCanonicalName());
					HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Leave This Group")});
					plugin.addEventsToAll(button, click, hover);
					plugin.addAll(actions, button);
				}
				if (gc.isModerator(player)) {
					{
						ArrayList<BaseComponent> button = processChat(null, "&b [Add]");
						ClickEvent click = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/group add " + gc.getCanonicalName() + " ");
						HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Add a user to this group")});
						plugin.addEventsToAll(button, click, hover);
						plugin.addAll(actions, button);
					}
					{
						ArrayList<BaseComponent> button = processChat(null, "&b [Kick]");
						ClickEvent click = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/group kick " + gc.getCanonicalName() + " ");
						HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Kick a user from this group")});
						plugin.addEventsToAll(button, click, hover);
						plugin.addAll(actions, button);
					}
					if (gc.isOwner(player)) {
						{
							ArrayList<BaseComponent> button = processChat(null, "&b [Mod]");
							ClickEvent click = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/group mod " + gc.getCanonicalName() + " ");
							HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Add a moderator to this group")});
							plugin.addEventsToAll(button, click, hover);
							plugin.addAll(actions, button);
						}
						{
							ArrayList<BaseComponent> button = processChat(null, "&b [Unmod]");
							ClickEvent click = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/group unmod " + gc.getCanonicalName() + " ");
							HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Remove a moderator from this group")});
							plugin.addEventsToAll(button, click, hover);
							plugin.addAll(actions, button);
						}
					}
					{
						ArrayList<BaseComponent> button = processChat(null, "&b [Ban]");
						ClickEvent click = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/group ban " + gc.getCanonicalName() + " ");
						HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Ban a user from this group")});
						plugin.addEventsToAll(button, click, hover);
						plugin.addAll(actions, button);
					}
					{
						ArrayList<BaseComponent> button = processChat(null, "&b [Unban]");
						ClickEvent click = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/group unban " + gc.getCanonicalName() + " ");
						HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Unban a user from this group")});
						plugin.addEventsToAll(button, click, hover);
						plugin.addAll(actions, button);
					}
				}
				player.sendMessage(actions);
			}
			break;
			case "unmute": {
				if (bypass || gc.allowJoining(player)) {
					gc.addUser(playerUUID);
					player.sendMessage(unify(processChat(null, "&6Unmuted the chat &6" + gc.getName() + "&r.")));
				} else {
					player.sendMessage(unify(processChat(null, "&cYou don't have access to this chat.")));
				}
			}
			break;
			case "mute": {
				gc.removeUser(playerUUID);
				player.sendMessage(unify(processChat(null, "&6Muted the chat &b" + gc.getName() + "&6.")));
			}
			break;
			case "leave": {
				if (gc.getOwner().equals(playerUUID)) {
					player.sendMessage(unify(processChat(null, "&cYou can't leave your own chat. You can &b/group delete&c it instead.")));
					return;
				}
				gc.removeModerator(playerUUID);
				gc.removeUser(playerUUID);
				gc.removeMember(playerUUID);
				player.sendMessage(unify(processChat(null, "&6You left the chat &b" + gc.getName() + "&6.")));
			}
			break;
			case "add": {
				if (bypass || gc.isModerator(player)) {
					gc.removeBanned(targetPlayer);
					gc.addMember(targetPlayer);
					gc.addUser(targetPlayer);
					player.sendMessage(unify(processChat(null, "&6Added " + targetPlayerName + " to the chat &b" + gc.getName() + "&6.")));
				} else {
					player.sendMessage(unify(processChat(null, "&cYou don't have permission to do this.")));
				}
			}
			break;
			case "mod": {
				if (bypass || gc.getOwner().equals(playerUUID)) {
					gc.removeBanned(targetPlayer);
					gc.addMember(targetPlayer);
					gc.addModerator(targetPlayer);
					player.sendMessage(unify(processChat(null, "&6Added " + targetPlayerName + " as a moderator in chat &b" + gc.getName() + "&6.")));
				} else {
					player.sendMessage(unify(processChat(null, "&cYou don't have permission to do this.")));
				}
			}
			break;
			case "unmod": {
				if (bypass || gc.getOwner().equals(playerUUID)) {
					gc.removeModerator(targetPlayer);
					player.sendMessage(unify(processChat(null, "&6Removed " + targetPlayerName + " as a moderator in chat &b" + gc.getName() + "&6.")));
				} else {
					player.sendMessage(unify(processChat(null, "&cYou don't have permission to do this.")));
				}
			}
			break;
			case "kick": {
				if (bypass || gc.isModerator(player)) {
					if (gc.isModerator(targetPlayer) && !(bypass || gc.getOwner().equals(playerUUID))) {
						player.sendMessage(unify(processChat(null, "&cYou don't have permission to do this.")));
					} else {
						player.sendMessage(unify(processChat(null, "&6Kicked " + targetPlayerName + " from the chat &b" + gc.getName() + "&6.")));
						gc.removeModerator(targetPlayer);
						gc.removeMember(targetPlayer);
						gc.removeUser(targetPlayer);
					}
				} else {
					player.sendMessage(unify(processChat(null, "&cYou don't have permission to do this.")));
				}
			}
			break;
			case "ban": {
				if (bypass || gc.isModerator(player)) {
					if (gc.isModerator(targetPlayer) && !(bypass || gc.getOwner().equals(playerUUID))) {
						player.sendMessage(unify(processChat(null, "&cYou don't have permission to do this.")));
					} else {
						player.sendMessage(unify(processChat(null, "&6Banned " + targetPlayerName + " from the chat &b" + gc.getName() + "&6.")));
						gc.removeModerator(targetPlayer);
						gc.removeMember(targetPlayer);
						gc.removeUser(targetPlayer);
						gc.addBanned(targetPlayer);
					}
				} else {
					player.sendMessage(unify(processChat(null, "&cYou don't have permission to do this.")));
				}
			}
			break;
			case "unban": {
				if (bypass || gc.isModerator(player)) {
					gc.removeBanned(targetPlayer);
				} else {
					player.sendMessage(unify(processChat(null, "&cYou don't have permission to do this.")));
				}
			}
			break;
			case "whitelist": {
				if (bypass || gc.isModerator(player)) {
					boolean whitelist = Util.parseBool(args[2]);
					gc.setWhitelist(whitelist);
					player.sendMessage(unify(processChat(null, "&6Set whitelist for chat &b" + gc.getName() + "&6: " + whitelist)));
				} else {
					player.sendMessage(unify(processChat(null, "&cYou don't have permission to do this.")));
				}
			}
			break;
			default: {
				player.sendMessage(unify(processChat(null, "&cUnknown command " + args[0] + ".")));
			}
			break;
		}
	}

	private final Comparator<GroupChat> orderChatsByLastActivity = (GroupChat o1, GroupChat o2) -> {
		long l1 = o1.getLastActivity();
		long l2 = o2.getLastActivity();
		if (l1 > l2) {
			return -1;
		} else if (l2 > l1) {
			return 1;
		} else {
			return 0;
		}
	};

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		ProxiedPlayer p = (ProxiedPlayer) sender;
		PlayerSession session = plugin.getSession(p);
		List<String> actions = Arrays.asList(new String[]{
			"create", "delete", "list", "listall", "info", "mute", "unmute", "leave", "whitelist", "add", "mod", "unmod", "kick", "ban", "unban"
		});
		List<String> actionsWithGroup = Arrays.asList(new String[]{
			// exclude "create" because it doesn't need autocompletion
			"delete", "info", "mute", "unmute", "leave", "whitelist"
		});
		List<String> actionsWithPlayerName = Arrays.asList(new String[]{
			"add", "mod", "unmod", "kick", "ban", "unban"
		});
		List<String> result = new LinkedList<>();
		String enteredAction = args[0];
		ChatController controller = plugin.getChatController();
		UUIDCache uc = plugin.getUUIDCache();
		NicknameCache nc = plugin.getNicknameCache();
		Consumer<String> addSuggestion = (suggestion) -> {
			if (suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
				result.add(suggestion);
			}
		};
		Consumer<UUID> addUUIDSuggestion = (suggestion) -> {
			String nick = nc.getNickname(suggestion);
			if (nick != null) {
				addSuggestion.accept(nick);
			}
			addSuggestion.accept(uc.getNameFromUUID(suggestion));
		};
		boolean bypassPerm = p.hasPermission("hk.siggi.bungeechat.groupchatbypass");
		boolean bypass = bypassPerm && session.groupTabBypass;
		if (args.length == 1) { // action
			actions.forEach(addSuggestion);
			if (bypassPerm) {
				addSuggestion.accept("tabbypass");
			}
		} else if (args.length == 2) { // group name
			if (actionsWithGroup.contains(enteredAction) || actionsWithPlayerName.contains(enteredAction)) {
				List<GroupChat> chats = controller.getChats();
				Collections.sort(chats, orderChatsByLastActivity);
				for (GroupChat chat : chats) {
					boolean allowBypass = bypass && !chat.hasAntiBypass();
					if (enteredAction.equals("unmute") && !allowBypass && (!chat.canJoin(p) || chat.isJoined(p))) {
						continue;
					} else if (enteredAction.equals("delete") && !allowBypass && !chat.isOwner(p)) {
						continue;
					} else if (enteredAction.equals("mute") && !chat.isJoined(p)) {
						continue;
					} else if (enteredAction.equals("leave") && !chat.isMember(p)) {
						continue;
					} else if (enteredAction.equals("whitelist") && !allowBypass && !chat.isModerator(p)) {
						continue;
					} else if (enteredAction.equals("add") && !allowBypass && !chat.isModerator(p)) {
						continue;
					} else if (enteredAction.equals("mod") && !allowBypass && !chat.isOwner(p)) {
						continue;
					} else if (enteredAction.equals("unmod") && !allowBypass && !chat.isOwner(p)) {
						continue;
					} else if (enteredAction.equals("kick") && !allowBypass && !chat.isModerator(p)) {
						continue;
					} else if (enteredAction.equals("ban") && !allowBypass && !chat.isModerator(p)) {
						continue;
					} else if (enteredAction.equals("unban") && !allowBypass && !chat.isModerator(p)) {
						continue;
					}
					addSuggestion.accept(chat.getCanonicalName());
					if (result.size() >= 100) {
						break;
					}
				}
			}
		} else if (args.length == 3) { // third argument, usually player name
			GroupChat chat = controller.getChat(args[1]);
			if (chat != null) {
				if (actionsWithPlayerName.contains(enteredAction)) {
					if (enteredAction.equals("mod")) {
						List<UUID> users = chat.getUsers();
						users.removeAll(chat.getModerators());
						users.remove(chat.getOwner());
						users.forEach(addUUIDSuggestion);
					} else if (enteredAction.equals("unmod")) {
						chat.getModerators().forEach(addUUIDSuggestion);
					} else if (enteredAction.equals("kick")) {
						chat.getUsers().forEach(addUUIDSuggestion);
					} else if (enteredAction.equals("unban")) {
						chat.getBanned().forEach(addUUIDSuggestion);
					} else {
						List<UUID> playersWithNamesStartingWith = uc.getPlayersWithNamesStartingWith(args[2], 100);
						playersWithNamesStartingWith.forEach(addUUIDSuggestion);
					}
				} else if (enteredAction.equals("whitelist")) {
					Arrays.asList(new String[]{
						"yes", "no", "on", "off", "true", "false", "1", "0"
					}).forEach(addSuggestion);
				}
			}
		}
		return result;
	}
}
