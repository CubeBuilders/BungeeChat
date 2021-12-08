package hk.siggi.bungeecord.bungeechat.playtime;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

public class PlayTimePlayer {

	private final PlayTime playTime;
	private final UUID player;
	private final File file, idxFile;

	private String currentServer = null;
	private long loginTime = -1L;
	private long sessionLoginTime = -1L;
	private long afkStart = -1L;

	PlayTimePlayer(PlayTime playTime, UUID player) {
		this.playTime = playTime;
		this.player = player;
		this.file = new File(
				playTime.getSessionRecordDataFolder(),
				player.toString().replaceAll("-", "").toLowerCase() + ".txt"
		);
		this.idxFile = new File(
				playTime.getSessionRecordDataFolder(),
				player.toString().replaceAll("-", "").toLowerCase() + ".idx"
		);
	}

	/**
	 * The UUID of this player.
	 *
	 * @return UUID of the player.
	 */
	public UUID getPlayer() {
		return player;
	}

	private File getFile() {
		return file;
	}

	private File getIdxFile() {
		return idxFile;
	}

	void loggedIn(String server, String playerIP) {
		afkEnd();
		long now = System.currentTimeMillis();
		if (sessionLoginTime != -1L) {
			recordServer(currentServer, loginTime, now);
			currentServer = server;
			loginTime = now;
		} else {
			recordLogin(now, playerIP);
			currentServer = server;
			loginTime = now;
			sessionLoginTime = now;
		}
	}

	void loggedOut() {
		afkEnd();
		long now = System.currentTimeMillis();
		recordLogout(currentServer, loginTime, now);
		currentServer = null;
		loginTime = -1L;
		sessionLoginTime = -1L;
	}

	void afkStart() {
		afkStart(System.currentTimeMillis());
	}

	void afkStart(long time) {
		if (afkStart == -1L) {
			afkStart = time;
			recordAfk(time);
		}
	}

	void afkEnd() {
		long now = System.currentTimeMillis();
		if (afkStart != -1L) {
			recordNotAfk(now);
			afkStart = -1L;
		}
	}

	private void recordLogin(long time, String playerIP) {
		updateIdx(file.length(), file.length(), time);
		log("Login=" + time + "," + playerIP);
	}

	private void recordServer(String prevServer, long prevServerLogin, long logoutTime) {
		updateIdx(-1L, file.length(), -1L);
		log("Server=" + prevServer + "," + prevServerLogin + "," + logoutTime);
	}

	private void recordLogout(String prevServer, long loginTime, long logoutTime) {
		recordServer(prevServer, loginTime, logoutTime);
		updateIdx(-1L, file.length(), -1L);
		log("Logout=" + logoutTime);
	}

	private void recordAfk(long time) {
		log("Afk=" + time);
	}

	private void recordNotAfk(long time) {
		log("AfkEnd=" + time);
	}

	private void updateIdx(long loginIdx, long lastRecordIdx, long newTime) {
		try (RandomAccessFile raf = new RandomAccessFile(getIdxFile(), "rw")) {
			prepareIdxFile(raf);
			long size = raf.length();
			if (loginIdx != -1L) {
				raf.seek(0L);
				raf.writeLong(loginIdx);
			}
			if (lastRecordIdx != -1L) {
				raf.seek(8L);
				raf.writeLong(lastRecordIdx);
			}
			if (newTime != -1L) {
				long currentPosition = file.length();
				long recordCount = (size - 1024L) / 16L;
				if (recordCount > 0L) {
					raf.seek(1024L + ((recordCount - 1) * 16L));
					long lastTime = raf.readLong();
					long lastIdx = raf.readLong();
					if (newTime - lastTime < 604800000L && currentPosition - lastIdx < 16384L) {
						return;
					}
				}
				raf.seek(1024L + (recordCount * 16L));
				raf.writeLong(newTime);
				raf.writeLong(currentPosition);
			}
		} catch (IOException e) {
		}
	}

	private long getLong(int idx) {
		if (idx < 0 || idx >= 128) {
			throw new ArrayIndexOutOfBoundsException("idx must be between 0 and 127!");
		}
		try (RandomAccessFile raf = new RandomAccessFile(getFile(), "r")) {
			raf.seek(8L * ((long) idx));
			return raf.readLong();
		} catch (Exception e) {
		}
		return -1L;
	}

	private void setLong(int idx, long val) {
		if (idx < 0 || idx >= 128) {
			throw new ArrayIndexOutOfBoundsException("idx must be between 0 and 127!");
		}
		try (RandomAccessFile raf = new RandomAccessFile(getIdxFile(), "rw")) {
			raf.seek(8L * ((long) idx));
			raf.writeLong(val);
		} catch (IOException e) {
		}
	}

	private void prepareIdxFile(RandomAccessFile raf) throws IOException {
		int minLength = 1024;
		long curLength = raf.length();
		if (curLength < ((long) minLength)) {
			int extend = minLength - ((int) curLength);
			byte[] extension = new byte[extend];
			for (int i = 0; i < extension.length; i++) {
				extension[i] = (byte) 0xff;
			}
			raf.seek(curLength);
			raf.write(extension);
		}
	}

	private long getIdx(long time) {
		long result = -1L;
		try (FileInputStream i = new FileInputStream(getIdxFile())) {
			DataInputStream di = new DataInputStream(i);
			di.skipBytes(16);
			while (di.available() >= 16) {
				long t = di.readLong();
				long idx = di.readLong();
				if (t > time) {
					return result;
				} else {
					result = idx;
				}
			}
		} catch (Exception e) {
		}
		return result;
	}

	private void log(String txt) {
		try (FileWriter fw = new FileWriter(getFile(), true)) {
			fw.write(txt + "\n");
		} catch (IOException e) {
		}
	}
}
