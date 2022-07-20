package hk.siggi.bungeecord.bungeechat.commands.messaging;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.PlayerNameHandler;
import hk.siggi.bungeecord.bungeechat.PlayerSession;
import hk.siggi.bungeecord.bungeechat.chat.handler.ChatHandler;
import hk.siggi.bungeecord.bungeechat.chat.handler.PrivateChatHandler;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import hk.siggi.bungeecord.bungeechat.util.Util;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandMsg extends Command implements TabExecutor {

	public final BungeeChat plugin;

	public CommandMsg(BungeeChat plugin) {
		super("msg", null, "m", "w", "t", "emsg", "tell", "etell", "whisper", "ewhisper", "pm", "epm");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			return;
		}
		if (args.length < 1) {
			BaseComponent usage = new TextComponent("Usage: ");
			usage.setColor(ChatColor.AQUA);
			BaseComponent extra = new TextComponent("/msg <username> <message>");
			extra.setColor(ChatColor.WHITE);
			usage.addExtra(extra);
			MessageSender.sendMessage(sender, usage);
			return;
		}
		ProxiedPlayer player = (ProxiedPlayer) sender;
		String destination = args[0];
		PlayerNameHandler playerNameHandler = plugin.getPlayerNameHandler();
		UUID plByName = playerNameHandler.getPlayerByName(destination, true);
		ProxiedPlayer destinationPlayer = plugin.getProxy().getPlayer(plByName);
//		if (destinationPlayer == null) {
//			for (ProxiedPlayer pl : plugin.getProxy().getPlayers()) {
//				if (plugin.getPlayerInfo(pl.getUniqueId()).getNickname().equals(destination)) {
//					destinationPlayer = pl;
//					break;
//				}
//			}
//		}
//		if (destinationPlayer == null) {
//			Pattern pattern = Pattern.compile(".*" + destination + ".*");
//			for (ProxiedPlayer pl : plugin.getProxy().getPlayers()) {
//				Matcher matcher = pattern.matcher(pl.getName());
//				Matcher matcher2 = pattern.matcher(plugin.getPlayerInfo(pl.getUniqueId()).getNickname());
//				if (matcher.matches() || matcher2.matches()) {
//					destinationPlayer = pl;
//					break;
//				}
//			}
//		}
		if (!sender.hasPermission("hk.siggi.bungeechat.vanish") && destinationPlayer != null) {
			// Do Not Msg Me!
			PlayerAccount destinationAccount = BungeeChat.getInstance().getPlayerInfo(destinationPlayer.getUniqueId());
			if (destinationAccount.isNoMsg()) {
				destinationPlayer = null;
			}
		}
		if (destinationPlayer == null) {
			BaseComponent message = new TextComponent("The player ");
			message.setColor(ChatColor.RED);
			BaseComponent extra = new TextComponent(plByName == null ? destination : playerNameHandler.getNameByPlayer(plByName));
			extra.setColor(ChatColor.WHITE);
			message.addExtra(extra);
			if (plByName == null) {
				extra = new TextComponent(" was not found. ");
				message.addExtra(extra);
				extra = new TextComponent("Edit?");
				extra.setColor(ChatColor.AQUA);
				extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to edit message")}));
				extra.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/m " + Util.getLine(args, 0)));
				message.addExtra(extra);
			} else {
				extra = new TextComponent(" is currently offline. ");
				message.addExtra(extra);
				extra = new TextComponent("Retry? ");
				extra.setColor(ChatColor.AQUA);
				extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to try sending again")}));
				extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/m " + Util.getLine(args, 0)));
				extra = new TextComponent("Edit?");
				extra.setColor(ChatColor.AQUA);
				extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to edit message")}));
				extra.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/m " + Util.getLine(args, 0)));
				message.addExtra(extra);
			}
			MessageSender.sendMessage(sender, message);
			return;
		}
		BungeeChat bc = BungeeChat.getInstance();
		ChatHandler chatHandler = new PrivateChatHandler(bc.getChatController(), destinationPlayer);
		if (args.length < 2) {
			// User didn't enter a message, just a name.
			// Set their default chat to PM to this person.
			PlayerSession session = BungeeChat.getSession(player);
			session.setChatHandler(chatHandler, true);
			return;
		}
		String chatLine = Util.getLine(args, 1);
		bc.getChatController().doProcessChat(chatHandler, player, chatLine);
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		List<String> result = new LinkedList<>();
		ProxiedPlayer p = (ProxiedPlayer) sender;
		if (args.length == 1) {
			result.addAll(plugin.getPlayerNameHandler().autocompleteOnlinePlayers(args[0], sender));
		}
		return result;
	}
}
