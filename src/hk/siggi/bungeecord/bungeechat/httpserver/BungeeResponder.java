package hk.siggi.bungeecord.bungeechat.httpserver;

import com.google.gson.stream.JsonWriter;
import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.NicknameCache;
import hk.siggi.bungeecord.bungeechat.ServerActivityReport;
import hk.siggi.bungeecord.bungeechat.UUIDCache;
import hk.siggi.bungeecord.bungeechat.UserActivityReport;
import hk.siggi.bungeecord.bungeechat.chat.censor.ChatCensor;
import hk.siggi.bungeecord.bungeechat.chatlog.ChatLogLine;
import hk.siggi.bungeecord.bungeechat.chatlog.ChatLogUser;
import hk.siggi.bungeecord.bungeechat.chatlog.FactionChatLog;
import hk.siggi.bungeecord.bungeechat.chatlog.GroupChatLog;
import hk.siggi.bungeecord.bungeechat.chatlog.MailChatLog;
import hk.siggi.bungeecord.bungeechat.chatlog.PrivateChatLog;
import hk.siggi.bungeecord.bungeechat.chatlog.PublicChatLog;
import hk.siggi.bungeecord.bungeechat.chatlog.StaffChatLog;
import hk.siggi.bungeecord.bungeechat.ontime.OnTime;
import hk.siggi.bungeecord.bungeechat.ontime.OnTimePlayer;
import hk.siggi.bungeecord.bungeechat.ontime.OnTimeSessionRecord;
import hk.siggi.bungeecord.bungeechat.player.MCBan;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import hk.siggi.bungeecord.bungeechat.player.Punishment;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.stripChatCodes;
import hk.siggi.bungeecord.bungeechat.util.TimeUtil;
import hk.siggi.bungeecord.bungeechat.util.Util;
import hk.siggi.iphelper.IP;
import hk.siggi.iphelper.IPv6;
import io.siggi.http.HTTPRequest;
import io.siggi.http.HTTPResponder;
import io.siggi.http.HTTPWebSocket;
import io.siggi.http.HTTPWebSocketHandler;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.cubebuilders.user.CBUser;

public class BungeeResponder implements HTTPResponder, HTTPWebSocketHandler {

	private final BungeeChat plugin;
	private final ArrayList<Punishment> recentPunishments = new ArrayList<Punishment>();
	private String variableServerAddress = "127.0.0.1";

	private final HashMap<UUID, TemporaryResource> temporaryResources = new HashMap<UUID, TemporaryResource>();

	private UUID addResource(TemporaryResource resource) {
		UUID uuid = UUID.randomUUID();
		synchronized (temporaryResources) {
			cleanTemporaryResources();
			while (temporaryResources.containsKey(uuid)) {
				uuid = UUID.randomUUID();
			}
			temporaryResources.put(uuid, resource);
		}
		return uuid;
	}

	private TemporaryResource getResource(UUID uuid) {
		synchronized (temporaryResources) {
			cleanTemporaryResources();
			return temporaryResources.get(uuid);
		}
	}

	private void cleanTemporaryResources() {
		Set<UUID> uuidSet = temporaryResources.keySet();
		ArrayList<UUID> uuidList = new ArrayList<UUID>();
		uuidList.addAll(uuidSet);
		for (UUID uuid : uuidList) {
			TemporaryResource resource = temporaryResources.get(uuid);
			if (System.currentTimeMillis() - resource.creationTime > 300000L) {
				temporaryResources.remove(uuid);
			}
		}
	}

	public void addPunishment(Punishment punishment) {
		synchronized (recentPunishments) {
			recentPunishments.add(punishment);
		}
	}

	public BungeeResponder(BungeeChat plugin) {
		this.plugin = plugin;
		startLoadingRecentPunishments();
	}

