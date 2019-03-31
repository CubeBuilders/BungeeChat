package hk.siggi.bungeecord.bungeechat.geolocation;

import java.math.BigInteger;
import java.util.Comparator;

class IPv6Pointer {

	private final BigInteger ip;
	private final long address;
	
	public BigInteger getIP(){return ip;}
	public long getAddress(){return address;}

	public IPv6Pointer(BigInteger ip, long address) {
		this.ip = ip;
		this.address = address;
	}

	public static final Comparator<IPv6Pointer> comparator = (ptr1, ptr2) -> {
		return ptr1.ip.compareTo(ptr2.ip);
	};
}
