package hk.siggi.bungeecord.bungeechat.commands.messaging;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
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

public class CommandCapsFilter extends Command implements TabExecutor {

	public final BungeeChat plugin;

	public CommandCapsFilter(BungeeChat plugin) {
		super("capsfilter", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender cs, String[] strings) {
		if (!(cs instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer p = (ProxiedPlayer) cs;
		PlayerAccount info = plugin.getPlayerInfo(p.getUniqueId());
		if (!p.hasPermission("hk.siggi.bungeechat.nofiltercaps")) {
			info.setDisableCapsFilter(false);
		} else if (strings.length == 1) {
			boolean capsFilter = strings[0].equalsIgnoreCase("on")
					|| strings[0].equalsIgnoreCase("1")
					|| strings[0].equalsIgnoreCase("true")
					|| strings[0].equalsIgnoreCase("enable")
					|| strings[0].equalsIgnoreCase("activate");
			info.setDisableCapsFilter(!capsFilter);
		}
		TextComponent base = new TextComponent("");
		TextComponent chatCensor = new TextComponent("Caps Filter: ");
		TextComponent onOff = new TextComponent(info.getDisableCapsFilter() ? "Off" : "On");
		chatCensor.setColor(ChatColor.GOLD);
		onOff.setColor(ChatColor.AQUA);
		base.addExtra(chatCensor);
		base.addExtra(onOff);
		cs.sendMessage(base);

		if (strings.length != 1) {
			TextComponent base2 = new TextComponent("");
			TextComponent usage = new TextComponent("Usage: ");
			TextComponent command = new TextComponent("/capsfilter [on|off]");
			usage.setColor(ChatColor.GOLD);
			command.setColor(ChatColor.AQUA);
			base2.addExtra(usage);
			base2.addExtra(command);
			cs.sendMessage(base2);
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
			addSuggestion.accept("off");
		}
		return options;
	}
}
