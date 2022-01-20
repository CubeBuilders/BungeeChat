package hk.siggi.bungeecord.bungeechat.commands.cubetokens;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.util.Util;
import hk.siggi.cubetokens.CT;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandGiveCubeTokens extends Command {

	public final BungeeChat plugin;

	public CommandGiveCubeTokens(BungeeChat plugin) {
		super("givecubetokens", null, "givect");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] split) {
		if (sender instanceof ProxiedPlayer) {
			if (!sender.hasPermission("cubetokens.give")) {
				return;
			}
		}
		UUID uuid;
		if (split[0].length() > 16) {
			uuid = Util.uuidFromString(split[0]);
		} else {
			uuid = plugin.getPlayerNameHandler().getPlayerByName(split[0]);
		}
		if (uuid == null) {
			BaseComponent noExist = new TextComponent("Player " + split[0] + " does not exist");
			noExist.setColor(ChatColor.RED);
			sender.sendMessage(noExist);
			return;
		}
		long cubeTokens = Long.parseLong(split[1]);
		String username = plugin.getUUIDCache().getNameFromUUID(uuid);
		String reason = "Admin Adjustment";
		if (split.length >= 3) {
			reason = split[2];
			for (int i = 3; i < split.length; i++) {
				reason += " " + split[i];
			}
		}
		if (CT.get().giveCubeTokens(uuid, cubeTokens, reason)) {
			BaseComponent ok = new TextComponent("Gave " + username + " " + cubeTokens + " CT.");
			ok.setColor(ChatColor.GREEN);
			sender.sendMessage(ok);
		} else {
			BaseComponent fail = new TextComponent("Transaction rejected by CubeTokens server.");
			fail.setColor(ChatColor.RED);
			sender.sendMessage(fail);
		}
	}
}
