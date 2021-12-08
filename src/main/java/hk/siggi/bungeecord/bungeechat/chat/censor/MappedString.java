package hk.siggi.bungeecord.bungeechat.chat.censor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MappedString {
	private String fullString;
	private int stringLength;
	private int shortStringLength;
	private boolean[] charactersDeleted;
	public MappedString(String string) {
		if (string == null) throw new NullPointerException();
		this.fullString = string;
		this.stringLength = string.length();
		this.shortStringLength = this.stringLength;
		this.charactersDeleted = new boolean[this.stringLength];
		for (int i = 0; i < this.charactersDeleted.length; i++) {
			this.charactersDeleted[i] = false;
		}
	}
	public String getFullString() {
		return fullString;
	}
	public void setFullString(String string) {
		if (string.length() != stringLength) {
			throw new IllegalArgumentException("String length does not match.");
		}
	}
	public String getShortString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < stringLength; i++) {
			if (!charactersDeleted[i]) {
				sb.append(fullString.charAt(i));
			}
		}
		return sb.toString();
	}
	public void setShortString(String string) {
		if (string.length() != shortStringLength) {
			throw new IllegalArgumentException("String length does not match.");
		}
		StringBuilder sb = new StringBuilder();
		int j = 0;
		for (int i = 0; i < stringLength; i++) {
			if (charactersDeleted[i]) {
				sb.append(fullString.charAt(i));
			} else {
				sb.append(string.charAt(j));
				j += 1;
			}
		}
		fullString = sb.toString();
	}
	public void setDeleted(int index, boolean delete) {
		if (charactersDeleted[index] != delete) {
			charactersDeleted[index] = delete;
			shortStringLength += delete ? -1 : 1;
		}
	}
	// asdf-hj-lzxc--nm
	//      5  8  1
	// asdfhjlzxcnm
	//     4 6  9
	public StringRange convertShortRangeToFullRange(StringRange range) {
		int start = range.start;
		int end = range.end;
		for (int longPos = 0; longPos <= start; longPos++) {
			if (charactersDeleted[longPos]) {
				start += 1;
			}
		}
		end -= 1;
		for (int longPos = 0; longPos <= end; longPos++) {
			if (charactersDeleted[longPos]) {
				end += 1;
			}
		}
		end += 1;
		return new StringRange(start, end);
	}
	
	public MappedString deleteAll(int start, int end) {
		for (int i = start; i < end; i++) {
			setDeleted(i, true);
		}
		return this;
	}
	public MappedString deleteAll(String regex) {
		deleteAll(Pattern.compile(regex));
		return this;
	}
	public MappedString deleteAll(Pattern pattern) {
		Matcher matcher = pattern.matcher(fullString);
		int nextFind = 0;
		while (nextFind < stringLength && matcher.find(nextFind)) {
			int startPos = matcher.start();
			int endPos = matcher.end();
			nextFind = startPos + 1;
			deleteAll(startPos, endPos);
		}
		return this;
	}
}
