package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandReload extends Command {

	public final BungeeChat plugin;

	public CommandReload(BungeeChat plugin) {
		super("reloadbc", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer p = (ProxiedPlayer) sender;
			if (!p.hasPermission("hk.siggi.bungeechat.reload")) {
				TextComponent no = new TextComponent("You can't do that.");
				no.setColor(ChatColor.RED);
				sender.sendMessage(no);
				return;
			}
		}
		plugin.reloadConfig();
		TextComponent ok = new TextComponent("Reloaded config.");
		ok.setColor(ChatColor.GREEN);
		sender.sendMessage(ok);
		return;
	}
}
