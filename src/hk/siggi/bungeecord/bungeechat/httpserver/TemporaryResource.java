package hk.siggi.bungeecord.bungeechat.httpserver;

import java.io.IOException;
import java.io.OutputStream;

public final class TemporaryResource {
	public final String filename;
	public final String contentType;
	public final long creationTime;
	private final byte[] bytes;
	public TemporaryResource(String filename, String contentType, byte[] bytes) {
		this.filename = filename;
		this.contentType = contentType;
		this.creationTime = System.currentTimeMillis();
		this.bytes = bytes;
	}
	public int getLength() {
		return bytes.length;
	}
	public void write(OutputStream out) throws IOException {
		out.write(bytes);
	}
}
