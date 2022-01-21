package hk.siggi.bungeecord.bungeechat.commands.messaging;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.PlayerSession;
import hk.siggi.bungeecord.bungeechat.chat.handler.ChatHandler;
import hk.siggi.bungeecord.bungeechat.chat.handler.PrivateChatHandler;
import hk.siggi.bungeecord.bungeechat.util.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandReply extends Command {

	public final BungeeChat plugin;

	public CommandReply(BungeeChat plugin) {
		super("r", null, "er", "reply", "ereply");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			return;
		}
		/*if (args.length < 1) {
		 BaseComponent usage = new TextComponent("Usage: ");
		 usage.setColor(ChatColor.AQUA);
		 BaseComponent extra = new TextComponent("/r <message>");
		 extra.setColor(ChatColor.WHITE);
		 usage.addExtra(extra);
		 MessageSender.sendMessage(sender, usage);
		 return;
		 }*/
		ProxiedPlayer player = (ProxiedPlayer) sender;
		String destination = BungeeChat.getInstance().getLastMessage(player.getName());
		if (destination == null) {
			BaseComponent message = new TextComponent("");

			BaseComponent extra = new TextComponent("You have not yet sent or received a message.");
			extra.setColor(ChatColor.RED);
			message.addExtra(extra);

			MessageSender.sendMessage(sender, message);
			return;
		}
		ProxiedPlayer destinationPlayer = plugin.getProxy().getPlayer(destination);
		if (destinationPlayer == null) {
			BaseComponent message = new TextComponent("");

			BaseComponent extra = new TextComponent("The player ");
			extra.setColor(ChatColor.RED);
			message.addExtra(extra);

			extra = new TextComponent(destination);
			extra.setColor(ChatColor.WHITE);
			message.addExtra(extra);

			extra = new TextComponent(" is currently offline.");
			extra.setColor(ChatColor.RED);
			message.addExtra(extra);

			MessageSender.sendMessage(sender, message);
			return;
		}

		BungeeChat bc = BungeeChat.getInstance();
		ChatHandler chatHandler = new PrivateChatHandler(bc.getChatController(), destinationPlayer);
		if (args.length < 1) {
			// User didn't enter a message.
			// Set their default chat to PM to this person.
			PlayerSession session = BungeeChat.getSession(player);
			session.setChatHandler(chatHandler, true);
			return;
		}

		String chatLine = Util.getLine(args, 0);
		bc.getChatController().doProcessChat(chatHandler, player, chatLine);
	}
}
