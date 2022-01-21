package hk.siggi.bungeecord.bungeechat.commands.messaging;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandCensor extends Command implements TabExecutor {

	public final BungeeChat plugin;

	public CommandCensor(BungeeChat plugin) {
		super("censor", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender cs, String[] strings) {
		if (!(cs instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer p = (ProxiedPlayer) cs;
		PlayerAccount info = plugin.getPlayerInfo(p.getUniqueId());
		if (strings.length == 1) {
			boolean censorOn = strings[0].equalsIgnoreCase("on")
					|| strings[0].equalsIgnoreCase("1")
					|| strings[0].equalsIgnoreCase("true")
					|| strings[0].equalsIgnoreCase("enable")
					|| strings[0].equalsIgnoreCase("activate");
			boolean censorSemi = strings[0].equalsIgnoreCase("semi");
			if (censorSemi) censorOn = true;
			info.setChatCensor(censorOn);
			info.setChatCensorSemi(censorSemi);
		}
		TextComponent base = new TextComponent("");
		TextComponent chatCensor = new TextComponent("Chat Censor: ");
		TextComponent onOff = new TextComponent(info.getChatCensor() ? (info.getChatCensorSemi() ? "Semi (Hover your mouse over this symbol to see uncensored: \u29bf)" : "On") : "Off");
		chatCensor.setColor(ChatColor.GOLD);
		onOff.setColor(ChatColor.AQUA);
		base.addExtra(chatCensor);
		base.addExtra(onOff);
		MessageSender.sendMessage(cs, base);

		if (strings.length != 1) {
			TextComponent base2 = new TextComponent("");
			TextComponent usage = new TextComponent("Usage: ");
			TextComponent command = new TextComponent("/censor [on|semi|off]");
			usage.setColor(ChatColor.GOLD);
			command.setColor(ChatColor.AQUA);
			base2.addExtra(usage);
			base2.addExtra(command);
			MessageSender.sendMessage(cs, base2);

		TextComponent base3 = new TextComponent("");
		TextComponent cbDisclaimer = new TextComponent("CubeBuilders / Siggi.io are not responsible for the content of other user's messages regardless of whether or not you use the chat censor.");
		cbDisclaimer.setColor(ChatColor.GOLD);
		base3.addExtra(cbDisclaimer);
		MessageSender.sendMessage(cs, base3);
		}
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender cs, String[] strings) {
		String a = strings[strings.length - 1].toLowerCase();
		List<String> options = new ArrayList<>();
		Consumer<String> addSuggestion = (suggestion) -> {
			if (suggestion.toLowerCase().startsWith(a)) {
				options.add(suggestion);
			}
		};
		if (strings.length == 1) {
			addSuggestion.accept("on");
			addSuggestion.accept("semi");
			addSuggestion.accept("off");
		}
		return options;
	}
}
