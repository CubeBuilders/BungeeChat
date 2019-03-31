package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
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
		String genSecretCode = plugin.genSecretCode(p.getUniqueId());
		p.sendMessage(plugin.unify(plugin.processChat(null, "&6Your Secret Code is: &b" + genSecretCode)));
		p.sendMessage(plugin.unify(plugin.processChat(null, "&6Enter it ONLY on CubeBuilders.net or Siggi.io!")));
	}

}
