package hk.siggi.bungeecord.bungeechat.util;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public abstract class OSSpecificUtil {

	private static final OSSpecificUtil util;

	static {
		OSSpecificUtil u = null;

		String os = System.getProperty("os.name");
		String osl = os.toLowerCase();
		if (osl.contains("mac") || osl.contains("os x") || osl.contains("macos") || osl.contains("osx")) {
			u = new MacUtil();
		} else if (osl.contains("windows")) {
			u = new WindowsUtil();
		} else if (osl.contains("linux") || osl.contains("unix") || osl.contains("bsd")) {
			u = new LinuxUtil();
		}

		if (u == null) {
			u = new NullUtil();
		}
		util = u;
	}

	public static OSSpecificUtil get() {
		return util;
	}

	public void compressFile(File file) {
	}

	private static class MacUtil extends OSSpecificUtil {

		@Override
		public void compressFile(File file) {
			List<String> args = new LinkedList<>();
			args.add("/usr/local/bin/afsctool");
			args.add("-c");
			args.add("-9");
			args.add(file.getAbsolutePath());
			try {
				Runtime.getRuntime().exec(args.toArray(new String[args.size()]));
			} catch (Exception e) {
			}
		}
	}

	private static class LinuxUtil extends OSSpecificUtil {

	}

	private static class WindowsUtil extends OSSpecificUtil {

	}

	private static class NullUtil extends OSSpecificUtil {
	}
}
