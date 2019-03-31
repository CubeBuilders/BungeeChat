package hk.siggi.bungeecord.bungeechat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import static hk.siggi.bungeecord.bungeechat.util.Util.getAllPlayersThatEverJoined;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class ServerActivityReport {

	public static ServerActivityReport generateReport(TimeZone timezone) {
		List<UUID> players = getAllPlayersThatEverJoined();
		ServerActivityReport report = new ServerActivityReport(timezone);
		for (UUID player : players) {
			UserActivityReport uar = UserActivityReport.generateReport(player);
			report.addReport(uar);
		}
		report.calculateAverages();
		return report;
	}

	private ServerActivityReport(TimeZone timezone) {
		this.timezone = timezone;
		calendar = new GregorianCalendar(timezone);
		calendar.setMinimalDaysInFirstWeek(7);
	}

	private final TimeZone timezone;
	private final transient GregorianCalendar calendar;

	@Getter
	private int activePlayers = 0, totalPlayers = 0, meanPlayerAge = 0, medianPlayerAge = 0;

	private final Map<Integer, StatisticReport> months = new HashMap<>();
	private final Map<Integer, StatisticReport> weeks = new HashMap<>();

	private void addReport(UserActivityReport report) {
		totalPlayers += 1;
		if (report.isPlayerActive()) {
			activePlayers += 1;
			activePlayerReport.add(report);
		}
		boolean firstActivity = true;
		for (Iterator<UserActivityReport.Activity> it = report.getActivities().iterator(); it.hasNext();) {
			boolean first = firstActivity;
			firstActivity = false;
			UserActivityReport.Activity activity = it.next();
			boolean lastActivity = !it.hasNext();
			long start = activity.getStart();
			long end = activity.getEnd();

			calendar.setTimeInMillis(start);

			int startYear = calendar.get(Calendar.YEAR);
			int startMonth = calendar.get(Calendar.MONTH);
			StatisticReport startM = getByMonth(startYear, startMonth, true);

			int startWeekYear = calendar.getWeekYear();
			int startWeek = calendar.get(Calendar.WEEK_OF_YEAR);
			StatisticReport startW = getByWeek(startWeekYear, startWeek, true);

			if (first) {
				if ((lastActivity && report.isPlayerActive()) || (end - start >= UserActivityReport.getActivityTimeout())) {
					startM.newPlayers += 1;
					startW.newPlayers += 1;
					if (report.isPlayerActive()) {
						startM.activePlayersFromHere += 1;
						startW.activePlayersFromHere += 1;
					}
				} else {
					startM.oneTimePlayers += 1;
					startW.oneTimePlayers += 1;
					firstActivity = true;
					continue;
				}
			} else {
				startM.returningPlayers += 1;
				startW.returningPlayers += 1;
			}

			if (start < end && (!lastActivity || !report.isPlayerActive())) {
				calendar.setTimeInMillis(end);

				int endYear = calendar.get(Calendar.YEAR);
				int endMonth = calendar.get(Calendar.MONTH);
				StatisticReport endM = getByMonth(endYear, endMonth, true);

				int endWeekYear = calendar.getWeekYear();
				int endWeek = calendar.get(Calendar.WEEK_OF_YEAR);
				StatisticReport endW = getByWeek(endWeekYear, endWeek, true);

				endM.quittingPlayers += 1;
				endW.quittingPlayers += 1;
			}
		}
		for (UserActivityReport.Activity activity : report.getActivities()) {
			long start = activity.getStart();
			long end = activity.getEnd();

			calendar.setTimeInMillis(start);
			int startYear = calendar.get(Calendar.YEAR);
			int startMonth = calendar.get(Calendar.MONTH);
			int startWeekYear = calendar.getWeekYear();
			int startWeek = calendar.get(Calendar.WEEK_OF_YEAR);

			calendar.setTimeInMillis(end);
			int endYear = calendar.get(Calendar.YEAR);
			int endMonth = calendar.get(Calendar.MONTH);
			int endWeekYear = calendar.getWeekYear();
			int endWeek = calendar.get(Calendar.WEEK_OF_YEAR);
			
			calendar.setTimeInMillis(getBeginningOfMonth(startYear, startMonth));
			long endTime = getBeginningOfMonth(endYear, endMonth);
			while (calendar.getTimeInMillis() <= endTime) {
				StatisticReport statistic = getByMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), true);
				statistic.activePlayers += 1;
				calendar.add(Calendar.MONTH, 1);
			}

			calendar.setTimeInMillis(getBeginningOfWeek(startWeekYear, startWeek));
			endTime = getBeginningOfWeek(endWeekYear, endWeek);
			while (calendar.getTimeInMillis() <= endTime) {
				StatisticReport statistic = getByWeek(calendar.getWeekYear(), calendar.get(Calendar.WEEK_OF_YEAR), true);
				statistic.activePlayers += 1;
				calendar.add(Calendar.DAY_OF_MONTH, 7);
			}
		}
	}

	private final transient List<UserActivityReport> activePlayerReport = new LinkedList<>();

	private void calculateAverages() {
		if (activePlayerReport.isEmpty()) {
			return;
		}
		long now = System.currentTimeMillis();
		int count = 0;
		double totalAge = 0.0;
		ArrayList<Integer> playerAges = new ArrayList<>();
		playerAges.ensureCapacity(activePlayerReport.size());
		for (UserActivityReport report : activePlayerReport) {
			long firstLogin = report.getFirstLogin();
			if (firstLogin == -1L) {
				continue;
			}
			long age = now - firstLogin;
			double ageDays = ((double) age) / 86400000.0;
			totalAge += ageDays;
			count += 1;
			playerAges.add((int) Math.floor(ageDays));
		}
		meanPlayerAge = (int) Math.floor(totalAge / ((double) count));
		playerAges.sort((Integer o1, Integer o2) -> {
			if (o1 < o2) {
				return -1;
			} else if (o1 > o2) {
				return 1;
			} else {
				return 0;
			}
		});
		int size = playerAges.size();
		if (size == 0) {
			medianPlayerAge = 0;
			return;
		}
		int half = size / 2;
		if (size % 2 == 0) {
			medianPlayerAge = (playerAges.get(half - 1) + playerAges.get(half)) / 2;
		} else {
			medianPlayerAge = playerAges.get(half);
		}
		activePlayerReport.clear();
	}

	public StatisticReport getByMonth(int year, int month) {
		return getByMonth(year, month, false);
	}

	public StatisticReport getByWeek(int year, int week) {
		return getByWeek(year, week, false);
	}

	private StatisticReport getByMonth(int year, int month, boolean create) {
		StatisticReport report = months.get((year * 100) + month);
		if (report == null && create) {
			months.put((year * 100) + month, report = new StatisticReport());
		}
		return report;
	}

	private StatisticReport getByWeek(int year, int week, boolean create) {
		StatisticReport report = weeks.get((year * 100) + week);
		if (report == null && create) {
			weeks.put((year * 100) + week, report = new StatisticReport());
		}
		return report;
	}

	public long getBeginningOfMonth(int year, int month) {
		calendar.set(year, month, 1, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis();
	}

	public long getBeginningOfWeek(int year, int week) {
		calendar.setWeekDate(year, week, calendar.getFirstDayOfWeek());
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis();
	}

	public static class StatisticReport {

		@Getter
		@Setter(AccessLevel.PRIVATE)
		private int newPlayers, oneTimePlayers, returningPlayers, quittingPlayers, activePlayers, activePlayersFromHere;

		private StatisticReport() {
		}
	}

	public Set<Integer> getMonths() {
		return sortedSet(months.keySet());
	}

	public Set<Integer> getWeeks() {
		return sortedSet(weeks.keySet());
	}

	private static Set<Integer> sortedSet(Set<Integer> set) {
		TreeSet<Integer> treeSet = new TreeSet<>((Integer o1, Integer o2) -> {
			if (o1 < o2) {
				return -1;
			} else if (o1 > o2) {
				return 1;
			} else {
				return 0;
			}
		});
		treeSet.addAll(set);
		return treeSet;
	}

	// <editor-fold desc="Gson" defaultstate="collapsed">
	private static class ServerActivityReportTypeAdapter extends TypeAdapter<ServerActivityReport> {

		@Override
		public ServerActivityReport read(JsonReader reader) throws IOException {
			int activePlayers = 0;
			int totalPlayers = 0;
			int meanPlayerAge = 0;
			int medianPlayerAge = 0;
			String timezone = null;
			Map<Integer, StatisticReport> months = null;
			Map<Integer, StatisticReport> weeks = null;
			reader.beginObject();
			JsonToken peek;
			while ((peek = reader.peek()) != JsonToken.END_OBJECT) {
				if (peek != JsonToken.NAME) {
					reader.skipValue();
					continue;
				}
				String name = reader.nextName();
				peek = reader.peek();
				if (name.equals("activePlayers") && peek == JsonToken.NUMBER) {
					activePlayers = reader.nextInt();
				} else if (name.equals("totalPlayers") && peek == JsonToken.NUMBER) {
					totalPlayers = reader.nextInt();
				} else if (name.equals("meanPlayerAge") && peek == JsonToken.NUMBER) {
					meanPlayerAge = reader.nextInt();
				} else if (name.equals("medianPlayerAge") && peek == JsonToken.NUMBER) {
					medianPlayerAge = reader.nextInt();
				} else if (name.equals("timezone") && peek == JsonToken.STRING) {
					timezone = reader.nextString();
				} else if (name.equals("months") && peek == JsonToken.BEGIN_OBJECT) {
					months = readMap(reader);
				} else if (name.equals("weeks") && peek == JsonToken.BEGIN_OBJECT) {
					weeks = readMap(reader);
				}
			}
			reader.endObject();
			ServerActivityReport report = new ServerActivityReport(TimeZone.getTimeZone(timezone));
			report.activePlayers = activePlayers;
			report.totalPlayers = totalPlayers;
			report.meanPlayerAge = meanPlayerAge;
			report.medianPlayerAge = medianPlayerAge;
			if (months != null) {
				report.months.putAll(months);
			}
			if (weeks != null) {
				report.weeks.putAll(weeks);
			}
			return report;
		}

		@Override
		public void write(JsonWriter writer, ServerActivityReport report) throws IOException {
			writer.beginObject();
			writer.name("activePlayers").value(report.activePlayers);
			writer.name("totalPlayers").value(report.totalPlayers);
			writer.name("meanPlayerAge").value(report.meanPlayerAge);
			writer.name("medianPlayerAge").value(report.medianPlayerAge);
			writer.name("timezone").value(report.timezone.getID());
			writer.name("months");
			writeMap(writer, report.months);
			writer.name("weeks");
			writeMap(writer, report.weeks);
			writer.endObject();
		}

		private Map<Integer, StatisticReport> readMap(JsonReader reader) throws IOException {
			Map<Integer, StatisticReport> map = new HashMap<>();
			reader.beginObject();
			JsonToken peek;
			while ((peek = reader.peek()) != JsonToken.END_OBJECT) {
				if (peek != JsonToken.NAME) {
					reader.skipValue();
					continue;
				}
				String name = reader.nextName();
				peek = reader.peek();
				if (peek != JsonToken.BEGIN_OBJECT) {
					continue;
				}
				int y, m;
				try {
					String[] pieces = name.split(",");
					y = Integer.parseInt(pieces[0]);
					m = Integer.parseInt(pieces[1]);
				} catch (Exception e) {
					continue;
				}
				int k = (y * 100) + m;
				StatisticReport report = statisticAdapter.read(reader);
				map.put(k, report);
			}
			reader.endObject();
			return map;
		}

		private void writeMap(JsonWriter writer, Map<Integer, StatisticReport> map) throws IOException {
			writer.beginObject();
			for (int section : sortedSet(map.keySet())) {
				int y = section / 100;
				int m = section % 100;
				writer.name(y + "," + m);
				statisticAdapter.write(writer, map.get(section));
			}
			writer.endObject();
		}
	}

	private static class StatisticReportTypeAdapter extends TypeAdapter<StatisticReport> {

		@Override
		public StatisticReport read(JsonReader reader) throws IOException {
			StatisticReport statistic = new StatisticReport();
			reader.beginObject();
			JsonToken peek;
			while ((peek = reader.peek()) != JsonToken.END_OBJECT) {
				if (peek != JsonToken.NAME) {
					reader.skipValue();
					continue;
				}
				String name = reader.nextName();
				peek = reader.peek();
				if (name.equals("newPlayers") && peek == JsonToken.NUMBER) {
					statistic.newPlayers = reader.nextInt();
				} else if (name.equals("oneTimePlayers") && peek == JsonToken.NUMBER) {
					statistic.oneTimePlayers = reader.nextInt();
				} else if (name.equals("returningPlayers") && peek == JsonToken.NUMBER) {
					statistic.returningPlayers = reader.nextInt();
				} else if (name.equals("quittingPlayers") && peek == JsonToken.NUMBER) {
					statistic.quittingPlayers = reader.nextInt();
				} else if (name.equals("activePlayers") && peek == JsonToken.NUMBER) {
					statistic.activePlayers = reader.nextInt();
				} else if (name.equals("activePlayersFromHere") && peek == JsonToken.NUMBER) {
					statistic.activePlayersFromHere = reader.nextInt();
				}
			}
			reader.endObject();
			return statistic;
		}

		@Override
		public void write(JsonWriter writer, StatisticReport statistic) throws IOException {
			writer.beginObject();
			writer.name("newPlayers").value(statistic.newPlayers);
			writer.name("oneTimePlayers").value(statistic.oneTimePlayers);
			writer.name("returningPlayers").value(statistic.returningPlayers);
			writer.name("quittingPlayers").value(statistic.quittingPlayers);
			writer.name("activePlayers").value(statistic.activePlayers);
			writer.name("activePlayersFromHere").value(statistic.activePlayersFromHere);
			writer.endObject();
		}
	}
	private static final ServerActivityReportTypeAdapter reportAdapter = new ServerActivityReportTypeAdapter();
	private static final StatisticReportTypeAdapter statisticAdapter = new StatisticReportTypeAdapter();

	public static TypeAdapter<ServerActivityReport> getGsonTypeAdapter() {
		return reportAdapter;
	}
	private static final Gson gson = new GsonBuilder().registerTypeAdapter(ServerActivityReport.class, reportAdapter).create();
	private static final Gson prettyGson = new GsonBuilder().registerTypeAdapter(ServerActivityReport.class, reportAdapter).setPrettyPrinting().create();

	public static Gson getGson() {
		return gson;
	}

	public static Gson getPrettyGson() {
		return prettyGson;
	}
	// </editor-fold>
}