	private void startLoadingRecentPunishments() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				loadRecentPunishments();
			}
		}).start();
	}

	private void loadRecentPunishments() {
		synchronized (recentPunishments) {
			recentPunishments.clear();
			File[] playerFiles = new File(plugin.getDataFolder(), "playerdata").listFiles();
			if (playerFiles == null) {
				return;
			}
			long now = System.currentTimeMillis();
			for (File playerFile : playerFiles) {
				try {
					String uuidString = playerFile.getName();
					if (uuidString.contains(".")) {
						uuidString = uuidString.substring(0, uuidString.indexOf("."));
					}
					UUID uuid = Util.uuidFromString(uuidString);
					PlayerAccount player = new PlayerAccount(uuid);
					Punishment[] playerPunishments = player.getPunishments();
					for (Punishment punishment : playerPunishments) {
						boolean added = false;
						for (int i = 0; i < recentPunishments.size(); i++) {
							Punishment other = recentPunishments.get(i);
							if (other.time > punishment.time) {
								recentPunishments.add(i, punishment);
								added = true;
								break;
							}
						}
						if (!added) {
							recentPunishments.add(punishment);
						}
					}
				} catch (Exception e) {
				}
			}
		}
	}

	private final ThreadLocal<SessionInfo> sessionInfo = new ThreadLocal<>();

	@Override
	public void handleWebSocket(HTTPWebSocket socket) throws IOException {
		if (!socket.requestURI.equals("/bc/webchat")) {
			return;
		}
		String sessionId = socket.cookies.get("SessID");
		if (sessionId == null) {
			sessionId = socket.get.get("session");
			if (sessionId == null) {
				return;
			}
		}
		plugin.getWebChat().acceptWebSocket(socket, sessionId);
	}

	private class SessionInfo {

		public final UUID uuid;
		public final String username;
		public final String sessionCookie;

		public SessionInfo(UUID uuid, String username, String sessionCookie) {
			this.uuid = uuid;
			this.username = username;
			this.sessionCookie = sessionCookie;
		}
	}

	@Override
	public void respond(HTTPRequest request) throws Exception {
		String timezone = request.cookies.getOrDefault("timezone", "America/Toronto");
		request.getCacheMap().put("timezone", timezone);

		boolean allowSeeingProsecutor = false;
		boolean allowChatlogs = false;
		boolean cubeSession = false;
		UUID userUUID = null;
		String username = null;
		String sessionCookie = null;
		String requestedPage = request.url;
		String myIP = request.getIPAddress();
//		if (myIP.equals("127.0.0.1")) {
//			allowSeeingProsecutor = true;
//			allowChatlogs = true;
//		}
		try {
			sessionCookie = request.cookies.get("SessID");
			if (sessionCookie != null) {
				HttpURLConnection conn = (HttpURLConnection) (new URL("http://127.0.0.1:2823/api/bc?cookie=" + sessionCookie).openConnection());
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				boolean loggedIn = reader.readLine().equals("1");
				cubeSession = true;
				if (loggedIn) {
					userUUID = UUID.fromString(reader.readLine().replaceAll("-", "").replaceAll("([0-9A-Fa-f]{8})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{12})", "$1-$2-$3-$4-$5"));
					username = reader.readLine();
					allowChatlogs = allowSeeingProsecutor = reader.readLine().equals("1");
				}
				reader.close();
			}
		} catch (Exception e) {
		}
		if (cubeSession) {
			sessionInfo.set(new SessionInfo(userUUID, username, sessionCookie));
		} else {
			sessionInfo.remove();
		}
//		for (ProxiedPlayer p : BungeeChat.getInstance().getProxy().getPlayers()) {
//			String playerIP = p.getAddress().getAddress().getHostAddress();
//			if (playerIP.equalsIgnoreCase(myIP)) {
//				if (p.hasPermission("hk.siggi.bungeechat.seeprosecutor")) {
//					allowSeeingProsecutor = true;
//				}
//				if (p.hasPermission("hk.siggi.bungeechat.allowchatlog")) {
//					allowChatlogs = true;
//				}
//			}
//		}
		boolean requestIsEncrypted = false;
		if (request.url.equals("/trigger")) {
			String uuidStr = request.get.get("uuid");
			boolean ban = request.get.get("ban") != null;
			String notifyPunishmentStr = request.get.get("notifypunishment");
			long notifyPunishment;
			try {
				notifyPunishment = Long.parseLong(notifyPunishmentStr);
			} catch (Exception e) {
				notifyPunishment = -1L;
			}
			if (uuidStr == null) {
				return;
			}
			try {
				UUID uuid = Util.uuidFromString(uuidStr);
				plugin.triggerUpdate(uuid, ban, notifyPunishment);
			} catch (Exception e) {
			}
			writePage(request, "OK");
			return;
		}
		if (request.url.startsWith("/api/playerips/")) {
			try {
				request.response.setHeader("Content-type", "text/plain");
				request.response.write("");
				String uuidStr = request.url.substring(15);
				UUID uuid = Util.uuidFromString(uuidStr);
				for (IP ip : plugin.getIPs(uuid)) {
					request.response.write(ip instanceof IPv6 ? ip.toShortString() : ip.toString() + "\r\n");
				}
			} catch (Exception e) {
			}
			return;
		}
		if (request.url.startsWith("/api/playersfromip/")) {
			try {
				request.response.setHeader("Content-type", "text/plain");
				request.response.write("");
				String ipBlock = request.url.substring(19);
				ipBlock = ipBlock.replace("-", ":");
				IP ip = IP.getIP(ipBlock);
				for (UUID uuid : plugin.getUUIDs(ip)) {
					request.response.write(uuid.toString().replace("-", "").toLowerCase() + "\r\n");
				}
			} catch (Exception e) {
			}
			return;
		}
		if (request.url.equals("/api/addserver")) {
			try {
				String server = request.get.get("server");
				String ip = request.get.get("ip");
				int port = Integer.parseInt(request.get.get("port"));
				plugin.addServer(server, ip, port);
				writePage(request, "OK");
			} catch (Exception e) {
				writePage(request, "FAIL");
			}
			return;
		}
		if (request.url.equals("/api/delserver")) {
			try {
				String server = request.get.get("server");
				plugin.delServer(server);
				writePage(request, "OK");
			} catch (Exception e) {
				writePage(request, "FAIL");
			}
			return;
		}
		if (request.url.equals("/api/servergroup")) {
			try {
				String server = request.get.get("server");
				String group = request.get.get("group");
				plugin.commandList.addAdditionalServer(group, server);
				writePage(request, "OK");
			} catch (Exception e) {
				writePage(request, "FAIL");
			}
			return;
		}
		if (request.url.equals("/api/allnames")) {
			try {
				request.response.setHeader("Content-Type", "application/json");
				UUIDCache uc = plugin.getUUIDCache();
				NicknameCache nc = plugin.getNicknameCache();
				JsonWriter writer = new JsonWriter(new OutputStreamWriter(request.response));
				writer.beginArray();
				for (UUID uuid : uc.getUUIDs()) {
					String name = uc.getNameFromUUID(uuid);
					String nick = nc.getNickname(uuid);
					if (name != null) {
						writer.beginObject();
						writer.name("uuid").value(uuid.toString());
						writer.name("name").value(name);
						if (nick != null) {
							writer.name("nick").value(nick);
						}
						writer.endObject();
					}
				}
				writer.endArray();
				writer.flush();
			} catch (Exception e) {
				writePage(request, "FAIL");
			}
			return;
		}
		if (requestedPage.toLowerCase().startsWith("/bc/t/")) {
			try {
				UUID uuid = Util.uuidFromString(requestedPage.substring(6));
				TemporaryResource resource = getResource(uuid);
				if (resource != null) {
					request.response.setHeader("Content-Type", resource.contentType);
					request.response.setHeader("Content-Length", Integer.toString(resource.getLength()));
					request.response.setHeader("Content-Disposition", "inline; filename=\"" + resource.filename + "\"");
					resource.write(request.response);
				}
			} catch (Exception e) {
			}
			return;
		}
		if (request.url.startsWith("/bc/") && !request.url.startsWith("/bc/chatlog")
				&& !request.url.equals("/bc/CB.svg")
				&& !request.url.equals("/bc/style.css")
				&& !request.url.equals("/bc/script.js")
				&& !request.url.equals("/bc/jstz.js")
				&& !request.url.equals("/bc/")) {
			if (username == null && !myIP.equals("127.0.0.1")) {
				request.response.setHeader("403 Forbidden");
				StringBuilder sb = new StringBuilder();
				sb.append("403 Forbidden: To access this page you must be logged in.");
				writePage(request, generatePage("CubeBuilders", sb.toString()));
				return;
			}
		}
		if (requestedPage.equalsIgnoreCase("/bc/")) {
			StringBuilder sb = new StringBuilder();
			sb.append("Choose an option above.");
			writePage(request, generatePage("CubeBuilders", sb.toString()));
			return;
		}
		if (requestedPage.equalsIgnoreCase("/bc/ontime")) {
			request.response.redirect("/bc/ontime/");
			return;
		}
		if (requestedPage.equalsIgnoreCase("/bc/ontime/")) {
			String player = request.get.getOrDefault("player", request.post.get("player"));
			String server = request.get.getOrDefault("server", request.post.get("server"));
			if (player == null && server != null) {
				request.response.completedRedirect("/bc/ontime/server/" + server);
			}
			String error = null;
			if (player != null) {
				if (player.contains("/")) {
					server = player.substring(player.indexOf("/") + 1);
					player = player.substring(0, player.indexOf("/"));
				}
				UUID uuid = BungeeChat.getInstance().getUUIDCache().getUUIDFromName(player);
				if (uuid == null) {
					error = "Player not found!";
				} else {
					request.response.completedRedirect("/bc/ontime/player/" + (uuid.toString().replaceAll("-", "").toLowerCase()) + (server == null ? "" : ("?server=" + server)));
					return;
				}
			}
			StringBuilder sb = new StringBuilder();
			sb.append("<h1>OnTime</h1><br>");
			if (error != null) {
				sb.append("Error: ").append(error).append("<br>");
			}
			sb.append("<form action=\"/bc/ontime/\" method=\"POST\">");
			sb.append("View OnTime for: <input type=\"text\" name=\"player\" id=\"playerfield\"><input type=\"submit\" value=\"Go!\">");
			sb.append("</form>");

			writePage(request, generatePage("OnTime", sb.toString(), "<meta name=\"ROBOTS\" content=\"NOINDEX, NOFOLLOW\"><script>window.onload = function() {document.getElementById(\"playerfield\").focus();}</script>"));
			return;
		}
		if (requestedPage.toLowerCase().startsWith("/bc/ontime/player/")) {
			String error = null;
			String req = requestedPage.substring(18);
			TimeZone timeZone = TimeZone.getTimeZone(timezone);
			int year = 0;
			if (req.contains("/")) {
				String[] parts = req.split("/");
				req = parts[0];
				try {
					year = Integer.parseInt(parts[1]);
				} catch (Exception e) {
				}
			}
			GregorianCalendar gregorianCalendar = new GregorianCalendar(timeZone);
			if (year == 0) {
				year = gregorianCalendar.get(Calendar.YEAR);
			}
			UUID uuid = Util.uuidFromString(req);
			req = uuid.toString().replaceAll("-", "").toLowerCase();
			String playerName = plugin.getUUIDCache().getNameFromUUID(uuid);
			StringBuilder sb = new StringBuilder();
			sb.append("<h1>OnTime</h1><br>");
			if (error != null) {
				sb.append("Error: ").append(error).append("<br>");
			}
			sb.append("<form action=\"/bc/ontime/\" method=\"POST\">");
			sb.append("View OnTime for: <input type=\"text\" name=\"player\" id=\"playerfield\"><input type=\"submit\" value=\"Go!\">");
			sb.append("</form>");

			long now = System.currentTimeMillis();
			OnTimePlayer player = OnTime.getInstance().getPlayer(uuid);
			OnTimeSessionRecord[] records = player.getSessionRecords();

			long totalTimeOn = OnTime.getTotalTimeLoggedIn(records);
			long lastLogout = 0L;
			if (records.length != 0) {
				lastLogout = records[records.length - 1].logout;
			}
			String lastSeen;
			if (lastLogout == 0L) {
				lastSeen = "Never";
			} else if (lastLogout == -1L) {
				lastSeen = "Currently Online";
			} else {
				lastSeen = TimeUtil.timeToString(lastLogout - now);
			}
			sb.append("<h1>OnTime for ");
			sb.append(playerName);
			sb.append("</h1>");

			sb.append("Last seen: ");
			sb.append(lastSeen);
			sb.append("<br>");
			sb.append("Total logins: ");
			sb.append(OnTime.getLoginCount(records));
			sb.append("<br>");
			sb.append("Total time online: ").append(TimeUtil.timeToString(totalTimeOn)).append("<br>");
			sb.append("<a href=\"/bc/ontime/player/").append(req).append("/").append(year - 1).append("\">").append(year - 1).append("</a>");
			sb.append(" | ").append(year);
			sb.append(" | <a href=\"/bc/ontime/player/").append(req).append("/").append(year + 1).append("\">").append(year + 1).append("</a>");
			sb.append(" | <a href=\"/bc/ontime/player_month/").append(req).append("\">Monthly</a>");
			sb.append(" | <a href=\"/bc/ontime/player_old/").append(req).append("\">Classic View</a>");
			sb.append("<div id=\"infotxt\">OnTime</div>");
			sb.append("<table>");
			sb.append("<tr>");
			for (int month = 0; month < 12; month++) {
				if (month != 0 && month % 3 == 0) {
					sb.append("</tr><tr>");
				}
				gregorianCalendar.set(Calendar.YEAR, year);
				gregorianCalendar.set(Calendar.MONTH, month);
				gregorianCalendar.set(Calendar.DAY_OF_MONTH, 1);
				gregorianCalendar.set(Calendar.HOUR_OF_DAY, 0);
				gregorianCalendar.set(Calendar.MINUTE, 0);
				gregorianCalendar.set(Calendar.SECOND, 0);
				gregorianCalendar.set(Calendar.MILLISECOND, 0);
				long startOfMonth = gregorianCalendar.getTimeInMillis();
				int firstDayOfMonth = gregorianCalendar.get(Calendar.DAY_OF_WEEK);
				int daysInMonth = gregorianCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
				gregorianCalendar.add(Calendar.MONTH, 1);
				long endOfMonth = gregorianCalendar.getTimeInMillis();
				int day = firstDayOfMonth - 1;
				OnTimeSessionRecord[] monthRecords = OnTime.trim(records, startOfMonth, endOfMonth);
				sb.append("<td>");
				sb.append(months[month]);
				if (month == 0) {
					sb.append(" ").append(year);
				}
				sb.append("<table class=\"monthtable\"><tr>");
				if (day > 0) {
					sb.append("<td colspan=").append(day).append("></td>");
				}
				for (int i = 1; i <= daysInMonth; i++) {
					gregorianCalendar.set(Calendar.YEAR, year);
					gregorianCalendar.set(Calendar.MONTH, month);
					gregorianCalendar.set(Calendar.DAY_OF_MONTH, i);
					long startOfDay = gregorianCalendar.getTimeInMillis();
					gregorianCalendar.add(Calendar.DAY_OF_MONTH, 1);
					long endOfDay = gregorianCalendar.getTimeInMillis();
					long timeOnlineToday = OnTime.getTotalTimeLoggedIn(monthRecords, startOfDay, endOfDay);
					day += 1;
					if (day > 7) {
						day = 1;
					}
					if (day == 1) {
						sb.append("</tr><tr>");
					}
					int textR = 0;
					int textG = 0;
					int textB = 0;
					int bgR = 0;
					int bgG = 0;
					int bgB = 0;
					if (timeOnlineToday == 0L) {
						textR = textG = textB = 255;
					} else if (timeOnlineToday < 3600000L) { // 0-1 hr, gray -> yellow
						bgR = scale(127, 255, 0L, 3600000L, timeOnlineToday);
						bgG = scale(127, 255, 0L, 3600000L, timeOnlineToday);
						bgB = scale(127, 0, 0L, 3600000L, timeOnlineToday);
					} else if (timeOnlineToday < 14400000L) { // 1-4 hrs, yellow -> green
						bgR = scale(255, 0, 3600000L, 14400000L, timeOnlineToday);
						bgG = scale(255, 255, 3600000L, 14400000L, timeOnlineToday);
						bgB = scale(0, 0, 3600000L, 14400000L, timeOnlineToday);
					} else if (timeOnlineToday < 28800000L) { // 4-8 hrs, green -> blue
						bgR = scale(0, 127, 14400000L, 28800000L, timeOnlineToday);
						bgG = scale(255, 127, 14400000L, 28800000L, timeOnlineToday);
						bgB = scale(0, 255, 14400000L, 28800000L, timeOnlineToday);
					} else if (timeOnlineToday < 43200000L) { // 8-12 hrs, blue -> magenta
						bgR = scale(127, 255, 28800000L, 43200000L, timeOnlineToday);
						bgG = scale(127, 0, 28800000L, 43200000L, timeOnlineToday);
						bgB = scale(255, 255, 28800000L, 43200000L, timeOnlineToday);
					} else { // 12-24 hrs, magenta -> red
						bgR = 255;
						bgG = 0;
						bgB = scale(255, 0, 43200000L, 86400000L, timeOnlineToday);
					}
					int textrgb = (textR << 16) + (textG << 8) + textB;
					int bgrgb = (bgR << 16) + (bgG << 8) + bgB;
					String textRgbStr = Integer.toString(textrgb, 16);
					String bgrgbStr = Integer.toString(bgrgb, 16);
					while (textRgbStr.length() < 6) {
						textRgbStr = "0" + textRgbStr;
					}
					while (bgrgbStr.length() < 6) {
						bgrgbStr = "0" + bgrgbStr;
					}
					String timeToString = TimeUtil.timeToString(timeOnlineToday);
					sb.append("<td style=\"color: #").append(textRgbStr).append("; background-color: #").append(bgrgbStr).append(";\" onmouseover=\"document.getElementById('infotxt').innerHTML='").append(timeToString).append("';\">");
					sb.append(i);
					sb.append("</td>");
				}
				if (day < 7) {
					sb.append("<td colspan=").append(7 - day).append("></td>");
				}
				sb.append("</tr></table>");
				sb.append("</td>");
			}
			sb.append("</tr></table>");
			sb.append("<table>");
			sb.append("<tr><td style=\"color: #ffffff; background-color: #000000;\">No Time</td></tr>");
			sb.append("<tr><td style=\"color: #000000; background-color: #7f7f7f;\">1 second</td></tr>");
			sb.append("<tr><td style=\"color: #000000; background-color: #ffff00;\">1 hour</td></tr>");
			sb.append("<tr><td style=\"color: #000000; background-color: #00ff00;\">4 hours</td></tr>");
			sb.append("<tr><td style=\"color: #000000; background-color: #7f7fff;\">8 hours</td></tr>");
			sb.append("<tr><td style=\"color: #000000; background-color: #ff00ff;\">12 hours</td></tr>");
			sb.append("<tr><td style=\"color: #000000; background-color: #ff0000;\">24 hours</td></tr>");
			sb.append("</table>");
			writePage(request, generatePage("OnTime", sb.toString(), "<meta name=\"ROBOTS\" content=\"NOINDEX, NOFOLLOW\"><script>window.onload = function() {document.getElementById(\"playerfield\").focus();}</script>"));
			return;
		}
		if (requestedPage.toLowerCase().startsWith("/bc/ontime/player_month/")) {
			String req = requestedPage.substring(24);
			int requestedYear = 0;
			int requestedMonth = 0;
			if (req.contains("/")) {
				String[] parts = req.split("/");
				req = parts[0];
				try {
					requestedYear = Integer.parseInt(parts[1]);
					requestedMonth = Integer.parseInt(parts[2]) - 1;
				} catch (Exception e) {
					requestedYear = 0;
					requestedMonth = 0;
				}
			}
			String server = request.get.get("server");
			UUID uuid = Util.uuidFromString(req);
			req = uuid.toString().replaceAll("-", "").toLowerCase();
			String playerName = plugin.getUUIDCache().getNameFromUUID(uuid);
			StringBuilder sb = new StringBuilder();
			OnTime onTime = plugin.getOnTime();
			OnTimePlayer onTimePlayer = onTime.getPlayer(uuid);
			OnTimeSessionRecord[] records = onTimePlayer.getSessionRecords();
			long now = System.currentTimeMillis();

			long totalTimeOn = OnTime.getTotalTimeLoggedIn(records);
			long totalTimeOnServer = OnTime.getTotalTimeLoggedIn(records, server);
			long lastLogout = 0L;
			if (records.length != 0) {
				lastLogout = records[records.length - 1].logout;
			}
			String lastSeen;
			if (lastLogout == 0L) {
				lastSeen = "Never";
			} else if (lastLogout == -1L) {
				lastSeen = "Currently Online";
			} else {
				lastSeen = TimeUtil.timeToString(lastLogout - now);
			}
			sb.append("<h1>OnTime for ");
			sb.append(playerName);
			sb.append("</h1>");

			TimeZone timeZone = TimeZone.getTimeZone(timezone);
			SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, MMMM d");
			dayFormat.setTimeZone(timeZone);

			int year = 0;
			int month = 0;

			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis(System.currentTimeMillis());
			year = cal.get(Calendar.YEAR);
			month = cal.get(Calendar.MONTH);

			if (requestedYear != 0) {
				year = requestedYear;
				month = requestedMonth;
			}

			int prevYear = year;
			int prevMonth = month - 1;
			if (prevMonth < 0) {
				prevMonth = 11;
				prevYear -= 1;
			}

			int nextYear = year;
			int nextMonth = month + 1;
			if (nextMonth > 11) {
				nextMonth = 0;
				nextYear += 1;
			}

			sb.append("<form action=\"/bc/ontime/\" method=\"POST\">");
			sb.append("View OnTime for: <input type=\"text\" name=\"player\" id=\"playerfield\"><input type=\"submit\" value=\"Go!\">");
			sb.append("</form>");
			sb.append("Total time online: ").append(TimeUtil.timeToString(totalTimeOn)).append("<br>");
			if (server != null) {
				sb.append("Total time on ").append(server).append(": ").append(TimeUtil.timeToString(totalTimeOnServer)).append("<br>");
			}
			sb.append("Last seen: ");
			sb.append(lastSeen);
			sb.append("<br>");
			sb.append("Total logins: ");
			sb.append(OnTime.getLoginCount(records));
			sb.append("<br>");
			sb.append("<a href=\"/bc/ontime/player_month/").append(req).append("/").append(prevYear).append("/").append(prevMonth + 1).append("\">Previous Month</a>");
			sb.append(" | <a href=\"/bc/ontime/player_month/").append(req).append("/").append(nextYear).append("/").append(nextMonth + 1).append("\">Next Month</a>");
			sb.append(" | <a href=\"/bc/ontime/player_old/").append(req).append("\">Classic View</a>");
			sb.append("<br><br>");
			sb.append("<h1>");
			sb.append(months[month]);
			sb.append(" ");
			sb.append(year);
			sb.append("</h1>");

			sb.append(generateMonthlyReport(records, year, month, timeZone, request));

			writePage(request, generatePage("OnTime for " + playerName, sb.toString(), "<meta name=\"ROBOTS\" content=\"NOINDEX, NOFOLLOW\"><script>window.onload = function() {document.getElementById(\"playerfield\").focus();}</script>"));
			return;
		}
		if (requestedPage.toLowerCase().startsWith("/bc/ontime/player_old/")) {
			String req = requestedPage.substring(22);
			String server = request.get.get("server");
			UUID uuid = Util.uuidFromString(req);
			req = uuid.toString().replaceAll("-", "").toLowerCase();
			String playerName = plugin.getUUIDCache().getNameFromUUID(uuid);
			StringBuilder sb = new StringBuilder();
			OnTime onTime = plugin.getOnTime();
			OnTimePlayer onTimePlayer = onTime.getPlayer(uuid);
			OnTimeSessionRecord[] records = onTimePlayer.getSessionRecords();
			long now = System.currentTimeMillis();

			long totalTimeOn = OnTime.getTotalTimeLoggedIn(records);
			long totalTimeOnServer = OnTime.getTotalTimeLoggedIn(records, server);
			long lastLogout = 0L;
			if (records.length != 0) {
				lastLogout = records[records.length - 1].logout;
			}
			String lastSeen;
			if (lastLogout == 0L) {
				lastSeen = "Never";
			} else if (lastLogout == -1L) {
				lastSeen = "Currently Online";
			} else {
				lastSeen = TimeUtil.timeToString(lastLogout - now);
			}
			HashMap<String, OnTimeInfo> onTimeInfoMap = new HashMap<String, OnTimeInfo>();
			sb.append("<h1>OnTime for ");
			sb.append(playerName);
			PlayerAccount info = plugin.getPlayerInfo(uuid);
			sb.append("</h1>");
			sb.append("<form action=\"/bc/ontime/\" method=\"POST\">");
			sb.append("View OnTime for: <input type=\"text\" name=\"player\" id=\"playerfield\"><input type=\"submit\" value=\"Go!\">");
			sb.append("</form>");
			sb.append("Total time online: ").append(TimeUtil.timeToString(totalTimeOn)).append("<br>");
			if (server != null) {
				sb.append("Total time on ").append(server).append(": ").append(TimeUtil.timeToString(totalTimeOnServer)).append("<br>");
			}
			sb.append("Last seen: ");
			sb.append(lastSeen);
			sb.append("<br>");
			sb.append("Total logins: ");
			sb.append(OnTime.getLoginCount(records));
			sb.append("<br><br>");

			TimeZone timeZone = TimeZone.getTimeZone(timezone);
			SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, MMMM d");
			dayFormat.setTimeZone(timeZone);

			GregorianCalendar beginningOfTodayCalendar = new GregorianCalendar();
			beginningOfTodayCalendar.setTimeZone(timeZone);
			beginningOfTodayCalendar.setTimeInMillis(System.currentTimeMillis());
			beginningOfTodayCalendar.set(Calendar.MILLISECOND, 0);
			beginningOfTodayCalendar.set(Calendar.SECOND, 0);
			beginningOfTodayCalendar.set(Calendar.MINUTE, 0);
			beginningOfTodayCalendar.set(Calendar.HOUR_OF_DAY, 0);
			long beginningOfToday = beginningOfTodayCalendar.getTimeInMillis();

			OnTimeSessionRecord[] record28Days = OnTime.trim(records, now - 2419200000L, now);
			for (int i = 0; i < 9; i++) {
				String day;
				long graphTime = beginningOfToday - (86400000L * i);
				if (i == 0) {
					day = "Today";
				} else if (i == 1) {
					day = "Yesterday";
				} else {
					day = dayFormat.format(new Date(graphTime));
				}
				sb.append(generateDailyReport_classic(record28Days, graphTime, onTimeInfoMap, request, day));
			}
//			sb.append(generateDailyReport(record28Days, now - 86400000L, onTimeInfoMap, request, "Last 24 hours"));
//			sb.append(generateDailyReport(record28Days, now - (86400000L * 2L), onTimeInfoMap, request, "24 hours before the previous 24 hours"));
//			sb.append(generateDailyReport(record28Days, now - (86400000L * 3L), onTimeInfoMap, request, "24 hours before the previous 48 hours"));
//			sb.append(generateDailyReport(record28Days, now - (86400000L * 4L), onTimeInfoMap, request, "24 hours before the previous 72 hours"));
//			sb.append(generateDailyReport(record28Days, now - (86400000L * 5L), onTimeInfoMap, request, "24 hours before the previous 96 hours"));
//			sb.append(generateDailyReport(record28Days, now - (86400000L * 6L), onTimeInfoMap, request, "24 hours before the previous 120 hours"));
//			sb.append(generateDailyReport(record28Days, now - (86400000L * 7L), onTimeInfoMap, request, "24 hours before the previous 144 hours"));
//			sb.append(generateDailyReport(record28Days, now - (86400000L * 8L), onTimeInfoMap, request, "24 hours before the previous 168 hours"));
//			sb.append(generateDailyReport(record28Days, now - (86400000L * 9L), onTimeInfoMap, request, "24 hours before the previous 192 hours"));

			sb.append(generateWeeklyReport_classic(record28Days, now - 604800000L, onTimeInfoMap, request, "Last 7 days"));
			sb.append(generateWeeklyReport_classic(record28Days, now - (604800000L * 2L), onTimeInfoMap, request, "2 weeks ago"));
			sb.append(generateWeeklyReport_classic(record28Days, now - (604800000L * 3L), onTimeInfoMap, request, "3 weeks ago"));
			sb.append(generateWeeklyReport_classic(record28Days, now - (604800000L * 4L), onTimeInfoMap, request, "4 weeks ago"));
			writePage(request, generatePage("OnTime for " + playerName, sb.toString(), "<meta name=\"ROBOTS\" content=\"NOINDEX, NOFOLLOW\"><script>window.onload = function() {document.getElementById(\"playerfield\").focus();}</script>"));
			return;
		}
		if (requestedPage.equalsIgnoreCase("/bc/recent")) {
			String player = request.get.getOrDefault("player", request.post.get("player"));
			String error = null;
			if (player != null) {
				UUID uuid = BungeeChat.getInstance().getUUIDCache().getUUIDFromName(player);
				if (uuid == null) {
					error = "Player not found!";
				} else {
					request.response.completedRedirect("/bc/player/" + (uuid.toString().replaceAll("-", "").toLowerCase()));
					return;
				}
			}
			StringBuilder sb = new StringBuilder();
			sb.append("<h1>Offence Records</h1><br>");
			sb.append("NOTE: This is the Offence History from the Legacy Offence System! If you're here to see more recent offences, or to appeal an offence, <a href=\"/mod/offences/\">click here for the new offence system.</a><br>");
			if (error != null) {
				sb.append("Error: ").append(error).append("<br>");
			}
			sb.append("<form action=\"/bc/recent\" method=\"POST\">");
			sb.append("View Offence History for: <input type=\"text\" name=\"player\"><input type=\"submit\" value=\"Go!\">");
			sb.append("</form>");
			sb.append("<table>");
			sb.append("<tr class=\"tablehead\">");
			if (allowSeeingProsecutor) {
				sb.append("<td>Prosecutor</td>");
			}
			sb.append("<td>Offender</td>");
			sb.append("<td>Sentence</td>");
			sb.append("<td>Date/Time</td>");
			sb.append("<td>Length</td>");
			sb.append("<td>Reason</td>");
			if (allowChatlogs) {
				sb.append("<td>Chat log</td>");
			}
			sb.append("</tr>");
			Punishment[] punishments;
			synchronized (recentPunishments) {
				punishments = recentPunishments.toArray(new Punishment[recentPunishments.size()]);
			}
			long now = System.currentTimeMillis();
			long earliestToAdd = now - (60 * 60 * 24 * 14 * 1000); // 2 weeks
			try {
				earliestToAdd = Long.parseLong(request.get.get("earliest"));
			} catch (Exception e) {
			}
			try {
				earliestToAdd = now - (60 * 60 * 24 * 1000 * Long.parseLong(request.get.get("age")));
			} catch (Exception e) {
			}
			boolean alternateLine = false;
			for (int i = punishments.length - 1; i >= 0; i--) {
				if (punishments[i].time < earliestToAdd) {
					continue;
				}
				alternateLine = !alternateLine;
				Punishment punishment = punishments[i];
				sb.append("<tr class=\"row").append(alternateLine ? "a" : "b").append("\">");
				if (allowSeeingProsecutor) {
					sb.append("<td>");
					if (punishment.issuedBy == BungeeChat.console) {
						sb.append("Console");
					} else {
						sb.append(plugin.getUUIDCache().getNameFromUUID(punishment.issuedBy));
					}
					sb.append("</td>");
				}
				sb.append("<td>");
				sb.append("<a href=\"/bc/player/");
				sb.append(punishment.issuedTo.toString().replaceAll("-", "").toLowerCase());
				sb.append("\">");
				sb.append(plugin.getUUIDCache().getNameFromUUID(punishment.issuedTo));
				sb.append("</a></td><td>");
				sb.append(punishment.action.toString());
				sb.append("</td><td>");
				sb.append(punishment.getIssueDate(timezone));
				sb.append("</td><td>");
				sb.append(punishment.getLength());
				sb.append("</td><td>");
				sb.append(punishment.reason);
				sb.append("</td>");
				if (allowChatlogs) {
					sb.append("<td>");
					boolean chatLogsAreApplicable;
					switch (punishment.action) {
						case BAN:
						case MUTE:
						case WARNING:
							chatLogsAreApplicable = true;
							break;
						default:
							chatLogsAreApplicable = false;
					}
					if (chatLogsAreApplicable) {
						sb.append("<a href=\"/bc/chatlog?players=");
						sb.append(plugin.getUUIDCache().getNameFromUUID(punishment.issuedTo));
						sb.append("&from=");
						sb.append((punishment.time / 1000L) - 120);
						sb.append("&to=");
						sb.append((punishment.time / 1000L) + 30);
						sb.append("\">Chat log</a>");
					} else {
						sb.append("n/a");
					}
				}
				sb.append("</td>");
				sb.append("</tr>");
			}
			sb.append("</table>");
			writePage(request, generatePage("Recent Offences", sb.toString()));
			return;
		}
		if (requestedPage.startsWith("/bc/player/")) {
			try {
				String input = requestedPage.substring(11);
				UUID playerUUID;
				if (input.length() <= 16) {
					playerUUID = BungeeChat.getInstance().getUUIDCache().getUUIDFromName(input);
				} else {
					playerUUID = Util.uuidFromString(input);
				}
				PlayerAccount player = new PlayerAccount(playerUUID);
				String playerName = plugin.getUUIDCache().getNameFromUUID(playerUUID);
				if (playerName == null) {
					playerName = "[" + (playerUUID.toString()) + "]";
				}
				StringBuilder sb = new StringBuilder();
				sb.append("<h1>Offence History - ").append(playerName).append("</h1><br>");
				sb.append("NOTE: This is the Offence History from the Legacy Offence System! If you're here to see more recent offences, or to appeal an offence, <a href=\"/mod/offences/").append(playerUUID.toString().toLowerCase().replaceAll("-", "")).append("\">click here for the new offence system.</a><br>");
				sb.append("<table>");
				sb.append("<tr class=\"tablehead\">");
				if (allowSeeingProsecutor) {
					sb.append("<td>Prosecutor</td>");
				}
				sb.append("<td>Offender</td>");
				sb.append("<td>Sentence</td>");
				sb.append("<td>Date/Time</td>");
				sb.append("<td>Length</td>");
				sb.append("<td>Reason</td>");
				if (allowChatlogs) {
					sb.append("<td>Chat log</td>");
				}
				sb.append("</tr>");
				Punishment[] punishments = player.getPunishments();
				boolean alternateLine = false;
				for (int i = punishments.length - 1; i >= 0; i--) {
					alternateLine = !alternateLine;
					Punishment punishment = punishments[i];
					sb.append("<tr class=\"row").append(alternateLine ? "a" : "b").append("\">");
					if (allowSeeingProsecutor) {
						sb.append("<td>");
						if (punishment.issuedBy == BungeeChat.console) {
							sb.append("Console");
						} else {
							sb.append(plugin.getUUIDCache().getNameFromUUID(punishment.issuedBy));
						}
						sb.append("</td>");
					}
					sb.append("<td>");
					sb.append(plugin.getUUIDCache().getNameFromUUID(punishment.issuedTo));
					sb.append("</td><td>");
					sb.append(punishment.action.toString());
					sb.append("</td><td>");
					sb.append(punishment.getIssueDate(timezone));
					sb.append("</td><td>");
					sb.append(punishment.getLength());
					sb.append("</td><td>");
					sb.append(punishment.reason);
					sb.append("</td>");
					if (allowChatlogs) {
						sb.append("<td>");
						boolean chatLogsAreApplicable;
						switch (punishment.action) {
							case BAN:
							case MUTE:
							case WARNING:
								chatLogsAreApplicable = true;
								break;
							default:
								chatLogsAreApplicable = false;
						}
						if (chatLogsAreApplicable) {
							sb.append("<a href=\"/bc/chatlog?players=");
							sb.append(plugin.getUUIDCache().getNameFromUUID(punishment.issuedTo));
							sb.append("&from=");
							sb.append((punishment.time / 1000L) - 120);
							sb.append("&to=");
							sb.append((punishment.time / 1000L) + 10);
							sb.append("\">Chat log</a>");
						} else {
							sb.append("n/a");
						}
						sb.append("</td>");
					}
					sb.append("</tr>");
				}
				if (!player.isMCBansExempt()) {
					MCBan[] mcBans = player.getMCBanList();
					for (MCBan mcBan : mcBans) {
						alternateLine = !alternateLine;
						sb.append("<tr class=\"row").append(alternateLine ? "a" : "b").append("\">");
						if (allowSeeingProsecutor) {
							sb.append("<td>");
							sb.append(mcBan.prosecutor);
							sb.append(" (");
							sb.append(mcBan.server);
							sb.append(")</td>");
						}
						sb.append("<td>");
						sb.append(plugin.getUUIDCache().getNameFromUUID(mcBan.player));
						sb.append("</td>");
						sb.append("<td>MCBan</td>");
						sb.append("<td></td>");
						sb.append("<td>Permanent</td>");
						sb.append("<td>");
						sb.append(mcBan.reason);
						sb.append("</td>");
						if (allowChatlogs) {
							sb.append("<td>n/a</td>");
						}
						sb.append("</tr>");
					}
				}
				sb.append("</table>");
				writePage(request, generatePage("Offence History - " + playerName + " - CubeBuilders", sb.toString()));
			} catch (Exception e) {
			}
			return;
		}
		if (requestedPage.equalsIgnoreCase("/bc/chatlog")) {
			StringBuilder sb = new StringBuilder();
			sb.append("<h1>Chat log</h1><br>");
			boolean autofocus = false;
			boolean bypassChatlogPermission = request.get.get("id") != null;
			if (!allowChatlogs && !bypassChatlogPermission) {
				sb.append("Sorry, you are not allowed to access the chat log.");
			} else {
				long from = -1;
				long to = -1;
				ArrayList<String> players = new ArrayList<String>();
				ArrayList<String> visiblePlayers = new ArrayList<String>();
				Map<String, String> logRequest = request.get;
				boolean requestedById = false;
				if (request.get.get("id") != null) {
					requestedById = true;
					long logId = Integer.parseInt(request.get.get("id"));
					String key = request.get.get("key");
					try {
						File savedLog = new File(plugin.getDataFolder(), "savedlog");
						if (key == null) {
							throw new Exception("No key?");
						}
						try {
							File file = new File(savedLog, logId + ".txt");
							Properties props = new Properties();
							props.load(new FileInputStream(file));
							for (Map.Entry a : props.entrySet()) {
								String kk = (String) a.getKey();
								String vv = (String) a.getValue();
								logRequest.put(kk, vv);
							}
						} catch (IOException ioe) {
							throw new Exception("Log not found.");
						}
						String correctKey = logRequest.get("key");
						if (!correctKey.equals(key)) {
							throw new Exception("Incorrect key.");
						}
					} catch (Exception e) {
						sb.append(e.getMessage());
						writePage(request, generatePage("Chat log", sb.toString()));
						return;
					}
				}
				String fromString = logRequest.get("from");
				String toString = logRequest.get("to");
				String playersString = logRequest.get("players");
				String vplayersString = logRequest.get("vplayers");
				if (fromString != null) {
					from = Long.parseLong(fromString) * 1000L;
				}
				if (toString != null) {
					to = Long.parseLong(toString) * 1000L;
				}
				if (playersString != null) {
					playersString = playersString.replaceAll(" ", "");
					if (!playersString.equals("")) {
						players.addAll(Arrays.asList(playersString.split(",")));
					}
				}
				if (vplayersString != null) {
					vplayersString = vplayersString.replaceAll(" ", "");
					if (!vplayersString.equals("")) {
						visiblePlayers.addAll(Arrays.asList(vplayersString.split(",")));
					}
				}
				if (to == -1) {
					to = System.currentTimeMillis();
				}
				if (from == -1) {
					sb.append("<form action=\"/bc/chatlog\" method=\"GET\">");
					sb.append("Players (comma separated): <input type=\"text\" name=\"players\" id=\"playersfield\"><br>");
					sb.append("Time: ");
					sb.append("<select name=\"from\">");
					long now = System.currentTimeMillis() / 1000L;
					sb.append("<option value=\"").append(now - 120).append("\">2 minutes</option>");
					sb.append("<option value=\"").append(now - 600).append("\">10 minutes</option>");
					sb.append("<option value=\"").append(now - 3600).append("\">1 hour</option>");
					sb.append("<option value=\"").append(now - 7200).append("\">2 hours</option>");
					sb.append("<option value=\"").append(now - 14400).append("\">4 hours</option>");
					sb.append("<option value=\"").append(now - 43200).append("\">12 hours</option>");
					sb.append("<option value=\"").append(now - 86400).append("\">24 hours</option>");
					sb.append("<option value=\"").append(now - 172800).append("\">48 hours</option>");
					sb.append("<option value=\"").append(now - 604800).append("\">1 week</option>");
					sb.append("<option value=\"").append(now - 2419200).append("\">4 weeks</option>");
					sb.append("</select>");
					sb.append("<br><input type=\"submit\" value=\"Get Chat log\">");
					sb.append("</form>");
					autofocus = true;
				} else {
					if (!requestedById && !requestIsEncrypted) {
						Properties properties = new Properties();
						properties.setProperty("ipaddress", request.getIPAddress());
						properties.setProperty("time", Long.toString(System.currentTimeMillis()));
						properties.setProperty("from", Long.toString(from / 1000L));
						properties.setProperty("to", Long.toString(to / 1000L));
						if (playersString != null) {
							properties.setProperty("players", playersString);
						}
						if (vplayersString != null) {
							properties.setProperty("vplayers", vplayersString);
						}
						String key = UUID.randomUUID().toString().replaceAll("-", "").toLowerCase();
						properties.setProperty("key", key);
						File file = new File(plugin.getDataFolder(), "savedlog");
						if (!file.exists()) {
							file.mkdirs();
						}
						long logId = nextLogID();
						file = new File(file, logId + ".txt");
						properties.store(new FileOutputStream(file), null);
						request.response.completedRedirect("/bc/chatlog?id=" + logId + "&key=" + key);
						return;
					}
					if (!allowChatlogs || requestIsEncrypted) {
						if (visiblePlayers.isEmpty()) {
							sb.append("Players: ");
							for (int i = 0; i < players.size(); i++) {
								if (i != 0) {
									sb.append(", ");
								}
								sb.append(players.get(i));
							}
							if (players.isEmpty()) {
								sb.append("<i>All</i>");
							}
						}
					} else {
						sb.append("<form action=\"/bc/chatlog\" method=\"GET\">");
						sb.append("<input type=\"hidden\" name=\"from\" value=\"").append(from / 1000L).append("\">");
						sb.append("<input type=\"hidden\" name=\"to\" value=\"").append(to / 1000L).append("\">");
						sb.append("Players to show (comma separated): <input type=\"text\" name=\"players\" value=\"").append(playersString == null ? "" : playersString).append("\"> (Leave blank to show all)<br>");
						sb.append("Unstarred names (case sensitive, comma separated): <input type=\"text\" name=\"vplayers\" value=\"").append(vplayersString == null ? "" : vplayersString).append("\"> (Leave blank to not star any name out)<br>");
						sb.append("<input type=\"submit\" value=\"OK\">");
						sb.append("</form>");
						sb.append("<br>");
						//sb.append("<a href=\"").append(encryptRequest("request=/bc/chatlog&showchatlog=1&from=" + (from / 1000L) + "&to=" + (to / 1000L) + (playersString == null ? "" : ("&players=" + playersString)) + (vplayersString == null ? "" : ("&vplayers=" + vplayersString)))).append("\">Encrypt chatlog</a> - click this then copy/paste the address bar to share this chatlog.<br><br><br>");
					}
					sb.append("<br>");
					sb.append("Note: All chat messages are shown as they were originally typed in by the player, before any caps filtering or censorship has been applied to it!<br><br>");
					ChatLogLine[] lines = ChatLogLine.getChatLogs(from, to);
					if (!requestIsEncrypted) {
						sb.append("<a href=\"/bc/chatlog?from=").append((from / 1000L) - 120L).append("&to=").append(to / 1000L).append(playersString == null ? "" : ("&players=" + playersString)).append(vplayersString == null ? "" : ("&vplayers=" + vplayersString)).append("\"> + 2 minutes before</a>");
					}
					sb.append("<table>");
					sb.append("<tr class=\"tablehead\">");
					if (!requestIsEncrypted) {
						sb.append("<td>Trim Chat Log</td>");
					}
					sb.append("<td>Time</td>");
					sb.append("<td>Type</td>");
					sb.append("<td>Recipient(s)</td>");
					sb.append("<td>Sender</td>");
					sb.append("<td>Message</td>");
					sb.append("</tr>");
					boolean alternateLine = false;
					for (ChatLogLine line : lines) {
						if (!players.isEmpty()) {
							boolean isInvolved = false;
							for (String player : players) {
								if (line.isPlayerLikelyInvolved(player)) {
									isInvolved = true;
									break;
								}
							}
							if (!isInvolved) {
								continue;
							}
						}
						alternateLine = !alternateLine;
						String chatSender = line.sender.username;
						if (!visiblePlayers.isEmpty() && !visiblePlayers.contains(chatSender)) {
							chatSender = "************";
						}
						long chatLineTime = (line.time) / 1000L;
						String startEnd = "<td><a href=\"/bc/chatlog?from=" + chatLineTime + "&to=" + (to / 1000L) + (playersString == null ? "" : ("&players=" + playersString)) + (vplayersString == null ? "" : ("&vplayers=" + vplayersString)) + "\">Start</a>/"
								+ "<a href=\"/bc/chatlog?from=" + (from / 1000L) + "&to=" + (chatLineTime + 1L) + (playersString == null ? "" : ("&players=" + playersString)) + (vplayersString == null ? "" : ("&vplayers=" + vplayersString)) + "\">End</a></td>";
						if (line instanceof PublicChatLog) {
							PublicChatLog chat = (PublicChatLog) line;
							sb.append("<tr class=\"row").append(alternateLine ? "a" : "b").append("\">");
							if (!requestIsEncrypted) {
								sb.append(startEnd);
							}
							sb.append("<td>").append(chat.getDateTime(timezone)).append("</td>");
							sb.append("<td>Public Chat (").append(chat.server).append(")</td>");
							sb.append("<td></td>");
							sb.append("<td>").append(chatSender).append("</td>");
							sb.append("<td>").append(chat.message).append("</td>");
							sb.append("</tr>");
						} else if (line instanceof FactionChatLog) {
							FactionChatLog chat = (FactionChatLog) line;
							sb.append("<tr class=\"row").append(alternateLine ? "a" : "b").append("\">");
							if (!requestIsEncrypted) {
								sb.append(startEnd);
							}
							sb.append("<td>").append(chat.getDateTime(timezone)).append("</td>");
							sb.append("<td>Faction Chat</td>");
							sb.append("<td>");
							if (visiblePlayers.isEmpty()) {
								boolean didPrintFirstNameAlready = false;
								for (ChatLogUser p : chat.getWitnesses()) {
									if (didPrintFirstNameAlready) {
										sb.append(", ");
									} else {
										didPrintFirstNameAlready = true;
									}
									sb.append(p.username);
								}
							} else {
								ChatLogUser[] witnesses = chat.getWitnesses();
								sb.append(witnesses.length);
								sb.append(" witness");
								if (witnesses.length != 1) {
									sb.append("es");
								}
							}
							sb.append("</td>");
							sb.append("<td>").append(chatSender).append("</td>");
							sb.append("<td>").append(chat.message).append("</td>");
							sb.append("</tr>");
						} else if (line instanceof GroupChatLog) {
							GroupChatLog chat = (GroupChatLog) line;
							sb.append("<tr class=\"row").append(alternateLine ? "a" : "b").append("\">");
							if (!requestIsEncrypted) {
								sb.append(startEnd);
							}
							sb.append("<td>").append(chat.getDateTime(timezone)).append("</td>");
							sb.append("<td>Group Chat:").append(chat.groupName).append("</td>");
							sb.append("<td>");
							if (visiblePlayers.isEmpty()) {
								boolean didPrintFirstNameAlready = false;
								for (ChatLogUser p : chat.getWitnesses()) {
									if (didPrintFirstNameAlready) {
										sb.append(", ");
									} else {
										didPrintFirstNameAlready = true;
									}
									sb.append(p.username);
								}
							} else {
								ChatLogUser[] witnesses = chat.getWitnesses();
								sb.append(witnesses.length);
								sb.append(" witness");
								if (witnesses.length != 1) {
									sb.append("es");
								}
							}
							sb.append("</td>");
							sb.append("<td>").append(chatSender).append("</td>");
							sb.append("<td>").append(chat.message).append("</td>");
							sb.append("</tr>");
						} else if (line instanceof PrivateChatLog) {
							PrivateChatLog chat = (PrivateChatLog) line;
							sb.append("<tr class=\"row").append(alternateLine ? "a" : "b").append("\">");
							if (!requestIsEncrypted) {
								sb.append(startEnd);
							}
							sb.append("<td>").append(chat.getDateTime(timezone)).append("</td>");
							sb.append("<td>Private Chat</td>");
							sb.append("<td>").append(!visiblePlayers.isEmpty() && !visiblePlayers.contains(chat.recipient.username) ? "************" : chat.recipient.username).append("</td>");
							sb.append("<td>").append(chatSender).append("</td>");
							sb.append("<td>").append(chat.message).append("</td>");
							sb.append("</tr>");
						} else if (line instanceof MailChatLog) {
							MailChatLog chat = (MailChatLog) line;
							sb.append("<tr class=\"row").append(alternateLine ? "a" : "b").append("\">");
							if (!requestIsEncrypted) {
								sb.append(startEnd);
							}
							sb.append("<td>").append(chat.getDateTime(timezone)).append("</td>");
							sb.append("<td>Mail</td>");
							sb.append("<td>").append(!visiblePlayers.isEmpty() && !visiblePlayers.contains(chat.recipient.username) ? "************" : chat.recipient.username).append("</td>");
							sb.append("<td>").append(chatSender).append("</td>");
							sb.append("<td>").append(chat.message).append("</td>");
							sb.append("</tr>");
						} else if (line instanceof StaffChatLog) {
							StaffChatLog chat = (StaffChatLog) line;
							sb.append("<tr class=\"row").append(alternateLine ? "a" : "b").append("\">");
							if (!requestIsEncrypted) {
								sb.append(startEnd);
							}
							sb.append("<td>").append(chat.getDateTime(timezone)).append("</td>");
							sb.append("<td>Staff Chat</td>");
							sb.append("<td></td>");
							sb.append("<td>").append(chatSender).append("</td>");
							sb.append("<td>").append(chat.message).append("</td>");
							sb.append("</tr>");
						}
					}
					sb.append("</table>");
					if (!requestIsEncrypted) {
						sb.append("<a href=\"/bc/chatlog?from=").append(from / 1000L).append("&to=").append((to / 1000L) + 120L).append(playersString == null ? "" : ("&players=" + playersString)).append(vplayersString == null ? "" : ("&vplayers=" + vplayersString)).append("\"> + 2 minutes after</a>");
					}
				}
			}
			writePage(request, generatePage("Chat log", sb.toString()).replaceAll("</head>", autofocus ? "<script>window.onload = function() {document.getElementById(\"playersfield\").focus();}</script></head>" : "</head>"));
			return;
		}
		if (requestedPage.equalsIgnoreCase("/bc/CB.svg")) {
			request.response.setHeader("Content-Type", "image/svg+xml");
			request.response.setHeader("Cache-Control", "max-age=3600");
			request.response.setHeader("User-Cache-Control", "max-age=3600");
			request.response.setHeader("Pragma", "cache");
			String file = read("/CB.svg");
			request.response.setHeader("Content-Length", Integer.toString(file.length()));
			request.response.write(file);
			return;
		}
		if (requestedPage.equalsIgnoreCase("/bc/style.css")) {
			request.response.setHeader("Content-Type", "text/css");
			request.response.setHeader("Cache-Control", "max-age=3600");
			request.response.setHeader("User-Cache-Control", "max-age=3600");
			request.response.setHeader("Pragma", "cache");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			out.write("body{".getBytes());
			out.write("background-color:#000000;".getBytes());
			out.write("color:#ffffff;".getBytes());
			out.write("font-family:Helvetica;".getBytes());
			out.write("margin:8px;".getBytes());
			out.write("font-size:12px;".getBytes());
			out.write("}".getBytes());
			out.write("a.logo{".getBytes());
			out.write("background:url(\"/bc/CB.svg\");".getBytes());
			out.write("width:128px;".getBytes());
			out.write("height:128px;".getBytes());
			out.write("display:block;".getBytes());
			out.write("}".getBytes());
			out.write("table{".getBytes());
			out.write("border-spacing:0;".getBytes());
			out.write("border-collapse:separate;".getBytes());
			out.write("}".getBytes());
			out.write("body>table{".getBytes());
			out.write("margin-left:-8px;".getBytes());
			out.write("margin-right:-8px;".getBytes());
			out.write("}".getBytes());
			out.write("td{".getBytes());
			out.write("padding:4px;".getBytes());
			out.write("min-width:70px;".getBytes());
			out.write("}".getBytes());
			out.write("tr.tablehead>td{".getBytes());
			out.write("background-color:#660000;".getBytes());
			out.write("font-weight:bold;".getBytes());
			out.write("}".getBytes());
			out.write("tr.rowa>td{".getBytes());
			out.write("background-color:#440000;".getBytes());
			out.write("}".getBytes());
			out.write("tr.rowb>td{".getBytes());
			out.write("background-color:#220000;".getBytes());
			out.write("}".getBytes());
			out.write("a:link{".getBytes());
			out.write("color:#ffffff;".getBytes());
			out.write("text-decoration:underline;".getBytes());
			out.write("}".getBytes());
			out.write("a:visited{".getBytes());
			out.write("color:#ffffff;".getBytes());
			out.write("text-decoration:underline;".getBytes());
			out.write("}".getBytes());
			out.write("a:hover{".getBytes());
			out.write("color:#ff8888;".getBytes());
			out.write("text-decoration:none;".getBytes());
			out.write("}".getBytes());
			out.write("input[type=text]{".getBytes());
			out.write("border:0;".getBytes());
			out.write("background-color:#440000;".getBytes());
			out.write("padding:3px;".getBytes());
			out.write("outline:0;".getBytes());
			out.write("color:#fff;".getBytes());
			out.write("}".getBytes());
			out.write("input:focus[type=text]{".getBytes());
			out.write("background-color:#550000;".getBytes());
			out.write("}".getBytes());
			out.write("input:hover[type=text]{".getBytes());
			out.write("background-color:#660000;".getBytes());
			out.write("}".getBytes());
			out.write("input:active[type=text]{".getBytes());
			out.write("background-color:#770000;".getBytes());
			out.write("}".getBytes());
			out.write("input[type=submit]{".getBytes());
			out.write("border:0;".getBytes());
			out.write("background-color:#440000;".getBytes());
			out.write("padding:3px;".getBytes());
			out.write("outline:0;".getBytes());
			out.write("color:#fff;".getBytes());
			out.write("}".getBytes());
			out.write("input:hover[type=submit]{".getBytes());
			out.write("background-color:#660000;".getBytes());
			out.write("}".getBytes());
			out.write("div.footer{".getBytes());
			out.write("display:block;".getBytes());
			out.write("width:100%;".getBytes());
			out.write("font-size:10px;".getBytes());
			out.write("text-align:center;".getBytes());
			out.write("}".getBytes());

			out.write("div.toplinks{".getBytes());
			out.write("display:block;".getBytes());
			out.write("margin:4px;".getBytes());
			out.write("}".getBytes());
			out.write("div.toplinks>a:link{".getBytes());
			out.write("color:#ffffff;".getBytes());
			out.write("text-decoration:none;".getBytes());
			out.write("display:inline-block;".getBytes());
			out.write("min-width:150px;".getBytes());
			out.write("padding:4px;".getBytes());
			out.write("text-align:center;".getBytes());
			out.write("background-color:#770000;".getBytes());
			out.write("}".getBytes());
			out.write("div.toplinks>a:visited{".getBytes());
			out.write("color:#ffffff;".getBytes());
			out.write("text-decoration:none;".getBytes());
			out.write("background-color:#770000;".getBytes());
			out.write("}".getBytes());
			out.write("div.toplinks>a:hover{".getBytes());
			out.write("color:#ffffff;".getBytes());
			out.write("text-decoration:none;".getBytes());
			out.write("background-color:#aa0000;".getBytes());
			out.write("}".getBytes());

			out.write("table.monthtable td {".getBytes());
			out.write("min-width: 20px;".getBytes());
			out.write("}".getBytes());

			byte[] b = out.toByteArray();
			request.response.setHeader("Content-Length", Integer.toString(b.length));
			request.response.write(b);
			return;
		}
		if (requestedPage.equalsIgnoreCase("/bc/script.js")) {
			request.response.setHeader("Content-Type", "application/javascript");
			request.response.setHeader("Cache-Control", "max-age=3600");
			request.response.setHeader("User-Cache-Control", "max-age=3600");
			request.response.setHeader("Pragma", "cache");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			out.write("document.cookie=\"timezone=\"+jstz.determine().name()+\";expires=Wed, 1 Jan 2020 12:00:00 UTC;path=/\";".getBytes());
			out.write("var retina=window.devicePixelRatio>1;".getBytes());
			out.write("document.cookie=\"hidpi=\"+(retina?\"1\":\"0\")+\";expires=Wed, 1 Jan 2020 12:00:00 UTC;path=/;\";".getBytes());
			byte[] b = out.toByteArray();
			request.response.setHeader("Content-Length", Integer.toString(b.length));
			request.response.write(b);
			return;
		}
		if (requestedPage.equalsIgnoreCase("/bc/jstz.js")) {
			request.response.setHeader("Content-Type", "application/javascript");
			request.response.setHeader("Cache-Control", "max-age=3600");
			request.response.setHeader("User-Cache-Control", "max-age=3600");
			request.response.setHeader("Pragma", "cache");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = BungeeResponder.class.getResourceAsStream("/jstz.js");
			int c;
			byte[] buffer = new byte[4096];
			while ((c = in.read(buffer, 0, buffer.length)) != -1) {
				out.write(buffer, 0, c);
			}
			byte[] b = out.toByteArray();
			request.response.setHeader("Content-Length", Integer.toString(b.length));
			request.response.write(b);
			return;
		}
		if (requestedPage.equals("/private/chatcensor")) {
			String word = request.post.get("word");
			if (word != null) {
				String lc = word.toLowerCase();
				request.response.setHeader("Content-Type", "text/plain");
				PrintWriter pw = new PrintWriter(request.response, true);
				pw.println("Word: " + lc);
				pw.println();
				ChatCensor censor = BungeeChat.getInstance().getChatCensor().filterOnly(word);
				for (ChatLogLine cll : Util.iterable(ChatLogLine.iterateAllChats())) {
					String message = stripChatCodes(cll.message);
					if (!message.toLowerCase().contains(lc)) {
						String censoredMessage = censor.filter(message);
						if (!message.equals(censoredMessage)) {
							pw.println(message);
							pw.println(censoredMessage);
							pw.println();
						}
					}
				}
				pw.close();
				return;
			}
			StringBuilder page = new StringBuilder();
			page.append("Chat Censor Test:<br>");
			page.append("<form action=\"/private/chatcensor\" method=\"POST\">");
			page.append("Enter word: <input type=\"text\" name=\"word\"><input type=\"submit\">");
			page.append("</form>");
			writePage(request, generatePage("Chat Censor Test", page.toString()));
			return;
		}
		if (requestedPage.equals("/private/stats") || requestedPage.equals("/private/stats/")) {
			request.response.setHeader("Content-Type", "application/json");
			request.response.write((request.get.get("pretty") != null ? ServerActivityReport.getPrettyGson() : ServerActivityReport.getGson()).toJson(ServerActivityReport.generateReport(TimeZone.getTimeZone(timezone))).getBytes());
			return;
		}
		if (requestedPage.startsWith("/private/stats/")) {
			UUID player = null;
			try {
				player = Util.uuidFromString(requestedPage.substring(15));
			} catch (Exception e) {
			}
			if (player != null) {
				request.response.setHeader("Content-Type", "application/json");
				request.response.write((request.get.get("pretty") != null ? UserActivityReport.getPrettyGson() : UserActivityReport.getGson()).toJson(UserActivityReport.generateReport(player)).getBytes());
				return;
			}
		}
		if (requestedPage.equalsIgnoreCase("/twilio/text")) {
			request.response.setHeader("Content-Type", "text/plain");
			String from = request.get.get("uuid");
			String text = request.get.get("text");
			UUID fromUUID = Util.uuidFromString(from);
			PlayerAccount account = plugin.getPlayerInfo(fromUUID);
			String textParts[] = text.split(" ");

			switch (textParts[0].toLowerCase()) {
				case "alerts": {
					if (textParts.length > 1) {
						boolean alertsOn = Util.parseBool(textParts[1]);
						account.setAlertsOn(alertsOn);
						if (alertsOn) {
							request.response.write("Alerts have been turned on!");
						} else {
							request.response.write("Alerts have been turned off!");
						}
					} else {
						request.response.write("Alerts are currently " + (account.isAlertsOn() ? "on" : "off") + ". Alerts may include warnings that an explosion occurred in your faction, or reminders.\nEnable with ALERTS ON, disable with ALERTS OFF.");
					}
				}
				break;
				case "messages": {
					if (textParts.length > 1) {
						boolean messagesOn = Util.parseBool(textParts[1]);
						account.setMessagesOn(messagesOn);
						if (messagesOn) {
							request.response.write("Messages have been turned on!");
						} else {
							request.response.write("Messages have been turned off!");
						}
					} else {
						request.response.write("Messages are currently " + (account.isMessagesOn() ? "on" : "off") + ". Messages may include offers, promotions, or notifications of in-game events.\nEnable with MESSAGES ON, disable with MESSAGES OFF.");
					}
				}
				break;
				case "mail": {
					if (textParts.length > 1) {
						boolean mailOn = Util.parseBool(textParts[1]);
						account.setMailOn(mailOn);
						if (mailOn) {
							request.response.write("Mail has been turned on!");
						} else {
							request.response.write("Mail has been turned off!");
						}
					} else {
						request.response.write("Mail is currently " + (account.isMailOn() ? "on" : "off") + ". Mail is messages sent to you by other players either through CubeBuilders Texts or using the in-game /mail command.\nEnable with MAIL ON, disable with MAIL OFF.\nSend mail to other users with MAIL [username] [message].");
					}
				}
				break;
			}
			String myName = plugin.getPlayerNameHandler().getNameByPlayer(fromUUID);
			boolean plusMember = account.getCurrentRank() != null;
			request.response.write("Registered to: " + myName + (plusMember ? " (Plus Member)" : " (Free Member)") + "\n"
					+ "To unregister, type UNLINK\n"
					+ "\n"
					+ "Commands:\n"
					+ "MAIL [username] [message]\n"
					+ "MAIL [ON|OFF]\n"
					+ "ALERTS [ON|OFF]\n"
					+ "MESSAGES [ON|OFF]\n");
			return;
		}
		if (requestedPage.equalsIgnoreCase("/twilio/mail")) {
			request.response.setHeader("Content-Type", "text/plain");
			String from = request.get.get("from");
			String to = request.get.get("to");
			String message = request.get.get("message");
			if (from == null || to == null || message == null) {
				request.response.write("Cannot send mail, a server error has occurred.");
				return;
			}
			if (to.equalsIgnoreCase("Server")) {
				request.response.write("Cannot send mail: Server does not accept incoming messages. Perhaps you meant to send it to Siggi88?");
				return;
			}
			UUID fromUUID = Util.uuidFromString(from);
			PlayerAccount accountFrom = plugin.getPlayerInfo(fromUUID);
			if (accountFrom.getCurrentRank() == null) {
				request.response.write("Cannot send mail: Mail is available to Plus members only! Add a Plus membership to your account here: https://cubebuilders.net/store (Msg&data rates may apply)");
				return;
			}
			String fromNameCorrected = Util.uuidToString(fromUUID);
			CBUser user = BungeeChat.getUser(fromUUID);
			boolean isNameBanned = plugin.isNameBanned(username);
			boolean isBanned = user.isBanned(); // TODO: Add a check!
			String customBanMessage = user.getUserData().banMessageString;
			if (customBanMessage != null) {
				isBanned = true;
			}
			boolean isMuted = user.isMuted(); // TODO: Add a check!
			boolean isMutePermanent = user.getExpiry(net.cubebuilders.user.Punishment.PunishmentAction.MUTE) == -1L; // TODO: Add a check!
			if (isBanned) {
				if (customBanMessage != null) {
					request.response.write("Cannot send mail: " + customBanMessage);
				} else {
					request.response.write("Cannot send mail: You are currently banned from CubeBuilders and cannot send mail.");
				}
				return;
			}
			if (isMuted) {
				if (isMutePermanent) {
					request.response.write("Cannot send mail: You are permanently muted on CubeBuilders and cannot send mail.");
				} else {
					request.response.write("Cannot send mail: You are currently muted on CubeBuilders and cannot send mail.");
				}
				return;
			}
			if (isNameBanned) {
				request.response.write("Cannot send mail: Your username (" + fromNameCorrected + ") is blacklisted. Please change your name at https://accounts.mojang.com/me and then login to CubeBuilders in-game at least once to update your name on CubeBuilders.");
				return;
			}
			UUID toUUID = plugin.getPlayerNameHandler().getPlayerByName(to);
			if (toUUID == null) {
				request.response.write("Cannot send mail: " + to + " has never joined CubeBuilders before.");
				return;
			}
			PlayerAccount accountTo = plugin.getPlayerInfo(toUUID);
			if (accountTo == null) {
				request.response.write("A server error occurred, please report this to Siggi88!");
				return;
			}
			/*if (accountTo.isCurrentlyBanned()) {
			 request.response.write("Cannot send mail: " + plugin.getUUIDCache().getNameFromUUID(accountTo.player) + " is currently banned from CubeBuilders.");
			 return;
			 }*/
			if (accountTo.getMail().length >= accountTo.getMaxMail()) {
				request.response.write("Cannot send mail: Recipient inbox is full!");
				return;
			}
			accountTo.sendMail(fromUUID, message, true, true);
			request.response.write("OK");
			return;
		}
	}

	private final Color[] colours = new Color[]{
		new Color(192, 0, 0), // Red
		new Color(255, 127, 0), // Orange
		new Color(255, 255, 0), // Yellow
		new Color(127, 255, 127), // Green
		new Color(0, 255, 255), // Cyan
		new Color(0, 0, 255), // Blue
		new Color(255, 127, 127), // Light Red
		new Color(255, 192, 127), // Light Orange
		new Color(255, 255, 127), // Light Yellow
		new Color(192, 255, 192), // Light Green
		new Color(127, 127, 255), // Light Blue
		new Color(255, 192, 203), // Pink
		new Color(127, 127, 0), // Dark Yellow
		new Color(0, 127, 0), // Dark Green
		new Color(0, 0, 127), // Dark Blue
		new Color(86, 60, 92), // Dark Purple
		new Color(255, 255, 255), // White
		new Color(191, 191, 191), // Light Gray
		new Color(127, 127, 127), // Gray
		new Color(63, 63, 63), // Dark Gray
		new Color(0, 0, 0) // Black
	};

	private String generateDailyReport_classic(OnTimeSessionRecord[] records, long startTime, HashMap<String, OnTimeInfo> onTimeInfoMap, HTTPRequest request, String graphName) {
		boolean hidpi = request.cookies.getOrDefault("hidpi", "0").equals("1");
		for (OnTimeInfo info : onTimeInfoMap.values()) {
			info.time = 0L;
		}
		String timezone = (String) request.getCacheMap().get("timezone");
		if (timezone == null) {
			timezone = "America/Toronto";
		}
		UUID playerUUID = null;
		String playerName = null;
		if (records.length > 0) {
			playerUUID = records[0].player;
			playerName = plugin.getUUIDCache().getNameFromUUID(playerUUID);
			if (playerName == null) {
				playerName = playerUUID.toString().toLowerCase();
			}
		}
		BufferedImage image = new BufferedImage(Integer.parseInt(request.get.getOrDefault("graphwidth", request.cookies.getOrDefault("graphwidth", "600"))) * (hidpi ? 2 : 1), 500 * (hidpi ? 2 : 1), BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(
				RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		int realWidth = imageWidth / (hidpi ? 2 : 1);
		int realHeight = imageHeight / (hidpi ? 2 : 1);

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, imageWidth, imageHeight);

		int graphHeight = 30 * (hidpi ? 2 : 1);

		long now = System.currentTimeMillis();

		TimeZone timeZone = TimeZone.getTimeZone(timezone);
		SimpleDateFormat dateFormat = new SimpleDateFormat("ha");
		dateFormat.setTimeZone(timeZone);

		SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd/MM/yyyy h:mm:ss a z");
		fullDateFormat.setTimeZone(timeZone);

		long endTime = startTime + 86400000L;
		records = OnTime.trim(records, startTime, endTime);
		if (records.length == 0) {
			return "";
		}
		String fontName = request.get.getOrDefault("graphfont", request.cookies.getOrDefault("graphfont", "Helvetica"));
		int fontSize = Integer.parseInt(request.get.getOrDefault("graphfontsize", request.cookies.getOrDefault("graphfontsize", "12")));
		Font font = new Font(fontName, 0, fontSize);
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics(font);
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int fontHeight = ascent + descent;

		g.setColor(new Color(48, 0, 0));
		g.fillRect(0, fontHeight * (hidpi ? 2 : 1), imageWidth, graphHeight);
		if (now > startTime && now < endTime) {
			g.setColor(new Color(24, 0, 0));
			int drawX = (int) (imageWidth * this.getPosition(now, startTime, endTime));
			int drawY = fontHeight * (hidpi ? 2 : 1);
			int drawWidth = ((int) (imageWidth * this.getPosition(endTime, startTime, endTime))) - drawX;
			int drawHeight = graphHeight;
			g.fillRect(drawX, drawY, drawWidth, drawHeight);
		}
		for (OnTimeSessionRecord record : records) {
			OnTimeInfo info = onTimeInfoMap.get(record.server);
			if (info == null) {
				info = new OnTimeInfo(record.server, colours[onTimeInfoMap.size()]);
				onTimeInfoMap.put(record.server, info);
			}
			long start = Math.max(record.login, startTime);
			long end = Math.min(record.logout == -1L ? now : record.logout, endTime);
			long totalTime = end - start;
			info.time += totalTime;
			double startPos = getPosition(start, startTime, endTime);
			double endPos = getPosition(end, startTime, endTime);
			g.setColor(info.color);
			g.fillRect((int) (startPos * imageWidth), fontHeight * (hidpi ? 2 : 1), ((int) ((endPos - startPos) * imageWidth)) + 1, graphHeight);
		}

		if (hidpi) {
			((Graphics2D) g).scale(2.0, 2.0);
		}

		GregorianCalendar theHourCalendar = new GregorianCalendar();
		theHourCalendar.setTimeZone(timeZone);
		theHourCalendar.setTimeInMillis(endTime);
		theHourCalendar.set(Calendar.MINUTE, 0);
		theHourCalendar.set(Calendar.SECOND, 0);
		theHourCalendar.set(Calendar.MILLISECOND, 0);
		long theHour = theHourCalendar.getTimeInMillis();

		g.setColor(Color.WHITE);
		while (theHour >= startTime) {
			int timeX = (int) (realWidth * getPosition(theHour, startTime, endTime));
			g.drawLine(timeX, 25 + fontHeight, timeX, 30 + fontHeight);
			g.drawString(dateFormat.format(new Date(theHour)).replaceAll("AM", "am").replaceAll("PM", "pm"), timeX, 30 + ascent + fontHeight);
			theHour -= (3600000L * 2L);
		}

		ArrayList<OnTimeInfo> onTimeInfoList = new ArrayList<OnTimeInfo>();
		for (String server : onTimeInfoMap.keySet()) {
			OnTimeInfo info = onTimeInfoMap.get(server);
			if (info.time <= 0) {
				continue;
			}
			boolean added = false;
			for (int i = onTimeInfoList.size() - 1; i >= 0; i--) {
				if (onTimeInfoList.get(i).time > info.time) {
					onTimeInfoList.add(i + 1, info);
					added = true;
					break;
				}
			}
			if (!added) {
				onTimeInfoList.add(0, info);
			}
		}

		int legendWidth = 196;
		int columns = realWidth / legendWidth;
		int rows = (int) Math.ceil(((float) onTimeInfoList.size()) / ((float) columns));
		int drawnRows = 0;
		int x = 2;
		int y = 30 + (fontHeight * 2);

		for (OnTimeInfo info : onTimeInfoList) {
			g.setColor(info.color);
			g.fillRect(x, y, fontHeight, fontHeight);
			g.setColor(Color.WHITE);
			g.drawRect(x, y, fontHeight, fontHeight);
			g.drawString(info.server + " (" + (TimeUtil.timeToString(info.time, 3, true)) + ")", x + fontHeight + 2, y + ascent);
			y += fontHeight + 2;
			drawnRows += 1;
			if (drawnRows >= rows) {
				drawnRows = 0;
				x += legendWidth;
				y = 30 + (fontHeight * 2);
			}
		}

		String bottomText;
		g.drawString(graphName, 0, ascent);
		if (playerName == null) {
			bottomText = "No Data";
		} else {
			bottomText = "Player: " + playerName + ", Time: " + (fullDateFormat.format(new Date(now)).replaceAll("AM", "am").replaceAll("PM", "pm"));
		}
		g.drawString(bottomText, 0, 30 + ((fontHeight + 2) * (rows)) + (fontHeight * 2) + 2 + ascent);
		realHeight = 30 + ((fontHeight + 2) * (rows)) + (fontHeight * 2) + 2 + fontHeight;
		imageHeight = realHeight * (hidpi ? 2 : 1);

		BufferedImage image2 = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
		Graphics g2 = image2.getGraphics();
		g2.drawImage(image, 0, 0, (ImageObserver) null);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(image2, "png", out);
		} catch (Exception e) {
		}
		TemporaryResource resource = new TemporaryResource("dailyreport.png", "image/png", out.toByteArray());
		UUID uuid = addResource(resource);
		return "<img src=\"/bc/t/" + uuid.toString().toLowerCase().replaceAll("-", "") + "\" width=\"" + realWidth + "\" height=\"" + realHeight + "\"><br>";
	}

	private String generateWeeklyReport_classic(OnTimeSessionRecord[] records, long startTime, HashMap<String, OnTimeInfo> onTimeInfoMap, HTTPRequest request, String graphName) {
		boolean hidpi = request.cookies.getOrDefault("hidpi", "0").equals("1");
		for (OnTimeInfo info : onTimeInfoMap.values()) {
			info.time = 0L;
		}
		String timezone = (String) request.getCacheMap().get("timezone");
		if (timezone == null) {
			timezone = "GMT+8";
		}
		UUID playerUUID = null;
		String playerName = null;
		if (records.length > 0) {
			playerUUID = records[0].player;
			playerName = plugin.getUUIDCache().getNameFromUUID(playerUUID);
			if (playerName == null) {
				playerName = playerUUID.toString().toLowerCase();
			}
		}
		BufferedImage image = new BufferedImage(Integer.parseInt(request.get.getOrDefault("graphwidth", request.cookies.getOrDefault("graphwidth", "600"))) * (hidpi ? 2 : 1), 500 * (hidpi ? 2 : 1), BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(
				RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		int realWidth = imageWidth / (hidpi ? 2 : 1);
		int realHeight = imageHeight / (hidpi ? 2 : 1);

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, imageWidth, imageHeight);

		int graphHeight = 30 * (hidpi ? 2 : 1);

		long now = System.currentTimeMillis();

		TimeZone timeZone = TimeZone.getTimeZone(timezone);
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");
		dateFormat.setTimeZone(timeZone);

		SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd/MM/yyyy h:mm:ss a z");
		fullDateFormat.setTimeZone(timeZone);

		long endTime = startTime + 604800000L;
		records = OnTime.trim(records, startTime, endTime);
		if (records.length == 0) {
			return "";
		}
		String fontName = request.get.getOrDefault("graphfont", request.cookies.getOrDefault("graphfont", "Helvetica"));
		int fontSize = Integer.parseInt(request.get.getOrDefault("graphfontsize", request.cookies.getOrDefault("graphfontsize", "12")));
		Font font = new Font(fontName, 0, fontSize);
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics(font);
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int fontHeight = ascent + descent;

		g.setColor(new Color(48, 0, 0));
		g.fillRect(0, fontHeight * (hidpi ? 2 : 1), imageWidth, graphHeight);
		if (now > startTime && now < endTime) {
			g.setColor(new Color(24, 0, 0));
			int drawX = (int) (imageWidth * this.getPosition(now, startTime, endTime));
			int drawY = fontHeight * (hidpi ? 2 : 1);
			int drawWidth = ((int) (imageWidth * this.getPosition(endTime, startTime, endTime))) - drawX;
			int drawHeight = graphHeight;
			g.fillRect(drawX, drawY, drawWidth, drawHeight);
		}
		for (OnTimeSessionRecord record : records) {
			OnTimeInfo info = onTimeInfoMap.get(record.server);
			if (info == null) {
				info = new OnTimeInfo(record.server, colours[onTimeInfoMap.size()]);
				onTimeInfoMap.put(record.server, info);
			}
			long start = Math.max(record.login, startTime);
			long end = Math.min(record.logout == -1L ? now : record.logout, endTime);
			long totalTime = end - start;
			info.time += totalTime;
			double startPos = getPosition(start, startTime, endTime);
			double endPos = getPosition(end, startTime, endTime);
			g.setColor(info.color);
			g.fillRect((int) (startPos * imageWidth), fontHeight * (hidpi ? 2 : 1), ((int) ((endPos - startPos) * imageWidth)) + 1, graphHeight);
		}

		if (hidpi) {
			((Graphics2D) g).scale(2.0, 2.0);
		}

		GregorianCalendar theHourCalendar = new GregorianCalendar();
		theHourCalendar.setTimeZone(timeZone);
		theHourCalendar.setTimeInMillis(endTime);
		theHourCalendar.set(Calendar.HOUR_OF_DAY, 0);
		theHourCalendar.set(Calendar.MINUTE, 0);
		theHourCalendar.set(Calendar.SECOND, 0);
		theHourCalendar.set(Calendar.MILLISECOND, 0);
		long theDay = theHourCalendar.getTimeInMillis();

		g.setColor(Color.WHITE);
		while (theDay >= startTime) {
			int timeX = (int) (realWidth * getPosition(theDay, startTime, endTime));
			g.drawLine(timeX, 25 + fontHeight, timeX, 30 + fontHeight);
			g.drawString(dateFormat.format(new Date(theDay)), timeX, 30 + ascent + fontHeight);
			theDay -= (86400000L);
		}

		ArrayList<OnTimeInfo> onTimeInfoList = new ArrayList<OnTimeInfo>();
		for (String server : onTimeInfoMap.keySet()) {
			OnTimeInfo info = onTimeInfoMap.get(server);
			if (info.time <= 0) {
				continue;
			}
			boolean added = false;
			for (int i = onTimeInfoList.size() - 1; i >= 0; i--) {
				if (onTimeInfoList.get(i).time > info.time) {
					onTimeInfoList.add(i + 1, info);
					added = true;
					break;
				}
			}
			if (!added) {
				onTimeInfoList.add(0, info);
			}
		}

		int legendWidth = 196;
		int columns = realWidth / legendWidth;
		int rows = (int) Math.ceil(((float) onTimeInfoList.size()) / ((float) columns));
		int drawnRows = 0;
		int x = 2;
		int y = 30 + (fontHeight * 2);

		for (OnTimeInfo info : onTimeInfoList) {
			g.setColor(info.color);
			g.fillRect(x, y, fontHeight, fontHeight);
			g.setColor(Color.WHITE);
			g.drawRect(x, y, fontHeight, fontHeight);
			g.drawString(info.server + " (" + (TimeUtil.timeToString(info.time, 3, true)) + ")", x + fontHeight + 2, y + ascent);
			y += fontHeight + 2;
			drawnRows += 1;
			if (drawnRows >= rows) {
				drawnRows = 0;
				x += legendWidth;
				y = 30 + (fontHeight * 2);
			}
		}

		String bottomText;
		g.drawString(graphName, 0, ascent);
		if (playerName == null) {
			bottomText = "No Data";
		} else {
			bottomText = "Player: " + playerName + ", Time: " + (fullDateFormat.format(new Date(now)).replaceAll("AM", "am").replaceAll("PM", "pm"));
		}
		g.drawString(bottomText, 0, 30 + ((fontHeight + 2) * (rows)) + (fontHeight * 2) + 2 + ascent);
		realHeight = 30 + ((fontHeight + 2) * (rows)) + (fontHeight * 2) + 2 + fontHeight;
		imageHeight = realHeight * (hidpi ? 2 : 1);

		BufferedImage image2 = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
		Graphics g2 = image2.getGraphics();
		g2.drawImage(image, 0, 0, (ImageObserver) null);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(image2, "png", out);
		} catch (Exception e) {
		}
		TemporaryResource resource = new TemporaryResource("weeklyreport.png", "image/png", out.toByteArray());
		UUID uuid = addResource(resource);
		return "<img src=\"/bc/t/" + uuid.toString().toLowerCase().replaceAll("-", "") + "\" width=\"" + realWidth + "\" height=\"" + realHeight + "\"><br>";
	}

	private String generateMonthlyReport(OnTimeSessionRecord[] records, int year, int month, TimeZone timezone, HTTPRequest request) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeZone(timezone);
		cal.setTimeInMillis(0L);
		cal.set(year, month, 1, 0, 0, 0);

		int blockWidth = 180;
		int blockHeight = 150;

		int firstDayOfMonth = cal.get(Calendar.DAY_OF_WEEK);
		int totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		int x = firstDayOfMonth - 1;
		int y = 0;
		int totalWeeks = ((totalDays + firstDayOfMonth - 2) / 7) + 1;
		int width = (blockWidth * 7) + 1;
		int height = (totalWeeks * blockHeight) + 1;

		cal.set(Calendar.MILLISECOND, 0);
		long startOfMonth = cal.getTimeInMillis();
		cal.set(year, month, totalDays, 23, 59, 59);
		cal.set(Calendar.MILLISECOND, 999);
		long endOfMonth = cal.getTimeInMillis();

		records = OnTime.trim(records, startOfMonth, endOfMonth);

		boolean hidpi = request.cookies.getOrDefault("hidpi", "0").equals("1");
		BufferedImage image = new BufferedImage(width * (hidpi ? 2 : 1), height * (hidpi ? 2 : 1), BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(
				RenderingHints.KEY_FRACTIONALMETRICS,
				RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		if (hidpi) {
			g2d.scale(2.0, 2.0);
		}
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		int realWidth = imageWidth / (hidpi ? 2 : 1);
		int realHeight = imageHeight / (hidpi ? 2 : 1);

		Font dayNumber = new Font("Helvetica", 0, 20);
		Font otherText = new Font("Helvetica", 0, 12);
		FontMetrics dayNumberFM = g.getFontMetrics(dayNumber);
		FontMetrics otherTextFM = g.getFontMetrics(otherText);
		for (int day = 1; day <= totalDays; day++) {
			cal.set(year, month, day, 0, 0, 0);
			cal.set(Calendar.MILLISECOND, 0);
			long startOfDay = cal.getTimeInMillis();
			cal.set(year, month, day, 23, 59, 59);
			cal.set(Calendar.MILLISECOND, 999);
			long endOfDay = cal.getTimeInMillis();

			OnTimeSessionRecord[] dayRecords = OnTime.trim(records, startOfDay, endOfDay);
			long totalTimeToday = endOfDay - startOfDay + 1;

			Graphics dayG = g.create(x * blockWidth, y * blockHeight, blockWidth + 1, blockHeight + 1);

			// fill background black
			dayG.setColor(calendarBG);
			dayG.fillRect(0, 0, blockWidth, blockHeight);

			long totalTime = 0L;

			long firstLogin = 0L;
			long lastLogout = 0L;

			dayG.setColor(calendarTimeUsed);
			for (OnTimeSessionRecord record : dayRecords) {
				long recordStart = Math.max(startOfDay, record.login);
				long recordEnd = Math.min(endOfDay, record.logout == -1L ? System.currentTimeMillis() : record.logout);
				if (firstLogin == 0L) {
					firstLogin = recordStart;
				}
				lastLogout = recordEnd;
				long totalTimeOn = recordEnd - recordStart + 1;
				totalTime += totalTimeOn;
				int startY = (int) (((recordStart - startOfDay) * blockHeight) / totalTimeToday);
				int endY = (int) (((recordEnd - startOfDay) * blockHeight) / totalTimeToday);
				int drawHeight = Math.max(1, endY - startY);
				dayG.fillRect(0, startY, blockWidth, drawHeight);
			}

			if (totalTime == 0L) {
				dayG.drawLine(0, 0, blockWidth, blockHeight);
				dayG.drawLine(blockWidth, 0, 0, blockHeight);
			}

			dayG.setColor(calendarFG);
			dayG.setFont(dayNumber);
			dayG.drawString(Integer.toString(day), 2, 2 + dayNumberFM.getAscent());
			int yInfo = 2 + dayNumberFM.getHeight() + 2;
			int lineAscent = otherTextFM.getAscent();
			int lineHeight = otherTextFM.getHeight();

			String totalTimeString = TimeUtil.timeToString(totalTime);
			String totalTimeInDayString = TimeUtil.timeToString(totalTimeToday);
			dayG.setFont(otherText);
			dayG.drawString(totalTimeString, 2, yInfo + lineAscent + (0 * lineHeight));
			dayG.drawString("out of " + totalTimeInDayString, 2, yInfo + lineAscent + (1 * lineHeight));

			// fill border white
			dayG.setColor(calendarFG);
			dayG.drawRect(0, 0, blockWidth, blockHeight);

			x += 1;
			if (x >= 7) {
				x = 0;
				y += 1;
			}
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ImageIO.write(image, "png", out);
		} catch (Exception e) {
		}
		TemporaryResource resource = new TemporaryResource("monthlyreport.png", "image/png", out.toByteArray());
		UUID uuid = addResource(resource);
		return "<img src=\"/bc/t/" + uuid.toString().toLowerCase().replaceAll("-", "") + "\" width=\"" + realWidth + "\" height=\"" + realHeight + "\"><br>";
	}

	private static final Color calendarBG = new Color(0, 0, 0);
	private static final Color calendarFG = new Color(255, 255, 255);
	private static final Color calendarTimeUsed = new Color(64, 0, 0);
	private static final String[] months = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

	private double getPosition(long value, long lowerLimit, long upperLimit) {
		value -= lowerLimit;
		upperLimit -= lowerLimit;
		return (((double) value) / ((double) upperLimit));
	}

	private void writePage(HTTPRequest request, String page) throws IOException {
		byte[] b = page.getBytes();
		request.response.setHeader("Content-Length", Integer.toString(b.length));
		request.response.write(b);
	}

	private String read(String file) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			InputStream in = BungeeResponder.class.getResourceAsStream(file);
			byte[] b = new byte[4096];
			int c;
			while ((c = in.read(b, 0, b.length)) != -1) {
				baos.write(b, 0, c);
			}
			return new String(baos.toByteArray());
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public void respond404(HTTPRequest request) throws Exception {
	}

	private String generatePage(String title, String page) {
		return generatePage(title, page, null);
	}

	private String generatePage(String title, String page, String head) {
		return new StringBuilder()
				.append("<!DOCTYPE html>")
				.append("<html>")
				.append("<head>")
				.append("<title>")
				.append(title)
				.append("</title>")
				.append("<link rel=\"STYLESHEET\" href=\"/bc/style.css\">")
				.append("<script src=\"/bc/jstz.js\"></script>")
				.append("<script src=\"/bc/script.js\"></script>")
				.append(head == null ? "" : head)
				.append("</head>")
				.append("<body>")
				.append("<a href=\"/\" class=\"logo\"></a>")
				.append("<div class=\"toplinks\">")
				.append(generateTopLinks())
				.append("</div>")
				.append(page)
				.append("<br><br>")
				.append("<div class=\"footer\">")
				.append("SiggiCore Plugin")
				.append("<br>")
				.append("Copyright &copy; 2014-2018 Siggi.io")
				.append("</div>")
				.append("</body>")
				.append("</html>")
				.append("\n")
				.toString();
	}

	private String generateTopLinks() {
		SessionInfo info = sessionInfo.get();
		StringBuilder links = new StringBuilder();
		links.append("<a href=\"/bc/recent\">Legacy Offence History</a> <a href=\"/bc/chatlog\">Chat log</a> <a href=\"/bc/ontime\">OnTime</a>");
		if (info != null && info.uuid != null) {
			links.append(" <a href=\"/user/logout?session=").append(info.sessionCookie).append("\">").append(info.username).append(": Logout</a>");
		} else {
			links.append(" <a href=\"/user/login\">Login</a>");
		}
		return links.toString();
	}

	private static String deURLEncode(String encoded) {
		byte abyte0[] = encoded.getBytes();
		int i = 0;
		byte abyte1[] = new byte[encoded.length()];
		for (int j = 0; j < abyte0.length; j++) {
			if (abyte0[j] == 37) {
				byte abyte3[] = {
					abyte0[j + 1], abyte0[j + 2]
				};
				byte byte0 = (byte) Integer.parseInt(new String(abyte3), 16);
				j += 2;
				abyte1[i++] = byte0;
			} else {
				abyte1[i++] = abyte0[j];
			}
		}

		byte abyte2[] = new byte[i];
		System.arraycopy(abyte1, 0, abyte2, 0, i);
		return new String(abyte2);
	}

	private static String fixString(String s) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(s.getBytes()), "UTF-8"));
			s = "";
			String s2 = null;
			while ((s2 = reader.readLine()) != null) {
				s += (s.equals("") ? "" : "\n") + s2;
			}
		} catch (Exception e) {
		}
		return s;
	}

	private long nextLogID = -1;
	private final Object nextLogLock = new Object();

	private long nextLogID() {
		long result = 0;
		synchronized (nextLogLock) {
			File savedLog = new File(plugin.getDataFolder(), "savedlog");
			if (!savedLog.exists()) {
				savedLog.mkdirs();
			}
			File nextFile = new File(savedLog, "next.txt");
			if (nextLogID == -1) {
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(nextFile)));
					nextLogID = Long.parseLong(reader.readLine());
					reader.close();
				} catch (Exception e) {
					nextLogID = 1;
				}
			}
			while (new File(savedLog, nextLogID + ".txt").exists()) {
				nextLogID += 1;
			}
			try {
				FileOutputStream fos = new FileOutputStream(nextFile);
				fos.write((nextLogID + "\n").getBytes());
				fos.close();
			} catch (Exception e) {
			}
			nextLogID += 1;
			result = nextLogID - 1;
		}
		return result;
	}

	private int scale(int lowOutput, int highOutput, long minInput, long maxOutput, long value) {
		value -= minInput;
		maxOutput -= minInput;
		double fraction = ((double) value) / ((double) maxOutput);
		double diff = ((double) highOutput) - ((double) lowOutput);
		double aDiff = diff * fraction;
		double resultD = ((double) lowOutput) + (aDiff);
		int result = (int) resultD;
		if (lowOutput < highOutput) {
			return Math.max(lowOutput, Math.min(highOutput, result));
		} else if (lowOutput > highOutput) {
			return Math.max(highOutput, Math.min(lowOutput, result));
		} else {
			return lowOutput;
		}
	}
}
