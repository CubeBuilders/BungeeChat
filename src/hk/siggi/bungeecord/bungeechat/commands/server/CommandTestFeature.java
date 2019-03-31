package hk.siggi.bungeecord.bungeechat.commands.server;

import com.google.gson.Gson;
import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.chat.string.ChatString;
import hk.siggi.bungeecord.bungeechat.chat.string.patcher.ChatPatcher;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import hk.siggi.bungeecord.bungeechat.player.Serialization;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandTestFeature extends Command {
	
	private final BungeeChat plugin;
	
	public CommandTestFeature(BungeeChat plugin) {
		super("testfeature");
		this.plugin = plugin;
	}
	
	@Override
	public void execute(CommandSender cs, String[] strings) {
		if (!(cs instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer pl = (ProxiedPlayer) cs;
		File testenvcheck = new File(plugin.getDataFolder(), "testenv");
		if (!testenvcheck.exists()) {
			pl.sendMessage(plugin.unify(plugin.processChat(null, "&6This command is not available on the production environment.")));
			return;
		}
		try {
			switch (strings[0]) {
				case "pajson": {
					PlayerAccount playerInfo = plugin.getPlayerInfo(pl.getUniqueId());
					Gson gson = Serialization.getGson();
					String toJson = gson.toJson(playerInfo);
					try (FileWriter fw = new FileWriter(new File(plugin.getDataFolder(), "pajson.json"))) {
						fw.write(toJson);
					}
					pl.sendMessage("OK");
				}
				break;
				case "pajson2": {
					Gson gson = Serialization.getGson();
					PlayerAccount playerInfo;
					try (FileReader rd = new FileReader(new File(plugin.getDataFolder(), "pajson.json"))) {
						playerInfo = gson.fromJson(rd, PlayerAccount.class);
					}
					String toJson = gson.toJson(playerInfo);
					try (FileWriter fw = new FileWriter(new File(plugin.getDataFolder(), "pajson2.json"))) {
						fw.write(toJson);
					}
					pl.sendMessage("OK");
				}
				break;
				case "chatstring": {
					StringBuilder sb = new StringBuilder();
					for (int i = 1; i < strings.length; i++) {
						if (i != 1) {
							sb.append(" ");
						}
						sb.append(strings[i]);
					}
					String chatStr = sb.toString();
					pl.sendMessage("Q: " + chatStr);
					ChatString chatString = new ChatString(chatStr, true);
					TextComponent a = new TextComponent("");
					a.addExtra("A: ");chatString.appendTo(a);
					pl.sendMessage(a);
					pl.sendMessage("B: " + chatString.toUnformattedString());
					pl.sendMessage("C: " + chatString.toFormattedString());
				}
				break;
				case "chatpatcher": {
					StringBuilder sb = new StringBuilder();
					for (int i = 1; i < strings.length; i++) {
						if (i != 1) {
							sb.append(" ");
						}
						sb.append(strings[i]);
					}
					String chatStr = sb.toString();
					ChatString chatString = new ChatString(chatStr, true);
					ChatPatcher patcher = new ChatPatcher(chatString);
					patcher.forEach("democrat", (word) -> {
						word.insert(4, word.charAt(3)=='O'?"N":"n");
					});
					patcher.forEach("democrats", (word) -> {
						word.insert(4, word.charAt(3)=='O'?"N":"n");
					});
					patcher.forEach("liberal", (word) -> {
						word.replace(3, "tard");
					});
					patcher.forEach("liberals", (word) -> {
						word.replace(3, "tard");
					});
					patcher.forEach("iPhone", (word) -> {
						word.replaceWhole("iPhone");
					});
					patcher.forEach("hate", (word) -> {
						word.replaceWhole("love");
					});
					patcher.forEach("you will be amazed", (word) -> {
						word.delete(0, 18);
						word.insert(0, "it is amazing");
					});
					
					pl.sendMessage(chatString.toTextComponent());
				}
				break;
			}
		} catch (Exception e) {
			try {
				CharArrayWriter caw = new CharArrayWriter();
				PrintWriter pw = new PrintWriter(caw);
				e.printStackTrace(pw);
				pw.flush();
				pw.close();
				BufferedReader reader = new BufferedReader(new CharArrayReader(caw.toCharArray()));
				String line;
				int l = 0;
				while ((line = reader.readLine()) != null) {
					pl.sendMessage(line);
					l += 1;
					if (l >= 9) {
						break;
					}
				}
			} catch (Exception e2) {
				throw new RuntimeException(e);
			}
		}
	}
	
}
