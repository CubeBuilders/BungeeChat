package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.ontime.OnTimePlayer;
import hk.siggi.bungeecord.bungeechat.util.DiscordBotAPI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandReport extends Command implements TabExecutor {

    private final BungeeChat plugin;

    public CommandReport(BungeeChat plugin) {
        super("report");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] strings) {
        if (!(cs instanceof ProxiedPlayer)) {
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) cs;
        if (strings.length < 2) {
            MessageSender.sendMessage(p, "&6Usage: /report <player-name> <explanation>");
            MessageSender.sendMessage(p, "&6You must include both player name and explanation in your report.");
            return;
        }
        long now = System.currentTimeMillis();

        String complainingAboutName = strings[0];
        UUID complainingAbout = plugin.getPlayerNameHandler().getPlayerByName(complainingAboutName);
        if (complainingAbout == null) {
            MessageSender.sendMessage(p, "&6Did not find a player named " + complainingAboutName + ".");
            MessageSender.sendMessage(p, "&6Usage: /report <player-name> <explanation>");
            return;
        }
        complainingAboutName = plugin.getPlayerNameHandler().getNameByPlayer(complainingAbout);
        ProxiedPlayer player = plugin.getProxy().getPlayer(complainingAbout);
        if (player == null) {
            OnTimePlayer onTimePlayer = plugin.getOnTime().getPlayer(complainingAbout);
            long lastOnline = onTimePlayer.getLastOnline();
            if (now - lastOnline > 600000L) {
                MessageSender.sendMessage(p, "&6This player has been offline for more than 10 minutes. When you see rule-breaking next time, please don't wait and report it as soon as possible.");
                return;
            }
        }
        String message = "Complaining party: " + p.getName() + " (" + p.getUniqueId() + ")\n"
            + "Accused party: " + complainingAboutName + " (" + complainingAbout + ")\n"
            + "Explanation:";
        for (int i = 1; i < strings.length; i++) {
            message += " " + strings[i];
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String replayCommandTimestamp = sdf.format(new Date(now));
        message += "\n\nReplay command at time of report: `/replay load " + replayCommandTimestamp + "-GMT`";
        String chatLogUrl = "https://cubebuilders.net/bc/chatlog?from=" + ((now - 300000L) / 1000L) + "&to=" + (now / 1000L);
        if (DiscordBotAPI.sendMessage("ingame-reports", message, true, new DiscordBotAPI.ActionLink(chatLogUrl, "Chat Log"))) {
            MessageSender.sendMessage(p, "&6Your report has been received. Thanks for helping keep CubeBuilders a fun and safe place!");
        } else {
            MessageSender.sendMessage(p, "&6There was a problem submitting your report. Please try again.");
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> result = new LinkedList<>();
        ProxiedPlayer p = (ProxiedPlayer) sender;
        if (args.length == 1) {
            result.addAll(plugin.getPlayerNameHandler().autocompleteOnlinePlayers(args[0], p));
        }
        return result;
    }
}
