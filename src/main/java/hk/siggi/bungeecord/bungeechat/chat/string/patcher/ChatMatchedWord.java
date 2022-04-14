package hk.siggi.bungeecord.bungeechat.chat.string.patcher;

import hk.siggi.bungeecord.bungeechat.chat.string.ChatString;

public class ChatMatchedWord implements CharSequence {

	private final ChatString string;
	private final int position;
	private int length;

	@Override
	public String toString() {
		return string.toRawString().substring(position, length);
	}

	@Override
	public int length() {
		return length;
	}

	public ChatMatchedWord(ChatString string, int position, int length) {
		this.string = string;
		this.position = position;
		this.length = length;
	}

	public ChatMatchedWord delete(int start, int end) {
		if (start < 0 || end < start || end > length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		string.delete(position + start, position + end);
		length -= (end - start);
		return this;
	}

	public ChatMatchedWord insert(int pos, String text) {
		if (pos > length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		string.insertAt(position + pos, text, pos == 0);
		length += text.length();
		return this;
	}

	public ChatMatchedWord replace(int pos, String text) {
		int end = pos + text.length();
		if (pos < 0 || end > length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		string.replaceText(position + pos, text);
		return this;
	}

	public ChatMatchedWord replaceWhole(String text) {
		if (text.length() != length) {
			throw new IllegalArgumentException("Input must be same length!");
		}
		string.replaceText(position, text);
		return this;
	}

	@Override
	public char charAt(int pos) {
		return string.charAt(position+pos);
	}

	@Override
	public ChatMatchedWord subSequence(int start, int end) {
		return new ChatMatchedWord(string, position + start, position + end);
	}

	public String getBefore() {
		return string.toRawString().substring(0, position);
	}

	public String getAfter() {
		return string.toRawString().substring(position + length);
	}
}
