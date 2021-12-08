package hk.siggi.bungeecord.bungeechat.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * A line reader that reads lines from an InputStream that can terminate with
 * either &lt;CR&gt; or &lt;LF&gt; and can tell you the address of the beginning
 * of the next line or previous line.
 *
 * @author Siggi
 */
public final class LineReader {

	private final InputStream in;
	private final Charset charset;
	private final byte[] buffer = new byte[8192];
	private final int half = buffer.length / 2;
	private int readPtr = 0;
	private int writePtr = 0;
	private boolean hitEOF = false;
	private long previousLineAddress = 0L;
	private long totalRead = 0L;

	private final ByteArrayOutputStream bb = new ByteArrayOutputStream(2048);

	public LineReader(InputStream in) {
		this(in, Charset.forName("UTF-8"));
	}

	public LineReader(InputStream in, Charset charset) {
		this.in = in;
		this.charset = charset;
	}

	/**
	 * Read a single line from the stream, does not include trailing &lt;CR&gt;
	 * or &lt;LF&gt;.
	 *
	 * @return A single line
	 * @throws IOException if something goes wrong
	 */
	public String readLine() throws IOException {
		if (hitEOF && readPtr == writePtr) {
			return null;
		}
		previousLineAddress = totalRead;
		bb.reset();
		while (true) {
			fillBuffer();
			int endOfLine = findEndOfLine();
			int amount = (endOfLine == -1) ? (writePtr - readPtr) : (endOfLine - readPtr);
			bb.write(buffer, readPtr, amount);
			readPtr += amount;
			totalRead += amount;
			if (endOfLine != -1) {
				fillBuffer();
				if (buffer[readPtr] == (byte) 0x0D) {
					if (buffer[readPtr + 1] == (byte) 0x0A) {
						readPtr += 2;
						totalRead += 2;
					} else {
						readPtr += 1;
						totalRead += 1;
					}
				} else if (buffer[readPtr] == (byte) 0x0A) {
					readPtr += 1;
					totalRead += 1;
				}
				break;
			}
			if (hitEOF && readPtr == writePtr) {
				break;
			}
		}
		byte[] result = bb.toByteArray();
		return new String(result, charset);
	}

	/**
	 * Get the address of the first byte of the previously read line.
	 *
	 * @return
	 */
	public long getPreviousLineAddress() {
		if (totalRead == 0L) {
			throw new IllegalStateException("No lines have been read yet!");
		}
		return previousLineAddress;
	}

	/**
	 * Get the total number of bytes read up, including the trailing &lt;CR&gt;
	 * or &lt;LF&gt; of the previously read line.
	 *
	 * @return
	 */
	public long getTotalRead() {
		return totalRead;
	}

	private int findEndOfLine() {
		for (int i = readPtr; i < writePtr; i++) {
			if (buffer[i] == 0x0D || buffer[i] == 0x0A) {
				return i;
			}
		}
		if (hitEOF) {
			return writePtr;
		} else {
			return -1;
		}
	}

	private void fillBuffer() throws IOException {
		if (readPtr >= half) {
			System.arraycopy(buffer, half, buffer, 0, half);
			readPtr -= half;
			writePtr -= half;
		}
		if (hitEOF) {
			return;
		}
		if (writePtr < buffer.length) {
			int readAmount = in.read(buffer, writePtr, buffer.length - writePtr);
			if (readAmount == -1) {
				hitEOF = true;
				return;
			}
			writePtr += readAmount;
		}
	}
}
