package hk.siggi.bungeecord.bungeechat.commands.messaging;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandNick extends Command {

	public final BungeeChat plugin;

	public CommandNick(BungeeChat plugin) {
		super("nick", null, "nickname");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender cs, String[] strings) {
		boolean allowMyNick = false;
		boolean allowOtherNick = false;
		ProxiedPlayer p = null;
		if (cs instanceof ProxiedPlayer) {
			p = (ProxiedPlayer) cs;
			allowMyNick = p.hasPermission("hk.siggi.bungeechat.nick");
			allowOtherNick = p.hasPermission("hk.siggi.bungeechat.nickother");
		} else {
			allowMyNick = false;
			allowOtherNick = true;
		}
		if (!plugin.getNicknameCache().nicknamesLoaded()) {
			TextComponent base = new TextComponent("Nicknames are not available yet, the server only recently booted up.");
			base.setColor(ChatColor.RED);
			cs.sendMessage(base);
			base = new TextComponent("Try again in one minute.");
			base.setColor(ChatColor.RED);
			cs.sendMessage(base);
			return;
		}
		if (strings.length == 1) {
			String newNickname = strings[0];
			if ((newNickname.equalsIgnoreCase("off") || allowMyNick) && p != null) {
				UUID playerUUID = p.getUniqueId();
				PlayerAccount a = plugin.getPlayerInfo(playerUUID);
				if (newNickname.equalsIgnoreCase("off")) {
					newNickname = null;
				}
				if (newNickname == null) {
					TextComponent base = new TextComponent("Your nickname has been removed.");
					base.setColor(ChatColor.GOLD);
					cs.sendMessage(base);
				} else {
					if (plugin.getUUIDCache().getUUIDFromName(newNickname) != null || plugin.getNicknameCache().isNicknameUsed(newNickname)) {
						TextComponent base = new TextComponent("That name is already in use!");
						base.setColor(ChatColor.RED);
						cs.sendMessage(base);
						return;
					}
					TextComponent base = new TextComponent("");
					TextComponent yourNameWasSetTo = new TextComponent("Your nickname is now: ");
					TextComponent usernameText = new TextComponent(newNickname);
					yourNameWasSetTo.setColor(ChatColor.GOLD);
					usernameText.setColor(ChatColor.AQUA);
					base.addExtra(yourNameWasSetTo);
					base.addExtra(usernameText);
					cs.sendMessage(base);
				}
				if (newNickname != null) {
					if (!newNickname.matches("[A-Za-z0-9_]*")) {
						TextComponent base = new TextComponent("A nickname can only contain letters, numbers, and underscore.");
						base.setColor(ChatColor.RED);
						cs.sendMessage(base);
						return;
					}
					if (newNickname.length() > 16) {
						TextComponent base = new TextComponent("A nickname can only be up to 16 characters.");
						base.setColor(ChatColor.RED);
						cs.sendMessage(base);
						return;
					}
					if (plugin.isNameBanned(newNickname)) {
						TextComponent base = new TextComponent("Your requested nickname is blacklisted and cannot be used.");
						base.setColor(ChatColor.RED);
						cs.sendMessage(base);
						return;
					}
				}
				a.setNickname(newNickname);
				plugin.sendInfoUpdate(p, p.getServer());
				plugin.getNicknameCache().setNicknameCache(playerUUID, newNickname);
			} else {
				TextComponent text = new TextComponent("You cannot modify your nickname!");
				text.setColor(ChatColor.RED);
				cs.sendMessage(text);
			}
		} else if (strings.length == 2 && allowOtherNick) {
			if (allowOtherNick) {
				UUID playerUUID = plugin.getUUIDCache().getUUIDFromName(strings[0]);
				if (playerUUID == null) {
					TextComponent base = new TextComponent("User not found.");
					base.setColor(ChatColor.RED);
					cs.sendMessage(base);
					return;
				}
				String username = plugin.getUUIDCache().getNameFromUUID(playerUUID);
				String newNickname = strings[1];
				if (newNickname.equalsIgnoreCase("off")) {
					newNickname = null;
				}
				PlayerAccount a = plugin.getPlayerInfo(playerUUID);
				ProxiedPlayer target = plugin.getProxy().getPlayer(playerUUID);
				if (newNickname == null) {
					if (target != null) {
						TextComponent base = new TextComponent("Your nickname has been removed.");
						base.setColor(ChatColor.GOLD);
						target.sendMessage(base);
					}
					TextComponent base2 = new TextComponent("Nickname removed.");
					base2.setColor(ChatColor.GOLD);
					cs.sendMessage(base2);
				} else {
					if (plugin.getUUIDCache().getUUIDFromName(newNickname) != null || plugin.getNicknameCache().isNicknameUsed(newNickname)) {
						TextComponent base = new TextComponent("That name is already in use!");
						base.setColor(ChatColor.RED);
						cs.sendMessage(base);
						return;
					}
					if (plugin.isNameBanned(newNickname)) {
						TextComponent base = new TextComponent("Your requested nickname is blacklisted and cannot be used.");
						base.setColor(ChatColor.RED);
						cs.sendMessage(base);
						return;
					}
					if (target != null) {
						TextComponent base = new TextComponent("");
						TextComponent yourNameWasSetTo = new TextComponent("Your nickname is now: ");
						TextComponent usernameText = new TextComponent(newNickname);
						yourNameWasSetTo.setColor(ChatColor.GOLD);
						usernameText.setColor(ChatColor.AQUA);
						base.addExtra(yourNameWasSetTo);
						base.addExtra(usernameText);
						target.sendMessage(base);
					}
					TextComponent base = new TextComponent("");
					TextComponent targetText = new TextComponent(username);
					TextComponent yourNameWasSetTo = new TextComponent("'s nickname has been set to: ");
					TextComponent usernameText = new TextComponent(newNickname);
					targetText.setColor(ChatColor.AQUA);
					yourNameWasSetTo.setColor(ChatColor.GOLD);
					usernameText.setColor(ChatColor.AQUA);
					base.addExtra(targetText);
					base.addExtra(yourNameWasSetTo);
					base.addExtra(usernameText);
					cs.sendMessage(base);
				}
				a.setNickname(newNickname);
				if (target != null) {
					plugin.sendInfoUpdate(target, target.getServer());
				}
				plugin.getNicknameCache().setNicknameCache(playerUUID, newNickname);
			} else {
				TextComponent text = new TextComponent("You cannot modify other's nicknames!");
				text.setColor(ChatColor.RED);
				cs.sendMessage(text);
			}
		} else {
			if (p != null) {
				PlayerAccount account = plugin.getPlayerInfo(p.getUniqueId());
				String currentNickname = account.getNickname();
				if (currentNickname != null) {
					TextComponent base = new TextComponent("");
					TextComponent yourNameIs = new TextComponent("Your nickname is currently: ");
					yourNameIs.setColor(ChatColor.GOLD);
					TextComponent nick = new TextComponent(currentNickname);
					nick.setColor(ChatColor.AQUA);
					base.addExtra(yourNameIs);
					base.addExtra(nick);
					cs.sendMessage(base);
				}
			}
			TextComponent text = new TextComponent("Usage: /nick [newnickname] or /nick off");
			text.setColor(ChatColor.GOLD);
			cs.sendMessage(text);
			if (allowOtherNick) {
				TextComponent text2 = new TextComponent("Usage: /nick [player] [newnickname] or /nick [player] off");
				text2.setColor(ChatColor.GOLD);
				cs.sendMessage(text2);
			}
		}
	}
}
