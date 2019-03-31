package hk.siggi.bungeecord.bungeechat.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class RafStream extends InputStream {

	private final RandomAccessFile raf;

	public RafStream(RandomAccessFile raf) {
		this.raf = raf;
	}

	@Override
	public int read() throws IOException {
		return raf.read();
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		return raf.read(buffer);
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		return raf.read(buffer, offset, length);
	}

	@Override
	public int available() throws IOException {
		long avail = raf.length() - raf.getFilePointer();
		if (avail > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) avail;
	}
}
