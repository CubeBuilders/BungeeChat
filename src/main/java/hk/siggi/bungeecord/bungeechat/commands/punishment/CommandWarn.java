package hk.siggi.bungeecord.bungeechat.commands.punishment;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.util.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.cubebuilders.user.Punishment;
import net.cubebuilders.user.Punishment.PunishmentAction;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandWarn extends Command implements TabExecutor {
	public final BungeeChat plugin;
	public CommandWarn(BungeeChat plugin) {
		super("warn", null);
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
			if (!player.hasPermission("hk.siggi.bungeechat.warn")) {
				player.sendMessage(Util.randomNotPermittedMessage());
				return;
			}
		} else {
			issuer = BungeeChat.console;
			issuerName = "<Console>";
		}
		if (args.length < 2) {
			BaseComponent usage = new TextComponent("Usage: ");
			usage.setColor(ChatColor.AQUA);
			BaseComponent extra = new TextComponent("/warn <name> <reason>");
			extra.setColor(ChatColor.WHITE);
			usage.addExtra(extra);
			sender.sendMessage(usage);
			return;
		}
		String receiver = args[0];
		String reason = Util.getLine(args, 1);
		if (reason.length() == 0) {
			BaseComponent usage = new TextComponent("Usage: ");
			usage.setColor(ChatColor.AQUA);
			BaseComponent extra = new TextComponent("/warn <name> <reason>");
			extra.setColor(ChatColor.WHITE);
			usage.addExtra(extra);
			sender.sendMessage(usage);
			BaseComponent message = new TextComponent("You must include name, and reason!");
			message.setColor(ChatColor.RED);
			sender.sendMessage(message);
			return;
		}
		UUID receiverUUID = plugin.getPlayerNameHandler().getPlayerByName(receiver);
		if (receiverUUID == null) {
			Pattern pattern = Pattern.compile(".*" + receiver + ".*");
			Collection<ProxiedPlayer> playerCollection = plugin.getProxy().getPlayers();
			ProxiedPlayer[] players = playerCollection.toArray(new ProxiedPlayer[playerCollection.size()]);
			for (int i = 0; i < players.length; i++) {
				Matcher matcher = pattern.matcher(players[i].getName());
				if (matcher.matches()) {
					ProxiedPlayer receiverPlayer = players[i];
					BaseComponent message = new TextComponent("Issue warning to " + receiverPlayer.getName() + "? ");
					message.setColor(ChatColor.GOLD);
					BaseComponent extra = new TextComponent("Click here to confirm");
					extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("Confirm Warning")}));
					extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warn " + receiverPlayer.getName() + " " + reason));
					extra.setColor(ChatColor.AQUA);
					message.addExtra(extra);
					sender.sendMessage(message);
					message = new TextComponent("If this is wrong, please enter the command again. To warn faster next time, please enter the player's full name.");
					message.setColor(ChatColor.GOLD);
					sender.sendMessage(message);
					return;
				}
			}
			BaseComponent message = new TextComponent("Did not find ");
			message.setColor(ChatColor.RED);
			BaseComponent extra = new TextComponent(receiver);
			extra.setColor(ChatColor.AQUA);
			message.addExtra(extra);
			extra = new TextComponent(". Did you enter the name correctly?");
			message.addExtra(extra);
			sender.sendMessage(message);
			return;
		}
		receiver = plugin.getUUIDCache().getNameFromUUID(receiverUUID);
		long now = System.currentTimeMillis();
		Punishment p = new Punishment(PunishmentAction.WARNING, "manual", now, now, 0L, reason, issuer, receiverUUID);
		plugin.postOffence(p);
		
		ProxiedPlayer pl = BungeeChat.getInstance().getProxy().getPlayer(receiverUUID);
		if (pl != null) {
			BaseComponent message = new TextComponent("You have received a warning: ");
			message.setColor(ChatColor.GOLD);
			BaseComponent extra = new TextComponent(reason);
			extra.setColor(ChatColor.WHITE);
			message.addExtra(extra);
			pl.sendMessage(message);
		}
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender cs, String[] strings) {
		List<String> list = new ArrayList<>();
		if (strings.length == 1) {
			list.addAll(plugin.getPlayerNameHandler().autocompletePlayers(strings[0]));
		}
		return list;
	}
}
