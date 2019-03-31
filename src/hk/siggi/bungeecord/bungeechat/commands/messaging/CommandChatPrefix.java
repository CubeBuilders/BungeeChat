package hk.siggi.bungeecord.bungeechat.commands.messaging;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount.ChatPrefixType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandChatPrefix extends Command implements TabExecutor {

	public final BungeeChat plugin;

	public CommandChatPrefix(BungeeChat plugin) {
		super("chatprefix", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender cs, String[] strings) {
		if (!(cs instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer p = (ProxiedPlayer) cs;
		PlayerAccount info = plugin.getPlayerInfo(p.getUniqueId());
		boolean showHelp = true;
		if (strings.length == 1) {
			try {
				String requestedValue = strings[0];
				ChatPrefixType cpt = ChatPrefixType.valueOf(requestedValue.toUpperCase());
				info.setChatPrefixType(cpt);
				showHelp = false;
			} catch (Exception e) {
			}
		}
		TextComponent base = new TextComponent("");
		TextComponent chatPrefixType = new TextComponent("Chat Prefix Type: ");
		String theTypeStr = info.getChatPrefixType().name().toLowerCase();
		if (theTypeStr.equals("auto")) theTypeStr += " (" + (plugin.getSession(p).getChatPrefixType(ChatPrefixType.AUTO).name().toLowerCase()) + ")";
		TextComponent theType = new TextComponent(theTypeStr);
		chatPrefixType.setColor(ChatColor.GOLD);
		theType.setColor(ChatColor.AQUA);
		base.addExtra(chatPrefixType);
		base.addExtra(theType);
		cs.sendMessage(base);
		if (showHelp) {
			TextComponent base2 = new TextComponent("");
			TextComponent usage = new TextComponent("Usage: ");
			TextComponent command = new TextComponent("/chatprefix [auto|classic|compact]");
			usage.setColor(ChatColor.GOLD);
			command.setColor(ChatColor.AQUA);
			base2.addExtra(usage);
			base2.addExtra(command);
			cs.sendMessage(base2);
		}
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender cs, String[] strings) {
		String a = strings[strings.length-1].toLowerCase();
		List<String> options = new ArrayList<>();
		Consumer<String> addSuggestion = (suggestion) -> {
			if (suggestion.toLowerCase().startsWith(a)) {
				options.add(suggestion);
			}
		};
		if (strings.length == 1) {
			addSuggestion.accept("auto");
			addSuggestion.accept("classic");
			addSuggestion.accept("compact");
		}
		return options;
	}
}
