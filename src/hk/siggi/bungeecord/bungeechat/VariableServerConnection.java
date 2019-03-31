package hk.siggi.bungeecord.bungeechat;

import static hk.siggi.bungeecord.bungeechat.util.Util.tryClose;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VariableServerConnection {

	private final List<Listener> listeners = new LinkedList<>();
	private Socket socket = null;
	private InputStream in = null;
	private OutputStream out = null;
	private final Object sendLock = new Object();
	private Thread sendThread = null;
	private final List<byte[]> queue = new ArrayList<>();
	private final String server;
	
	public void addListener(Listener listener) {
		if (!listeners.contains(listener))listeners.add(listener);
	}
	public void removeListener(Listener listener) {listeners.remove(listener);}
	public Listener[] getListeners(){return listeners.toArray(new Listener[listeners.size()]);}

	public VariableServerConnection(String server) {
		this.server = server;
		final VariableServerConnection v = this;
		new Thread(new Runnable() {
			@Override
			public void run() {
				v.run();
			}
		}).start();
	}

	private void run() {
		boolean wasConnected = false;
		try {
			socket = new Socket(server, 6673);
			in = socket.getInputStream();
			synchronized (sendLock) {
				out = socket.getOutputStream();
				sendLock.notifyAll();
			}
			socket.setTcpNoDelay(true);
			socket.setSoTimeout(10000);
			System.out.println("Connected to VariableServer");
			wasConnected = true;
			(sendThread = new Thread(new Runnable() {
				@Override
				public void run() {
					sendThread();
				}
			})).start();
			while (true) {
				int packetId = in.read();
				if (packetId == -1) {
					break;
				}
				if (packetId == 0) {
					synchronized (sendLock) {
						while (out == null) {
							sendLock.wait();
						}
						out.write(0);
					}
				} else if (packetId == 1) {
					int variableLength = (in.read() << 8) | in.read();
					byte[] variableBytes = readFully(in, new byte[variableLength]);
					int valueLength = (in.read() << 8) | in.read();
					byte[] valueBytes = readFully(in, new byte[valueLength]);
					for (Listener listener : listeners) {try {
						listener.receivedVariable(new String(variableBytes), new String(valueBytes));}catch(Exception e){}
					}
				} else if (packetId == 0x02) {
				} else if (packetId == 0x03) {
					int fromNameLength = (in.read() << 8) | in.read();
					byte[] fromNameBytes = readFully(in, new byte[fromNameLength]);
					int messageLength = (in.read() << 8) | in.read();
					byte[] message = readFully(in, new byte[messageLength]);
					// TODO: plugin.receivedMessage
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			if (wasConnected) {
				System.out.println("Disconnected from VariableServer");
			}
			for (Listener listener : listeners) {try {
				listener.disconnectedVariableServer(this);}catch(Exception e){}
			}
			stop();
		}
	}

	public void stop() {
		try {
			socket.close();
		} catch (Exception e) {
		}
		try {
			if (sendThread != null) {
				sendThread.interrupt();
			}
		} catch (Exception e) {
		}
	}

	public void updateVariable(String variable, String value) {
		sendVariable(variable, value);
	}

	private void sendVariable(String variable, String value) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(0x01);
			byte[] b = variable.getBytes();
			baos.write((b.length >> 8) & 0xff);
			baos.write(b.length & 0xff);
			baos.write(b);
			b = value.getBytes();
			baos.write((b.length >> 8) & 0xff);
			baos.write(b.length & 0xff);
			baos.write(b);
			synchronized (queue) {
				queue.add(baos.toByteArray());
				queue.notify();
			}
		} catch (Exception e) {
		}
	}

	public void setName(String name) {
		if (name == null) {
			return;
		}
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(0x02);
			byte[] b = name.getBytes();
			baos.write((b.length >> 8) & 0xff);
			baos.write(b.length & 0xff);
			baos.write(b);
			synchronized (queue) {
				queue.add(baos.toByteArray());
				queue.notify();
			}
		} catch (Exception e) {
		}
	}

	public void sendMessage(String destination, byte[] message) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(0x03);
			byte[] b = destination.getBytes();
			baos.write((b.length >> 8) & 0xff);
			baos.write(b.length & 0xff);
			baos.write(b);
			baos.write((message.length >> 8) & 0xff);
			baos.write(message.length & 0xff);
			baos.write(message);
			synchronized (queue) {
				queue.add(baos.toByteArray());
				queue.notify();
			}
		} catch (Exception e) {
		}
	}

	private void sendThread() {
		try {
			while (true) {
				byte[] b = null;
				synchronized (queue) {
					while (queue.size() <= 0) {
						queue.wait();
					}
					b = queue.get(0);
					queue.remove(0);
				}
				synchronized (sendLock) {
					while (out == null) {
						sendLock.wait();
					}
					out.write(b);
				}
			}
		} catch (InterruptedException ie) {
		} catch (IOException e) {
			tryClose(socket);
		}
	}

	private byte[] readFully(InputStream in, byte[] b) throws IOException {
		int read = 0;
		int c;
		while (read < b.length) {
			c = in.read(b, read, b.length - read);
			if (c == -1) {
				throw new IOException("End of stream");
			}
			read += c;
		}
		return b;
	}

	public interface Listener {

		public void receivedVariable(String name, String value);

		public void receivedMessage(String from, byte[] message);

		public void disconnectedVariableServer(VariableServerConnection conn);
	}
}
