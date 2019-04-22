package hk.siggi.bungeecord.bungeechat.commands.messaging;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.PlayerSession;
import hk.siggi.bungeecord.bungeechat.chat.handler.ChatHandler;
import hk.siggi.bungeecord.bungeechat.chat.handler.PublicChatHandler;
import hk.siggi.bungeecord.bungeechat.util.Util;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandPub extends Command {

	public final BungeeChat plugin;

	public CommandPub(BungeeChat plugin) {
		super("pub", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer player = (ProxiedPlayer) sender;

		BungeeChat bc = BungeeChat.getInstance();
		ChatHandler chatHandler = new PublicChatHandler(bc.getChatController());
		if (args.length < 1) {
			// User didn't enter a message.
			// Set their default chat to Public chat.
			PlayerSession session = BungeeChat.getSession(player);
			session.setChatHandler(chatHandler, true);
			return;
		}

		String chatLine = Util.getLine(args, 0);

		bc.getChatController().doProcessChat(chatHandler, player, chatLine);
	}
}
