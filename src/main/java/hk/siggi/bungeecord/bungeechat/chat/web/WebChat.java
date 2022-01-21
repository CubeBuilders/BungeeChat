package hk.siggi.bungeecord.bungeechat.chat.web;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.processChat;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.unify;
import hk.siggi.bungeecord.bungeechat.util.Util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import io.siggi.simplewebsocket.SimpleWebsocket;
import io.siggi.simplewebsocket.WebSocketMessage;
import net.md_5.bungee.api.chat.BaseComponent;

public class WebChat {

	final BungeeChat plugin;

	private final Map<UUID, Set<WebChatClient>> allClients = new HashMap<>();
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();

	void addClient(WebChatClient client) {
		writeLock.lock();
		try {
			Set<WebChatClient> set = allClients.get(client.uuid);
			if (set == null) {
				allClients.put(client.uuid, set = new HashSet<>());
			}
			set.add(client);
		} finally {
			writeLock.unlock();
		}
	}

	void removeClient(WebChatClient client) {
		writeLock.lock();
		try {
			Set<WebChatClient> set = allClients.get(client.uuid);
			if (set == null) {
				return;
			}
			set.remove(client);
			if (set.isEmpty()) {
				allClients.remove(client.uuid);
			}
		} finally {
			writeLock.unlock();
		}
	}

	public WebChat(BungeeChat plugin) {
		this.plugin = plugin;
	}

	public void acceptWebSocket(SimpleWebsocket socket, UUID uuid, String username) throws IOException {
		if (uuid == null) {
			WebSocketMessage msg = WebSocketMessage.create("{\"result\":\"notloggedin\"}");
			socket.send(msg);
			socket.close();
			return;
		}
		String localName = plugin.getPlayerNameHandler().getNameByPlayer(uuid);
		if (localName != null) {
			username = localName;
		}
		WebChatClient client = new WebChatClient(this, uuid, username, socket);
		client.start();
		client.sendMessage(unify(processChat(null, "Hi&6 there, " + username + "! <https://cubebuilders.net/>")));
	}

	public void send(UUID uuid, BaseComponent component) {
		readLock.lock();
		try {
			Set<WebChatClient> c = allClients.get(uuid);
			if (c == null) {
				return;
			}
			for (WebChatClient wcc : c) {
				wcc.sendMessage(component);
			}
		} finally {
			readLock.unlock();
		}
	}

	public Set<WebChatClient> getClients(UUID uuid) {
		readLock.lock();
		try {
			Set<WebChatClient> result = new HashSet<>();
			Set<WebChatClient> c = allClients.get(uuid);
			if (c != null) {
				result.addAll(c);
			}
			return Collections.unmodifiableSet(result);
		} finally {
			readLock.unlock();
		}
	}

	public boolean isUserOnline(UUID uuid) {
		readLock.lock();
		try {
			Set<WebChatClient> c = allClients.get(uuid);
			if (c == null) {
				return false;
			}
			return (!c.isEmpty());
		} finally {
			readLock.unlock();
		}
	}

	public Set<WebChatClient> getAllForPublicChannel(String publicChannel) {
		Set<WebChatClient> cl = new HashSet<>();
		readLock.lock();
		try {
			for (Set<WebChatClient> c : allClients.values()) {
				for (WebChatClient wcc : c) {
					if (wcc.getPublicChatChannel().equals(publicChannel)) {
						cl.add(wcc);
					}
				}
			}
		} catch (Exception e) {
		} finally {
			readLock.unlock();
		}
		return cl;
	}
}
