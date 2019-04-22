package hk.siggi.bungeecord.bungeechat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public final class CommandAutoCompleter implements Listener {

	private final BungeeChat plugin;
	private final Map<String, List<String>> commands = new HashMap<>();
	private long lastModified = 0L;

	public CommandAutoCompleter(BungeeChat plugin) {
		this.plugin = plugin;
		plugin.getProxy().getPluginManager().registerListener(plugin, this);
	}

	@EventHandler
	public void handleTab(TabCompleteEvent event) {
		Connection sender = event.getSender();
		if (sender instanceof ProxiedPlayer) {
			String cursor = event.getCursor();
			if (!cursor.startsWith("/") || cursor.contains(" ")) {
				return;
			}
			String cursorCheck = cursor.toLowerCase().substring(1);
			loadCommands();
			ProxiedPlayer pl = (ProxiedPlayer) sender;
			PlayerSession session = BungeeChat.getSession(pl);
			List<String> suggestions = event.getSuggestions();
			Consumer<String> addCommand = (command) -> {
				if (command.toLowerCase().startsWith(cursorCheck)) {
					suggestions.add("/" + command);
				}
			};
			for (Map.Entry<String, List<String>> c : commands.entrySet()) {
				String cmd = c.getKey();
				List<String> permissions = c.getValue();
				for (String p : permissions) {
					if (p == null || session.testPermission(p)) {
						addCommand.accept(cmd);
						break;
					}
				}
			}
			if (suggestions.isEmpty()) {
				event.setCancelled(true);
			} else {
				suggestions.sort((a, b) -> {
					a = a.toLowerCase();
					b = b.toLowerCase();
					if (a.startsWith("//") && !b.startsWith("//")) {
						return 1;
					} else if (!a.startsWith("//") && b.startsWith("//")) {
						return -1;
					}
					return a.compareTo(b);
				});
			}
		}
	}

	private void loadCommands() {
		File f = new File(plugin.getDataFolder(), "commandautocompletion.txt");
		if (!f.exists() || f.lastModified() == lastModified) {
			return;
		}
		lastModified = f.lastModified();
		commands.clear();
		try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.replaceAll("\t", " ");
				while (line.contains("  ")) {
					line = line.replaceAll("  ", " ");
				}
				if (line.contains("#")) {
					line = line.substring(0, line.indexOf("#"));
				}
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				String[] parts = line.split(" ");
				if (parts.length == 1) {
					String command = parts[0];
					List<String> l = commands.get(command);
					if (l == null) {
						commands.put(command, l = new LinkedList<>());
					}
					l.add(null);
				} else if (parts.length == 2) {
					String command = parts[0];
					String permission = parts[1];
					List<String> l = commands.get(command);
					if (l == null) {
						commands.put(command, l = new LinkedList<>());
					}
					l.add(permission);
				}
			}
		} catch (IOException e) {
		}
	}
}
