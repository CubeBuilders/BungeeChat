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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandShadowMute extends Command implements TabExecutor {

	public final BungeeChat plugin;

	public CommandShadowMute(BungeeChat plugin) {
		super("shadowmute", null, "silentmute");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length < 1 || args.length > 2) {
			return;
		}
		String receiver = args[0];
		UUID receiverUUID = plugin.getPlayerNameHandler().getPlayerByName(receiver);
		ProxiedPlayer player = null;
		if (sender instanceof ProxiedPlayer) {
			player = (ProxiedPlayer) sender;
			if (!player.hasPermission("hk.siggi.bungeechat.shadowmute")) {
				MessageSender.sendMessage(player, Util.randomNotPermittedMessage());
				return;
			}
		}
		if (receiverUUID == null) {
			Pattern pattern = Pattern.compile(".*" + receiver + ".*", Pattern.CASE_INSENSITIVE);
			Collection<ProxiedPlayer> playerCollection = plugin.getProxy().getPlayers();
			ProxiedPlayer[] players = playerCollection.toArray(new ProxiedPlayer[playerCollection.size()]);
			for (int i = 0; i < players.length; i++) {
				Matcher matcher = pattern.matcher(players[i].getName());
				if (matcher.matches()) {
					receiverUUID = players[i].getUniqueId();
					break;
				}
			}
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
		}
		if (args.length == 1) {
			args = new String[]{args[0], "on"};
		}
		PlayerAccount info = plugin.getPlayerInfo(receiverUUID);
		if (!args[1].equals("check")) {
			boolean on = args[1].equalsIgnoreCase("on")
					|| args[1].equalsIgnoreCase("1")
					|| args[1].equalsIgnoreCase("yes");
			info.setShadowMuted(on);
			BaseComponent message = new TextComponent("Set shadow mute for ");
			BaseComponent username = new TextComponent(plugin.getUUIDCache().getNameFromUUID(receiverUUID));
			BaseComponent onTxt = new TextComponent(on ? " On." : " Off.");
			message.setColor(ChatColor.GOLD);
			username.setColor(ChatColor.AQUA);
			onTxt.setColor(ChatColor.GREEN);
			message.addExtra(username);
			message.addExtra(onTxt);
			MessageSender.sendMessage(sender, message);
			return;
		}
		BaseComponent message = new TextComponent("Shadow mute for ");
		BaseComponent username = new TextComponent(plugin.getUUIDCache().getNameFromUUID(receiverUUID));
		BaseComponent onTxt = new TextComponent(info.isShadowMuted() ? " On." : " Off.");
		message.setColor(ChatColor.GOLD);
		username.setColor(ChatColor.AQUA);
		onTxt.setColor(ChatColor.GREEN);
		message.addExtra(username);
		message.addExtra(onTxt);
		MessageSender.sendMessage(sender, message);
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
			addSuggestion.accept("on");
			addSuggestion.accept("off");
			addSuggestion.accept("check");
		}
		return list;
	}
}
