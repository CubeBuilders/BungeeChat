package hk.siggi.bungeecord.bungeechat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class Endpoints {
	private Endpoints() {
	}
	private static final Map<String, String> map = new HashMap<>();

	static void loadFrom(File file) {
		map.clear();
		try (FileInputStream in = new FileInputStream(file)) {
			Properties properties = new Properties();
			properties.load(in);
			for (Map.Entry<Object,Object> entry : properties.entrySet()) {
				map.put((String) entry.getKey(), (String) entry.getValue());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String get(String api) {
		return map.get(api);
	}
}
