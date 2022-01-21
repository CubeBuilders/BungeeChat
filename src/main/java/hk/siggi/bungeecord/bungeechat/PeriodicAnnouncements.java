package hk.siggi.bungeecord.bungeechat;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class PeriodicAnnouncements {
	private TimeZone timezone = null;
	private final BungeeChat plugin;
	private final File file;
	private long lastModified = 0L;
	private int lastAnnouncement = -1;
	private final List<Message> announcements = new ArrayList<>();

	public PeriodicAnnouncements(BungeeChat plugin, File file) {
		this.plugin = plugin;
		this.file = file;
	}

	void start() {
		plugin.getScheduler().schedule(plugin, this::sendAMessage, 5L, 5L, TimeUnit.MINUTES);
	}

	private void sendAMessage() {
		Message message = next();
		if (message == null) {
			return;
		}
		for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
			MessageSender.sendMessage(player, message.message);
		}
	}

	private Message next() {
		load();
		if (announcements.isEmpty())
			return null;
		int nextAnnouncement = lastAnnouncement + 1;
		while (true) {
			Message message = announcements.get(nextAnnouncement);
			if (message.canShow())
				return message;
			if (nextAnnouncement == lastAnnouncement) {
				return null;
			}
			nextAnnouncement += 1;
			if (nextAnnouncement >= announcements.size()) {
				nextAnnouncement = 0;
				if (lastAnnouncement == -1) {
					return null;
				}
			}
		}
	}

	private void load() {
		if (file.lastModified() == lastModified)
			return;
		timezone = TimeZone.getTimeZone("GMT");
		Message previousLast = null;
		if (lastAnnouncement != -1 && announcements.size() > lastAnnouncement) {
			previousLast = announcements.get(lastAnnouncement);
		}
		lastModified = file.lastModified();
		announcements.clear();
		lastAnnouncement = -1;
		Message lastMessage = null;
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				int equalPosition = line.indexOf("=");
				if (equalPosition == -1) continue;
				String key = line.substring(0, equalPosition).trim();
				String value = line.substring(equalPosition + 1).trim();
				switch (key) {
					case "timezone": {
						timezone = TimeZone.getTimeZone(value);
					}
					break;
					case "message": {
						announcements.add(lastMessage = new Message(value));
					}
					break;
					case "earliest": {
						try {
							lastMessage.earliest = parseDate(value);
						} catch (Exception e) {
						}
					}
					break;
					case "expires": {
						try {
							lastMessage.expires = parseDate(value);
						} catch (Exception e) {
						}
					}
					break;
				}
			}
		} catch (Exception e) {
		}
		if (previousLast != null) {
			lastAnnouncement = announcements.indexOf(previousLast);
		}
	}

	private long parseDate(String value) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sdf.setTimeZone(timezone);
			return sdf.parse(value).getTime();
		} catch (Exception e) {
			return 0L;
		}
	}

	private static class Message {
		private final String message;
		private long earliest;
		private long expires;

		private Message(String message) {
			if (message == null)
				throw new NullPointerException();
			this.message = message;
		}

		private boolean canShow() {
			long now = System.currentTimeMillis();
			return now > earliest && (expires == 0L || now < expires);
		}

		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof Message))
				return false;
			Message o = (Message) other;
			return o.message.equals(message);
		}

		public int hashCode() {
			return message.hashCode();
		}
	}
}
