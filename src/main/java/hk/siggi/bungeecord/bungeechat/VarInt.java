package hk.siggi.bungeecord.bungeechat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class VarInt {
	private VarInt() {
	}
	public static int read(InputStream in) throws IOException {
		int i = 0;
		int j = 0;
		while (true) {
			int kk = in.read();
			if (kk == -1) {
				throw new IOException("End of stream");
			}
			int k = ((int) kk) & 0xff;
			i |= (k & 0x7F) << j++ * 7;
			if (j > 5) throw new RuntimeException("VarInt too big");
			if ((k & 0x80) != 128) break;
		}
		return i;
	}
	public static void write(OutputStream out, int paramInt) throws IOException {
		while (true) {
			if ((paramInt & 0xFFFFFF80) == 0) {
				out.write(paramInt);
				return;
			}

			out.write(paramInt & 0x7F | 0x80);
			paramInt >>>= 7;
		}
	}
}
