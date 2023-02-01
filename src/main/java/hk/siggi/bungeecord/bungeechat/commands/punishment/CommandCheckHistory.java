package hk.siggi.bungeecord.bungeechat.commands.punishment;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import hk.siggi.bungeecord.bungeechat.util.TimeUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.cubebuilders.user.CBUser;
import net.cubebuilders.user.Punishment;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandCheckHistory extends Command implements TabExecutor {

	public final BungeeChat plugin;

	public CommandCheckHistory(BungeeChat plugin) {
		super("phistory", null, "punishmenthistory");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		int perPage = 5;
		if (!(sender instanceof ProxiedPlayer)) {
			perPage = 20;
		}
		if (args.length < 1 || args.length > 2) {

			return;
		}
		TimeZone tz = (sender instanceof ProxiedPlayer) ? BungeeChat.getSession(((ProxiedPlayer) sender)).user.getUserData().getTimeZone() : null;
		String receiver = args[0];
		int pageNumber = 1;
		if (args.length == 2) {
			pageNumber = Integer.parseInt(args[1]);
		}
		UUID receiverUUID = plugin.getPlayerNameHandler().getPlayerByName(receiver);
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
		if (pageNumber <= 0) {
			return;
		}
		receiver = plugin.getUUIDCache().getNameFromUUID(receiverUUID);
		PlayerAccount playerInfo = plugin.getPlayerInfo(receiverUUID);
		CBUser user = BungeeChat.getUser(receiverUUID);
		Punishment[] punishments = user.getUserData().getPunishments();
		String muteReason = null;
		String banReason = null;
		for (Punishment p : punishments) {
			if (p.isCancelled()) {
				continue;
			}
			Punishment.PunishmentAction action = p.getAction();
			if (action == Punishment.PunishmentAction.MUTE) {
				muteReason = p.getReason();
			} else if (action == Punishment.PunishmentAction.BAN) {
				banReason = p.getReason();
			}
		}
		if (pageNumber == 1) {
			if (user.isMuted()) {
				long muteExpires = user.getExpiry(Punishment.PunishmentAction.MUTE);
				if (muteExpires == -1L) {
					BaseComponent message = new TextComponent(receiver + " is permanently muted.");
					message.setColor(ChatColor.AQUA);
					MessageSender.sendMessage(sender, message);
				} else {
					BaseComponent message = new TextComponent(receiver + " is muted, expires on " + plugin.formatDate(muteExpires, tz) + " (in " + TimeUtil.timeDifference(System.currentTimeMillis(), muteExpires) + ").");
					message.setColor(ChatColor.AQUA);
					MessageSender.sendMessage(sender, message);
				}
				BaseComponent message = new TextComponent("Mute Reason: " + muteReason);
				message.setColor(ChatColor.AQUA);
				MessageSender.sendMessage(sender, message);
			}
			if (user.isBanned()) {
				long banExpires = user.getExpiry(Punishment.PunishmentAction.BAN);
				if (banExpires == -1L) {
					BaseComponent message = new TextComponent(receiver + " is permanently banned.");
					message.setColor(ChatColor.AQUA);
					MessageSender.sendMessage(sender, message);
				} else {
					BaseComponent message = new TextComponent(receiver + " is banned, expires on " + plugin.formatDate(banExpires, tz) + " (in " + TimeUtil.timeDifference(System.currentTimeMillis(), banExpires) + ").");
					message.setColor(ChatColor.AQUA);
					MessageSender.sendMessage(sender, message);
				}
				BaseComponent message = new TextComponent("Ban Reason: " + banReason);
				message.setColor(ChatColor.AQUA);
				MessageSender.sendMessage(sender, message);
			}
		}
		if (punishments.length == 0) {
			BaseComponent message = new TextComponent(receiver + " has a clean record on CubeBuilders!");
			message.setColor(ChatColor.AQUA);
			MessageSender.sendMessage(sender, message);
		} else {
			int pageCount = getPageCount(punishments.length, perPage);
			int offset = (pageNumber - 1) * perPage;
			BaseComponent top = new TextComponent("Recent offences for " + receiver);
			top.setColor(ChatColor.AQUA);
			MessageSender.sendMessage(sender, top);
			for (int i = punishments.length - 1; i >= Math.max(0, punishments.length - 10); i--) {
				long longLength = punishments[i].getLength();
				String length = "n/a";
				if (punishments[i].getAction() != Punishment.PunishmentAction.WARNING) {
					if (longLength == -1L) {
						length = "Permanent";
					} else {
						length = TimeUtil.timeToString(longLength);
					}
				}
				String prefix = "";
				if (length.equals("n/a")) {
					length = "";
				} else if (length.equals("Permanent")) {
					length = "";
					prefix = "Permanent ";
				} else {
					length = " for " + length;
				}
				BaseComponent punishment = new TextComponent(plugin.formatDate(punishments[i].getStartTime(), tz) + " " + plugin.getPlayerNameHandler().getNameByPlayer(punishments[i].getIssuedBy()) + ": " + prefix + punishments[i].getAction().toString() + length + ", " + punishments[i].getReason());
				punishment.setColor(ChatColor.AQUA);
				if (punishments[i].isCancelled()) {
					punishment.setStrikethrough(true);
					punishment.setColor(ChatColor.GRAY);
				}
				MessageSender.sendMessage(sender, punishment);
			}
		}
		BaseComponent moreInfo = new TextComponent("Click Here For More Information");
		moreInfo.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://cubebuilders.net/mod/offences/" + (playerInfo.getPlayerUUID().toString().replaceAll("-", "").toLowerCase())));
		MessageSender.sendMessage(sender, moreInfo);
//		if (sender instanceof ProxiedPlayer && pageCount > 1) {
//			BaseComponent pageSelector = new TextComponent("Go: ");
//			BaseComponent nextPage = new TextComponent("Next Page ");
//			nextPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/phistory " + receiver + " " + (pageNumber+1)));
//			BaseComponent previousPage = new TextComponent("Previous Page");
//			previousPage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/phistory " + receiver + " " + (pageNumber-1)));
//			if (pageNumber < pageCount) pageSelector.addExtra(nextPage);
//			if (pageNumber > 1) pageSelector.addExtra(previousPage);
//			MessageSender.sendMessage(sender, pageSelector);
//		}
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender cs, String[] strings) {
		List<String> list = new ArrayList<>();
		if (strings.length == 1) {
			list.addAll(plugin.getPlayerNameHandler().autocompletePlayers(strings[0]));
		}
		return list;
	}

	public static int getPageCount(int items, int itemsPerPage) {
		if (items <= 0) {
			return 1;
		}
		if (items % itemsPerPage == 0) {
			return items / itemsPerPage;
		} else {
			return (items + (itemsPerPage - (items % itemsPerPage))) / itemsPerPage;
		}
	}
}
