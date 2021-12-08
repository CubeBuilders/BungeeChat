package hk.siggi.bungeecord.bungeechat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * FileConcatenatedInputStream reads multiple files as a single InputStream.
 *
 * @author Siggi
 */
public class FileConcatenatedInputStream extends InputStream {

	private final Object closeLock = new Object();
	private volatile boolean closed = false;

	private final List<File> fileList;
	private int currentFileIndex = -1;
	private InputStream currentInputStream = null;
	private File currentFile = null;
	private long amountReadFromCurrentFile = 0L;
	private long currentFileSize = 0L;
	private long nextFileSize = 0L;

	public FileConcatenatedInputStream(List<File> fileList) {
		this.fileList = fileList;
	}

	private void nextFile() throws IOException {
		if (closed) {
			return;
		}
		currentFileIndex += 1;
		if (currentInputStream != null) {
			try {
				currentInputStream.close();
			} catch (Exception e) {
			}
		}
		currentInputStream = null;
		currentFile = null;
		amountReadFromCurrentFile = 0L;
		currentFileSize = 0L;
		nextFileSize = 0L;
		int totalFiles = fileList.size();
		if (totalFiles > currentFileIndex) {
			currentFile = fileList.get(currentFileIndex);
			try {
				currentInputStream = new FileInputStream(currentFile);
			} finally {
				if (currentInputStream == null) {
					close();
				}
			}
			currentFileSize = currentFile.length();
			if (totalFiles > currentFileIndex + 1) {
				File next = fileList.get(currentFileIndex + 1);
				nextFileSize = next.length();
			}
		}
	}

	@Override
	public int read() throws IOException {
		if (closed) {
			throw new IOException("Stream closed");
		}
		if (currentInputStream == null) {
			if (currentFileIndex == -1) {
				nextFile();
			} else {
				return -1;
			}
		}
		int val = currentInputStream.read();
		if (val != -1) {
			amountReadFromCurrentFile += 1;
		}
		if (amountReadFromCurrentFile >= currentFileSize || val == -1) {
			nextFile();
		}
		return val;
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		if (closed) {
			throw new IOException("Stream closed");
		}
		if (currentInputStream == null) {
			if (currentFileIndex == -1) {
				nextFile();
			} else {
				return -1;
			}
		}
		int amountRead = currentInputStream.read(b, off, len);
		if (amountRead != -1) {
			amountReadFromCurrentFile += amountRead;
		}
		if (amountReadFromCurrentFile >= currentFileSize || amountRead == -1) {
			nextFile();
		}
		return amountRead;
	}

	@Override
	public int available() throws IOException {
		if (closed) {
			throw new IOException("Stream closed");
		}
		if (currentInputStream == null) {
			if (currentFileIndex == -1) {
				nextFile();
			} else {
				return -1;
			}
		}
		int calc = (int) Math.min(2147483647L, currentFileSize + nextFileSize - amountReadFromCurrentFile);
		if (calc < 0) {
			calc = 0;
		}
		return calc;
	}

	@Override
	public void close() throws IOException {
		synchronized (closeLock) {
			if (closed) {
				return;
			}
			closed = true;
		}
		try {
			if (currentInputStream != null) {
				currentInputStream.close();
			}
		} catch (Exception e) {
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}
}
