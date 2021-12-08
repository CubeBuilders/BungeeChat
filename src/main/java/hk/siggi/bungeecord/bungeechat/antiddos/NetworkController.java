package hk.siggi.bungeecord.bungeechat.antiddos;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.util.TimeUtil;
import java.util.Collection;
import java.util.Hashtable;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class NetworkController {
	public static final Hashtable<String,NetworkController> networkControllers = new Hashtable<String,NetworkController>();
	public final Hashtable<String,Long> recentLogins = new Hashtable<String,Long>();
	public final Hashtable<String,RecentChat> recentChats = new Hashtable<String,RecentChat>();
	public long nextLoginAllowed = 0L;
	public int blockLevel = 0;
	public int chatSameMessageLevel = 0;
	public long expires = 0L;
	public final String ip;
	public final boolean ipv6;
	public String blockMessage = "";
	private static String getNCIP(String ip) {
		if (ip.startsWith("[")) ip = ip.substring(1);
		if (ip.endsWith("]")) ip = ip.substring(0, ip.length() - 1);
		if (ip.contains(":")) {
			ip = ip.substring(0, ip.lastIndexOf(":"));
		}
		return ip;
	}
	public static NetworkController get(String ip) {
		ip = getNCIP(ip);
		synchronized (networkControllers) {
			NetworkController controller = networkControllers.get(ip);
			if (controller == null) networkControllers.put(ip, controller = new NetworkController(ip));
			return controller;
		}
	}
	private NetworkController(String ip) {
		this.ip = ip;
		this.ipv6 = ip.contains(":");
		expires = System.currentTimeMillis() + 3600000L;
	}
	public boolean hasExpired() {
		long now = System.currentTimeMillis();
		return expires <= now;
	}
	public long[] getRecentLoginTimes() {
		return getLongs(recentLogins);
	}
	public static long[] getLongs(Hashtable<String,Long> hashtable) {
		long[] longs;
		synchronized (hashtable) {
			Collection<Long> longList = hashtable.values();
			Long[] longArray = longList.toArray(new Long[longList.size()]);
			longs = new long[longArray.length];
			for (int i = 0; i < longs.length; i++) {
				longs[i] = longArray[i].longValue();
			}
		}
		return longs;
	}
	public Collection<RecentChat> getRecentChats() {
		synchronized (recentChats) {
			return recentChats.values();
		}
	}
	public boolean chat(ProxiedPlayer player, String line) {
		long now = System.currentTimeMillis();
		String name = player.getName();
		Collection<RecentChat> chats = getRecentChats();
		int recentSameMessageCount = 0;
		for (RecentChat chat : chats) {
			if (now - chat.time < 15000L && line.equalsIgnoreCase(chat.line)) {
				recentSameMessageCount += 1;
			}
		}
		if (recentSameMessageCount >= 5) {
			chatSameMessageLevel += 1;
			long blockTime = 0L;
			switch (chatSameMessageLevel) {
				case 1:
				blockTime = 20000L; // 20 seconds
				break;
				case 2:
				blockTime = 60000L; // 1 minute
				break;
				case 3:
				blockTime = 1200000L; // 20 minutes
				break;
				case 4:
				blockTime = 7200000L; // 2 hours
				break;
				case 5:
				blockTime = 21600000L; // 6 hours
				break;
				case 6:
				blockTime = 32400000L; // 9 hours
				break;
				default:
				blockTime = 43200000L; // 12 hours
				break;
			}
			expires = Math.max(expires, now + 3600000L + blockTime);
			nextLoginAllowed = now + blockTime;
			blockMessage = "You are temporarily blocked for spamming the chat.";
			String message = "Too many messages from same network address in a short time! You can login again in " + TimeUtil.timeToString(blockTime) + ".";
			kickAll(message);
			return false;
		}
		recentChats.put(name, new RecentChat(line, now));
		expires = Math.max(expires, now + 3600000L);
		return true;
	}
	public String login(String name) {
		long now = System.currentTimeMillis();
		if (nextLoginAllowed > now) {
			String message = blockMessage + " Try again in " + TimeUtil.timeToString(nextLoginAllowed-now) + ".";
			return message;
		}
		int loginCountA = 0;
		int loginCountB = 0;
		long[] recentLogins = getRecentLoginTimes();
		for (int i = 0; i < recentLogins.length; i++) {
			if (now - recentLogins[i] < 120000) {
				loginCountA += 1;
			}
			if (now - recentLogins[i] < 5000) {
				loginCountB += 1;
			}
		}
		if (loginCountA >= 10 || loginCountB >= 4) {
			blockLevel++;
			long blockTime = 0L;
			switch (blockLevel) {
				case 1:
				blockTime = 120000L; // 2 minutes
				break;
				case 2:
				blockTime = 300000L; // 5 minutes
				break;
				case 3:
				blockTime = 3600000L; // 1 hour
				break;
				case 4:
				blockTime = 10800000L; // 3 hours
				break;
				case 5:
				blockTime = 21600000L; // 6 hours
				break;
				case 6:
				blockTime = 32400000L; // 9 hours
				break;
				default:
				blockTime = 43200000L; // 12 hours
				break;
			}
			expires = Math.max(expires, now + 3600000L + (blockTime * 2));
			nextLoginAllowed = now + blockTime;
			blockMessage = "You are temporarily blocked for login spam.";
			String message = "Too many logins from the same address in a short time! Try again in " + TimeUtil.timeToString(blockTime) + ".";
			kickAll(message);
			return message;
		} else {
			this.recentLogins.put(name, new Long(now));
			expires = Math.max(expires, now + 3600000L);
			return null;
		}
	}
	public void kickAll(String message) {
		ProxyServer proxy = BungeeChat.getInstance().getProxy();
		BaseComponent msg = new TextComponent(message);
		for (String name : recentLogins.keySet()) {
			ProxiedPlayer p = proxy.getPlayer(name);
			if ((p.getAddress().getAddress().getHostAddress()).equals(ip)) {
				p.disconnect(msg);
			}
		}
	}
}
