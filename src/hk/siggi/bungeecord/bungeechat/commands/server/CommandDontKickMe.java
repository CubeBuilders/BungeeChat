package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.PlayerSession;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandDontKickMe extends Command {

	public final BungeeChat plugin;

	public CommandDontKickMe(BungeeChat plugin) {
		super("dontkickme", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer player = (ProxiedPlayer) sender;
		if (!player.hasPermission("hk.siggi.bungeechat.dontkickme")) {
			TextComponent noPerm = new TextComponent("");
			TextComponent a = new TextComponent("You don't have permission to use this command!");
			a.setColor(ChatColor.RED);
			noPerm.addExtra(a);
			sender.sendMessage(noPerm);
			return;
		}
//		{
//			TextComponent tc = new TextComponent("");
//			TextComponent a = new TextComponent("NO!  No more /dontkickme bc everyone abuses it!  Sounds are played as a warning that you're about to get kicked, so just don't turn off your sound and don't leave your computer.");
//			a.setColor(ChatColor.RED);
//			tc.addExtra(a);
//			sender.sendMessage(tc);
//			if (true) {
//				return;
//			}
//		}
		PlayerSession session = BungeeChat.getSession(player);
		if (args.length >= 2) {
			String targetPlayer = args[1];
			session = BungeeChat.getSession(plugin.getProxy().getPlayer(targetPlayer));
		}
		int newKickTimer = Integer.MAX_VALUE;
		try {
			newKickTimer = Integer.parseInt(args[0]);
		} catch (Exception e) {
		}
		session.afkKickTimer = newKickTimer;

		session.showingAfkTimer = false;
		TextComponent a = new TextComponent("");
		TextComponent wb = new TextComponent("§6Y U WANNA AFK?! ლ(ಠ益ಠლ)");
		wb.setColor(ChatColor.GOLD);
		a.addExtra(wb);
		session.sendHotbarMessage(a);
	}
}
