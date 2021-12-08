package hk.siggi.bungeecord.bungeechat.util;

import static hk.siggi.bungeecord.bungeechat.util.Util.getURL;
import static hk.siggi.bungeecord.bungeechat.util.Util.uuidToString;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.cubebuilders.user.CBUser;

public class APIUtil {

	private APIUtil() {
		// not meant to be instantiated
	}

	public static String genSecretCode(UUID uuid) {
		try {
			return new String(getURL("http://127.0.0.1:2823/api/secretcode?uuid=" + uuidToString(uuid)));
		} catch (Exception e) {
			return "";
		}
	}

	public static CBUser getUser(UUID uuid) {
		byte[] bytes = getURL("http://127.0.0.1:2823/api/getuser?uuid=" + (uuid.toString().replaceAll("-", "").toLowerCase()));
		if (bytes == null) {
			return null;
		}
		try {
			return CBUser.fromJson(new String(bytes, "UTF-8"));
		} catch (Exception e) {
		}
		return null;
	}

	public static boolean setEmail(UUID uuid, String email) {
		byte[] bytes;
		try {
			bytes = getURL("http://127.0.0.1:2823/api/setemail?uuid=" + (uuid.toString().replaceAll("-", "").toLowerCase()) + "&email=" + (URLEncoder.encode(email, "UTF-8").replace("%20", "+")));
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
		if (bytes == null) {
			return false;
		}
		try {
			if (new String(bytes, "UTF-8").equals("OK")) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean resendEmail(UUID uuid) {
		byte[] bytes;
		bytes = getURL("http://127.0.0.1:2823/api/resendemail?uuid=" + (uuid.toString().replaceAll("-", "").toLowerCase()));
		if (bytes == null) {
			return false;
		}
		try {
			if (new String(bytes, "UTF-8").equals("OK")) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean isIPBanned(String ip) {
		byte[] bytes = getURL("http://127.0.0.1:2823/api/isipbanned/" + (ip.replace(":", "-")));
		if (bytes == null) {
			return false;
		}
		String str = new String(bytes);
		return str.equals("1");
	}

	public static List<String> pullCommands(UUID uuid) {
		try {
			byte[] data = Util.getURL("http://127.0.0.1:2823/api/pullcommands?uuid=" + (uuid.toString().replaceAll("-", "").toLowerCase()));
			ArrayList<String> commands = new ArrayList<>();
			String commandsStr = new String(data, "UTF-8");
			BufferedReader reader = new BufferedReader(new CharArrayReader(commandsStr.toCharArray()));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.equals("")) {
					continue;
				}
				if (line.startsWith("/")) {
					line = line.substring(1);
				}
				commands.add(line);
			}
			return commands;
		} catch (IOException e) {
			return null;
		}
	}
}
