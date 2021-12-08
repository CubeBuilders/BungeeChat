package hk.siggi.bungeecord.bungeechat.chat.string.patcher;

import hk.siggi.bungeecord.bungeechat.chat.string.ChatString;
import java.util.function.Consumer;

public class ChatPatcher {

	private final ChatString string;

	public ChatPatcher(ChatString string) {
		this.string = string;
	}

	public void forEach(String needle, Consumer<ChatMatchedWord> function) {
		String haystack = string.toRawString().toLowerCase();
		needle = needle.toLowerCase();
		int haystackLength = haystack.length();
		int needleLength = needle.length();
		int end = haystackLength - needleLength;
		int pos = 0;
		int adjustment = 0;
		while ((pos = haystack.indexOf(needle, pos)) != -1) {
			if ((pos == 0 || isWordBorderCharacter(haystack.charAt(pos - 1))) && (pos == end || isWordBorderCharacter(haystack.charAt(pos + needleLength)))) {
				ChatMatchedWord word = new ChatMatchedWord(string, pos + adjustment, needleLength);
				function.accept(word);
				adjustment += word.length() - needleLength;
				pos += needleLength;
			} else {
				pos += 1;
			}
		}
	}

	private static boolean isWordBorderCharacter(char c) {
		return c == ' ' || c == '.' || c == ',' || c == '?' || c == '!'
				|| c == '/' || c == '\\' || c == '|'
				|| c == ':' || c == ';'
				|| c == '"' || c == '\''
				|| c == '<' || c == '>' || c == '[' || c == ']'
				|| c == '{' || c == '}' || c == '(' || c == ')'
				|| c == '@' || c == '#' || c == '$' || c == '%'
				|| c == '^' || c == '&' || c == '*' || c == '-'
				|| c == '_' || c == '`' || c == '~';
	}
}
