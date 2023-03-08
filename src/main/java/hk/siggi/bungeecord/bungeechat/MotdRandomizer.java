package hk.siggi.bungeecord.bungeechat;

import io.siggi.cubecore.bungee.CubeCoreBungee;
import io.siggi.cubecore.util.CubeCoreUtil;
import io.siggi.cubecore.util.text.FormattedText;
import io.siggi.cubecore.util.text.processor.CubeBuildersClassicTextProcessor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MotdRandomizer implements Listener {
    private final BungeeChat plugin;
    private final File motdFile;
    private final CubeBuildersClassicTextProcessor textProcessor;

    public MotdRandomizer(BungeeChat plugin, File motdFile) {
        this.plugin = plugin;
        this.motdFile = motdFile;
        this.textProcessor = new CubeBuildersClassicTextProcessor(null, null);
        textProcessor.setAllowCustomTooltip(true);
    }

    @EventHandler
    public void multiplayerListPing(ProxyPingEvent event) {
        List<String> strings = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(motdFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;
                line = line.replace("\\n", "\n");
                strings.add(line);
            }
        } catch (Exception e) {
            return;
        }
        if (strings.isEmpty()) return;
        int item = (int) Math.floor(Math.random() * ((double) strings.size()));
        String message = strings.get(item);
        FormattedText formattedText = textProcessor.process(message, null, null);
        TextComponent finalText = CubeCoreUtil.glueComponents(formattedText.toTextComponents(CubeCoreBungee.shouldUseFallbackColors(event.getConnection())));
        ServerPing response = event.getResponse();
        response.setDescriptionComponent(finalText);
    }
}
