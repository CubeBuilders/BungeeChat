package hk.siggi.bungeecord.bungeechat.util;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Util {

	private static final ArrayList<BaseComponent> notPermittedMessages;

	static {
		notPermittedMessages = new ArrayList<>();

		{
			BaseComponent message = new TextComponent("Did you really think that'd work?");
			message.setColor(ChatColor.RED);
			notPermittedMessages.add(message);
		}
		{
			BaseComponent message = new TextComponent("This command is off limits, sorry!");
			message.setColor(ChatColor.RED);
			notPermittedMessages.add(message);
		}
		{
			BaseComponent message = new TextComponent("lolwut? You think you can tell ");
			message.setColor(ChatColor.RED);
			BaseComponent extra = new TextComponent("me");
			extra.setItalic(true);
			extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to apply for a staff spot!")}));
			extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://cubebuilders.net/apply"));
			message.addExtra(extra);
			extra = new TextComponent(" what to do?");
			message.addExtra(extra);
			notPermittedMessages.add(message);
		}
		{
			BaseComponent message = new TextComponent("You know, we do take staff applications. Staff members have access to more commands, such as the command you just tried. ");
			message.setColor(ChatColor.RED);
			BaseComponent extra = new TextComponent("Click here to apply for a staff spot");
			extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to apply for a staff spot!")}));
			extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://cubebuilders.net/apply"));
			notPermittedMessages.add(message);
		}
		{
			BaseComponent message = new TextComponent("You didn't actually think I'd let you do that, did you? Oh, you did... that's so sweet! <3");
			message.setColor(ChatColor.RED);
			notPermittedMessages.add(message);
		}
	}

	public static UUID uuidFromString(String uuid) {
		return UUID.fromString(uuid.replaceAll("-", "").replaceAll("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})", "$1-$2-$3-$4-$5"));
	}

	public static String uuidToString(UUID uuid) {
		return uuid.toString().replaceAll("-", "").toLowerCase();
	}

	public static String getLine(String[] args, int start) {
		StringBuilder b = new StringBuilder();
		for (int i = start; i < args.length; i++) {
			if (i != start) {
				b.append(" ");
			}
			b.append(args[i]);
		}
		return b.toString();
	}

	public static BaseComponent randomNotPermittedMessage() {
		return notPermittedMessages.get((int) Math.floor(Math.random() * notPermittedMessages.size()));
	}

	public static byte[] getURL(String url) {
		try {
			HttpURLConnection connection = (HttpURLConnection)(new URL(url).openConnection());
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			InputStream in = connection.getInputStream();
			byte[] b = new byte[4096];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int c = -1;
			while ((c = in.read(b, 0, b.length)) != -1) {
				out.write(b, 0, c);
			}
			return out.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean parseBool(String arg) {
		arg = arg.toLowerCase();
		return arg.startsWith("t") || arg.startsWith("y") || arg.equals("on") || arg.equals("1");
	}

	private Util() {
	}

	public static byte[] sha1(byte[] data) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] digest = md.digest(data);
			return digest;
		} catch (NoSuchAlgorithmException ex) {
			return null;
		}
	}

	public static byte[] sha1(String data) {
		return sha1(data.getBytes(Charset.forName("UTF-8")));
	}

	public static byte[] hexToBytes(String hex) {
		hex = hex.replaceAll("[^0-9A-Fa-f]", "");
		if (hex.length() % 2 != 0) {
			return null;
		}
		byte[] b = new byte[hex.length() / 2];
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) Integer.parseInt(hex.substring(i * 2, (i * 2) + 2), 16);
		}
		return b;
	}

	public static String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			int b = bytes[i] & 0xff;
			if (b < 0x10) {
				sb.append("0");
			}
			sb.append(Integer.toString(b, 16));
		}
		return sb.toString();
	}

	public static HttpURLConnection post(String req, Properties request) {
		try {
			byte[] and = "&".getBytes();
			byte[] eq = "=".getBytes();
			URL urla = new URL(req);
			URLConnection connection = urla.openConnection();
			HttpURLConnection httpc = (HttpURLConnection) connection;
			httpc.setConnectTimeout(2000);
			httpc.setReadTimeout(2000);
			httpc.setDoOutput(true);
			httpc.setRequestMethod("POST");
			httpc.setRequestProperty("User-Agent", "CubeBuilders (cubebuilders.net)");
			httpc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			OutputStream out = httpc.getOutputStream();
			for (Object keyO : request.keySet()) {
				String key = (String) keyO;
				String val = request.getProperty(key);
				out.write(and);
				out.write(urlEncode(key).getBytes());
				out.write(eq);
				out.write(urlEncode(val).getBytes());
			}
			out.write(and);
			out.flush();
			return httpc;
		} catch (Exception ex) {
			return null;
		}
	}

	public static String urlEncode(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			return null;
		}
	}

	public static byte[] readFullyToArray(InputStream in) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] b = new byte[4096];
		int c;
		while ((c = in.read(b, 0, b.length)) != -1) {
			baos.write(b, 0, c);
		}
		return baos.toByteArray();
	}

	public static int random(int max) {
		return (int) Math.floor(Math.random() * max);
	}

	public static void assertNotNull(Object object, String exceptionMessage) {
		if (object == null) {
			throw new NullPointerException(exceptionMessage);
		}
	}

	public static boolean notNull(Object... objects) {
		for (Object object : objects) {
			if (object == null) {
				return false;
			}
		}
		return true;
	}

	public static void tryClose(Closeable... closeable) {
		for (Closeable c : closeable) {
			try {
				if (c != null) {
					c.close();
				}
			} catch (Exception e) {
			}
		}
	}

	public static String doubleToString(double value, int decimalPlaces) {
		double multiplier = Math.pow(10, decimalPlaces);
		return Double.toString(Math.round(value * multiplier) / multiplier);
	}

	public static <T> Iterable<T> iterable(Iterator<T> iterator) {
		return () -> iterator;
	}

	public static List<UUID> getAllPlayersThatEverJoined() {
		LinkedList<UUID> players = new LinkedList<>();
		try {
			File root = new File(BungeeChat.getInstance().getDataFolder(), "sessionrecord");
			File[] files = root.listFiles();
			for (File f : files) {
				try {
					players.add(uuidFromString(f.getName()));
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
		}
		return players;
	}

	private static Set<String> readList(String file) {
		Set<String> set = new HashSet<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(BungeeChat.getInstance().getDataFolder(), file))))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String trim = line.trim();
				if (trim.isEmpty()) {
					continue;
				}
				set.add(trim.toLowerCase());
			}
		} catch (Exception e) {
		}
		return Collections.unmodifiableSet(set);
	}

	public static Set<String> chatLinkWhitelist = readList("website_whitelist.txt");

	public static Set<String> chatLinkBlacklist = readList("website_blacklist.txt");

	public static boolean checkChatLinkList(String link, Set<String> list) {
		link = link.toLowerCase();
		try {
			int domainStart = 0;
			protoCheck:
			{
				int protoEnd = link.indexOf("://");
				if (protoEnd == -1) {
					break protoCheck;
				}
				String proto = link.substring(0, protoEnd);
				if (!proto.equals("http") && !proto.equals("https")) {
					return false;
				}
				domainStart = protoEnd + 3;
			}
			int domainEnd = link.indexOf("/", domainStart);
			if (domainEnd == -1) {
				domainEnd = link.length();
			}
			String domain = link.substring(domainStart, domainEnd);
			String[] parts = domain.split("\\.");
			String[] check = new String[parts.length];
			for (int i = 0; i < parts.length; i++) {
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j < parts.length - i; j++) {
					if (j != 0) {
						sb.append(".");
					}
					sb.append(parts[i + j]);
				}
				check[i] = sb.toString();
			}
			for (String checkItem : check) {
				if (list.contains(checkItem.toLowerCase())) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean isChatLinkWhitelisted(String link) {
		return checkChatLinkList(link, chatLinkWhitelist);
	}

	public static boolean isChatLinkBlacklisted(String link) {
		return checkChatLinkList(link, chatLinkBlacklist);
	}

	private static final Map<String, String> titles = new ConcurrentHashMap<>();

	/**
	 * Get the title of an online page.
	 *
	 * @param url
	 * @return
	 */
	public static String getTitle(String url) {
		{
			String title = titles.get(url);
			if (title != null) {
				return title.equals(url) ? null : title;
			}
		}
		try {
			URL u = new URL(url);
			URLConnection uc = u.openConnection();
			HttpURLConnection huc = (HttpURLConnection) uc;
			huc.setConnectTimeout(1000);
			huc.setReadTimeout(1000);
			huc.setInstanceFollowRedirects(true);
			huc.setRequestProperty("User-Agent", "CubeBuilders.net - In-game Chat Link Metadata Grabber (hey@siggi.io)");
			InputStream in = huc.getInputStream();
			String contentType = huc.getContentType();
			if (!contentType.toLowerCase().contains("text/htm") || huc.getContentLengthLong() > 524288) {
				huc.disconnect();
				return null;
			}
			StringBuilder sb;
			try (Reader reader = new InputStreamReader(in)) {
				sb = new StringBuilder();
				char[] cc = new char[4096];
				int r;
				while ((r = reader.read(cc, 0, cc.length)) != -1) {
					sb.append(cc, 0, r);
				}
			}
			String str = sb.toString();
			String strLowerCase = str.toLowerCase();
			int titleIdx = strLowerCase.indexOf("<title");
			if (titleIdx == -1) {
				return null;
			}
			int beginTitle = strLowerCase.indexOf(">", titleIdx);
			if (beginTitle == -1) {
				return null;
			}
			beginTitle += 1;
			int endTitle = strLowerCase.indexOf("</title>", beginTitle);
			if (endTitle == -1) {
				return null;
			}
			String title = str.substring(beginTitle, endTitle);
			title = unescapeHtml(title);
			titles.putIfAbsent(url, title);
			return title;
		} catch (Exception e) {
			titles.putIfAbsent(url, url);
		}
		return null;
	}

	public static void copyCreationDate(File from, File to) {
		try {
			Path fromPath = from.toPath();
			Path toPath = to.toPath();
			BasicFileAttributeView fromV = Files.getFileAttributeView(fromPath, BasicFileAttributeView.class);
			BasicFileAttributeView toV = Files.getFileAttributeView(toPath, BasicFileAttributeView.class);
			toV.setTimes(null, null, fromV.readAttributes().creationTime());
		} catch (Exception e) {
		}
	}

	// <editor-fold defaultstate="collapsed" desc="htmlescapes">
	private static final Map<String, String> escapes = new HashMap<>();

	static {
		escapes.put("quot", "\"");
		escapes.put("amp", "&");
		escapes.put("apos", "'");
		escapes.put("lt", "<");
		escapes.put("gt", ">");
		escapes.put("nbsp", " ");
		escapes.put("iexcl", "¡");
		escapes.put("cent", "¢");
		escapes.put("pound", "£");
		escapes.put("curren", "¤");
		escapes.put("yen", "¥");
		escapes.put("brvbar", "¦");
		escapes.put("sect", "§");
		escapes.put("uml", "¨");
		escapes.put("copy", "©");
		escapes.put("ordf", "ª");
		escapes.put("laquo", "«");
		escapes.put("not", "¬");
		escapes.put("reg", "®");
		escapes.put("macr", "¯");
		escapes.put("deg", "°");
		escapes.put("plusmn", "±");
		escapes.put("sup2", "²");
		escapes.put("sup3", "³");
		escapes.put("acute", "´");
		escapes.put("micro", "µ");
		escapes.put("para", "¶");
		escapes.put("middot", "·");
		escapes.put("cedil", "¸");
		escapes.put("sup1", "¹");
		escapes.put("ordm", "º");
		escapes.put("raquo", "»");
		escapes.put("frac14", "¼");
		escapes.put("frac12", "½");
		escapes.put("frac34", "¾");
		escapes.put("iquest", "¿");
		escapes.put("Agrave", "À");
		escapes.put("Aacute", "Á");
		escapes.put("Acirc", "Â");
		escapes.put("Atilde", "Ã");
		escapes.put("Auml", "Ä");
		escapes.put("Aring", "Å");
		escapes.put("AElig", "Æ");
		escapes.put("Ccedil", "Ç");
		escapes.put("Egrave", "È");
		escapes.put("Eacute", "É");
		escapes.put("Ecirc", "Ê");
		escapes.put("Euml", "Ë");
		escapes.put("Igrave", "Ì");
		escapes.put("Iacute", "Í");
		escapes.put("Icirc", "Î");
		escapes.put("Iuml", "Ï");
		escapes.put("ETH", "Ð");
		escapes.put("Ntilde", "Ñ");
		escapes.put("Ograve", "Ò");
		escapes.put("Oacute", "Ó");
		escapes.put("Ocirc", "Ô");
		escapes.put("Otilde", "Õ");
		escapes.put("Ouml", "Ö");
		escapes.put("times", "×");
		escapes.put("Oslash", "Ø");
		escapes.put("Ugrave", "Ù");
		escapes.put("Uacute", "Ú");
		escapes.put("Ucirc", "Û");
		escapes.put("Uuml", "Ü");
		escapes.put("Yacute", "Ý");
		escapes.put("THORN", "Þ");
		escapes.put("szlig", "ß");
		escapes.put("agrave", "à");
		escapes.put("aacute", "á");
		escapes.put("acirc", "â");
		escapes.put("atilde", "ã");
		escapes.put("auml", "ä");
		escapes.put("aring", "å");
		escapes.put("aelig", "æ");
		escapes.put("ccedil", "ç");
		escapes.put("egrave", "è");
		escapes.put("eacute", "é");
		escapes.put("ecirc", "ê");
		escapes.put("euml", "ë");
		escapes.put("igrave", "ì");
		escapes.put("iacute", "í");
		escapes.put("icirc", "î");
		escapes.put("iuml", "ï");
		escapes.put("eth", "ð");
		escapes.put("ntilde", "ñ");
		escapes.put("ograve", "ò");
		escapes.put("oacute", "ó");
		escapes.put("ocirc", "ô");
		escapes.put("otilde", "õ");
		escapes.put("ouml", "ö");
		escapes.put("divide", "÷");
		escapes.put("oslash", "ø");
		escapes.put("ugrave", "ù");
		escapes.put("uacute", "ú");
		escapes.put("ucirc", "û");
		escapes.put("uuml", "ü");
		escapes.put("yacute", "ý");
		escapes.put("thorn", "þ");
		escapes.put("yuml", "ÿ");
		escapes.put("OElig", "Œ");
		escapes.put("oelig", "œ");
		escapes.put("Scaron", "Š");
		escapes.put("scaron", "š");
		escapes.put("Yuml", "Ÿ");
		escapes.put("fnof", "ƒ");
		escapes.put("circ", "ˆ");
		escapes.put("tilde", "˜");
		escapes.put("Alpha", "Α");
		escapes.put("Beta", "Β");
		escapes.put("Gamma", "Γ");
		escapes.put("Delta", "Δ");
		escapes.put("Epsilon", "Ε");
		escapes.put("Zeta", "Ζ");
		escapes.put("Eta", "Η");
		escapes.put("Theta", "Θ");
		escapes.put("Iota", "Ι");
		escapes.put("Kappa", "Κ");
		escapes.put("Lambda", "Λ");
		escapes.put("Mu", "Μ");
		escapes.put("Nu", "Ν");
		escapes.put("Xi", "Ξ");
		escapes.put("Omicron", "Ο");
		escapes.put("Pi", "Π");
		escapes.put("Rho", "Ρ");
		escapes.put("Sigma", "Σ");
		escapes.put("Tau", "Τ");
		escapes.put("Upsilon", "Υ");
		escapes.put("Phi", "Φ");
		escapes.put("Chi", "Χ");
		escapes.put("Psi", "Ψ");
		escapes.put("Omega", "Ω");
		escapes.put("alpha", "α");
		escapes.put("beta", "β");
		escapes.put("gamma", "γ");
		escapes.put("delta", "δ");
		escapes.put("epsilon", "ε");
		escapes.put("zeta", "ζ");
		escapes.put("eta", "η");
		escapes.put("theta", "θ");
		escapes.put("iota", "ι");
		escapes.put("kappa", "κ");
		escapes.put("lambda", "λ");
		escapes.put("mu", "μ");
		escapes.put("nu", "ν");
		escapes.put("xi", "ξ");
		escapes.put("omicron", "ο");
		escapes.put("pi", "π");
		escapes.put("rho", "ρ");
		escapes.put("sigmaf", "ς");
		escapes.put("sigma", "σ");
		escapes.put("tau", "τ");
		escapes.put("upsilon", "υ");
		escapes.put("phi", "φ");
		escapes.put("chi", "χ");
		escapes.put("psi", "ψ");
		escapes.put("omega", "ω");
		escapes.put("thetasym", "ϑ");
		escapes.put("upsih", "ϒ");
		escapes.put("piv", "ϖ");
		escapes.put("ensp", " ");
		escapes.put("emsp", " ");
		escapes.put("mdash", "—");
		escapes.put("lsquo", "‘");
		escapes.put("rsquo", "’");
		escapes.put("sbquo", "‚");
		escapes.put("ldquo", "“");
		escapes.put("rdquo", "”");
		escapes.put("bdquo", "„");
		escapes.put("dagger", "†");
		escapes.put("Dagger", "‡");
		escapes.put("bull", "•");
		escapes.put("hellip", "…");
		escapes.put("permil", "‰");
		escapes.put("prime", "′");
		escapes.put("Prime", "″");
		escapes.put("lsaquo", "‹");
		escapes.put("rsaquo", "›");
		escapes.put("oline", "‾");
		escapes.put("frasl", "⁄");
		escapes.put("euro", "€");
		escapes.put("image", "ℑ");
		escapes.put("weierp", "℘");
		escapes.put("real", "ℜ");
		escapes.put("trade", "™");
		escapes.put("alefsym", "ℵ");
		escapes.put("larr", "←");
		escapes.put("uarr", "↑");
		escapes.put("rarr", "→");
		escapes.put("darr", "↓");
		escapes.put("harr", "↔");
		escapes.put("crarr", "↵");
		escapes.put("lArr", "⇐");
		escapes.put("uArr", "⇑");
		escapes.put("rArr", "⇒");
		escapes.put("dArr", "⇓");
		escapes.put("hArr", "⇔");
		escapes.put("forall", "∀");
		escapes.put("part", "∂");
		escapes.put("exist", "∃");
		escapes.put("empty", "∅");
		escapes.put("nabla", "∇");
		escapes.put("isin", "∈");
		escapes.put("notin", "∉");
		escapes.put("ni", "∋");
		escapes.put("prod", "∏");
		escapes.put("sum", "∑");
		escapes.put("minus", "−");
		escapes.put("lowast", "∗");
		escapes.put("radic", "√");
		escapes.put("prop", "∝");
		escapes.put("infin", "∞");
		escapes.put("ang", "∠");
		escapes.put("and", "∧");
		escapes.put("or", "∨");
		escapes.put("cap", "∩");
		escapes.put("cup", "∪");
		escapes.put("int", "∫");
		escapes.put("there4", "∴");
		escapes.put("sim", "∼");
		escapes.put("cong", "≅");
		escapes.put("asymp", "≈");
		escapes.put("ne", "≠");
		escapes.put("equiv", "≡");
		escapes.put("le", "≤");
		escapes.put("ge", "≥");
		escapes.put("sub", "⊂");
		escapes.put("sup", "⊃");
		escapes.put("nsub", "⊄");
		escapes.put("sube", "⊆");
		escapes.put("supe", "⊇");
		escapes.put("oplus", "⊕");
		escapes.put("otimes", "⊗");
		escapes.put("perp", "⊥");
		escapes.put("sdot", "⋅");
		escapes.put("lceil", "⌈");
		escapes.put("rceil", "⌉");
		escapes.put("lfloor", "⌊");
		escapes.put("rfloor", "⌋");
		escapes.put("loz", "◊");
		escapes.put("spades", "♠");
		escapes.put("clubs", "♣");
		escapes.put("hearts", "♥");
		escapes.put("diams", "♦");
		escapes.put("lang", "〈");
	}

	private static final Pattern htmlEscape = Pattern.compile("&([A-Za-z0-9]{1,}|#(x[A-Fa-f0-9]{1,}|[0-9]{1,}));");

	private static String unescapeHtml(String str) {
		if (str.contains("<br>") || str.contains("<br />")) {
			str = str.replace("\r", "");
			str = str.replace("\n", "");
			str = str.replace("<br>", "\n");
			str = str.replace("<br />", "\n");
		}
		StringBuilder sb = new StringBuilder();
		Matcher matcher = htmlEscape.matcher(str);
		int pos = 0;
		while (matcher.find()) {
			int a = matcher.start();
			int b = matcher.end();
			sb.append(str.substring(pos, a));
			pos = b;
			String entity = matcher.group(1);
			String get = escapes.get(entity);
			if (get != null) {
				sb.append(get);
			} else if (entity.startsWith("#x")) {
				int cc = Integer.parseInt(entity.substring(2), 16);
				char c = (char) cc;
				sb.append(c);
			} else if (entity.startsWith("#")) {
				int cc = Integer.parseInt(entity.substring(1));
				char c = (char) cc;
				sb.append(c);
			} else {
				sb.append(matcher.group(0));
			}
		}
		sb.append(str.substring(pos));
		return sb.toString();
	}
	// </editor-fold>
}
