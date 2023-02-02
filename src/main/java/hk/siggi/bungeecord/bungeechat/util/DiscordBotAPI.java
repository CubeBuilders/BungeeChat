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

    public static boolean sendMessage(String channel, String message, boolean isTask, ActionLink... links) {
        String endpoint = Endpoints.get("discordbot");
        if (endpoint == null) {
            return false;
        }
        try {
            Map<String, String> postData = new HashMap<>();
            postData.put("channel", channel);
            postData.put("message", message);
            if (isTask) postData.put("task", "1");
            if (links != null) {
                for (int i = 0; i < links.length; i++) {
                    postData.put("link" + (i+1), links[i].url);
                    postData.put("text" + (i+1), links[i].text);
                }
            }
            return new String(Util.post(endpoint + "/message", postData)).equals("OK");
        } catch (Exception e) {
            return false;
        }
    }

    public static class ActionLink {
        private final String url;
        private final String text;
        public ActionLink(String url, String text) {
            this.url = url;
            this.text = text;
        }
    }
}
