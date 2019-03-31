package hk.siggi.bungeecord.bungeechat.chat;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ServerPublicChatGroupController {

	private final BungeeChat plugin;
	private final Map<String, String> mappings = new HashMap<>();

	public ServerPublicChatGroupController(BungeeChat plugin) {
		this.plugin = plugin;
	}

	private void clean() {
		for (Iterator<String> it = mappings.keySet().iterator(); it.hasNext();)
			if (plugin.getProxy().getServerInfo(it.next()) == null)
				it.remove();
	}

	public void setGroup(String server, String group) {clean();
		if(group==null)mappings.remove(server);else
		mappings.put(server, group);
	}

	public String getGroup(String server) {
		return mappings.get(server);
	}
}
