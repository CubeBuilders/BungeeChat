package hk.siggi.bungeecord.bungeechat.geolocation;

import hk.siggi.bungeecord.bungeechat.util.RafStream;
import hk.siggi.bungeecord.bungeechat.util.LineReader;
import hk.siggi.iphelper.IP;
import hk.siggi.iphelper.IPv4;
import hk.siggi.iphelper.IPv6;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.TreeSet;

class GeolocatorIPv6 extends Geolocator {

	private final File file;
	private final File idxFile;
	private final RandomAccessFile raf;
	private final GeolocatorIPv4 ipv4;
	private final IPv6 ipv4Range = (IPv6) IP.getIP("::ffff:0:0/96");

	public GeolocatorIPv6(File file, GeolocatorIPv4 ipv4) {
		this.file = file;
		this.ipv4 = ipv4;
		this.idxFile = new File(file.getPath() + ".ipv6idx");
		try {
			this.raf = new RandomAccessFile(file, "rw");
		} catch (IOException e) {
			throw new RuntimeException("Couldn't open file");
		}
		buildIndex();
	}

	@Override
	public Geolocation get(String ip) {
		if (!ip.contains(":")) {
			return ipv4.get(ip);
		}
		ip = ip.replace("[", "").replace("]", "");
		try {
			IPv6 ipv6 = (IPv6) IP.getIP(ip);
			if (ipv4Range.contains(ipv6)) {
				byte[] bytes = ipv6.getBytes();
				byte[] ipv4Bytes = new byte[]{bytes[12], bytes[13], bytes[14], bytes[15]};
				return ipv4.get(new IPv4(ipv4Bytes).toString());
			}
			BigInteger ipBigInteger = new BigInteger(ipv6.toLongString().replace(":", ""), 16);
			IPv6Pointer ptr = index.floor(new IPv6Pointer(ipBigInteger, 0L));
			long addr = ptr.getAddress();
			synchronized (raf) {
				raf.seek(addr);
				LineReader reader = new LineReader(new RafStream(raf));
				String line;
				while ((line = reader.readLine()) != null) {
					String[] csv = parseCsv(line);
					// do some stuff
					BigInteger firstIP = new BigInteger(csv[0]);
					BigInteger lastIP = new BigInteger(csv[1]);
					if (ipBigInteger.compareTo(firstIP) != -1
							&& ipBigInteger.compareTo(lastIP) != 1) {
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
					if (firstIP.compareTo(ipBigInteger) == 1) {
						return nullGeolocation;
					}
				}
			}
		} catch (Exception e) {
		}
		return nullGeolocation;
	}

	private final TreeSet<IPv6Pointer> index = new TreeSet<>(IPv6Pointer.comparator);

	private void buildIndex() {
		boolean didReadIdx = false;
		if (idxFile.exists()) {
			if (file.lastModified() > idxFile.lastModified()) {
				idxFile.delete();
			} else {
				try (FileInputStream fis = new FileInputStream(idxFile)) {
					index.clear();
					int readBytes;
					int c;
					byte[] ipBytes = new byte[16];
					readLoop:
					while (true) {
						readBytes = 0;
						while (readBytes < ipBytes.length) {
							c = fis.read(ipBytes, readBytes, ipBytes.length - readBytes);
							if (c == -1) {
								break readLoop;
							}
							readBytes += c;
						}
						long addr = readLong(fis);
						IPv6 ipAddr = new IPv6(ipBytes);
						BigInteger ipAddrBI = new BigInteger(ipAddr.toLongString().replace(":", ""), 16);
						index.add(new IPv6Pointer(ipAddrBI, addr));
					}
					didReadIdx = true;
				} catch (Exception e) {
					idxFile.delete();
				}
			}
		}
		if (!didReadIdx) {
			try {
				synchronized (raf) {
					index.clear();
					long prevBlock = -1L;
					raf.seek(0L);
					LineReader reader = new LineReader(new RafStream(raf));
					String line;
					while ((line = reader.readLine()) != null) {
						long addr = reader.getPreviousLineAddress();
						String[] csv = parseCsv(line);

						BigInteger firstIP = new BigInteger(csv[0]);
						BigInteger lastIP = new BigInteger(csv[1]);

						long lastBlock = lastIP.shiftRight(96).longValueExact();

						if (lastBlock > prevBlock) {
							IPv6Pointer ipv6Ptr = new IPv6Pointer(firstIP, addr);
							index.add(ipv6Ptr);
							prevBlock = lastBlock;
						}
					}
				}
			} catch (Exception e) {
			}
			try (FileOutputStream fos = new FileOutputStream(idxFile)) {
				for (IPv6Pointer ptr : index) {
					BigInteger ip = ptr.getIP();
					IPv6 ipv6 = getIPv6(ip);
					byte[] bytes = ipv6.getBytes();
					long addr = ptr.getAddress();
					fos.write(bytes);
					writeLong(fos, addr);
				}
			} catch (Exception e) {
			}
		}
	}

	private static IPv6 getIPv6(BigInteger bi) {
		if (bi.compareTo(BigInteger.ZERO) == -1) {
			return null;
		}
		String ip = bi.toString(16);
		int len = ip.length();
		StringBuilder sb = new StringBuilder();
		for (int i = len; i < 32; i++) {
			sb.append("0");
		}
		sb.append(ip);
		ip = sb.toString();
		if (ip.length() > 32) {
			return null;
		}
		ip = ip.toLowerCase();
		ip = ip.replaceAll("([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})",
				"$1:$2:$3:$4:$5:$6:$7:$8");
		return (IPv6) IP.getIP(ip);
	}
}
