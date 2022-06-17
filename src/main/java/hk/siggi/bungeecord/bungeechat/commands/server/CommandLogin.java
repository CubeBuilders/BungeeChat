package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.util.APIUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import java.net.InetSocketAddress;

public class CommandLogin extends Command {

	private final BungeeChat plugin;

	public CommandLogin(BungeeChat plugin) {
		super("login");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender cs, String[] strings) {
		if (!(cs instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer p = (ProxiedPlayer) cs;
		if (strings.length < 1) {
			MessageSender.sendMessage(p, "&6This command is used to log into our website.");
			return;
		}
		String code = strings[0];
		String ipAddress = null;
		if (strings.length < 2 || !strings[1].equals("no-check-ip")) {
			ipAddress = ((InetSocketAddress) p.getSocketAddress()).getAddress().getHostAddress();
		}
		String result = APIUtil.codeLogin(p.getUniqueId(), strings[0], ipAddress);
		switch (result) {
			case "OK":
				MessageSender.sendMessage(p, "&6You have been logged in! You can return to your web browser!");
				break;
			case "IP_MISMATCH":
				MessageSender.sendMessage(p, "&4&lPay attention and READ: &r&6Make sure you got this code from https://cubebuilders.net and &4not&6 from a different website or from someone who asked you to type it in!");
				MessageSender.sendMessage(p, "&6&lIf you're sure you got this code from the right place, </login " + code + " no-check-ip><click here to continue the login>.");
				break;
			case "INVALID":
				MessageSender.sendMessage(p, "&6The code you entered is invalid or already used.");
				break;
			case "EXCEPTION":
			default:
				MessageSender.sendMessage(p, "&6There was a problem logging in. Try again, and if it still fails then try later.");
				break;
		}
	}

}
