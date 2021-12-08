package hk.siggi.bungeecord.bungeechat.chat.user;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.chat.web.WebChat;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ChatUsers {

	final BungeeChat plugin;
	final WebChat webChat;

	public ChatUsers(BungeeChat plugin, WebChat webChat) {
		this.plugin = plugin;
		this.webChat = webChat;
	}
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	private final Map<UUID, WeakReference<ChatUser>> chatUserMap = new HashMap<>();

	public ChatUser getChatUser(UUID uuid) {
		readLock.lock();
		try {
			WeakReference<ChatUser> ref = chatUserMap.get(uuid);
			if (ref != null) {
				ChatUser user = ref.get();
				if (user != null) {
					return user;
				}
			}
		} finally {
			readLock.unlock();
		}
		writeLock.lock();
		try {
			for (Iterator<WeakReference<ChatUser>> it = chatUserMap.values().iterator(); it.hasNext();) {
				WeakReference<ChatUser> ref = it.next();
				if (ref.get() == null) {
					it.remove();
				}
			}
			WeakReference<ChatUser> ref = chatUserMap.get(uuid);
			if (ref != null) {
				ChatUser user = ref.get();
				if (user != null) {
					return user;
				}
			}
			ChatUser user;
			chatUserMap.put(uuid, new WeakReference<>(user = new ChatUser(this, uuid)));
			return user;
		} finally {
			writeLock.unlock();
		}
	}
}
