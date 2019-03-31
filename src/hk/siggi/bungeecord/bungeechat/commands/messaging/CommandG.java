package hk.siggi.bungeecord.bungeechat.commands.messaging;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.PlayerSession;
import hk.siggi.bungeecord.bungeechat.chat.ChatController;
import hk.siggi.bungeecord.bungeechat.chat.GroupChat;
import hk.siggi.bungeecord.bungeechat.util.Util;
import java.util.LinkedList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandG extends Command implements TabExecutor {

	public final BungeeChat plugin;

	public CommandG(BungeeChat plugin) {
		super("g", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			return;
		}
		if (args.length < 1) {
			BaseComponent usage = new TextComponent("Usage: ");
			usage.setColor(ChatColor.AQUA);
			BaseComponent extra = new TextComponent("/g <group> <message>");
			extra.setColor(ChatColor.WHITE);
			usage.addExtra(extra);
			sender.sendMessage(usage);
			return;
		}
		ProxiedPlayer player = (ProxiedPlayer) sender;
		String destination = args[0];
		GroupChat destinationChat = plugin.getChatController().getChat(destination);
		if (destinationChat == null) {
			BaseComponent message = new TextComponent("The group ");
			message.setColor(ChatColor.RED);
			BaseComponent extra = new TextComponent(destination);
			extra.setColor(ChatColor.WHITE);
			message.addExtra(extra);
			extra = new TextComponent(" was not found. ");
			message.addExtra(extra);
			extra = new TextComponent("Edit?");
			extra.setColor(ChatColor.AQUA);
			extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to edit message")}));
			extra.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/g " + Util.getLine(args, 0)));
			message.addExtra(extra);
			sender.sendMessage(message);
			return;
		}
		if (args.length < 2) {
			// User didn't enter a message, just a name.
			// Set their default chat to PM to this person.
			BungeeChat bc = BungeeChat.getInstance();
			PlayerSession session = bc.getSession(player);
			session.setChatHandler(destinationChat, true);
			return;
		}
		String chatLine = Util.getLine(args, 1);
		plugin.getChatController().doProcessChat(destinationChat, player, chatLine);
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		List<String> result = new LinkedList<>();
		if (args.length != 1) {
			return result;
		}
		ChatController controller = plugin.getChatController();
		List<GroupChat> chats = controller.getChats();
		ProxiedPlayer p = (ProxiedPlayer) sender;
		chats.removeIf((chat) -> 
			!chat.isJoined(p) && !chat.isMember(p)
		);
		for (GroupChat chat : chats) {
			if (chat.getCanonicalName().startsWith(args[0])) {
				result.add(chat.getCanonicalName());
			}
		}
		return result;
	}
}
