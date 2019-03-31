package hk.siggi.bungeecord.bungeechat.ontime;

import hk.siggi.bungeecord.bungeechat.util.IteratorAbstract;
import static hk.siggi.bungeecord.bungeechat.util.Util.tryClose;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class OnTimePlayer {

	private final UUID player;
	private boolean loggedIn = false;
	private String currentServer = null;
	private long loginTime = -1L;

	private final Object loginLock = new Object();

	private static final Logger logger = Logger.getLogger(OnTimePlayer.class.getName());

	OnTimePlayer(UUID player) {
		this.player = player;
	}

	/**
	 * The UUID of the player for this OnTimePlayer object.
	 *
	 * @return UUID of the player.
	 */
	public UUID getPlayer() {
		return player;
	}

	void recordLogin(String targetServer) {
		synchronized (loginLock) {
			long time = System.currentTimeMillis();
			if (loggedIn) {
				recordSession(currentServer, loginTime, time);
			}
			loggedIn = true;
			currentServer = targetServer;
			loginTime = time;
		}
	}

	void recordLogout() {
		synchronized (loginLock) {
			if (!loggedIn) {
				return;
			}
			long time = System.currentTimeMillis();
			recordSession(currentServer, loginTime, time);
			loggedIn = false;
			currentServer = null;
			loginTime = -1L;
		}
	}

	private File getFile() {
		return new File(OnTime.getInstance().getSessionRecordDataFolder(), (player.toString().replaceAll("-", "").toLowerCase()));
	}

	private void recordSession(String server, long login, long logout) {
		try {
			FileOutputStream out = new FileOutputStream(getFile(), true);
			out.write(("Session=" + server + "," + login + "," + logout + "\n").getBytes());
			out.close();
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		}
	}

	public Iterator<OnTimeSessionRecord> sessionRecordsIterator() {
		try {
			final FileInputStream in;
			final BufferedReader reader;
			reader = new BufferedReader(new InputStreamReader(in = new FileInputStream(getFile())));
			Iterator<OnTimeSessionRecord> iterator = new IteratorAbstract<OnTimeSessionRecord>() {
				private FileInputStream fileIn = in;
				private BufferedReader bufReader = reader;

				@Override
				protected OnTimeSessionRecord get() {
					try {
						String line;
						while ((line = bufReader.readLine()) != null) {
							if (line.startsWith("Session=")) {
								String[] info = line.substring(8).split(",");
								String server = info[0];
								long login = Long.parseLong(info[1]);
								long logout = Long.parseLong(info[2]);
								if (login < 0L || logout < 0L) {
									continue; // invalid record!
								}
								return new OnTimeSessionRecord(player, server, login, logout);
							}
						}
						tryClose(fileIn);
						bufReader = null;
						fileIn = null;
					} catch (Exception e) {
						tryClose(fileIn);
						bufReader = null;
						fileIn = null;
					}
					return null;
				}

				@Override
				@SuppressWarnings("FinalizeDeclaration")
				protected void finalize() throws Throwable {
					super.finalize();
					tryClose(fileIn);
				}
			};
			return iterator;
		} catch (Exception e) {
			return new IteratorAbstract<OnTimeSessionRecord>() {

				@Override
				protected OnTimeSessionRecord get() {
					return null;
				}
			};
		}
	}

	/**
	 * Reads session records from disk. If the player is currently logged in,
	 * the current session will also be included with a logout time of -1.
	 *
	 * @return session records
	 */
	public OnTimeSessionRecord[] getSessionRecords() {
		ArrayList<OnTimeSessionRecord> list = new ArrayList<>();
		FileInputStream fis = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis = new FileInputStream(getFile())));
			String line;
			while ((line = reader.readLine()) != null) {
				try {
					if (line.startsWith("Session=")) {
						String[] info = line.substring(8).split(",");
						String server = info[0];
						long login = Long.parseLong(info[1]);
						long logout = Long.parseLong(info[2]);
						if (login < 0L || logout < 0L) {
							continue; // invalid record!
						}
						list.add(new OnTimeSessionRecord(player, server, login, logout));
					}
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
		} finally {
			tryClose(fis);
		}
		@SuppressWarnings("LocalVariableHidesMemberVariable")
		boolean loggedIn;
		@SuppressWarnings("LocalVariableHidesMemberVariable")
		String currentServer;
		@SuppressWarnings("LocalVariableHidesMemberVariable")
		long loginTime;
		synchronized (loginLock) {
			loggedIn = this.loggedIn;
			currentServer = this.currentServer;
			loginTime = this.loginTime;
		}
		if (loggedIn) {
			list.add(new OnTimeSessionRecord(player, currentServer, loginTime, -1L));
		}
		return list.toArray(new OnTimeSessionRecord[list.size()]);
	}

	/**
	 * Quickly retrieve the last online time for this user without reading the
	 * entire history.
	 *
	 * @return
	 */
	public long getLastOnline() {return getLastOnline(false);}
	public long getLastOnline(boolean ignoreLoggedIn){
		if (!ignoreLoggedIn && loggedIn) {
			return -1L;
		}
		try {
			long time = 0L;
			byte[] quickRead;
			try (RandomAccessFile raf = new RandomAccessFile(getFile(), "r")) {
				raf.seek(Math.max(0L, raf.length() - 512L));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int c = 0;
				byte[] b = new byte[512];
				while ((c = raf.read(b, 0, b.length)) != -1) {
					baos.write(b, 0, c);
				}
				quickRead = baos.toByteArray();
			}
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(quickRead)))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("Session=")) {
						String[] info = line.substring(8).split(",");
						String server = info[0];
						long login = Long.parseLong(info[1]);
						long logout = Long.parseLong(info[2]);
						if (login < 0L || logout < 0L) {
							continue; // invalid record!
						}
						time = logout;
					}
				}
			}
			return time;
		} catch (Exception e) {
		}
		return 0L;
	}
}
