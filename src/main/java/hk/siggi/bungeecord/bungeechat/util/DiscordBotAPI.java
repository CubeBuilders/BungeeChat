package hk.siggi.bungeecord.bungeechat.util;

import hk.siggi.bungeecord.bungeechat.Endpoints;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static hk.siggi.bungeecord.bungeechat.util.Util.getURL;
import static hk.siggi.bungeecord.bungeechat.util.Util.uuidToString;

public class DiscordBotAPI {

    private DiscordBotAPI() {
        // not meant to be instantiated
    }

    public static String discordLink(UUID uniqueId, String code) {
        String endpoint = Endpoints.get("discordbot");
        if (endpoint == null) {
            return "ENDPOINT_NOT_SET";
        }
        try {
            return new String(getURL(endpoint + "/link?uuid=" + uuidToString(uniqueId) + "&code=" + URLEncoder.encode(code, "UTF-8")));
        } catch (Exception e) {
            return "EXCEPTION";
        }
    }

    public static void sendMessage(String channel, String message) {
        String endpoint = Endpoints.get("discordbot");
        if (endpoint == null) {
            return;
        }
        try {
            Map<String, String> postData = new HashMap<>();
            postData.put("channel", channel);
            postData.put("message", message);
            Util.post(endpoint + "/message", postData);
        } catch (Exception e) {
        }
    }
}
