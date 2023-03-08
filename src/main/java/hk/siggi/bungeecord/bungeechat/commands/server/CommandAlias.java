package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import io.siggi.cubecore.bungee.CubeCoreBungee;
import java.util.Collections;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandAlias extends Command implements TabExecutor {
    private final BungeeChat plugin;
    private final String targetCommand;

    public CommandAlias(BungeeChat plugin, String aliasCommand, String targetCommand) {
        super(aliasCommand);
        this.plugin = plugin;
        this.targetCommand = targetCommand;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        CubeCoreBungee.chatAsPlayer((ProxiedPlayer) sender, "/" + targetCommand);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
        return Collections.emptyList();
    }
}
