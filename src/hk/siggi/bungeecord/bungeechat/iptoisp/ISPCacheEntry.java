package hk.siggi.bungeecord.bungeechat.iptoisp;

import java.util.Objects;

public final class ISPCacheEntry {

	public final String ip, isp;
	public long expire;

	public ISPCacheEntry(String ip, String isp) {
		this.ip = ip;
		this.isp = isp;
		resetExpire();
	}

	public void resetExpire() {
		this.expire = System.currentTimeMillis() + 3600000L;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ISPCacheEntry) {
			ISPCacheEntry other = (ISPCacheEntry) o;
			return this.ip == null ?other.ip==null : other.ip.equals(this.ip);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 41 * hash + Objects.hashCode(this.ip);
		return hash;
	}
}
