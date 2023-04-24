package hk.siggi.bungeecord.bungeechat.commands.moderation;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.util.Util;
import java.util.ArrayList;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandClearChat extends Command implements TabExecutor {
    private final BungeeChat plugin;

    public CommandClearChat(BungeeChat plugin) {
        super("clearchat", null, "cc");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer player = (sender instanceof ProxiedPlayer) ? ((ProxiedPlayer) sender) : null;
        if (player != null && !player.hasPermission("hk.siggi.bungeechat.clearchat")) {
            MessageSender.sendMessage(player, Util.randomNotPermittedMessage());
            return;
        }
        for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
            if (p.hasPermission("hk.siggi.bungeechat.clearchatimmunity")) {
                MessageSender.sendMessage(p, sender.getName() + " has cleared the chat.");
            } else {
                for (int i = 0; i < 100; i++) {
                    MessageSender.sendMessage(p, "");
                }
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
