package hk.siggi.bungeecord.bungeechat.commands.punishment;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.PlayerSession;
import hk.siggi.bungeecord.bungeechat.util.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.cubebuilders.user.Punishment;
import net.cubebuilders.user.Punishment.PunishmentAction;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandPermanentBan extends Command implements TabExecutor {

	public final BungeeChat plugin;

	public CommandPermanentBan(BungeeChat plugin) {
		super("pban", null, "permaban", "permanentban");
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
			if (!player.hasPermission("hk.siggi.bungeechat.ban")) {
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
			BaseComponent extra = new TextComponent("/pban <name> <reason>");
			extra.setColor(ChatColor.WHITE);
			usage.addExtra(extra);
			sender.sendMessage(usage);
			return;
		}
		String receiver = args[0];
		String reason = Util.getLine(args, 1);
		boolean fake = false;
		boolean quiet = false;
		if (reason.equalsIgnoreCase("fake")) {
			reason = "";
			fake = true;
		} else if (reason.toLowerCase().startsWith("fake ")) {
			reason = reason.substring(5);
			fake = true;
		} else if (reason.equalsIgnoreCase("quiet")) {
			reason = "";
			quiet = true;
		} else if (reason.toLowerCase().startsWith("quiet ")) {
			reason = reason.substring(6);
			quiet = true;
		}
		if (reason.length() == 0) {
			BaseComponent usage = new TextComponent("Usage: ");
			usage.setColor(ChatColor.AQUA);
			BaseComponent extra = new TextComponent("/pban <name> <reason>");
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
					BaseComponent message = new TextComponent("Issue permanent ban to " + receiverPlayer.getName() + "? ");
					message.setColor(ChatColor.GOLD);
					BaseComponent extra = new TextComponent("Click here to confirm");
					extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Confirm Ban")}));
					extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pban " + receiverPlayer.getName() + " " + reason));
					extra.setColor(ChatColor.AQUA);
					message.addExtra(extra);
					sender.sendMessage(message);
					message = new TextComponent("If this is wrong, please enter the command again. To ban faster next time, please enter the player's full name.");
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
		long timeSinceLastBan = -1L;
		try {
			PlayerSession session = plugin.getSession(plugin.getProxiedPlayer(receiverUUID));
			if (session != null) {
				if (session.recentBan > 0L) {
					timeSinceLastBan = now - session.recentBan;
				}
				session.recentBan = now;
			}
		} catch (Exception e) {
		}
		if (timeSinceLastBan >= 0L && timeSinceLastBan <= 15000L) {
			BaseComponent message = new TextComponent("Cannot ban: ");
			message.setColor(ChatColor.RED);
			BaseComponent extra = new TextComponent(receiver);
			extra.setColor(ChatColor.AQUA);
			message.addExtra(extra);
			extra = new TextComponent(" was banned recently.");
			message.addExtra(extra);
			sender.sendMessage(message);
			return;
		}
		Punishment p = new Punishment(PunishmentAction.BAN, "manual", now, now, -1L, reason, issuer, receiverUUID);
		if (fake) {
			ProxiedPlayer targetPlayer = BungeeCord.getInstance().getPlayer(receiverUUID);
			if (targetPlayer == null) {
				BaseComponent message = new TextComponent("Cannot fake ban: ");
				message.setColor(ChatColor.RED);
				BaseComponent extra = new TextComponent(receiver);
				extra.setColor(ChatColor.AQUA);
				message.addExtra(extra);
				extra = new TextComponent(" is not online.");
				message.addExtra(extra);
				sender.sendMessage(message);
			} else {
				PlayerSession session = plugin.getSession(targetPlayer);
				session.user.getUserData().punishments.add(p);
				plugin.youAreBanned(targetPlayer, session.user);
				session.user.getUserData().punishments.remove(p);
				BaseComponent message = new TextComponent("Fake banned: ");
				message.setColor(ChatColor.GOLD);
				BaseComponent extra = new TextComponent(receiver);
				extra.setColor(ChatColor.AQUA);
				message.addExtra(extra);
				extra = new TextComponent(" xD!");
				message.addExtra(extra);
				sender.sendMessage(message);
			}
		} else {
			plugin.postOffence(p);
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
