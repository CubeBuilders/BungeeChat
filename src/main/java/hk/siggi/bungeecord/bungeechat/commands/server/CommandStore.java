package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class CommandStore extends Command {
	public final BungeeChat plugin;

	public CommandStore(BungeeChat plugin) {
		super("store", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] strings) {
		MessageSender.sendMessage(sender, "&6--------------------");
		MessageSender.sendMessage(sender, "&6Click for our webstore! -> <https://cube.builders/store><cube.builders/store>");
		MessageSender.sendMessage(sender, "&6--------------------");
	}
}
