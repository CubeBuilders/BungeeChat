package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.util.DiscordBotAPI;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandLinkDiscord extends Command {

	private final BungeeChat plugin;

	public CommandLinkDiscord(BungeeChat plugin) {
		super("linkdiscord");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender cs, String[] strings) {
		if (!(cs instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer p = (ProxiedPlayer) cs;
		if (strings.length < 1) {
			MessageSender.sendMessage(p, "&6This command is used to link your Discord account.");
			return;
		}
		String code = strings[0];
		String result = DiscordBotAPI.discordLink(p.getUniqueId(), code);
		switch (result) {
			case "OK":
				MessageSender.sendMessage(p, "&6Your account has been linked!");
				break;
			case "INVALID":
				MessageSender.sendMessage(p, "&6The code you entered is invalid or already used.");
				break;
			case "ENDPOINT_NOT_SET":
				MessageSender.sendMessage(p, "&6The bot API endpoint is not set.");
				break;
			case "EXCEPTION":
			default:
				MessageSender.sendMessage(p, "&6There was a problem logging in. Try again, and if it still fails then try later.");
				break;
		}
	}

}
