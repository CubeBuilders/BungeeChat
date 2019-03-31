package hk.siggi.bungeecord.bungeechat.chatlog;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.util.ReaderAsIterator;
import hk.siggi.bungeecord.bungeechat.util.Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TimeZone;
import java.util.UUID;

public abstract class ChatLogLine {

	/**
	 * Convert a string in the format "name/UUID" to a ChatLogUser.
	 *
	 * @param user in the format "name/UUID"
	 * @return a ChatLogUser
	 */
	private static ChatLogUser userStringToObject(String user) {
		int slashPos = user.indexOf("/");
		if (slashPos >= 0) {
			String name = user.substring(0, slashPos);
			UUID uuid = Util.uuidFromString(user.substring(slashPos + 1));
			return new ChatLogUser(name, uuid);
		} else {
			return new ChatLogUser(user);
		}
	}

	private static List<ChatLogUser> userCsvToList(String users) {
		List<ChatLogUser> userList = new ArrayList<>();
		if (!users.equals("")) {
			String[] userArray = users.split(",");
			for (String user : userArray) {
				userList.add(userStringToObject(user));
			}
		}
		return userList;
	}

	ChatLogLine(String message, ChatLogUser sender, long time) {
		this.message = message;
		this.sender = sender;
		this.time = time;
	}
	public final String message;
	public final ChatLogUser sender;
	public final long time;

	public abstract boolean isPlayerLikelyInvolved(String str);

	public static ChatLogLine parseLine(String line) {
		try {
			int idx = line.indexOf(":");
			long time = Long.parseLong(line.substring(0, idx));
			line = line.substring(idx + 1);
			idx = line.indexOf(":");
			String msgType = line.substring(0, idx);
			line = line.substring(idx + 1);
			if (msgType.startsWith("Public-")) {
				String server = msgType.substring(7);
				String[] parts = line.split(":", 2);
				ChatLogUser sender = userStringToObject(parts[0]);
				String message = parts[1];
				return new PublicChatLog(message, sender, server, time);
			} else if (msgType.startsWith("Faction-")) {
				String server = msgType.substring(8);
				String[] parts = line.split(":", 3);
				ChatLogUser sender = userStringToObject(parts[0]);
				String witnesses = parts[1];
				String message = parts[2];
				List<ChatLogUser> witnessList = userCsvToList(witnesses);
				return new FactionChatLog(message, sender, server, witnessList, time);
			} else if (msgType.equals("Private")) {
				String[] parts = line.split(":", 3);
				ChatLogUser sender = userStringToObject(parts[0]);
				ChatLogUser recipient = userStringToObject(parts[1]);
				String message = parts[2];
				return new PrivateChatLog(message, sender, recipient, time);
			} else if (msgType.equals("Mail")) {
				String[] parts = line.split(":", 3);
				ChatLogUser sender = userStringToObject(parts[0]);
				ChatLogUser recipient = userStringToObject(parts[1]);
				String message = parts[2];
				return new MailChatLog(message, sender, recipient, time);
			} else if (msgType.equals("StaffChat")) {
				String[] parts = line.split(":", 2);
				ChatLogUser sender = userStringToObject(parts[0]);
				String message = parts[1];
				return new StaffChatLog(message, sender, time);
			} else if (msgType.startsWith("Group-")) {
				String groupID = msgType.substring(6);
				String[] parts = line.split(":", 3);
				ChatLogUser sender = userStringToObject(parts[0]);
				String witnesses = parts[1];
				String message = parts[2];
				List<ChatLogUser> witnessList = userCsvToList(witnesses);
				int slashPos = groupID.indexOf("/");
				String groupName = groupID.substring(0, slashPos);
				UUID groupUUID = Util.uuidFromString(groupID.substring(slashPos + 1));
				return new GroupChatLog(message, groupName, groupUUID, sender, witnessList, time);
			}
		} catch (Exception e) {
		}
		return null;
	}

