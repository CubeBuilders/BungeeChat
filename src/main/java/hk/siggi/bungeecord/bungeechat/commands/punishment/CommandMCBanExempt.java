package hk.siggi.bungeecord.bungeechat.commands.punishment;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import hk.siggi.bungeecord.bungeechat.util.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandMCBanExempt extends Command implements TabExecutor {
	public final BungeeChat plugin;
	public CommandMCBanExempt(BungeeChat plugin) {
		super("mcbanexempt", null);
		this.plugin = plugin;
	}
	@Override
	public void execute(CommandSender sender, String[] args) {
		UUID issuer;
		String issuerName;
		if (sender instanceof ProxiedPlayer) {
			ProxiedPlayer player = ((ProxiedPlayer) sender);
			issuer = player.getUniqueId();
			issuerName = player.getName();
			if (!player.hasPermission("hk.siggi.bungeechat.mcbanexempt")) {
				MessageSender.sendMessage(player, Util.randomNotPermittedMessage());
				return;
			}
		} else {
			issuer = BungeeChat.console;
			issuerName = "<Console>";
		}
		if (args.length < 2) {
			BaseComponent usage = new TextComponent("Usage: ");
			usage.setColor(ChatColor.AQUA);
			BaseComponent extra = new TextComponent("/mcbanexempt <name> <yes/no>");
			extra.setColor(ChatColor.WHITE);
			usage.addExtra(extra);
			MessageSender.sendMessage(sender, usage);
			return;
		}
		String receiver = args[0];
		boolean exempt = args[1].equals("y") || args[1].equals("1") || args[1].equals("yes") || args[1].equals("on") || args[1].equals("t") || args[1].equals("true");
		UUID receiverUUID = plugin.getPlayerNameHandler().getPlayerByName(receiver);
		if (receiverUUID == null) {
			BaseComponent message = new TextComponent("Did not find ");
			message.setColor(ChatColor.RED);
			BaseComponent extra = new TextComponent(receiver);
			extra.setColor(ChatColor.AQUA);
			message.addExtra(extra);
			extra = new TextComponent(". Did you enter the name correctly?");
			message.addExtra(extra);
			MessageSender.sendMessage(sender, message);
			return;
		}
		receiver = plugin.getUUIDCache().getNameFromUUID(receiverUUID);
		PlayerAccount playerInfo = plugin.getPlayerInfo(receiverUUID);
		playerInfo.setMCBansExempt(exempt);

		BaseComponent setBanExempt = new TextComponent(issuerName + " is making " + receiver + (exempt ? "" : " NOT") + " exempt from MCBans.com bans.");
		setBanExempt.setColor(ChatColor.AQUA);

		Collection<ProxiedPlayer> playerCollection = plugin.getProxy().getPlayers();
		ProxiedPlayer[] players = playerCollection.toArray(new ProxiedPlayer[playerCollection.size()]);
		for (int i = 0; i < players.length; i++) {
			if (players[i].hasPermission("hk.siggi.bungeechat.punishmentalert")) {
				players[i].sendMessage(setBanExempt);
			}
		}
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender cs, String[] strings) {
		List<String> list = new ArrayList<>();
		if (strings.length == 1) {
			list.addAll(plugin.getPlayerNameHandler().autocompletePlayers(strings[0]));
		} else if (strings.length == 2) {
			String a = strings[1].toLowerCase();
			Consumer<String> addSuggestion = (suggestion) -> {
				if (suggestion.toLowerCase().startsWith(a)) {
					list.add(suggestion);
				}
			};
			addSuggestion.accept("yes");
			addSuggestion.accept("no");
		}
		return list;
	}
}