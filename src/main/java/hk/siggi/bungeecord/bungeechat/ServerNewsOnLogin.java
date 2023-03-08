package hk.siggi.bungeecord.bungeechat;

import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import io.siggi.cubecore.bedrockapi.BedrockDeviceInfo;
import io.siggi.cubecore.bungee.CubeCoreBungee;
import io.siggi.cubecore.util.text.book.BookParser;
import io.siggi.nbt.NBTCompound;
import java.io.File;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.scheduler.BungeeScheduler;

public class ServerNewsOnLogin implements Listener {
    private final BungeeChat plugin;
    private final File newsBook;
    private final BookParser bookParser;
    private final BungeeScheduler scheduler;

    public ServerNewsOnLogin(BungeeChat plugin, File newsBook, BookParser bookParser) {
        this.plugin = plugin;
        this.newsBook = newsBook;
        this.bookParser = bookParser;
        this.scheduler = new BungeeScheduler();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerJoinedServerEvent(ServerConnectedEvent event) {
        try {
            long now = System.currentTimeMillis();
            ProxiedPlayer player = event.getPlayer();
            PlayerAccount account = plugin.getPlayerInfo(player.getUniqueId());
            if (newsBook.lastModified() <= account.getLastShownNews()) return;
            scheduler.schedule(plugin, () -> {
                try {
                    NBTCompound nbtBook = bookParser.loadBook(newsBook, CubeCoreBungee.shouldUseFallbackColors(player), BedrockDeviceInfo.isOnBedrock(player.getUniqueId()));
                    CubeCoreBungee.openBook(player, nbtBook);
                    account.setLastShownNews(now);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }, 1L, TimeUnit.SECONDS);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
