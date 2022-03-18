package hk.siggi.bungeecord.bungeechat.chat.censor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatCensor {

	private final File dataFolder;

	private final ArrayList<String> censored = new ArrayList<>();
	private final ArrayList<Pattern> censoredPatterns = new ArrayList<>();
	private final ArrayList<String> whitelist = new ArrayList<>();
	private final ArrayList<Pattern> whitelistPatterns = new ArrayList<>();

	public ChatCensor(File dataFolder) {
		this.dataFolder = dataFolder;
		reloadWords();
	}

	private ChatCensor(File dataFolder, String word, Pattern censoredPattern, ArrayList<String> whitelist, ArrayList<Pattern> whitelistPatterns) {
		this.dataFolder = dataFolder;
		censored.add(word);
		censoredPatterns.add(censoredPattern);
		this.whitelist.addAll(whitelist);
		this.whitelistPatterns.addAll(whitelistPatterns);
	}

	public void reloadWords() {
		try {
			censored.clear();
			censoredPatterns.clear();
			whitelist.clear();
			BufferedReader reader = new BufferedReader(new FileReader(new File(dataFolder, "BadWords.txt")));
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.equals("") && !line.startsWith("//")) {
					censored.add(line);
					censoredPatterns.add(Pattern.compile(line, Pattern.CASE_INSENSITIVE));
				}
			}
			reader.close();
			reader = new BufferedReader(new FileReader(new File(dataFolder, "Whitelist.txt")));
			while ((line = reader.readLine()) != null) {
				if (!line.equals("") && !line.startsWith("//")) {
					whitelist.add(line);
					whitelistPatterns.add(Pattern.compile(line, Pattern.CASE_INSENSITIVE));
				}
			}
			reader.close();
		} catch (Exception e) {
			System.out.println("Error loading the censor.");
		}
	}

	public ChatCensor filterOnly(String word) {
		int x = -1;
		for (int i = 0; i < censoredPatterns.size(); i++) {
			Pattern get = censoredPatterns.get(i);
			if (get.matcher(word).matches()) {
				x = i;
				break;
			}
		}
		if (x != -1) {
			String c = censored.get(x);
			Pattern cp = censoredPatterns.get(x);
			return new ChatCensor(dataFolder, c, cp, whitelist, whitelistPatterns);
		}
		return null;
	}

	public String filter(String chatLine) {
		ArrayList<StringRange> rangesList = new ArrayList<>();
		for (Pattern word : whitelistPatterns) {
			Matcher matcher = word.matcher(chatLine);
			while (matcher.find()) {
				int start = matcher.start();
				int end = matcher.end();
				rangesList.add(new StringRange(start, end));
			}
		}
		StringRange[] ranges = rangesList.toArray(new StringRange[rangesList.size()]);
		ArrayList<StringRange> matchedRanges = new ArrayList<>();
		MappedString[] mappedStrings;
		{
			ArrayList<MappedString> mappedStringsArray = new ArrayList<>();

			mappedStringsArray.add(new MappedString(chatLine));
			mappedStringsArray.add(fix(new MappedString(chatLine)));
			mappedStringsArray.add(fix2(new MappedString(chatLine)));
			mappedStringsArray.add(fix3(new MappedString(chatLine)));

			mappedStrings = mappedStringsArray.toArray(new MappedString[mappedStringsArray.size()]);
		}
		try {
			for (Pattern pattern : censoredPatterns) {
				matchedRanges.clear();
				String regex = pattern.toString();

				for (MappedString mappedString : mappedStrings) {
					int nextFind = 0;
					String str = mappedString.getShortString();
					int lengthOfString = str.length();
					Matcher matcher = pattern.matcher(str);
					while (nextFind < lengthOfString && matcher.find(nextFind)) {
						int startPos = matcher.start();
						int endPos = matcher.end();
						nextFind = startPos + 1;
						matchedRanges.add(mappedString.convertShortRangeToFullRange(new StringRange(startPos, endPos)));
					}
				}

				for (int i = 0; i < matchedRanges.size(); i++) {
					StringRange range = matchedRanges.get(i);
					int startPos = range.start;
					int endPos = range.end;
					boolean containsWhitelistWord = false;
					for (int whitelistIndex = 0; whitelistIndex < ranges.length && !containsWhitelistWord; whitelistIndex++) {
						StringRange whitelistRange = ranges[whitelistIndex];
						if (startPos < whitelistRange.start) {
							if (endPos > whitelistRange.end) {
								containsWhitelistWord = true;
							}
						} else {
							if (startPos < whitelistRange.end) {
								containsWhitelistWord = true;
							}
						}
					}
					if (containsWhitelistWord) {
						continue;
					}
					String stringToCheck = chatLine.substring(startPos, endPos);
					while (stringToCheck.startsWith(" ")) {
						stringToCheck = stringToCheck.substring(1);
						startPos += 1;
					}
					while (stringToCheck.endsWith(" ")) {
						stringToCheck = stringToCheck.substring(0, stringToCheck.length() - 1);
						endPos -= 1;
					}
					if (stringToCheck.contains(" ") && !isOnWordBoundary(chatLine, startPos, endPos)) {
						continue;
					}
					chatLine = chatLine.substring(0, startPos) + star(stringToCheck) + chatLine.substring(endPos);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return chatLine;
	}

	private static boolean isOnWordBoundary(String line, int startPos, int endPos) {
		return (startPos == 0 || !isWordCharacter(line.charAt(startPos-1)))
				&& (endPos == line.length() || !isWordCharacter(line.charAt(endPos)));
	}

	private static boolean isWordCharacter(char c) {
		return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c > (char) 127);
	}

	private static String star(String s) {
		char c[] = s.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] != 32) {
				c[i] = 42;
			}
		}
		return new String(c);
	}

	private static final Pattern DIACRITICS_AND_FRIENDS = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");

	private static String stripDiacritics(String str) {
		str = Normalizer.normalize(str, Normalizer.Form.NFD);
		str = DIACRITICS_AND_FRIENDS.matcher(str).replaceAll("");
		return str;
	}

	private static MappedString stripDiacritics(MappedString s) {
		String str = Normalizer.normalize(s.getFullString(), Normalizer.Form.NFD);
		if (str.length() == s.getFullString().length()) {
			s.setFullString(str);
		}
		s = s.deleteAll(DIACRITICS_AND_FRIENDS);
		return s;
	}

	private static String fix(String s) {
		s = stripDiacritics(s).replaceAll("[ \\.,!#%^&\\-]", "");
		if (s.contains(" ")) {
			s = s.replaceAll(" ", "");
		}
		if (s.contains(".")) {
			s = s.replaceAll("\\.", "");
		}
		if (s.contains(",")) {
			s = s.replaceAll(",", "");
		}
		if (s.contains("!")) {
			s = s.replaceAll("!", "");
		}
		if (s.contains("#")) {
			s = s.replaceAll("#", "");
		}
		if (s.contains("%")) {
			s = s.replaceAll("%", "");
		}
		if (s.contains("^")) {
			s = s.replaceAll("^", "");
		}
		if (s.contains("&")) {
			s = s.replaceAll("&", "");
		}
		if (s.contains("-")) {
			s = s.replaceAll("-", "");
		}
		s = s.toLowerCase();
		return s;
	}

	private static final Pattern fix = Pattern.compile("[ \\.,!#%^&\\-]");

	private static MappedString fix(MappedString s) {
		s = stripDiacritics(s).deleteAll(fix);
		return s;
	}

	private static String fix2(String s) {
		return stripDiacritics(s).replaceAll("[^A-Za-z0-9]", "").toLowerCase();
	}

	private static final Pattern fix2 = Pattern.compile("[^A-Za-z0-9]");

	private static MappedString fix2(MappedString s) {
		return stripDiacritics(s).deleteAll(fix2);
	}

	private static String fix3(String s) {
		return stripDiacritics(s).replaceAll("[^A-Za-z]", "").toLowerCase();
	}

	private static final Pattern fix3 = Pattern.compile("[^A-Za-z]");

	private static MappedString fix3(MappedString s) {
		return stripDiacritics(s).deleteAll(fix3);
	}
}
