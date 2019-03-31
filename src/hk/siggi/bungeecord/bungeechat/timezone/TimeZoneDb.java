package hk.siggi.bungeecord.bungeechat.timezone;

import java.util.HashMap;
import java.util.Map;

public abstract class TimeZoneDb {

	TimeZoneDb() {
	}
	private static final Map<String, TimeZoneDb> dbsByCountry = new HashMap<>();

	private static void add(TimeZoneDb db) {
		for (String str : db.getCountryStrings()) {
			dbsByCountry.put(str.toLowerCase(), db);
		}
	} 

	static {
		add(new TimeZoneDbUnitedStates());
		add(new TimeZoneDbCanada());
		add(new TimeZoneDbIceland());
		add(new TimeZoneDbUnitedKingdom());
		add(new TimeZoneDbHongKong());
		add(new TimeZoneDbPhilippines());
		add(new TimeZoneDbThailand());
		add(new TimeZoneDbChina());
	}

	public static TimeZoneDb getByCountry(String country) {
		return dbsByCountry.get(country.toLowerCase());
	}

	public static String getTimeZone(String zip, String country) {
		TimeZoneDb db = getByCountry(country);
		if (db == null) {
			return null;
		}
		return db.getTimeZone(zip);
	}

	public abstract String[] getCountryStrings();

	public abstract String getTimeZone(String zip);
}
