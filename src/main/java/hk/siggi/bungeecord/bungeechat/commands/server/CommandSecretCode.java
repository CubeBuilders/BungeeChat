package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.util.APIUtil;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.processChat;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.unify;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandSecretCode extends Command {

	private final BungeeChat plugin;

	public CommandSecretCode(BungeeChat plugin) {
		super("secretcode");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender cs, String[] strings) {
		if (!(cs instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer p = (ProxiedPlayer) cs;
		String secretCode = APIUtil.genSecretCode(p.getUniqueId());
		if (secretCode == null) {
			p.sendMessage(unify(processChat(null, "&6A secret code was not able to be generated at this time. Please try again later.")));
		} else {
			p.sendMessage(unify(processChat(null, "&6Your Secret Code is: &b" + secretCode)));
			p.sendMessage(unify(processChat(null, "&6Enter it ONLY on CubeBuilders.net or Siggi.io!")));
		}
	}

}
