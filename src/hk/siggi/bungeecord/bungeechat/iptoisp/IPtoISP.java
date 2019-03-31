package hk.siggi.bungeecord.bungeechat.iptoisp;

import hk.siggi.iphelper.IP;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class IPtoISP {
	
	public static void main(String args[]) {
		System.out.println(new IPtoISP(new File("iptoisp.txt")).getISP(args[0]));
	}

	private final Map<IP, ISPCacheEntry> caches = new HashMap<>();
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();

	private final Map<String, String> hostNamesToISP = new HashMap<>();
	private final ReentrantReadWriteLock dbLock = new ReentrantReadWriteLock();
	private final Lock dbReadLock = dbLock.readLock();
	private final Lock dbWriteLock = dbLock.writeLock();

	private long lastSweep = 0L;

	private final File f;

	public IPtoISP(File readFrom) {
		this.f = readFrom;
		readFromFile();
	}

	public void readFromFile() {
		try {
			if (f == null) {
				return;
			}
			dbWriteLock.lock();
			try {
				try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
					hostNamesToISP.clear();
					String line;
					String ispName = null;
					while ((line = reader.readLine()) != null) {
						if (line.contains("#")) {
							line=line.substring(0,line.indexOf("#"));
						}
						line = line.trim();
						if (line.isEmpty()) {
							ispName = null;
							continue;
						}
						if (ispName == null) {
							ispName = line;
						} else {
							setForSuffix0(line, ispName);
						}
					}
				}
			} finally {
				dbWriteLock.unlock();
			}
		} catch (Exception e) {
		}
	}

	public void setForSuffix(String hostNameSuffix, String isp) {
		dbWriteLock.lock();
		try {
			setForSuffix0(hostNameSuffix, isp);
		} finally {
			dbWriteLock.unlock();
		}
	}

	private void setForSuffix0(String hostNameSuffix, String isp) {
		hostNamesToISP.put(hostNameSuffix, isp);
	}

	private void sweep() {
		long now = System.currentTimeMillis();
		readLock.lock();
		try {
			if (now - lastSweep < 600000L) {
				return;
			}
		} finally {
			readLock.unlock();
		}
		writeLock.lock();
		try {
			if (now - lastSweep < 600000L) {
				return;
			}
			lastSweep = now;
			for (Iterator<Map.Entry<IP, ISPCacheEntry>> it = caches.entrySet().iterator(); it.hasNext();) {
				Map.Entry<IP, ISPCacheEntry> entry = it.next();
				if (entry.getValue().expire <= now) {
					it.remove();
				}
			}
		} finally {
			writeLock.unlock();
		}
	}

	public String getISP(String ip) {
		return getISP(IP.getIP(ip));
	}

	public String getISP(IP ip) {
		if (ip == null) {
			throw new NullPointerException();
		}
		sweep();
		ISPCacheEntry cache;
		readLock.lock();
		try {
			cache = caches.get(ip);
		} finally {
			readLock.unlock();
		}
		if (cache != null) {
			cache.resetExpire();
			return cache.isp;
		}
		String hostName = getHostName(ip);
		String isp = getISPFromHostName(hostName);
		writeLock.lock();
		try {
			caches.put(ip, new ISPCacheEntry(ip.toShortString(), isp));
		} finally {
			writeLock.unlock();
		}
		return isp;
	}

	private static String getHostName(IP ip) {
		InetAddress ia;
		try {
			ia = InetAddress.getByName(ip.toLongString());
		} catch (UnknownHostException uhe) {
			// this shouldn't ever happen
			throw new RuntimeException(uhe);
		}
		return ia.getCanonicalHostName();
	}

	private String getISPFromHostName(String hostName) {
		while (hostName.startsWith(".")) {
			hostName = hostName.substring(1);
		}
		while (hostName.endsWith(".")) {
			hostName = hostName.substring(0, hostName.length() - 1);
		}
		dbReadLock.lock();
		try {
			String[] parts = hostName.split("\\.");
			for (int i = 0; i < parts.length; i++) {
				StringBuilder p = new StringBuilder();
				for (int j = i; j < parts.length; j++) {
					if (j != i) {
						p.append(".");
					}
					p.append(parts[j]);
				}
				String part = p.toString();
				String result = hostNamesToISP.get(part);
				if (result != null) {
					return result;
				}
			}
		} finally {
			dbReadLock.unlock();
		}
		return null;
	}
}
