package hk.siggi.bungeecord.bungeechat.geolocation;

import hk.siggi.bungeecord.bungeechat.util.LineReader;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

public abstract class Geolocator {

	Geolocator() {
	}

	public static Geolocator get(File file) {
		boolean isIPv6 = false;
		try {
			try (FileInputStream fis = new FileInputStream(file)) {
				LineReader reader = new LineReader(fis);
				String line = null;
				BigInteger fourB = new BigInteger("4294967296");
				for (int i = 0; i < 16 && (line = reader.readLine()) != null; i++) {
					String[] parseCsv = parseCsv(line);
					BigInteger firstIP = new BigInteger(parseCsv[0]);
					BigInteger lastIP = new BigInteger(parseCsv[1]);
					if (firstIP.compareTo(fourB) == -1 || lastIP.compareTo(fourB) == -1) {
						isIPv6 = true;
						break;
					}
				}
			}
		} catch (Exception e) {
			return null;
		}
		if (isIPv6) {
			return new GeolocatorIPv6(file, new GeolocatorIPv4(file));
		} else {
			return new GeolocatorIPv4(file);
		}
	}

	public abstract Geolocation get(String ip);

	Geolocation nullGeolocation = new Geolocation(
			"-",
			"-",
			"-",
			"-",
			"0.000000",
			"0.000000",
			"-",
			"-"
	);

	static String[] parseCsv(String line) {
		try {
			List<String> result = new LinkedList<>();
			CharArrayReader in = new CharArrayReader(line.toCharArray());
			int cInt;
			char c;
			char comma = ',';
			int commaInt = (int) comma;
			boolean insideQuote = false;
			StringBuilder buffer = new StringBuilder();
			readLine:
			while (true) {
				cInt = in.read();
				if (cInt == -1) {
					result.add(buffer.toString());
					buffer = null;
					break;
				}
				c = (char) cInt;
				if (insideQuote) {
					if (c == '"') {
						insideQuote = false;
						result.add(buffer.toString());
						while ((cInt = in.read()) != commaInt) {
							if (cInt == -1) {
								buffer = null;
								break readLine;
							}
						}
						buffer = new StringBuilder();
					} else if (c == '\\') {
						cInt = in.read();
						if (cInt == -1) {
							result.add(buffer.toString());
							buffer = null;
							break;
						}
						c = (char) cInt;
						if (c == 'r') {
							buffer.append("\r");
						} else if (c == 'n') {
							buffer.append("\n");
						} else if (c == 't') {
							buffer.append("\t");
						} else {
							buffer.append(c);
						}
					} else {
						buffer.append(c);
					}
				} else {
					if (c == ',') {
						result.add(buffer.toString());
						buffer = new StringBuilder();
					} else if (c == '"') {
						insideQuote = true;
						buffer = new StringBuilder();
					} else {
						buffer.append(c);
					}
				}
			}
			if (buffer != null) {
				result.add(buffer.toString());
			}
			return result.toArray(new String[result.size()]);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static long readLong(InputStream in) throws IOException {
		return (((long) in.read()) << 56) + (((long) in.read()) << 48)
				+ (((long) in.read()) << 40) + (((long) in.read()) << 32)
				+ (((long) in.read()) << 24) + (((long) in.read()) << 16)
				+ (((long) in.read()) << 8) + ((long) in.read());
	}

	static void writeLong(OutputStream out, long val) throws IOException {
		out.write((int) ((val >> 56) & 0xff));
		out.write((int) ((val >> 48) & 0xff));
		out.write((int) ((val >> 40) & 0xff));
		out.write((int) ((val >> 32) & 0xff));
		out.write((int) ((val >> 24) & 0xff));
		out.write((int) ((val >> 16) & 0xff));
		out.write((int) ((val >> 8) & 0xff));
		out.write((int) (val & 0xff));
	}

	public static void main(String args[]) throws Throwable {
		Geolocator geo = get(new File("geolocation.csv"));
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while ((line = reader.readLine()) != null) {
			Geolocation get = geo.get(line);
			System.out.println(get);
		}
	}
}
