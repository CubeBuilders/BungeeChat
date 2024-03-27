package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.PlayerSession;
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
		PlayerSession session = BungeeChat.getSession(p);
		String code = strings[0];
		String ipAddress = null;
		if (strings.length < 2 || !strings[1].equals(session.nonce)) {
			ipAddress = ((InetSocketAddress) p.getSocketAddress()).getAddress().getHostAddress();
		}
		String result = APIUtil.codeLogin(p.getUniqueId(), strings[0], ipAddress);
		String extraData = null;
		if (result.contains(":")) {
			extraData = result.substring(result.indexOf(":") + 1);
			result = result.substring(0, result.indexOf(":"));
		} else {
		}
		switch (result) {
			case "OK":
				MessageSender.sendMessage(p, "&6You have been logged in! You can return to your web browser!");
				break;
			case "IP_MISMATCH":
				MessageSender.sendMessage(p, "");
				MessageSender.sendMessage(p, "&e==================================================");
				MessageSender.sendMessage(p, "&c&lPay attention and READ:");
				MessageSender.sendMessage(p, "&6Make sure you got this code from &dhttps://cube.builders &6and &c&onot&r&6 from another website or from someone who asked you to type it in!");
				MessageSender.sendMessage(p, "&e==================================================");
				if (extraData != null) {
					if (extraData.contains("/")) {
						extraData = extraData.substring(extraData.indexOf("/") + 1);
					}
					MessageSender.sendMessage(p, "&6Location of the browser that generated this code:");
					MessageSender.sendMessage(p, "&e - " + extraData);
				}
				MessageSender.sendMessage(p, "&6If you're sure you're logging into the website, </login " + code + " " + session.nonce + "><click here>.");
				MessageSender.sendMessage(p, "&6");
				MessageSender.sendMessage(p, "&e==================================================");
				MessageSender.sendMessage(p, "&6You received this message your IP address does not match the IP address of the web browser the code was generated at.");
				MessageSender.sendMessage(p, "&e==================================================");
				MessageSender.sendMessage(p, "");
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
