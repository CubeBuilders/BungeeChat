package hk.siggi.bungeecord.bungeechat.geolocation;

import io.siggi.ipdb.IPDB;

import java.io.File;
import java.util.Map;

public final class Geolocator {
	private final IPDB ipdb;

	private Geolocator(File file) {
		this.ipdb = new IPDB(file);
	}

	public static Geolocator get(File file) {
		return new Geolocator(file);
	}

	public Geolocation get(String ip) {
		Map<String, String> map = ipdb.get(ip);
		return new Geolocation(
				map.getOrDefault("countryCode", "-"),
				map.getOrDefault("country", "-"),
				map.getOrDefault("region", "-"),
				map.getOrDefault("city", "-"),
				map.getOrDefault("latitude", "0.000000"),
				map.getOrDefault("longitude", "0.000000"),
				map.getOrDefault("postalCode", "-"),
				map.getOrDefault("timezone", "-"),
				map.getOrDefault("isp", "-")
		);
	}

	private String defaultIfNull(String value, String defaultIfNull) {
		return value == null ? defaultIfNull : value;
	}
}
