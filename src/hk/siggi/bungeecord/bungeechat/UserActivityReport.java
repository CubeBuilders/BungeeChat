package hk.siggi.bungeecord.bungeechat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import hk.siggi.bungeecord.bungeechat.ontime.OnTime;
import hk.siggi.bungeecord.bungeechat.ontime.OnTimePlayer;
import hk.siggi.bungeecord.bungeechat.ontime.OnTimeSessionRecord;
import hk.siggi.bungeecord.bungeechat.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class UserActivityReport {

	public static UserActivityReport generateReport(UUID player) {
		OnTimePlayer otp = OnTime.getInstance().getPlayer(player);
		UserActivityReport report = new UserActivityReport(player);
		for (OnTimeSessionRecord record : Util.iterable(otp.sessionRecordsIterator())) {
			report.add(record.login, record.logout);
		}
		return report;
	}

	private static final long activityTimeout = (86400000L * 21L); // 3 weeks

	private UserActivityReport(UUID player) {
		this.player = player;
	}

	public static long getActivityTimeout() {
		return activityTimeout;
	}

	private final UUID player;
	private final LinkedList<Activity> activities = new LinkedList<>();

	private void add(long login, long logout) {
		if (logout <= 0L) {
			logout = System.currentTimeMillis();
		}
		if (activities.isEmpty()) {
			activities.add(new Activity(login, logout));
		} else {
			Activity last = activities.getLast();
			long timeSince = login - last.end;
			if (timeSince < activityTimeout) {
				last.setEnd(logout);
			} else {
				activities.add(new Activity(login, logout));
			}
		}
	}

	public long getFirstLogin() {
		try {
			return activities.getFirst().start;
		} catch (Exception e) {
			return -1L;
		}
	}

	public List<Activity> getActivities() {
		List<Activity> act = new ArrayList<>(activities.size());
		act.addAll(activities);
		return Collections.unmodifiableList(act);
	}

	public boolean isPlayerActive() {
		if (activities.isEmpty()) {
			return false;
		}
		Activity last = activities.getLast();
		return System.currentTimeMillis() - last.end < activityTimeout;
	}

	public static class Activity {

		@Getter
		@Setter(AccessLevel.PRIVATE)
		private long start, end;

		private Activity(long start, long end) {
			this.start = start;
			this.end = end;
		}
	}

	// <editor-fold desc="Gson" defaultstate="collapsed">
	private static class UserActivityReportTypeAdapter extends TypeAdapter<UserActivityReport> {

		@Override
		public UserActivityReport read(JsonReader reader) throws IOException {
			UUID player = null;
			List<Activity> activities = new LinkedList<>();
			reader.beginObject();
			JsonToken peek;
			while ((peek = reader.peek()) != JsonToken.END_OBJECT) {
				if (peek != JsonToken.NAME) {
					reader.skipValue();
					continue;
				}
				String name = reader.nextName();
				peek = reader.peek();
				if (name.equals("player") && peek == JsonToken.STRING) {
					player = Util.uuidFromString(reader.nextString());
				} else if (name.equals("activities") && peek == JsonToken.BEGIN_ARRAY) {
					reader.beginArray();
					while ((peek = reader.peek()) != JsonToken.END_ARRAY) {
						if (peek != JsonToken.BEGIN_OBJECT) {
							reader.skipValue();
							continue;
						}
						activities.add(activityAdapter.read(reader));
					}
					reader.endArray();
				}
			}
			reader.endObject();
			UserActivityReport report = new UserActivityReport(player);
			report.activities.addAll(activities);
			return report;
		}

		@Override
		public void write(JsonWriter writer, UserActivityReport report) throws IOException {
			writer.beginObject();
			writer.name("player").value(Util.uuidToString(report.player));
			writer.name("activities");
			writer.beginArray();
			for (Activity activity : report.activities) {
				activityAdapter.write(writer, activity);
			}
			writer.endArray();
			writer.endObject();
		}
	}

	private static class ActivityTypeAdapter extends TypeAdapter<Activity> {

		@Override
		public Activity read(JsonReader reader) throws IOException {
			long start = 0L;
			long end = 0L;
			reader.beginObject();
			JsonToken peek;
			while ((peek = reader.peek()) != JsonToken.END_OBJECT) {
				if (peek != JsonToken.NAME) {
					reader.skipValue();
					continue;
				}
				String name = reader.nextName();
				peek = reader.peek();
				if (name.equals("start") && peek == JsonToken.NUMBER) {
					start = reader.nextLong();
				} else if (name.equals("end") && peek == JsonToken.NUMBER) {
					end = reader.nextLong();
				}
			}
			reader.endObject();
			return new Activity(start, end);
		}

		@Override
		public void write(JsonWriter writer, Activity activity) throws IOException {
			writer.beginObject();
			writer.name("start").value(activity.start);
			writer.name("end").value(activity.end);
			writer.endObject();
		}
	}
	private static final TypeAdapter<UserActivityReport> reportAdapter = new UserActivityReportTypeAdapter();
	private static final TypeAdapter<Activity> activityAdapter = new ActivityTypeAdapter();

	public static TypeAdapter<UserActivityReport> getGsonTypeAdapter() {
		return reportAdapter;
	}
	private static final Gson gson = new GsonBuilder().registerTypeAdapter(UserActivityReport.class, reportAdapter).create();
	private static final Gson prettyGson = new GsonBuilder().registerTypeAdapter(UserActivityReport.class, reportAdapter).setPrettyPrinting().create();

	public static Gson getGson() {
		return gson;
	}

	public static Gson getPrettyGson() {
		return prettyGson;
	}
	// </editor-fold>
}
