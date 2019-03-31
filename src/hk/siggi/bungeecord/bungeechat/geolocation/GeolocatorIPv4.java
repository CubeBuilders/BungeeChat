package hk.siggi.bungeecord.bungeechat.geolocation;

import hk.siggi.bungeecord.bungeechat.util.RafStream;
import hk.siggi.bungeecord.bungeechat.util.LineReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

class GeolocatorIPv4 extends Geolocator {

	private final File file;
	private final File idxFile;
	private final RandomAccessFile raf;

	public GeolocatorIPv4(File file) {
		this.file = file;
		this.idxFile = new File(file.getPath() + ".ipv4idx");
		try {
			this.raf = new RandomAccessFile(file, "rw");
		} catch (IOException e) {
			throw new RuntimeException("Couldn't open file");
		}
		buildIndex();
	}

	@Override
	public Geolocation get(String ip) {
		try {
			String[] parts = ip.split("\\.");
			long ipLong = (Long.parseLong(parts[0]) << 24) + (Long.parseLong(parts[1]) << 16)
					+ (Long.parseLong(parts[2]) << 8) + Long.parseLong(parts[3]);
			int index = (int) ((ipLong >> 16) & 0xffff);
			long address = indexes[index];
			try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
				raf.seek(address);
				LineReader reader = new LineReader(new RafStream(raf));
				String line;
				while ((line = reader.readLine()) != null) {
					String[] csv = parseCsv(line);
					long firstIP = fix(Long.parseLong(csv[0]));
					long lastIP = fix(Long.parseLong(csv[1]));
					if (ipLong >= firstIP && ipLong <= lastIP) {
						String countryCode = csv[2];
						String countryName = csv[3];
						String regionName = csv[4];
						String cityName = csv[5];
						String latitude = csv[6];
						String longitude = csv[7];
						String zipcode = csv[8];
						String timezone = csv[9];
						return new Geolocation(countryCode, countryName, regionName, cityName, latitude, longitude, zipcode, timezone);
					}
					if (firstIP > ipLong) {
						return nullGeolocation;
					}
				}
			}
		} catch (Exception e) {
		}
		return nullGeolocation;
	}

	private final long[] indexes = new long[65536];

	private void buildIndex() {
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = -1L;
		}
		boolean didReadIdx = false;
		if (idxFile.exists()) {
			if (file.lastModified() > idxFile.lastModified()) {
				idxFile.delete();
			} else {
				try (FileInputStream fis = new FileInputStream(idxFile)) {
					for (int i = 0; i < indexes.length; i++) {
						long val = readLong(fis);
						indexes[i] = val;
					}
					didReadIdx = true;
				} catch (Exception e) {
					idxFile.delete();
				}
			}
		}
		if (!didReadIdx) {
			synchronized (raf) {try {
				raf.seek(0L);
				LineReader reader = new LineReader(new RafStream(raf));
				String line;
				while ((line = reader.readLine()) != null) {
					long addr = reader.getPreviousLineAddress();
					String[] csv = parseCsv(line);
					long firstIP, lastIP;
					try {
						firstIP = fix(Long.parseLong(csv[0]));
						lastIP = fix(Long.parseLong(csv[1]));
					} catch (NumberFormatException nfe) {
						continue;
					}
					int firstBlock = (int) ((firstIP >> 16) & 0xffff);
					int lastBlock = (int) ((lastIP >> 16) & 0xffff);
					for (int i = firstBlock; i <= lastBlock; i++) {
						if (indexes[i] == -1L) {
							indexes[i] = addr;
						} else {
							indexes[i] = Math.min(addr, indexes[i]);
						}
					}
				}
			} catch (Exception e) {
			}}
			try (FileOutputStream fos = new FileOutputStream(idxFile)) {
				for (int i = 0; i < indexes.length; i++) {
					writeLong(fos, indexes[i]);
				}
			} catch (Exception e) {
			}
		}
	}

	private long fix(long val) {
		// did we read from an IPv6 database?
		if (val >= 0xFFFF00000000L && val <= 0xFFFFFFFFFFFFL) {
			return val - 0xFFFF00000000L;
		}
		return val;
	}
}