	public static ChatLogLine[] getChatLogs(long start, long end) {
		long startFile = BungeeChat.getChatLogNumber(start);
		long endFile = BungeeChat.getChatLogNumber(end);
		ArrayList<ChatLogLine> chatlog = new ArrayList<ChatLogLine>();
		File chatlogDir = new File(BungeeChat.getInstance().getDataFolder(), "chatlogs");
		for (long i = startFile; i <= endFile; i++) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(chatlogDir, i + ".txt"))));
				String line;
				while ((line = reader.readLine()) != null) {
					ChatLogLine l = parseLine(line);
					if (l == null) {
						continue;
					}
					if (l.time >= start && l.time <= end) {
						chatlog.add(l);
					}
				}
				reader.close();
			} catch (Exception e) {
			}
		}
		return chatlog.toArray(new ChatLogLine[chatlog.size()]);
	}

	public static Iterator<ChatLogLine> iterateAllChats() {
		File chatlogDir = new File(BungeeChat.getInstance().getDataFolder(), "chatlogs");
		File[] chatLogFiles = chatlogDir.listFiles((dir, name) -> {
			if (!name.endsWith(".txt")) {
				return false;
			}
			try {
				Long.parseLong(name.substring(0, name.length() - 4));
			} catch (Exception e) {
				return false;
			}
			return true;
		});
		Arrays.sort(chatLogFiles, (o1, o2) -> {
			String n1 = o1.getName();
			String n2 = o2.getName();
			long l1 = -1L, l2 = -1L;
			if (n1.endsWith(".txt")) {
				try {
					l1 = Long.parseLong(n1.substring(0, n1.length() - 4));
				} catch (Exception e) {
				}
			}
			if (n2.endsWith(".txt")) {
				try {
					l2 = Long.parseLong(n2.substring(0, n2.length() - 4));
				} catch (Exception e) {
				}
			}
			if (l1 == l2 || (l1 < 0 && l2 < 0)) {
				return 0;
			} else if (l1 < 0 && l2 >= 0) {
				return 1;
			} else if (l1 >= 0 && l2 < 0) {
				return -1;
			} else if (l1 < l2) {
				return -1;
			} else if (l1 > l2) {
				return 1;
			}
			return 0;
		});
		return new ReaderAsIterator<ChatLogLine>() {
			BufferedReader reader = null;
			int pos = -1;

			@Override
			protected ChatLogLine getNext() throws NoSuchElementException {
				ChatLogLine cll = null;
				while (cll == null) {
					String line = null;
					while (line == null) {
						try {
							if (reader == null || (line = reader.readLine()) == null) {
								if (reader != null) {
									reader.close();
									reader = null;
								}
								pos += 1;
								if (chatLogFiles.length <= pos) {
									throw new NoSuchElementException();
								}
								reader = new BufferedReader(new FileReader(chatLogFiles[pos]));
							}
						} catch (Exception e) {
							throw new NoSuchElementException();
						}
					}
					cll = parseLine(line);
				}
				return cll;
			}

			@Override
			protected void finalize() throws Throwable {
				try {
					reader.close();
				} catch (Exception e) {
				} finally {
					reader = null;
					super.finalize();
				}
			}
		};
	}
	private static final TimeZone timeZone;
	private static final SimpleDateFormat dateFormat;
	private static final SimpleDateFormat dateShortFormat;

	static {
		timeZone = TimeZone.getTimeZone("America/New_York");
		dateFormat = new SimpleDateFormat("yyyy-MM-dd h:mm:ss a");
		dateFormat.setTimeZone(timeZone);
		dateShortFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateShortFormat.setTimeZone(timeZone);
	}

	public String getDate() {
		return dateShortFormat.format(new Date(time));
	}

	public String getDateTime() {
		return dateFormat.format(new Date(time)).replaceAll("AM", "am").replaceAll("PM", "pm");
	}

	public String getDateTime(String timezone) {
		SimpleDateFormat df = (SimpleDateFormat) dateFormat.clone();
		df.setTimeZone(TimeZone.getTimeZone(timezone));
		return df.format(new Date(time)).replaceAll("AM", "am").replaceAll("PM", "pm");
	}

	@Override
	public abstract String toString();
}
