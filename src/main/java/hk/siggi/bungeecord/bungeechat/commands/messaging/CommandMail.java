package hk.siggi.bungeecord.bungeechat.commands.messaging;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.PlayerNameHandler;
import hk.siggi.bungeecord.bungeechat.PlayerSession;
import hk.siggi.bungeecord.bungeechat.player.Mail;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import hk.siggi.bungeecord.bungeechat.util.Util;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandMail extends Command implements TabExecutor {
	
	public final BungeeChat plugin;
	
	public CommandMail(BungeeChat plugin) {
		super("mail", null);
		this.plugin = plugin;
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			return;
		}
		if (args.length < 1) {
			{
				BaseComponent usage = new TextComponent("Usage: ");
				usage.setColor(ChatColor.AQUA);
				BaseComponent extra = new TextComponent("/mail send <name> <message> - Send a message");
				extra.setColor(ChatColor.WHITE);
				usage.addExtra(extra);
				MessageSender.sendMessage(sender, usage);
			}
			{
				BaseComponent usage = new TextComponent("Usage: ");
				usage.setColor(ChatColor.AQUA);
				BaseComponent extra = new TextComponent("/mail read - Read messages");
				extra.setColor(ChatColor.WHITE);
				usage.addExtra(extra);
				MessageSender.sendMessage(sender, usage);
			}
			{
				BaseComponent usage = new TextComponent("Usage: ");
				usage.setColor(ChatColor.AQUA);
				BaseComponent extra = new TextComponent("/mail clear - Deletes all your messages");
				extra.setColor(ChatColor.WHITE);
				usage.addExtra(extra);
				MessageSender.sendMessage(sender, usage);
			}
			return;
		}
		ProxiedPlayer player = (ProxiedPlayer) sender;
		PlayerAccount playerInfo = plugin.getPlayerInfo(player.getUniqueId());
		PlayerSession session = BungeeChat.getSession(player);
		PlayerNameHandler playerNameHandler = plugin.getPlayerNameHandler();
		if (args[0].equalsIgnoreCase("send")) {
			if (args.length < 3) {
				BaseComponent usage = new TextComponent("Usage: ");
				usage.setColor(ChatColor.AQUA);
				BaseComponent extra = new TextComponent("/mail send <name> <message>");
				extra.setColor(ChatColor.WHITE);
				usage.addExtra(extra);
				MessageSender.sendMessage(sender, usage);
				return;
			}
			if (session.user.isMuted()) {
				plugin.youAreMuted(player, session.user);
				return;
			}
			if (args[1].equalsIgnoreCase("Server")) {
				BaseComponent fail = new TextComponent("Cannot send mail: Server does not accept incoming messages. Perhaps you meant to send it to Siggi88?");
				fail.setColor(ChatColor.RED);
				MessageSender.sendMessage(sender, fail);
				return;
			}
			UUID recipientUUID = plugin.getPlayerNameHandler().getPlayerByName(args[1]);
			if (recipientUUID == null) {
				BaseComponent message = new TextComponent("Did not find ");
				message.setColor(ChatColor.RED);
				BaseComponent extra = new TextComponent(args[1]);
				extra.setColor(ChatColor.AQUA);
				message.addExtra(extra);
				extra = new TextComponent(". Did you enter the name correctly?");
				message.addExtra(extra);
				MessageSender.sendMessage(sender, message);
				return;
			}
			String message = Util.getLine(args, 2);
			PlayerAccount recipient = plugin.getPlayerInfo(recipientUUID);
			if (playerInfo.isIgnoring(player.getUniqueId())) {
				TextComponent baseFail = new TextComponent("");
				TextComponent cannotPM = new TextComponent("Cannot send mail to ");
				TextComponent targetUserTC = new TextComponent(plugin.getPlayerNameHandler().getNameByPlayer(recipientUUID));
				TextComponent because = new TextComponent(" because you are ignoring them.");
				cannotPM.setColor(ChatColor.RED);
				because.setColor(ChatColor.RED);
				baseFail.addExtra(cannotPM);
				baseFail.addExtra(targetUserTC);
				baseFail.addExtra(because);
				return;
			}
			if (recipient.isIgnoring(player.getUniqueId()) && !player.hasPermission("hk.siggi.bungeechat.ignoreexempt")) {
				TextComponent baseFail = new TextComponent("");
				TextComponent cannotPM = new TextComponent("Cannot send mail to ");
				TextComponent targetUserTC = new TextComponent(plugin.getPlayerNameHandler().getNameByPlayer(recipientUUID));
				TextComponent because = new TextComponent(" because you are on their ignore list.");
				cannotPM.setColor(ChatColor.RED);
				because.setColor(ChatColor.RED);
				baseFail.addExtra(cannotPM);
				baseFail.addExtra(targetUserTC);
				baseFail.addExtra(because);
				return;
			}
			if (recipient.getMail().length >= recipient.getMaxMail()) {
				BaseComponent cannotSend = new TextComponent("Cannot send mail: Recipient inbox is full!");
				cannotSend.setColor(ChatColor.RED);
				MessageSender.sendMessage(sender, cannotSend);
			} else {
				recipient.sendMail(player.getUniqueId(), message, true, true);
			}
		} else if (args[0].equalsIgnoreCase("read")) {
			Mail[] mail = playerInfo.getMail();
			if (mail.length == 0) {
				BaseComponent message = new TextComponent("You have no mail.");
				message.setColor(ChatColor.GOLD);
				MessageSender.sendMessage(player, message);
				return;
			}
			for (int i = 0; i < mail.length; i++) {
				BaseComponent message = new TextComponent(playerNameHandler.getNameByPlayer(mail[i].from) + ": ");
				message.setColor(ChatColor.GOLD);
				BaseComponent extra = new TextComponent(mail[i].message);
				extra.setColor(ChatColor.WHITE);
				message.addExtra(extra);
				extra = new TextComponent(" Reply");
				extra.setColor(ChatColor.GRAY);
				extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click here to reply")}));
				extra.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mail send " + mail[i].getFrom() + " "));
				message.addExtra(extra);
				MessageSender.sendMessage(player, message);
			}
			BaseComponent message = new TextComponent("To clear mail, type ");
			message.setColor(ChatColor.GOLD);
			BaseComponent extra = new TextComponent("/mail clear");
			extra.setColor(ChatColor.AQUA);
			extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("(or you could click here!)")}));
			extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mail clear"));
			message.addExtra(extra);
			MessageSender.sendMessage(player, message);
		} else if (args[0].equalsIgnoreCase("clear")) {
			playerInfo.clearMail();
			BaseComponent message = new TextComponent("Mail cleared");
			message.setColor(ChatColor.GOLD);
			MessageSender.sendMessage(player, message);
		}
	}
	
	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		String a = args[0].toLowerCase();
		String b = args[args.length - 1].toLowerCase();
		List<String> result = new LinkedList<>();
		ProxiedPlayer p = (ProxiedPlayer) sender;
		Consumer<String> addSuggestion = (str) -> {
			if (str.toLowerCase().startsWith(b)) {
				result.add(str);
			}
		};
		if (args.length == 1) {
			addSuggestion.accept("read");
			addSuggestion.accept("clear");
			addSuggestion.accept("send");
		}
		if (args.length == 2 && args[0].equalsIgnoreCase("send")) {
			result.addAll(plugin.getPlayerNameHandler().autocompletePlayers(args[1]));
		}
		return result;
	}
}
