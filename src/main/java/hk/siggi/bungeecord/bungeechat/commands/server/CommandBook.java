package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import io.siggi.cubecore.bedrockapi.BedrockDeviceInfo;
import io.siggi.cubecore.bungee.CubeCoreBungee;
import io.siggi.cubecore.util.text.book.BookParser;
import io.siggi.nbt.NBTCompound;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandBook extends Command implements TabExecutor {

    private final BungeeChat plugin;
    private final File bookDirectory;
    private final BookParser bookParser;

    public CommandBook(BungeeChat plugin, File bookDirectory, BookParser bookParser) {
        super("book");
        this.plugin = plugin;
        this.bookDirectory = bookDirectory;
        this.bookParser = bookParser;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            MessageSender.sendMessage(sender, "&6Only a player can use this command.");
            return;
        }
        ProxiedPlayer p = (ProxiedPlayer) sender;
        if (args.length != 1) {
            MessageSender.sendMessage(p, "&6Usage: /book [bookname]");
            return;
        }
        String bookName = args[0];
        File bookFile;
        if (bookName.contains("..") || !(bookFile = new File(bookDirectory, bookName + ".txt")).exists()) {
            MessageSender.sendMessage(p, "&6The book &b" + bookName + "&6 was not found.");
            return;
        }
        try {
            NBTCompound bookCompound = bookParser.loadBook(bookFile, CubeCoreBungee.shouldUseFallbackColors(p), BedrockDeviceInfo.isOnBedrock(p.getUniqueId()));
            CubeCoreBungee.openBook(p, bookCompound);
        } catch (Exception e) {
            MessageSender.sendMessage(p, "&6There was a problem loading the book.");
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> results = new ArrayList<>();
        if (args.length == 1 && !args[0].contains("..")) {
            String partialBookName = args[0];
            File[] files = bookDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    if (!name.endsWith(".txt") || name.startsWith("test-")) continue;
                    if (name.toLowerCase(Locale.ROOT).startsWith(partialBookName)) {
                        results.add(name.substring(0, name.length() - 4));
                    }
                }
            }
        }
        return results;
    }
}
