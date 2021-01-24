package hk.siggi.bungeecord.bungeechat;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gnu.trove.map.TMap;
import hk.siggi.bungeecord.bungeechat.antiddos.NetworkController;
import hk.siggi.bungeecord.bungeechat.chat.ChatController;
import hk.siggi.bungeecord.bungeechat.chat.GroupChat;
import hk.siggi.bungeecord.bungeechat.chat.ServerPublicChatGroupController;
import hk.siggi.bungeecord.bungeechat.chat.censor.ChatCensor;
import hk.siggi.bungeecord.bungeechat.chat.web.WebChat;
import hk.siggi.bungeecord.bungeechat.chatlog.ChatLogLine;
import hk.siggi.bungeecord.bungeechat.chatlog.ChatLogUser;
import hk.siggi.bungeecord.bungeechat.chatlog.FactionChatLog;
import hk.siggi.bungeecord.bungeechat.chatlog.GroupChatLog;
import hk.siggi.bungeecord.bungeechat.chatlog.MailChatLog;
import hk.siggi.bungeecord.bungeechat.chatlog.PrivateChatLog;
import hk.siggi.bungeecord.bungeechat.chatlog.PublicChatLog;
import hk.siggi.bungeecord.bungeechat.chatlog.StaffChatLog;
import hk.siggi.bungeecord.bungeechat.commands.cbtwilio.CommandCBT;
import hk.siggi.bungeecord.bungeechat.commands.cbtwilio.CommandCBTConfirm;
import hk.siggi.bungeecord.bungeechat.commands.config.CommandReIP;
import hk.siggi.bungeecord.bungeechat.commands.cubetokens.CommandGiveCubeTokens;
import hk.siggi.bungeecord.bungeechat.commands.messaging.CommandCapsFilter;
import hk.siggi.bungeecord.bungeechat.commands.messaging.CommandCensor;
import hk.siggi.bungeecord.bungeechat.commands.messaging.CommandChatPrefix;
import hk.siggi.bungeecord.bungeechat.commands.messaging.CommandG;
import hk.siggi.bungeecord.bungeechat.commands.messaging.CommandGroup;
import hk.siggi.bungeecord.bungeechat.commands.messaging.CommandIgnore;
import hk.siggi.bungeecord.bungeechat.commands.messaging.CommandMail;
import hk.siggi.bungeecord.bungeechat.commands.messaging.CommandMsg;
import hk.siggi.bungeecord.bungeechat.commands.messaging.CommandNick;
import hk.siggi.bungeecord.bungeechat.commands.messaging.CommandPub;
import hk.siggi.bungeecord.bungeechat.commands.messaging.CommandRealName;
import hk.siggi.bungeecord.bungeechat.commands.messaging.CommandReply;
import hk.siggi.bungeecord.bungeechat.commands.moderation.CommandAntiSpy;
import hk.siggi.bungeecord.bungeechat.commands.moderation.CommandNoMsg;
import hk.siggi.bungeecord.bungeechat.commands.moderation.CommandNoSpy;
import hk.siggi.bungeecord.bungeechat.commands.moderation.CommandSneaky;
import hk.siggi.bungeecord.bungeechat.commands.moderation.CommandSpy;
import hk.siggi.bungeecord.bungeechat.commands.moderation.CommandStream;
import hk.siggi.bungeecord.bungeechat.commands.moderation.CommandVanish;
import hk.siggi.bungeecord.bungeechat.commands.moderation.CommandVanishOnLogin;
import hk.siggi.bungeecord.bungeechat.commands.punishment.CommandBanName;
import hk.siggi.bungeecord.bungeechat.commands.punishment.CommandCheckHistory;
import hk.siggi.bungeecord.bungeechat.commands.punishment.CommandMCBanExempt;
import hk.siggi.bungeecord.bungeechat.commands.punishment.CommandMute;
import hk.siggi.bungeecord.bungeechat.commands.punishment.CommandOldTemporaryBan;
import hk.siggi.bungeecord.bungeechat.commands.punishment.CommandPermanentBan;
import hk.siggi.bungeecord.bungeechat.commands.punishment.CommandPermanentMute;
import hk.siggi.bungeecord.bungeechat.commands.punishment.CommandPunish;
import hk.siggi.bungeecord.bungeechat.commands.punishment.CommandSilentMute;
import hk.siggi.bungeecord.bungeechat.commands.punishment.CommandStrike;
import hk.siggi.bungeecord.bungeechat.commands.punishment.CommandStrikeHistory;
import hk.siggi.bungeecord.bungeechat.commands.punishment.CommandTemporaryBan;
import hk.siggi.bungeecord.bungeechat.commands.punishment.CommandUnban;
import hk.siggi.bungeecord.bungeechat.commands.punishment.CommandUnmute;
import hk.siggi.bungeecord.bungeechat.commands.punishment.CommandWarn;
import hk.siggi.bungeecord.bungeechat.commands.server.CommandCTReward;
import hk.siggi.bungeecord.bungeechat.commands.server.CommandDontKickMe;
import hk.siggi.bungeecord.bungeechat.commands.server.CommandFakeIP;
import hk.siggi.bungeecord.bungeechat.commands.server.CommandHub;
import hk.siggi.bungeecord.bungeechat.commands.server.CommandImpersonate;
import hk.siggi.bungeecord.bungeechat.commands.server.CommandList;
import hk.siggi.bungeecord.bungeechat.commands.server.CommandNotify;
import hk.siggi.bungeecord.bungeechat.commands.server.CommandRegister;
import hk.siggi.bungeecord.bungeechat.commands.server.CommandReload;
import hk.siggi.bungeecord.bungeechat.commands.server.CommandSecretCode;
import hk.siggi.bungeecord.bungeechat.commands.server.CommandSeen;
import hk.siggi.bungeecord.bungeechat.commands.server.CommandServer;
import hk.siggi.bungeecord.bungeechat.commands.server.CommandSetGroup;
import hk.siggi.bungeecord.bungeechat.commands.server.CommandTestFeature;
import hk.siggi.bungeecord.bungeechat.commands.socialmedia.CommandSocialMedia;
import hk.siggi.bungeecord.bungeechat.commands.socialmedia.CommandVote;
import hk.siggi.bungeecord.bungeechat.event.MineWatchEvent;
import hk.siggi.bungeecord.bungeechat.event.PlayerSpeedingEvent;
import hk.siggi.bungeecord.bungeechat.event.PunishmentIssuedEvent;
import hk.siggi.bungeecord.bungeechat.geolocation.Geolocation;
import hk.siggi.bungeecord.bungeechat.geolocation.Geolocator;
import hk.siggi.bungeecord.bungeechat.httpserver.BungeeResponder;
import hk.siggi.bungeecord.bungeechat.iptoisp.IPtoISP;
import hk.siggi.bungeecord.bungeechat.module.VotifierModule;
import hk.siggi.bungeecord.bungeechat.notifications.Notifications;
import hk.siggi.bungeecord.bungeechat.ontime.OnTime;
import hk.siggi.bungeecord.bungeechat.ontime.OnTimePlayer;
import hk.siggi.bungeecord.bungeechat.ontime.OnTimeSessionRecord;
import hk.siggi.bungeecord.bungeechat.permissionloader.PermissionLoader;
import hk.siggi.bungeecord.bungeechat.player.MCBan;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import hk.siggi.bungeecord.bungeechat.relog.RelogHandler;
import hk.siggi.bungeecord.bungeechat.util.APIUtil;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.processChat;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.unify;
import hk.siggi.bungeecord.bungeechat.util.IteratorAbstract;
import hk.siggi.bungeecord.bungeechat.util.Prowl;
import hk.siggi.bungeecord.bungeechat.util.SimpleIterable;
import hk.siggi.bungeecord.bungeechat.util.TimeUtil;
import hk.siggi.bungeecord.bungeechat.util.Util;
import static hk.siggi.bungeecord.bungeechat.util.Util.doubleToString;
import hk.siggi.cubetokens.CT;
import hk.siggi.iphelper.IP;
import hk.siggi.iphelper.IPv4;
import hk.siggi.iphelper.IPv6;
import io.siggi.http.HTTPServer;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.cubebuilders.discordbot.DiscordBotPlugin;
import net.cubebuilders.discordbot.IDiscordBot;
import net.cubebuilders.user.CBUser;
import net.cubebuilders.user.Punishment;
import net.cubebuilders.user.Punishment.PunishmentAction;
import net.cubebuilders.user.UserData;
import net.cubebuilders.user.UserDonation;
import net.md_5.bungee.BungeeServerInfo;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.conf.Configuration;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.scheduler.BungeeScheduler;

public class BungeeChat extends Plugin implements Listener, VariableServerConnection.Listener {

	private static BungeeChat instance = null;

	public static BungeeChat getInstance() {
		return instance;
	}
	private boolean pEnabled = false;

	private final Map<UUID, PlayerAccount> playerInfo = new HashMap<>();
	private final HashMap<InetSocketAddress, PlayerSession> sessionMap = new HashMap<>();
	private final ReentrantReadWriteLock sessionMapLock = new ReentrantReadWriteLock();
	private final Lock sessionMapReadLock = sessionMapLock.readLock();
	private final Lock sessionMapWriteLock = sessionMapLock.writeLock();

	public static final String NICK_PREFIX = "*";

	public Map<UUID, PlayerAccount> getPlayerInfoMap(CommandImpersonate.ImpersonationLock lock) {
		if (lock == null) {
			throw new NullPointerException();
		}
		return playerInfo;
	}

	public Iterator<PlayerAccount> getAllPlayerInfo() {
		File r = new File(getDataFolder(), "playerdata");
		File[] theFiles = r.listFiles();
		Iterator<File> fileIterator = Arrays.asList(theFiles).iterator();
		return new IteratorAbstract<PlayerAccount>() {
			@Override
			protected PlayerAccount get() {
				while (fileIterator.hasNext()) {
					try {
						File next = fileIterator.next();
						String name = next.getName();
						int pos = name.lastIndexOf(".");
						if (pos == -1) {
							continue;
						}
						String extension = name.substring(pos + 1);
						name = name.substring(0, pos);
						if (extension.equalsIgnoreCase("txt")) {
							if (new File(r, name + ".json").exists()) {
								continue;
							}
						}
						if (extension.equalsIgnoreCase("json") || extension.equalsIgnoreCase("txt")) {
							UUID uuid = Util.uuidFromString(name);
							return getPlayerInfo(uuid);
						}
					} catch (Exception e) {
					}
				}
				return null;
			}
		};
	}

	private final Properties messageReply = new Properties();
	public final Object fsLock = new Object();
	private OnTime onTime;
	private String variableServerAddress = "127.0.0.1";

	public static final UUID console = new UUID(0L, 0L);
	public static final UUID antiCheatDetector = new UUID(0L, 1L);
	public static final UUID cubeBuildersStore = new UUID(0L, 2L);

	public static final String pencil = "\u270E";
	public static final String heart = "\u2764";
	public static final String star6 = "\u273B";
	public static final String star8 = "\u274B";
	public static final String staroutline = "\u269D";
	public static final String starcircled = "\u272A";

	private Geolocator geolocation;

	public static String getSpecialUser(UUID uuid) {
		long most = uuid.getMostSignificantBits();
		long least = uuid.getLeastSignificantBits();
		if (most == 0L && least == 0L) {
			return "Server";
		}
		if (most == 0L && least == 1L) {
			return "Anticheat Detector";
		}
		if (most == 0L && least == 2L) {
			return "CubeBuilders Store";
		}
		return null;
	}

	public ServerInfo addServer(String serverName, String ip, int port) {
		ProxyServer proxy = getProxy();
		Configuration config = (Configuration) proxy.getConfig();
		TMap<String, ServerInfo> servers = config.getServers();
		ServerInfo get = servers.get(serverName);
		if (get != null) {
			return get;
		}
		ServerInfo newServer = proxy.constructServerInfo(serverName, net.md_5.bungee.Util.getAddr(ip + ":" + port), "TEMPORARY_SERVER", false);
		servers.put(serverName, newServer);
		return newServer;
	}

	public void delServer(String serverName) {
		ProxyServer proxy = getProxy();
		Configuration config = (Configuration) proxy.getConfig();
		TMap<String, ServerInfo> servers = config.getServers();
		servers.remove(serverName);
	}

	public void deleteAllTemporaryServers() {
		ProxyServer proxy = getProxy();
		Configuration config = (Configuration) proxy.getConfig();
		TMap<String, ServerInfo> servers = config.getServers();
		ArrayList<String> serversToDelete = new ArrayList<>();
		for (String server : servers.keySet()) {
			ServerInfo inf = servers.get(server);
			if (inf.getMotd().equals("TEMPORARY_SERVER")) {
				serversToDelete.add(server);
			}
		}
		for (String server : serversToDelete) {
			servers.remove(server);
		}
	}

	public static PlayerSession getSession(ProxiedPlayer player) {
		if (player == null) {
			return null;
		}
		BungeeChat bc = getInstance();
		PlayerSession result;
		bc.sessionMapReadLock.lock();
		try {
			UUID uuid = player.getUniqueId();
			result = bc.sessionMap.get(player.getAddress());
		} finally {
			bc.sessionMapReadLock.unlock();
		}
		return result;
	}

	public void reIP(String server, String ip, int port) {
		try {
			BungeeServerInfo info = (BungeeServerInfo) getProxy().getServerInfo(server);
			if (addressField == null) {
				try {
					addressField = BungeeServerInfo.class.getDeclaredField("socketAddress");
				} catch (Exception e) {
					addressField = BungeeServerInfo.class.getDeclaredField("address");
				}
				addressField.setAccessible(true);
				Field modifiers = addressField.getClass().getDeclaredField("modifiers");
				modifiers.setAccessible(true);
				modifiers.setInt(addressField, addressField.getModifiers() & ~Modifier.FINAL);
			}
			addressField.set(info, new InetSocketAddress(ip, port));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateReIP(String server, String ip, int port) {
		try {
			File reIPFile = new File(getDataFolder(), "reip.txt");
			if (!reIPFile.exists()) {
				FileOutputStream out = new FileOutputStream(reIPFile);
				out.write((server + " " + ip + " " + port + "\n").getBytes());
				out.close();
			}
			File reIPFile2 = new File(getDataFolder(), "reip_.txt");
			FileOutputStream out = new FileOutputStream(reIPFile2);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(reIPFile)));
			String line;
			boolean didUpdate = false;
			while ((line = reader.readLine()) != null) {
				try {
					String mod = line.trim();
					String parts[] = mod.split(" ");
					if (parts.length != 3) {
						out.write((line + "\n").getBytes());
					} else {
						if (parts[0].equals(server)) {
							didUpdate = true;
							out.write((server + " " + ip + " " + port + "\n").getBytes());
						} else {
							out.write((line + "\n").getBytes());
						}
					}
				} catch (Exception e) {
					out.write((line + "\n").getBytes());
				}
			}
			if (!didUpdate) {
				out.write((server + " " + ip + " " + port + "\n").getBytes());
			}
			reader.close();
			out.close();
			reIPFile2.renameTo(reIPFile);
		} catch (Exception e) {
		}
	}

	private Field addressField = null;
	private UUIDCache uuidCache = null;
	private PlayerNameHandler playerNameHandler = null;

	public UUIDCache getUUIDCache() {
		return uuidCache;
	}

	public PlayerNameHandler getPlayerNameHandler() {
		return playerNameHandler;
	}

	@Deprecated
	public String getPlayer(UUID uuid) {
		return uuidCache.getNameFromUUID(uuid);
	}

	@Deprecated
	public UUID getUUID(String player) {
		return uuidCache.getUUIDFromName(player);
	}

	public static ProxiedPlayer getProxiedPlayer(UUID uuid) {
		return getInstance().getProxy().getPlayer(uuid);
	}

	private BungeeScheduler bungeeScheduler = null;

	public BungeeScheduler getScheduler() {
		return bungeeScheduler;
	}

	public void reloadConfig() {
		File dataFolder = getDataFolder();
		//Geolocation.startLoading(new File(dataFolder, "geolocation.csv"), new File(dataFolder, "geolocation-ipv6.csv"));
		try {
			new Thread(() -> {
				try {
					Thread.sleep(1000L);
				} catch (Exception e) {
				}
				Geolocator geo = Geolocator.get(new File(dataFolder, "geolocation-ipv6.csv"));
				if (geo != null) {
					geolocation = geo;
					updateGeolocationForOnlinePlayers();
				}
			}).start();
		} catch (Exception e) {
		}
		if (chatCensor == null) {
			chatCensor = new ChatCensor(new File(dataFolder, "chatcensor"));
		} else {
			chatCensor.reloadWords();
		}
		if (ipToIsp == null) {
			ipToIsp = new IPtoISP(new File(dataFolder, "iptoisp.txt"));
		} else {
			ipToIsp.readFromFile();
		}
	}

	public CommandList commandList = null;

	private ChatController chatController = null;

	public ChatController getChatController() {
		return chatController;
	}

	private Notifications notifications = null;

	public Notifications getNotifications() {
		return notifications;
	}

	@Override
	public void onEnable() {
		System.setProperty("http.keepAlive", "false");
		migrateIPLogs();
		deleteAllTemporaryServers();
		reloadConfig();
		instance = this;
		nicknameCache = new NicknameCache();
		bungeeScheduler = new BungeeScheduler();
		chatController = new ChatController(this);
		notifications = new Notifications(this, new File(getDataFolder(), "notifications.json"));
		commandAutoCompleter = new CommandAutoCompleter(this);

		PluginManager pm = getProxy().getPluginManager();

		pm.registerListener(this, this);
		pm.registerListener(this, chatController);
		pm.registerListener(this, notifications);
		pm.registerCommand(this, new CommandMsg(this));
		pm.registerCommand(this, new CommandIgnore(this));
		pm.registerCommand(this, new CommandPub(this));
		pm.registerCommand(this, new CommandGroup(this));
		pm.registerCommand(this, new CommandG(this));
		pm.registerCommand(this, new CommandReply(this));
		pm.registerCommand(this, new CommandMail(this));
		pm.registerCommand(this, new CommandWarn(this));
		pm.registerCommand(this, new CommandMute(this));
		pm.registerCommand(this, new CommandPermanentMute(this));
		pm.registerCommand(this, new CommandSilentMute(this));
		pm.registerCommand(this, new CommandUnmute(this));
		pm.registerCommand(this, new CommandTemporaryBan(this));
		pm.registerCommand(this, new CommandOldTemporaryBan(this));
		pm.registerCommand(this, new CommandPermanentBan(this));
		pm.registerCommand(this, new CommandBanName(this));
		pm.registerCommand(this, new CommandUnban(this));
		pm.registerCommand(this, new CommandPunish(this));
		pm.registerCommand(this, new CommandCheckHistory(this));
		pm.registerCommand(this, new CommandMCBanExempt(this));
		pm.registerCommand(this, new CommandStrike(this));
		pm.registerCommand(this, new CommandStrikeHistory(this));
		pm.registerCommand(this, new CommandCBT(this));
		pm.registerCommand(this, new CommandCBTConfirm(this));
		pm.registerCommand(this, new CommandNoSpy(this));
		pm.registerCommand(this, new CommandAntiSpy(this));
		pm.registerCommand(this, new CommandSpy(this));
		pm.registerCommand(this, new CommandVanish(this));
		pm.registerCommand(this, new CommandVanishOnLogin(this));
		pm.registerCommand(this, new CommandSneaky(this));
		pm.registerCommand(this, new CommandStream(this));
		pm.registerCommand(this, new CommandReIP(this));
		pm.registerCommand(this, new CommandGiveCubeTokens(this));
		pm.registerCommand(this, new CommandHub(this));
		pm.registerCommand(this, commandList = new CommandList(this));
		pm.registerCommand(this, new CommandServer(this));
		pm.registerCommand(this, new CommandSeen(this));
		pm.registerCommand(this, new CommandNoMsg(this));
		pm.registerCommand(this, new CommandNick(this));
		pm.registerCommand(this, new CommandRealName(this));
		pm.registerCommand(this, new CommandCensor(this));
		pm.registerCommand(this, new CommandChatPrefix(this));
		pm.registerCommand(this, new CommandReload(this));
		pm.registerCommand(this, new CommandSetGroup(this));
		pm.registerCommand(this, new CommandDontKickMe(this));
		pm.registerCommand(this, new CommandCTReward(this));
		pm.registerCommand(this, new CommandNotify(this));
		pm.registerCommand(this, new CommandImpersonate(this));
		pm.registerCommand(this, new CommandFakeIP(this));

		pm.registerCommand(this, new CommandSecretCode(this));
		pm.registerCommand(this, new CommandRegister(this));

		pm.registerCommand(this, new CommandVote(this));
		pm.registerCommand(this, new CommandSocialMedia(this));

		pm.registerCommand(this, new CommandCapsFilter(this));

		pm.registerCommand(this, new CommandTestFeature(this));

		RelogHandler rh = new RelogHandler(this);
		getProxy().setReconnectHandler(rh);
		pm.registerListener(this, rh);
		synchronized (variableServerLock) {
			variableServer = new VariableServerConnection(variableServerAddress);
			variableServer.addListener(this);
		}
		pEnabled = true;
		webChat = new WebChat(this);
		uuidCache = new UUIDCache(new File(getDataFolder(), "UUIDs.txt"));
		nicknameCache.startLoadingNicknames();
		playerNameHandler = new PlayerNameHandler(this);
		onTime = new OnTime(this);
		pm.registerListener(this, onTime);
		try {
			httpServer = new HTTPServer(20857);
			httpServer.responderRegistry.register("/", bungeeResponder = new BungeeResponder(this), true, false);
			httpServer.responderRegistry.registerWebSocketHandler("/", bungeeResponder, true, true);
			httpServer.listen();
		} catch (Exception e) {
		}
		try {
			File reIPFile = new File(getDataFolder(), "reip.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(reIPFile)));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				try {
					String[] parts = line.split(" ");
					String server = parts[0];
					String ip = parts[1];
					int port = Integer.parseInt(parts[2]);
					reIP(server, ip, port);
				} catch (Exception e) {
				}
			}
			reader.close();
		} catch (Exception e) {
		}
		try {
			File mcBansApiKeyFile = new File(getDataFolder(), "mcbans_apikey.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mcBansApiKeyFile)));
			String line = reader.readLine();
			if (line != null) {
				mcBansApiKey = line;
			}
			reader.close();
		} catch (Exception e) {
		}
		try {
			new Thread(this::runLoop).start();
		} catch (Exception e) {
		}
		try {
			new Thread(() -> {
				try {
					long now = System.currentTimeMillis();
					long next = now;
					while (pEnabled) {
						while (next > now) {
							Thread.sleep(next - now);
							now = System.currentTimeMillis();
						}
						next += 60000L * 15L;
						try {
							ctLoop();
						} catch (Exception e) {
						}
					}
				} catch (InterruptedException e) {
				}
			}).start();
		} catch (Exception e) {
		}
		permissionLoader = new PermissionLoader(new File(getDataFolder(), "permissions.txt"));
		groupInfo = new GroupInfo(this, new File(getDataFolder(), "groups.txt"));
		try (BufferedReader reader = new BufferedReader(new FileReader(new File(getDataFolder(), "minewatch.txt")))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				mineWatchServers.add(line);
			}
		} catch (Exception e) {
		}
		serverPublicChatGroupController = new ServerPublicChatGroupController(this);
		try {
			Class<VotifierModule> clz = (Class<VotifierModule>) Class.forName("hk.siggi.bungeecord.bungeechat.module.VotifierModuleImpl");
			votifierModule = clz.newInstance();
			votifierModule.setBungeeChat(this);
			pm.registerListener(this, votifierModule);
		} catch (Exception e) {
		}
		File upgradePlayerInfo = new File(getDataFolder(), "upgrade_player_info");
		if (upgradePlayerInfo.exists()) {
			for (PlayerAccount playerAccount : new SimpleIterable<>(getAllPlayerInfo())) {
				UUID playerUUID = playerAccount.getPlayerUUID();
				System.out.println("Upgrading " + playerUUID.toString());
				playerAccount.forceResave();
			}
			upgradePlayerInfo.delete();
		}
		File updatePlayerInfo = new File(getDataFolder(), "update_player_info");
		if (updatePlayerInfo.exists()) {
			boolean updatePhoneNumbers = false;
			Map<UUID, String> phoneNumbers = new HashMap<>();
			try {
				URLConnection urlc = new URL("http://127.0.0.1:8895/twilio/cubebuilders/registereduuids").openConnection();
				BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					int pos = line.indexOf("=");
					if (pos == -1) {
						continue;
					}
					try {
						UUID uuid = UUID.fromString(line.substring(0, pos));
						String number = line.substring(pos + 1);
						phoneNumbers.put(uuid, number);
					} catch (Exception e) {
					}
				}
				updatePhoneNumbers = true;
			} catch (Exception e) {
			}
			updatePlayerInfo.delete();
			for (PlayerAccount acc : new SimpleIterable<>(getAllPlayerInfo())) {
				int tries = 0;
				while (tries < 60) {
					UUID uuid = acc.getPlayerUUID();
					try {
						CBUser user = getUser(uuid);
						acc.updateFromCBUser(user);
						if (updatePhoneNumbers) {
							String savedNumber = acc.getPhoneNumber();
							String phoneNumber = phoneNumbers.get(uuid);
							if (!(phoneNumber == null ? (savedNumber == null) : (phoneNumber.equals(savedNumber)))) {
								acc.setPhoneNumber(phoneNumber);
							}
						}
						System.out.println("Updated " + uuid.toString());
						break;
					} catch (Exception e) {
					}
					System.out.println("Update Failed on " + uuid.toString());
					try {
						Thread.sleep(1000L);
					} catch (Exception e) {
					}
					tries += 1;
				}
			}
		}
		File pullUUIDs = new File(getDataFolder(), "pull_uuids");
		if (pullUUIDs.exists()) {
			pullUUIDs.delete();
			try {
				uuidCache.preventSaving();
				URL url = new URL("http://127.0.0.1:8592/dump");
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()))) {
					String line;
					while ((line = reader.readLine()) != null) {
						try {
							int p = line.indexOf("=");
							if (p == -1) {
								continue;
							}
							UUID uuid = Util.uuidFromString(line.substring(0, p));
							String name = line.substring(p + 1);
							uuidCache.storeToCache(name, uuid);
						} catch (Exception e) {
						}
					}
				}
			} catch (Exception e) {
			} finally {
				uuidCache.resumeSaving();
			}
		}
		File sendSkins = new File(getDataFolder(), "send_skins");
		if (sendSkins.exists()) {
			sendSkins.delete();
			for (PlayerAccount acc : new SimpleIterable<>(getAllPlayerInfo())) {
				String mojangSkin = acc.getMojangSkin();
				String customSkin = acc.getCustomSkin();
				String skinToSend = customSkin == null ? mojangSkin : customSkin;
				sendSkin0(acc.getPlayerUUID(), skinToSend);
			}
		}
	}

	private VotifierModule votifierModule = null;

	private CommandAutoCompleter commandAutoCompleter;
	private PermissionLoader permissionLoader;

	public PermissionLoader getPermissionLoader() {
		return permissionLoader;
	}

	private void runLoop() {
		long now = System.currentTimeMillis();
		long nextTick = now;
		while (pEnabled) {
			now = System.currentTimeMillis();
			if (nextTick > now) {
				try {
					Thread.sleep(nextTick - now);
				} catch (Exception e) {
				}
				continue;
			}
			if (now - nextTick > 1000L) {
				nextTick = now;
			}
			nextTick += 1000L;
			try {
				tick();
			} catch (Exception e) {
			}
		}
	}

	private final Object discordUpdateLock = new Object();
	private long lastDiscordSync = 0L;
	private boolean discordSyncing = false;

	private void tick() {
		sessionMapWriteLock.lock();
		try {
			for (Iterator<PlayerSession> it = sessionMap.values().iterator(); it.hasNext();) {
				PlayerSession session = it.next();
				if (!session.isValid()) {
					it.remove();
				} else {
					try {
						session.tick();
					} catch (Exception e) {
					}
				}
			}
		} finally {
			sessionMapWriteLock.unlock();
		}
		synchronized (discordUpdateLock) {
			if (!discordSyncing && System.currentTimeMillis() - lastDiscordSync > 15000L) {
				discordSyncing = true;
				new Thread(this::syncDiscord).start();
			}
		}
	}

	private void syncDiscord() {
		try {
			IDiscordBot bot = DiscordBotPlugin.getInstance().getBot();
			for (ProxiedPlayer pl : getProxy().getPlayers()) {
				String status = bot.getStatus(pl.getUniqueId());
				PlayerSession session = getSession(pl);
				session.discordStatus = status;
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			synchronized (discordUpdateLock) {
				discordSyncing = false;
				lastDiscordSync = System.currentTimeMillis();
			}
		}
	}

	public double baseCT() {
		int playerCount = getProxy().getOnlineCount();
		return (((double) playerCount) * ((double) 5)) / (double) 20;
	}

	public double perPlayerMultiplier(ProxiedPlayer p) {
		PlayerSession session = getSession(p);
		long currentSessionLength = (System.currentTimeMillis() - session.loginTime);
		long timeInLast2Weeks = session.timeInLast2Weeks + currentSessionLength;

		double hoursInLast2Weeks = ((double) timeInLast2Weeks) / 3600000L;

		return Math.min(2.5, 1 + (hoursInLast2Weeks / 8.0));
	}

	private void ctLoop() {
		Collection<ProxiedPlayer> players = getProxy().getPlayers();
		double baseCT = baseCT();
		long now = System.currentTimeMillis();
		for (ProxiedPlayer p : players) {
			double multiplier = perPlayerMultiplier(p);

			double preFinalCT = baseCT * multiplier;
			int finalCT = (int) Math.round(preFinalCT);

			if (finalCT < 1) {
				finalCT = 1;
				continue;
			}

			CT.get().giveCubeTokens(p.getUniqueId(), finalCT, "Playtime Gift", 60L * 65L * 10000L);

			TextComponent t = new TextComponent("");
			TextComponent youHaveReceived = new TextComponent("You have received ");
			TextComponent numberCT = new TextComponent(finalCT + " CT");
			TextComponent period = new TextComponent(".");

			TextComponent t2 = new TextComponent("");
			TextComponent forBeingOnline = new TextComponent("Be online more and invite friends to get more! ");
			TextComponent qMark = new TextComponent("[?]");

			youHaveReceived.setColor(ChatColor.GOLD);
			numberCT.setColor(ChatColor.AQUA);
			period.setColor(ChatColor.GOLD);
			forBeingOnline.setColor(ChatColor.GOLD);
			qMark.setColor(ChatColor.AQUA);

			TextComponent info = new TextComponent("Click here for information.");

			qMark.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ctreward"));
			qMark.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{info}));

			t.addExtra(youHaveReceived);
			t.addExtra(numberCT);
			t.addExtra(period);
			t2.addExtra(forBeingOnline);
			t2.addExtra(qMark);

			p.sendMessage(t);
			p.sendMessage(t2);
		}
	}
	private String mcBansApiKey = null;

	public NicknameCache getNicknameCache() {
		return nicknameCache;
	}
	private NicknameCache nicknameCache = null;

	private HTTPServer httpServer = null;
	private BungeeResponder bungeeResponder = null;

	public BungeeResponder getBungeeResponder() {
		return bungeeResponder;
	}

	public OnTime getOnTime() {
		return onTime;
	}

	@Override
	public void onDisable() {
		pEnabled = false;
		synchronized (variableServerLock) {
			try {
				variableServer.stop();
			} catch (Exception e) {
			}
			variableServer = null;
		}
		try {
			if (httpServer != null) {
				httpServer.kill();
			}
		} catch (Exception e) {
		}
		closeChatLogOutputStream();
		onTime.serverShuttingDown();
	}

	public PlayerAccount getPlayerInfo(UUID uuid) {
		PlayerAccount info;
		synchronized (playerInfo) {
			info = playerInfo.get(uuid);
			if (info == null) {
				info = new PlayerAccount(uuid);
				ProxiedPlayer player = getProxy().getPlayer(uuid);
				if (player != null) {
					playerInfo.put(uuid, info);
				}
			}
		}
		return info;
	}

	@EventHandler
	public void pluginMessage(PluginMessageEvent event) {
		try {
			if (event.getTag().equals("MC|Brand")
					|| event.getTag().equals("minecraft:brand")) {
				if (event.getSender() instanceof ProxiedPlayer) {
					ProxiedPlayer p = (ProxiedPlayer) event.getSender();
					PlayerSession session = getSession(p);
					byte[] brandBytes = event.getData();
					String brand = null;
					try {
						ByteArrayInputStream bais = new ByteArrayInputStream(brandBytes);
						int v = VarInt.read(bais);
						int a = bais.available();
						if (v == a) {
							byte[] bb = new byte[v];
							bais.read(bb, 0, bb.length);
							brand = new String(bb);
						}
					} catch (Exception e) {
					}
					if (brand == null) {
						brand = new String(brandBytes);
					}
					session.clientBrand = brand;
					if (session.isConfirmedFullClient()) {
						PlayerAccount playerAcc = getPlayerInfo(p.getUniqueId());
						if (playerAcc.getMineChatGiftOnNextLogin()) {
							playerAcc.setMineChatGiftOnNextLogin(false);
							if (!playerAcc.gaveMineChatGift()) {
								playerAcc.setGaveMineChatGift(true);

								TextComponent msg = new TextComponent("");

								TextComponent welcomeBack = new TextComponent("Thanks for coming back on PC! As promised, you would receive a gift for coming back, you've received 500 CubeTokens that you can spend in Factions or Skyblock. To go to the server lobby, type ");
								welcomeBack.setColor(ChatColor.GOLD);

								TextComponent lobbyCommand = new TextComponent("/lobby");
								lobbyCommand.setColor(ChatColor.AQUA);
								lobbyCommand.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lobby"));
								lobbyCommand.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to go to the lobby")}));

								TextComponent toBuyStuff = new TextComponent(". To buy things using CubeTokens, type ");
								toBuyStuff.setColor(ChatColor.GOLD);

								TextComponent cubeTokensCommand = new TextComponent("/cubetokens");
								cubeTokensCommand.setColor(ChatColor.AQUA);
								cubeTokensCommand.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cubetokens"));
								cubeTokensCommand.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to open CubeTokens Store")}));

								TextComponent thanks = new TextComponent(" while you're in a server that has a CubeTokens Store (for example, Factions, or Skyblock)");
								thanks.setColor(ChatColor.GOLD);

								msg.addExtra(welcomeBack);
								msg.addExtra(lobbyCommand);
								msg.addExtra(toBuyStuff);
								msg.addExtra(cubeTokensCommand);
								msg.addExtra(thanks);

								TextComponent blank = new TextComponent("");

								p.sendMessage(blank);
								p.sendMessage(blank);
								p.sendMessage(msg);
								p.sendMessage(blank);
								p.sendMessage(blank);

								CT.get().giveCubeTokens(p.getUniqueId(), 500, "MineChat Gift");
							}
						}
					}
				}
			}
			if (event.getTag().equals("BungeeCord")) {
				byte[] data = event.getData();
				DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
				String subChannel = in.readUTF();
				if (subChannel.equals("GetSecretCode")) {
					Connection receiver = event.getReceiver();
					Connection sender = event.getSender();
					if (receiver instanceof ProxiedPlayer && sender instanceof Server) {
						ProxiedPlayer player = (ProxiedPlayer) receiver;
						Server server = (Server) sender;
						event.setCancelled(true);
						String secretCode = APIUtil.genSecretCode(player.getUniqueId());

						if (secretCode == null) {
							secretCode = "";
						}

						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						DataOutputStream out = new DataOutputStream(baos);
						out.writeUTF("GetSecretCode");
						out.writeUTF(secretCode);
						out.flush();
						byte[] sendData = baos.toByteArray();
						server.sendData("BungeeCord", sendData);
					}
				} else if (subChannel.equals("SetSkin")) {
					Connection receiver = event.getReceiver();
					Connection sender = event.getSender();
					if (receiver instanceof ProxiedPlayer && sender instanceof Server) {
						event.setCancelled(true);
						ProxiedPlayer p = ((ProxiedPlayer) receiver);
						String value = in.readUTF();
						String signature = in.readUTF();
						LoginResult.Property skin = getSkinProperty(p.getPendingConnection(), true);
						skin.setValue(value);
						skin.setSignature(signature);
						PlayerAccount acc = getPlayerInfo(p.getUniqueId());
						if (acc.getMojangSkin().equals(value)) {
							acc.setCustomSkin(null, null);
						} else {
							acc.setCustomSkin(value, signature);
						}
						sendSkin(p.getUniqueId(), value);
					}
				} else if (subChannel.equals("GetSkin")) {
					Connection receiver = event.getReceiver();
					Connection sender = event.getSender();
					if (receiver instanceof ProxiedPlayer && sender instanceof Server) {
						event.setCancelled(true);
						ProxiedPlayer player = (ProxiedPlayer) receiver;
						Server server = (Server) sender;
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						DataOutputStream out = new DataOutputStream(baos);
						out.writeUTF("GetSkin");
						PlayerAccount info = getPlayerInfo(player.getUniqueId());
						if (info.getMojangSkin() != null) {
							out.writeUTF(info.getMojangSkin());
							out.writeUTF(info.getMojangSkinSignature());
						} else {
							out.writeUTF("NO");
						}
						if (info.getCustomSkin() != null) {
							out.writeUTF(info.getCustomSkin());
							out.writeUTF(info.getCustomSkinSignature());
						} else {
							out.writeUTF("NO");
						}
						out.flush();
						byte[] sendData = baos.toByteArray();
						server.sendData("BungeeCord", sendData);
					}
				} else if (subChannel.equals("GetOtherSkin")) {
					Connection receiver = event.getReceiver();
					Connection sender = event.getSender();
					if (receiver instanceof ProxiedPlayer && sender instanceof Server) {
						event.setCancelled(true);
						ProxiedPlayer player = (ProxiedPlayer) receiver;
						Server server = (Server) sender;
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						DataOutputStream out = new DataOutputStream(baos);
						out.writeUTF("GetOtherSkin");
						String usernameToGet = in.readUTF();
						UUID uuid = getUUIDCache().getUUIDFromName(usernameToGet);
						if (uuid == null) {
							out.writeUTF("");
						} else {
							String correctName = getUUIDCache().getNameFromUUID(uuid);
							out.writeUTF(correctName);
							out.writeLong(uuid.getMostSignificantBits());
							out.writeLong(uuid.getLeastSignificantBits());
							PlayerAccount info = getPlayerInfo(uuid);
							if (info == null) {
								out.writeUTF("NO");
								out.writeUTF("NO");
							} else {
								if (info.getMojangSkin() != null) {
									out.writeUTF(info.getMojangSkin());
									out.writeUTF(info.getMojangSkinSignature());
								} else {
									out.writeUTF("NO");
								}
								if (info.getCustomSkin() != null) {
									out.writeUTF(info.getCustomSkin());
									out.writeUTF(info.getCustomSkinSignature());
								} else {
									out.writeUTF("NO");
								}
							}
						}
						out.flush();
						byte[] sendData = baos.toByteArray();
						server.sendData("BungeeCord", sendData);
					}
				} else if (subChannel.equals("Punish")) {
					Connection receiver = event.getReceiver();
					Connection sender = event.getSender();
					if (receiver instanceof ProxiedPlayer && sender instanceof Server) {
						event.setCancelled(true);
						long now = System.currentTimeMillis();
						ProxiedPlayer p = ((ProxiedPlayer) receiver);
						String punishmentType = in.readUTF();
						String offence = in.readUTF();
						long most = in.readLong();
						long least = in.readLong();
						UUID issuerUUID = new UUID(most, least);
						String reason = in.readUTF();
						long length = in.readLong();
						Punishment pl = new Punishment(Punishment.PunishmentAction.fromString(punishmentType), offence, now, now, length, reason, issuerUUID, p.getUniqueId());
						postOffence(pl);
					}
				} else if (subChannel.equals("MineWatch")) {
					Connection receiver = event.getReceiver();
					Connection sender = event.getSender();
					if (receiver instanceof ProxiedPlayer && sender instanceof Server) {
						event.setCancelled(true);
						String worldName = in.readUTF();
						int x = in.readInt();
						int y = in.readInt();
						int z = in.readInt();
						String ore = in.readUTF();
						int count = in.readInt();
						int lightLevel = in.readInt();
						mineWatch((ProxiedPlayer) receiver, ((Server) sender).getInfo(), worldName, x, y, z, ore, count, lightLevel);
					}
				} else if (subChannel.equals("SpeedReport")) {
					Connection receiver = event.getReceiver();
					Connection sender = event.getSender();
					if (receiver instanceof ProxiedPlayer && sender instanceof Server) {
						event.setCancelled(true);
						long now = System.currentTimeMillis();
						ProxiedPlayer player = (ProxiedPlayer) receiver;
						int infoByte = in.readUnsignedByte();
						int speedPotion = in.readUnsignedShort();
						double speed3D = in.readDouble();
						double speedXZ = in.readDouble();
						double speedY = in.readDouble();
						boolean flying = (infoByte & 0x80) != 0;
						boolean gliding = (infoByte & 0x40) != 0;
						int gameMode = infoByte & 0x3F; // 0=survival, 1=creative, 2=adventure, 3=spectator
						PlayerSession session = getSession(player);
						if (gliding || gameMode == 3) {
							session.highSpeedXZ = 0L;
							session.highSpeedY = 0L;
							return;
						}
						double maxSpeedXZ = (flying ? 24.0 : 10.0) * (1.0 + (0.3 * ((double) speedPotion)));
						double maxSpeedY = 11.0;
						boolean speedingXZ = false;
						boolean speedingY = false;
						if (speedXZ > maxSpeedXZ) {
							long timeSince = now - session.highSpeedXZ;
							if (session.highSpeedXZ == 0L) {
								session.highSpeedXZ = now;
							} else if (timeSince > 2000L) {
								speedingXZ = true;
							}
						} else {
							session.highSpeedXZ = 0L;
						}
						if (speedY > maxSpeedY) {
							long timeSince = now - session.highSpeedY;
							if (session.highSpeedY == 0L) {
								session.highSpeedY = now;
							} else if (timeSince > 2000L) {
								speedingY = true;
							}
						} else {
							session.highSpeedY = 0L;
						}
						if ((speedingXZ || speedingY) && now - session.lastSpeedReport >= 10000L) {
							boolean first = session.lastSpeedReport == 0L;
							session.lastSpeedReport = now;
							getProxy().getPluginManager().callEvent(new PlayerSpeedingEvent(player, speed3D, speedXZ, speedY, flying, gliding, gameMode, first));
						}
					}
				} else if (subChannel.equals("ResetAFKTimer")) {
					Connection receiver = event.getReceiver();
					Connection sender = event.getSender();
					if (receiver instanceof ProxiedPlayer && sender instanceof Server) {
						event.setCancelled(true);
						ProxiedPlayer p = ((ProxiedPlayer) receiver);
						resetAFKTimer(p);
					}
				} else if (subChannel.equals("HotbarMessage")) {
					Connection receiver = event.getReceiver();
					Connection sender = event.getSender();
					if (receiver instanceof ProxiedPlayer && sender instanceof Server) {
						event.setCancelled(true);
						ProxiedPlayer p = ((ProxiedPlayer) receiver);
						String message = in.readUTF();
						PlayerSession session = getSession(p);
						if (session.allowHotbarMessage()) {
							session.sendHotbarMessage(new TextComponent(message));
						}
					}
				} else if (subChannel.equals("HotbarMessageRaw")) {
					Connection receiver = event.getReceiver();
					Connection sender = event.getSender();
					if (receiver instanceof ProxiedPlayer && sender instanceof Server) {
						event.setCancelled(true);
						ProxiedPlayer p = ((ProxiedPlayer) receiver);
						String message = in.readUTF();
						PlayerSession session = getSession(p);
						if (session.allowHotbarMessage()) {
							session.sendHotbarMessageRaw(message);
						}
					}
				} else if (subChannel.equals("AddHotbar")) {
					Connection receiver = event.getReceiver();
					Connection sender = event.getSender();
					if (receiver instanceof ProxiedPlayer && sender instanceof Server) {
						event.setCancelled(true);
						ProxiedPlayer p = ((ProxiedPlayer) receiver);
						PlayerSession session = getSession(p);
						String id = in.readUTF();
						String message = in.readUTF();
						session.addHotbar(id, message);
					}
				} else if (subChannel.equals("RemoveHotbar")) {
					Connection receiver = event.getReceiver();
					Connection sender = event.getSender();
					if (receiver instanceof ProxiedPlayer && sender instanceof Server) {
						event.setCancelled(true);
						ProxiedPlayer p = ((ProxiedPlayer) receiver);
						PlayerSession session = getSession(p);
						String id = in.readUTF();
						session.removeHotbar(id);
					}
				} else if (subChannel.equals("PlayerGroups")) {
					Connection receiver = event.getReceiver();
					Connection sender = event.getSender();
					if (receiver instanceof ProxiedPlayer && sender instanceof Server) {
						event.setCancelled(true);
						ProxiedPlayer p = ((ProxiedPlayer) receiver);
						PlayerSession session = getSession(p);
						String serverName = in.readUTF();
						int groupCount = in.readInt();
						List<String> groups = new ArrayList<>(groupCount);
						for (int i = 0; i < groupCount; i++) {
							groups.add(in.readUTF());
						}
						int pluginCount = in.readInt();
						List<String> plugins = new ArrayList<>(groupCount);
						for (int i = 0; i < pluginCount; i++) {
							plugins.add(in.readUTF());
						}
						session.updateBukkitPermissionCache(serverName, groups, plugins);
					}
				} else if (subChannel.equals("IssueOffence")) {
					Connection receiver = event.getReceiver();
					Connection sender = event.getSender();
					if (receiver instanceof ProxiedPlayer && sender instanceof Server) {
						event.setCancelled(true);
						ProxiedPlayer p = ((ProxiedPlayer) receiver);
						String targetString = in.readUTF();
						String offence = in.readUTF();
						UUID target = Util.uuidFromString(targetString);
						issueOffence(p, target, offence);
					}
				} else if (subChannel.equals("ConfirmOffence")) {
					Connection receiver = event.getReceiver();
					Connection sender = event.getSender();
					if (receiver instanceof ProxiedPlayer && sender instanceof Server) {
						event.setCancelled(true);
						ProxiedPlayer p = ((ProxiedPlayer) receiver);
						String targetString = in.readUTF();
						String offence = in.readUTF();
						UUID target = Util.uuidFromString(targetString);
						String type = in.readUTF();
						long length = in.readLong();
						confirmOffence(p, target, offence, type, length);
					}
				} else if (subChannel.equals("PublicChatGroup")) {
					Connection receiver = event.getReceiver();
					Connection sender = event.getSender();
					if (receiver instanceof ProxiedPlayer && sender instanceof Server) {
						event.setCancelled(true);
						ProxiedPlayer p = ((ProxiedPlayer) receiver);
						String chatGroup = in.readUTF();
						if (chatGroup.isEmpty()) {
							chatGroup = null;
						}
						getSession(p).setPublicChatGroup(chatGroup);
					}
				} else if (subChannel.equals("AddPlus")) {
					Connection receiver = event.getReceiver();
					Connection sender = event.getSender();
					if (receiver instanceof ProxiedPlayer && sender instanceof Server) {
						event.setCancelled(true);
						ProxiedPlayer p = ((ProxiedPlayer) receiver);
						String paymentRef = in.readUTF();
						long time = in.readLong();
						addPlus(p.getUniqueId(), paymentRef, time);
					}
				}
			}
		} catch (IOException e) {
			//this will never happen
		}
	}

	public void issueOffence(ProxiedPlayer issuer, UUID target, String offence) {
		boolean isWarning = false;
		boolean isBan = isBannableOffence(offence);
		CBUser user = getUser(target);
		Punishment[] punishments = user.getUserData().getPunishments();
		int matchingPunishmentType = 0;
		int matchingOffence = 0;
		for (Punishment punishment : punishments) {
			if (punishment.getStatus() == Punishment.PunishmentStatus.MISTAKE) {
				continue;
			}
			Punishment.PunishmentAction action = punishment.getAction();
			if (offencesMatch(punishment.getOffence(), offence)) {
				matchingOffence += 1;
			} else if (action == Punishment.PunishmentAction.BAN && isBan) {
				matchingPunishmentType += 1;
			} else if ((action == Punishment.PunishmentAction.WARNING || action == Punishment.PunishmentAction.MUTE) && !isBan) {
				matchingPunishmentType += 1;
			}
		}
		int totalMatching = matchingPunishmentType + matchingOffence;
		OnTimePlayer ot = onTime.getPlayer(target);
		OnTimeSessionRecord[] sessionRecords = ot.getSessionRecords();
		long totalTimeOnline = 0L;
		for (OnTimeSessionRecord record : sessionRecords) {
			totalTimeOnline += record.getTimeLoggedIn();
		}

		long punishmentLength = isBan ? (86400000L * 2L) : (3600000L * 1L);
		if (offence.equals("alt_account")) {
			punishmentLength = -1L;
			isWarning = false;
			isBan = true;
		} else if (offence.equals("spam")) {
			punishmentLength = 0L;
			if (matchingOffence == 0) {
				isWarning = true;
			}
			if (matchingOffence >= 1) {
				punishmentLength = 60000L * 15L; // 15 mins
			}
			if (matchingOffence >= 2) {
				punishmentLength = 3600000L; // 1 hour
			}
			if (matchingOffence >= 3) {
				punishmentLength = 3600000L * 2L; // 2 hours
			}
		} else {
			if (isBan) {
				if (matchingOffence >= 1 || totalMatching >= 2) {
					punishmentLength = 86400000L * 7L; // 7 days
				}
				if (matchingOffence >= 2 || totalMatching >= 3) {
					punishmentLength = 86400000L * 14L; // 14 days
				}
				if (matchingOffence >= 3 || totalMatching >= 4
						|| ((matchingOffence >= 2 || totalMatching >= 3) && broadenOffence(offence).equals("cheating"))) {
					punishmentLength = -1L; // permanent
				}
			} else {
				if (matchingOffence >= 1 || totalMatching >= 2) {
					punishmentLength = 86400000L * 2L; // 2 days
				}
				if (matchingOffence >= 2 || totalMatching >= 3) {
					punishmentLength = 86400000L * 7L; // 7 days
					isBan = true;
				}
				if (matchingOffence >= 3 || totalMatching >= 4) {
					punishmentLength = 86400000L * 14L; // 14 days
					isBan = true;
				}
				if (matchingOffence >= 4 || totalMatching >= 5) {
					punishmentLength = -1L; // permanent
					isBan = true;
				}
			}
		}
		/*long now = System.currentTimeMillis();
		 Punishment punishment = new Punishment(isWarning ? Punishment.PunishmentAction.WARNING : (isBan ? Punishment.PunishmentAction.BAN : Punishment.PunishmentAction.MUTE), offence, now, now, punishmentLength, getReasonForOffence(offence), issuer.getUniqueId(), target);
		 postOffence(punishment);*/
		boolean allowTroll = issuer.hasPermission("hk.siggi.bungeechat.ban");
		boolean allowMute = issuer.hasPermission("hk.siggi.bungeechat.mute");
		boolean allowBan = issuer.hasPermission("hk.siggi.bungeechat.ban");
		getSession(issuer).openPunishmentSetup(target, offence, isWarning ? "warning" : (isBan ? "ban" : "mute"), punishmentLength, allowTroll, allowMute, allowBan);
	}

	private void confirmOffence(ProxiedPlayer issuer, UUID target, String offence, String type, long length) {
		long now = System.currentTimeMillis();
		Punishment.PunishmentAction action = Punishment.PunishmentAction.WARNING;
		if (type.equalsIgnoreCase("mute")) {
			action = Punishment.PunishmentAction.MUTE;
		}
		if (type.equalsIgnoreCase("ban")) {
			action = Punishment.PunishmentAction.BAN;
		}
		Punishment punishment = new Punishment(action, offence, now, now, length, getReasonForOffence(offence), issuer.getUniqueId(), target);
		postOffence(punishment);
	}

	private boolean offencesMatch(String offence1, String offence2) {
		return broadenOffence(offence1).equals(broadenOffence(offence2));
	}

	private String broadenOffence(String offence) {
		switch (offence) {
			case "xray":
			case "kill_aura":
			case "fly":
			case "modded_client":
			case "bug_abuse":
				return "cheating";
		}
		return offence;
	}

	private String getReasonForOffence(String offence) {
		switch (offence) {
			case "advertising":
				return "Advertising other server or website";
			case "language":
				return "Offensive language in chat";
			case "sexual_harassment":
				return "Sexual harassment to other players";
			case "personal_info":
				return "Requesting or giving out personal information";
			case "grief":
				return "Griefing";
			case "spam":
				return "Spamming";
			case "xray":
				return "X-ray";
			case "kill_aura":
				return "Kill aura";
			case "fly":
				return "Flying";
			case "modded_client":
				return "Modified client used";
			case "bug_abuse":
				return "Abusing bugs";
			case "waste_staff_time":
				return "Wasting staff member's time";
			case "misleading":
				return "Misleading other players";
			case "impersonation":
				return "Impersonating staff members";
			case "real_world_trading":
				return "Real world trading";
			case "encouraging_rulebreaking":
				return "Encouraging rule breaking";
			case "alt_account":
				return "Alt Account";
			case "foreign_language":
				return "Speaking foreign languages in public chat";
			case "illegal_activities":
				return "Breaking the law in the real world";
		}
		return "Breaking the rules";
	}

	private boolean isBannableOffence(String offence) {
		return offence.equals("grief")
				|| offence.equals("xray")
				|| offence.equals("kill_aura")
				|| offence.equals("fly")
				|| offence.equals("modded_client")
				|| offence.equals("bug_abuse")
				|| offence.equals("real_world_trading")
				|| offence.equals("alt_account")
				|| offence.equals("illegal_activities");
	}

	public String formatDate(long time) {
		return formatDate(time, null);
	}

	public String formatDate(long time, TimeZone timeZone) {
		if (timeZone == null) {
			timeZone = TimeZone.getTimeZone("America/New_York");
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd h:mm:ss a");
		sdf.setTimeZone(timeZone);
		String formattedString = sdf.format(new Date(time)).replace("AM", "am").replace("PM", "pm");
		return formattedString;
	}

	public void resetAFKTimer(ProxiedPlayer p) {
		PlayerSession session = getSession(p);
		session.afkTime = 0;
	}

	private GroupInfo groupInfo = null;

	public GroupInfo getGroupInfo() {
		return groupInfo;
	}

	private WebChat webChat = null;

	public WebChat getWebChat() {
		return webChat;
	}

	public void addAll(TextComponent base, List<? extends BaseComponent> components) {
		for (BaseComponent component : components) {
			base.addExtra(component);
		}
	}

	public void addEventsToAll(List<? extends BaseComponent> components, ClickEvent click, HoverEvent hover) {
		for (BaseComponent component : components) {
			if (click != null) {
				component.setClickEvent(click);
			}
			if (hover != null) {
				component.setHoverEvent(hover);
			}
		}
	}

	public ChatCensor getChatCensor() {
		return chatCensor;
	}
	private ChatCensor chatCensor = null;
	private IPtoISP ipToIsp = null;

	public ArrayList<BaseComponent> censor(ArrayList<BaseComponent> original, ChatCensor cc) {
		ArrayList<BaseComponent> newComponents = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		for (BaseComponent c : original) {
			if (!(c instanceof TextComponent)) {
				continue;
			}
			TextComponent text = (TextComponent) c;
			builder.append(text.getText());
		}
		String textToCensor = builder.toString();
		String censoredText = censor(textToCensor, cc);
		if (textToCensor.equals(censoredText)) {
			return original;
		}
		int index = 0;
		for (BaseComponent c : original) {
			if (!(c instanceof TextComponent)) {
				continue;
			}
			TextComponent text = (TextComponent) c;
			int length = text.getText().length();
			String censoredTextPart = censoredText.substring(index, index + length);
			index += length;
			TextComponent censoredTextComponent = new TextComponent(censoredTextPart);
			copyFormat(text, censoredTextComponent);
			newComponents.add(censoredTextComponent);
		}
		return newComponents;
	}

	public void copyFormat(TextComponent copyFrom, TextComponent copyTo) {
		copyTo.setBold(copyFrom.isBold());
		copyTo.setClickEvent(copyFrom.getClickEvent());
		copyTo.setColor(copyFrom.getColor());
		copyTo.setHoverEvent(copyFrom.getHoverEvent());
		copyTo.setItalic(copyFrom.isItalic());
		copyTo.setObfuscated(copyFrom.isObfuscated());
		copyTo.setStrikethrough(copyFrom.isStrikethrough());
		copyTo.setUnderlined(copyFrom.isUnderlined());
	}

	public String censor(String text, ChatCensor cc) {
		int[] map = new int[text.length()];
		char[] originalChars = text.toCharArray();
		char[] newChars = new char[originalChars.length];
		for (int i = 0; i < map.length; i++) {
			map[i] = -1;
		}
		int pos = 0;
		for (int i = 0; i < map.length; i++) {
			newChars[pos] = originalChars[i];
			map[pos] = i;
			pos += 1;
		}
		char[] charsToCensor = new char[pos];
		System.arraycopy(newChars, 0, charsToCensor, 0, pos);
		String textToCensor = new String(charsToCensor);
		textToCensor = cc.filter(textToCensor);
		char[] censored = textToCensor.toCharArray();
		for (int i = 0; i < pos; i++) {
			originalChars[map[i]] = censored[i];
		}
		return new String(originalChars);
	}

	private final String[] emptyStringArray = new String[0];

	public static CBUser getUser(UUID uuid) {
		return getUser(uuid, false);
	}

	public static CBUser getUser(UUID uuid, boolean forceUpdate) {
		if (!forceUpdate) {
			ProxiedPlayer p = getProxiedPlayer(uuid);
			if (p != null) {
				PlayerSession session = getSession(p);
				if (session != null) {
					CBUser user = session.user;
					if (user != null) {
						return user;
					}
				}
			}
		}
		return APIUtil.getUser(uuid);
	}

	public void triggerUpdate(UUID uuid, boolean ban, long notifyPunishment) {
		ProxiedPlayer p = getProxiedPlayer(uuid);
		PlayerSession session = p == null ? null : getSession(p);
		CBUser cb = getUser(uuid, true);
		if (cb == null) {
			return;
		}
		if (session != null) {
			session.user = cb;
		}
		PlayerAccount pi = getPlayerInfo(uuid);
		pi.updateFromCBUser(cb);
		if (ban && cb.isBanned()) {
			if (p != null) {
				youAreBanned(p, cb);
			}
			if (notifyPunishment != -1L) {
				Punishment[] punishments = cb.getUserData().getPunishments();
				for (Punishment punishment : punishments) {
					if (punishment.getIssueDate() == notifyPunishment) {
						notifyPosted(punishment);
					}
				}
			}
		} else {
			if (p != null) {
				updateGroup(p);
			}
		}
	}

	public boolean postOffence(Punishment punishment) {
		try {
			String data = punishment.toJson(false);
			URL url = new URL("http://127.0.0.1:2823/api/postoffence");
			URLConnection urlc = url.openConnection();
			HttpURLConnection http = (HttpURLConnection) urlc;
			http.setRequestMethod("POST");
			http.setDoOutput(true);
			http.setRequestProperty("Content-Type", "application/json");
			OutputStream out = http.getOutputStream();
			out.write(data.getBytes("UTF-8"));
			out.flush();
			InputStream in = http.getInputStream();
			in.read();
			notifyPosted(punishment);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void notifyPosted(Punishment punishment) {
		String issuerName = getSpecialUser(punishment.getIssuedBy());
		if (issuerName == null) {
			issuerName = getUUIDCache().getNameFromUUID(punishment.getIssuedBy());
		}
		String receiver = getUUIDCache().getNameFromUUID(punishment.getIssuedTo());
		PunishmentAction punishmentAction = punishment.getAction();
		String punishmentType = punishmentAction.toString().toLowerCase();
		boolean permanent = punishment.getLength() == -1;
		String banLengthAsString = permanent ? null : TimeUtil.timeToString(punishment.getLength());
		String reason = punishment.getReason();

		BaseComponent message = new TextComponent(issuerName + " is issuing a " + (permanent ? "permanent " : "") + punishmentType + " to " + receiver + ((permanent || punishmentAction == PunishmentAction.WARNING) ? "" : (" for " + banLengthAsString)) + ".");
		message.setColor(ChatColor.AQUA);
		BaseComponent messageReason = new TextComponent("Reason: ");
		messageReason.setColor(ChatColor.AQUA);
		BaseComponent extra = new TextComponent(reason);
		extra.setColor(ChatColor.WHITE);
		messageReason.addExtra(extra);

		Collection<ProxiedPlayer> playerCollection = getProxy().getPlayers();
		ProxiedPlayer[] players = playerCollection.toArray(new ProxiedPlayer[playerCollection.size()]);
		for (int i = 0; i < players.length; i++) {
			if (players[i].hasPermission("hk.siggi.bungeechat.punishmentalert")) {
				players[i].sendMessage(message);
				players[i].sendMessage(messageReason);
			}
		}
		getProxy().getPluginManager().callEvent(new PunishmentIssuedEvent(punishment));
	}

	private void updateGroup(final ProxiedPlayer p) {
		String[] groups = p.getGroups().toArray(emptyStringArray);
		p.removeGroups(groups);
		ArrayList<String> newGroups = new ArrayList<String>();
		CBUser user = getSession(p).user;
		UserData userData = user.getUserData();
		String staffRank = userData.staffRank;
		if (staffRank != null) {
			newGroups.add(staffRank);
			if (userData.hiddenStaff) {
				newGroups.add("hiddenstaff");
			}
		}
		UserDonation latestRank = userData.getLatestRank();
		if (latestRank != null) {
			if (latestRank.isActive()) {
				newGroups.add(latestRank.rank);
			}
		}
		if (newGroups.isEmpty()) {
			if (user.getUserData().isMember) {
				newGroups.add("member");
			} else {
				newGroups.add("default");
			}
		}
		p.addGroups(newGroups.toArray(emptyStringArray));

		getSession(p).updateBungeePermissionCache();

		new Thread(() -> {
			List<String> commands = APIUtil.pullCommands(p.getUniqueId());
			if (commands != null) {
				for (String command : commands) {
					command = command.replaceAll("%Name%", p.getName());
					getProxy().getPluginManager().dispatchCommand(getProxy().getConsole(), command);
				}
			}
			sendInfoUpdate(p, p.getServer());
		}).start();
	}

	@EventHandler
	public void preLogin(PreLoginEvent event) {
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		NetworkController controller = NetworkController.get(connection.getAddress().getAddress().getHostAddress());
		String blockMessage = controller.login(name);
		if (blockMessage != null) {
			event.setCancelled(true);
			event.setCancelReason(blockMessage);
		}
		InetSocketAddress address = connection.getAddress();
		ipToIsp.getISP(address.getAddress().getHostAddress());
	}

	@EventHandler
	public void login(PostLoginEvent event) {
		PlayerSession session;
		ProxiedPlayer player = event.getPlayer();
		updateGroup(player);
		UUID uuid = player.getUniqueId();
		sessionMapReadLock.lock();
		try {
			session = sessionMap.get(player.getAddress());
		} finally {
			sessionMapReadLock.unlock();
		}
		if (session == null) {
			player.disconnect("An error has occurred.");
			return;
		}
		getUUIDCache().storeToCache(player.getName(), uuid);
		PlayerAccount a = null;
		synchronized (playerInfo) {
			playerInfo.put(uuid, a = new PlayerAccount(uuid));
		}
		//a.fetchIfNotUpToDate(player.getName());
		youGotMail(player, a);
		youHaventVoted(player, a);
		updateYourEmail(player);
		loginMessage(player);
		InetAddress playerInetAddress = player.getAddress().getAddress();
		Geolocation geolocation = getGeolocation(playerInetAddress.getHostAddress());
		session.geolocation = geolocation;
		logIP(player, uuid, playerInetAddress);
	}

	public void migrateIPLogs() {
		try {
			File ipLoginRecords = new File(getDataFolder(), "ipLoginRecords");
			File[] files = ipLoginRecords.listFiles();
			for (File f : files) {
				String name = f.getName();
				if (f.isDirectory() || !name.endsWith(".txt") || name.startsWith(".")) {
					continue;
				}
				try {
					IP ip = IP.getIP(name.substring(0, name.length() - 4));
					String formatIP = ip.toString();

					String[] pieces = formatIP.toLowerCase().split("\\.");
					String dir = "IPv4"
							+ File.separator + pieces[0] + "." + pieces[1];

					File parentDir = new File(ipLoginRecords, dir);
					if (!parentDir.exists()) {
						parentDir.mkdirs();
					}

					File newFile = new File(dir, formatIP + ".txt");
					if (newFile.exists()) {
						newFile.delete();
					}
					f.renameTo(newFile);
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
		}
	}

	private File getIPLogFile(String ip) {
		File ipLoginRecords = new File(getDataFolder(), "ipLoginRecords");
		if (!ipLoginRecords.exists()) {
			ipLoginRecords.mkdirs();
		}
		File loginRecordFile;
		if (ip.contains(":")) { // IPv6
			IP ipObj = IP.getIP(ip);
			String formatIP = ipObj.toLongString();
			String ipFS = formatIP.replace(":", "-");

			// Even though Siggi's implementation always returns lowercase hexadecimal, just
			// toLowerCase() it anyway in case the implementation is swapped with someone else's
			// that returns uppercase hexadecimal.
			String[] pieces = formatIP.toLowerCase().split(":");

			String dir = "IPv6"
					+ File.separator + pieces[0]
					+ File.separator + pieces[1]
					+ File.separator + pieces[2]
					+ File.separator + pieces[3];

			File parentDir = new File(ipLoginRecords, dir);
			if (!parentDir.exists()) {
				parentDir.mkdirs();
			}

			loginRecordFile = new File(parentDir, ipFS + ".txt");
		} else { // IPv4
			IP ipObj = IP.getIP(ip);
			String formatIP = ipObj.toString();

			String[] pieces = formatIP.toLowerCase().split("\\.");
			String dir = "IPv4"
					+ File.separator + pieces[0] + "." + pieces[1];

			File parentDir = new File(ipLoginRecords, dir);
			if (!parentDir.exists()) {
				parentDir.mkdirs();
			}

			loginRecordFile = new File(parentDir, formatIP + ".txt");
		}
		return loginRecordFile;
	}

	public void logIP(ProxiedPlayer player, UUID uuid, InetAddress playerInetAddress) {
		String ip = playerInetAddress.getHostAddress();
		String uuidStr = uuid.toString().toLowerCase().replaceAll("-", "");
		try {
			File playerLoginRecords = new File(getDataFolder(), "playerLoginRecords");
			if (!playerLoginRecords.exists()) {
				playerLoginRecords.mkdirs();
			}
			File loginRecordFile = new File(playerLoginRecords, uuidStr + ".txt");
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(loginRecordFile, true);
				fos.write((System.currentTimeMillis() + "/" + ip + "\n").getBytes());
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}
				}
			}
		} catch (Exception e) {
		}
		try {
			File loginRecordFile = getIPLogFile(ip);

			boolean foundUser = false;
			if (loginRecordFile.exists()) {
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(loginRecordFile)));
					String line;
					while ((line = reader.readLine()) != null) {
						if (line.equals(uuidStr)) {
							foundUser = true;
							break;
						}
					}
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException e) {
						}
					}
				}
			}
			if (!foundUser) {
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(loginRecordFile, true);
					fos.write((uuidStr + "\n").getBytes());
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
						}
					}
				}
			}
		} catch (Exception e) {
		}
	}

	public Iterable<IP> getIPs(final UUID player) {
		final File f = new File(getDataFolder(), "playerLoginRecords" + File.separator + (player.toString().replace("-", "").toLowerCase()) + ".txt");
		final Iterable<IP> iterable = () -> new Iterator<IP>() {
			private final List<IP> alreadyListed = new LinkedList<>();
			private BufferedReader reader = null;
			private boolean init = false;
			private IP next = null;

			private void init() {
				if (init) {
					return;
				}
				init = true;
				try {
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				} catch (Exception e) {
					return;
				}
				determineNext();
			}

			private void determineNext() {
				next = null;
				while (next == null) {
					try {
						String line;
						try {
							line = reader.readLine();
							if (line == null) {
								try {
									reader.close();
								} catch (Exception e) {
								}
								break;
							}
						} catch (IOException e) {
							try {
								reader.close();
							} catch (Exception e2) {
							}
							break;
						}
						line = line.substring(line.indexOf("/") + 1);
						IP ip = IP.getIP(line);
						if (!alreadyListed.contains(ip)) {
							next = ip;
							alreadyListed.add(ip);
						}
					} catch (Exception e) {
					}
				}
			}

			@Override
			public boolean hasNext() {
				init();
				return next != null;
			}

			@Override
			public IP next() {
				init();
				if (next != null) {
					IP n = next;
					determineNext();
					return n;
				} else {
					throw new NoSuchElementException();
				}
			}
		};
		return iterable;
	}

	public Iterable<UUID> getUUIDs(IP ip) {
		final List<File> fileList = getUUIDsByFiles(ip);
		final Iterable<UUID> iterable = new Iterable<UUID>() {
			@Override
			public Iterator<UUID> iterator() {
				return new Iterator<UUID>() {

					private final List<UUID> alreadyListed = new LinkedList<>();
					private BufferedReader reader = null;
					private boolean init = false;
					private UUID next = null;

					private void init() {
						if (init) {
							return;
						}
						init = true;
						try {
							reader = new BufferedReader(new InputStreamReader(new FileConcatenatedInputStream(fileList)));
						} catch (Exception e) {
							return;
						}
						determineNext();
					}

					private void determineNext() {
						next = null;
						while (next == null) {
							try {
								String line;
								try {
									line = reader.readLine();
									if (line == null) {
										try {
											reader.close();
										} catch (Exception e) {
										}
										break;
									}
								} catch (IOException e) {
									try {
										reader.close();
									} catch (Exception e2) {
									}
									break;
								}
								UUID uuid = Util.uuidFromString(line);
								if (!alreadyListed.contains(uuid)) {
									next = uuid;
									alreadyListed.add(uuid);
								}
							} catch (Exception e) {
							}
						}
					}

					@Override
					public boolean hasNext() {
						init();
						return next != null;
					}

					@Override
					public UUID next() {
						init();
						if (next != null) {
							UUID n = next;
							determineNext();
							return n;
						} else {
							throw new NoSuchElementException();
						}
					}
				};
			}
		};
		return iterable;
	}

	public List<File> getUUIDsByFiles(IP ip) {
		List<File> resultList = new LinkedList<>();
		tryBlock:
		try {
			if (ip instanceof IPv4) {
				String ipStr = ip.toString();
				if (ipStr.contains("/")) {
					ipStr = ipStr.substring(0, ipStr.indexOf("/"));
				}
				String[] ipParts = ipStr.split("\\.");
				if (ip.getBlockSize() == 32) { // exact IP
					File f = new File(getDataFolder(), "ipLoginRecords"
							+ File.separator + "IPv4"
							+ File.separator + ipParts[0] + "." + ipParts[1]
							+ File.separator + ipStr + ".txt");
					resultList.add(f);
					break tryBlock;
				}
				File loginRecordsIPv4 = new File(getDataFolder(), "ipLoginRecords" + File.separator + "IPv4");
				readLoginRecords(loginRecordsIPv4, resultList, ip);
			} else if (ip instanceof IPv6) {
				String ipStr = ip.toLongString();
				if (ipStr.contains("/")) {
					ipStr = ipStr.substring(0, ipStr.indexOf("/"));
				}
				String[] ipParts = ipStr.split(":");
				String ipFS = ipStr.replace(":", "-");
				if (ip.getBlockSize() == 128) { // exact IP
					File f = new File(getDataFolder(), "ipLoginRecords"
							+ File.separator + "IPv6"
							+ File.separator + ipParts[0]
							+ File.separator + ipParts[1]
							+ File.separator + ipParts[2]
							+ File.separator + ipParts[3]
							+ File.separator + ipFS + ".txt");
					resultList.add(f);
					break tryBlock;
				}
				File loginRecordsIPv6 = new File(getDataFolder(), "ipLoginRecords" + File.separator + "IPv6");
				readLoginRecords(loginRecordsIPv6, resultList, ip);
			}
		} catch (Exception e) {
		}
		return resultList;
	}

	private void readLoginRecords(File directory, List<File> resultList, IP subnet) throws IOException {
		IP rootSubnet = null;
		if (subnet instanceof IPv4) {
			rootSubnet = IP.getIP("0/0");
		} else if (subnet instanceof IPv6) {
			rootSubnet = IP.getIP("::/0");
		}
		readLoginRecords(directory, resultList, subnet, rootSubnet);
	}

	private <IPv extends IP> IPv appendSubnet(IPv subnet, String partToAppend) {
		IP newSubnet = subnet;
		if (subnet instanceof IPv4) {
			String[] parts = partToAppend.split("\\.");
			byte[] b = subnet.getBytes();
			int blockSize = subnet.getBlockSize();
			int plus = blockSize / 8;
			for (int i = 0; i < parts.length; i++) {
				b[i + plus] = (byte) Integer.parseInt(parts[i]);
			}
			blockSize += parts.length * 8;
			newSubnet = new IPv4(b, blockSize);
		} else if (subnet instanceof IPv6) {
			String[] parts = partToAppend.split(":");
			byte[] b = subnet.getBytes();
			int blockSize = subnet.getBlockSize();
			int plus = blockSize / 16;
			for (int i = 0; i < parts.length; i++) {
				int n = Integer.parseInt(parts[i], 16);
				int n1 = (n >> 8) & 0xff;
				int n2 = n & 0xff;
				b[(i + plus) * 2] = (byte) n1;
				b[((i + plus) * 2) + 1] = (byte) n2;
			}
			blockSize += parts.length * 16;
			newSubnet = new IPv6(b, blockSize);
		}
		return (IPv) newSubnet;
	}

	private void readLoginRecords(File directory, List<File> resultList, IP subnet, IP thisDirectorySubnet) throws IOException {
		File[] files = directory.listFiles();
		for (File f : files) {
			String name = f.getName();
			if (f.isDirectory()) {
				IP newDirectorySubnet;
				try {
					newDirectorySubnet = appendSubnet(thisDirectorySubnet, name);
				} catch (Exception e) {
					break;
				}
				if (subnet.contains(newDirectorySubnet) || newDirectorySubnet.contains(subnet)) {
					readLoginRecords(f, resultList, subnet, newDirectorySubnet);
				}
			} else if (name.endsWith(".txt")) {
				IP ipAddr;
				try {
					ipAddr = IP.getIP(name.substring(0, name.length() - 4).replace("-", ":"));
				} catch (Exception e) {
					break;
				}
				if (subnet.contains(ipAddr)) {
					resultList.add(f);
				}
			}
		}
	}

	public Geolocation getGeolocation(String ip) {
		return geolocation == null ? null : geolocation.get(ip);
	}

	public Geolocation getGeolocation(ProxiedPlayer player) {
		try {
			return getSession(player).geolocation;
		} catch (Exception e) {
		}
		return null;
	}

	public void setGeolocation(ProxiedPlayer player, Geolocation geolocation) {
		try {
			getSession(player).geolocation = geolocation;
		} catch (Exception e) {
		}
	}

	private void youGotMail(final ProxiedPlayer player, PlayerAccount playerInfo) {
		if (playerInfo.getMail().length > 0) {
			final BaseComponent blankLine = new TextComponent("");
			blankLine.setColor(ChatColor.WHITE);
			final BaseComponent youGotMail = new TextComponent("Hey!!!  You have mail! ");
			youGotMail.setColor(ChatColor.GOLD);
			BaseComponent toRead = new TextComponent("To read, type /mail read");
			toRead.setColor(ChatColor.AQUA);
			toRead.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Or click here to read!")}));
			toRead.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mail read"));
			youGotMail.addExtra(toRead);
			final BaseComponent warningFull;
			BaseComponent inboxIsFull = new TextComponent("WARNING: Your Inbox is full! Read your mail and then type ");
			inboxIsFull.setColor(ChatColor.RED);
			BaseComponent toClear = new TextComponent("/mail clear");
			toClear.setColor(ChatColor.AQUA);
			toClear.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to delete all messages")}));
			toClear.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mail clear"));
			inboxIsFull.addExtra(toClear);
			if (playerInfo.getMail().length >= playerInfo.getMaxMail()) {
				warningFull = inboxIsFull;
			} else {
				warningFull = null;
			}
			getScheduler().schedule(this, new Runnable() {
				@Override
				public void run() {
					player.sendMessage(blankLine);
					player.sendMessage(blankLine);
					player.sendMessage(youGotMail);
					if (warningFull != null) {
						player.sendMessage(warningFull);
					}
					player.sendMessage(blankLine);
					player.sendMessage(blankLine);
				}
			}, 2000, TimeUnit.MILLISECONDS);
		}
	}

	private void youHaventVoted(final ProxiedPlayer player, PlayerAccount playerInfo) {
		if (playerInfo.getLastVoted() < System.currentTimeMillis() - 86400000L) {
			TextComponent blankLine = unify(processChat(null, ""));
			TextComponent youHaventVoted = unify(processChat(null, "&6You haven't voted today! <https://cubebuilders.net/vote><Click here and vote now!>"));
			getScheduler().schedule(this, () -> {
				player.sendMessage(blankLine);
				player.sendMessage(youHaventVoted);
				player.sendMessage(blankLine);
			}, 2250, TimeUnit.MILLISECONDS);
		}
	}

	private void updateYourEmail(ProxiedPlayer player) {
		getScheduler().schedule(this, () -> {
			long now = System.currentTimeMillis();
			PlayerSession session = getSession(player);
			long emailConfirmedDate = session.user.getEmailConfirmedDate();
			if (session.user.isEmailVerified()) {
				if (now - emailConfirmedDate > 86400000L * 90L) {
					session.emailNeedsConfirmation = true;
					TextComponent blankLine = unify(processChat(null, ""));
					player.sendMessage(blankLine);
					player.sendMessage(unify(processChat(null, "&6Hey " + player.getName() + ", is your email still &b" + session.user.getEmail() + "&6?")));
					player.sendMessage(unify(processChat(null, "&6If it is, </register " + session.user.getEmail() + "><click here to dismiss this message>!")));
					player.sendMessage(unify(processChat(null, "&6If it is not, please enter your new email with </register ...></register [email]>")));
					player.sendMessage(blankLine);
				}
			} else if (session.user.getEmail() != null) {
				TextComponent blankLine = unify(processChat(null, ""));
				player.sendMessage(blankLine);
				player.sendMessage(unify(processChat(null, "&6Hey " + player.getName() + ", you haven't confirmed your email yet!")));
				player.sendMessage(unify(processChat(null, "&6The email we have on file is: &b" + session.user.getEmail() + ".")));
				player.sendMessage(unify(processChat(null, "&6If the email is correct and you can't find the confirmation email, try checking your junk mail. If you still can't find the email, </register resend><click here to resend it>!")));
				player.sendMessage(unify(processChat(null, "&6If that email is wrong, enter a new one with </register ...></register [email]>")));
				player.sendMessage(blankLine);
			}
		}, 2500, TimeUnit.MILLISECONDS);
	}

	private void loginMessage(final ProxiedPlayer player) {
		final PlayerSession session = getSession(player);
		String[] msg = new String[]{
			ChatColor.GOLD + "Welcome to CubeBuilders!",
			ChatColor.GOLD + "All activity on CubeBuilders is logged for moderation and security purposes.",
			ChatColor.GOLD + "By playing here, you agree to our Rules and Terms of Service.",
			ChatColor.GOLD + "Players must be at least 13 years old to play on this server.",
			ChatColor.GOLD + "Players under 18 must view our Rules and TOS with a parent or guardian.",
			ChatColor.GOLD + "View our Rules and Terms of Service at any time by typing " + ChatColor.AQUA + "/rules",
			ChatColor.GOLD + "Lack of knowledge of our Rules and TOS is no excuse for breaking them.",
			ChatColor.GOLD + "Thanks for choosing CubeBuilders! <3"
		};
		int t = 5000;
		for (String m : msg) {
			getScheduler().schedule(this, () -> {
				TextComponent mm = new TextComponent("");
				TextComponent mmm = new TextComponent(m);
				mm.addExtra(mmm);
				session.sendHotbarMessage(mm);
			}, t, TimeUnit.MILLISECONDS);
			t += 3000;
		}
	}

	public LoginResult.Property getSkinProperty(PendingConnection connection, boolean add) {
		if (connection instanceof InitialHandler) {
			InitialHandler ih = (InitialHandler) connection;
			LoginResult result = ih.getLoginProfile();
			LoginResult.Property[] properties = result.getProperties();
			for (LoginResult.Property prop : properties) {
				if (prop.getName().equals("textures")) {
					return prop;
				}
			}
			if (add) {
				LoginResult.Property[] newProps = new LoginResult.Property[properties.length];
				for (int i = 0; i < properties.length; i++) {
					newProps[i] = properties[i];
				}
				newProps[properties.length] = new LoginResult.Property("textures", "", null);
				result.setProperties(newProps);
				return newProps[properties.length];
			}
		}
		return null;
	}

	public LoginResult reconstructLoginResultFromCache(UUID user) {
		String username = getUUIDCache().getNameFromUUID(user);
		if (username == null) {
			return null;
		}
		PlayerAccount pi = getPlayerInfo(user);
		String mojangSkin = pi.getMojangSkin();
		String mojangSkinSignature = pi.getMojangSkinSignature();
		LoginResult.Property[] properties;
		if (mojangSkin != null && mojangSkinSignature != null) {
			properties = new LoginResult.Property[1];
			properties[0] = new LoginResult.Property("textures", mojangSkin, mojangSkinSignature);
		} else {
			properties = new LoginResult.Property[0];
		}
		LoginResult lr = new LoginResult(user.toString().replace("-", "").toLowerCase(), username, properties);
		return lr;
	}

	private final List<String> badIPAddresses = new LinkedList<>();

	@EventHandler
	public void login(LoginEvent event) {
		final PendingConnection connection = event.getConnection();
		final UUID uuid = connection.getUniqueId();

		final PlayerSession session = new PlayerSession(connection.getAddress(), uuid);
		sessionMapWriteLock.lock();
		try {
			sessionMap.put(connection.getAddress(), session);
		} finally {
			sessionMapWriteLock.unlock();
		}
		long now = System.currentTimeMillis();
		{
			OnTimeSessionRecord[] sessionRecords = OnTime.getInstance().getPlayer(uuid).getSessionRecords();
			session.ontimeOnLogin = OnTime.getTotalTimeLoggedIn(sessionRecords);
			session.timeInLast2Weeks = OnTime.getTotalTimeLoggedIn(sessionRecords, now - (86400000L * 14L), now);
			session.loginTime = now;
		}

		getUUIDCache().storeToCache(connection.getName(), uuid);
		final PlayerAccount playerInfo = getPlayerInfo(uuid);

		LoginResult.Property skinProperty = getSkinProperty(connection, false);
		if (skinProperty != null) {
			session.skinProperty = skinProperty;
			playerInfo.setMojangSkin(skinProperty.getValue(), skinProperty.getSignature());
			String customSkin = playerInfo.getCustomSkin();
			if (customSkin != null) {
				skinProperty.setValue(customSkin);
				skinProperty.setSignature(playerInfo.getCustomSkinSignature());
			}
			sendSkin(uuid, skinProperty.getValue());
		}

		String userIPAddress = connection.getAddress().getAddress().getHostAddress();
		{
			boolean ipIsVPN = false;
			vpnProxyCheck:
			try {
				File f = getIPLogFile(userIPAddress);
				if (f.exists()) {
					break vpnProxyCheck;
				}
				synchronized (badIPAddresses) {
					if (badIPAddresses.contains(userIPAddress)) {
						ipIsVPN = true;
						break vpnProxyCheck;
					}
				}
				URL url = new URL("http://check.getipintel.net/check.php?ip=" + userIPAddress + "&contact=siggi@siggi.hk&flags=m");
				HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
				urlc.setRequestProperty("User-Agent", "CubeBuilders Server (cubebuilders.net / siggi@siggi.hk)");
				urlc.setConnectTimeout(2000);
				urlc.setReadTimeout(2000);
				BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
				String resultStr = reader.readLine();
				double result = Double.parseDouble(resultStr);
				if (result >= 1) {
					ipIsVPN = true;
					synchronized (badIPAddresses) {
						if (!badIPAddresses.contains(userIPAddress)) {
							badIPAddresses.add(userIPAddress);
						}
					}
				}
			} catch (Exception e) {
			}
			if (ipIsVPN) {
				event.setCancelled(true);
				TextComponent c = new TextComponent("Your network address (" + userIPAddress + ") is blacklisted.");
				connection.disconnect(c);
				return;
			}
		}

		final boolean ipBanned;
		final CBUser user;
		{
			CBUser cb = null;
			try {
				byte[] bytes = null;
				for (int i = 0; i < 5; i++) {
					if (i > 0) {
						try {
							Thread.sleep(2000L);
						} catch (Exception e) {
						}
					}
					bytes = Util.getURL("http://127.0.0.1:2823/api/userlogin?name=" + connection.getName() + "&uuid=" + (uuid.toString().replaceAll("-", "").toLowerCase()) + "&ip=" + userIPAddress);
					if (bytes != null) {
						break;
					}
				}
				if (bytes == null) {
					event.setCancelled(true);
					TextComponent c = new TextComponent("Could not load your profile data. Please try again in 1 minute. If this problem persists, contact Siggi on Discord Siggi#7788");
					connection.disconnect(c);
					return;
				}
				JsonObject result = new JsonParser().parse(new String(bytes, "UTF-8")).getAsJsonObject();
				String status = result.get("status").getAsString();
				if (status.equals("ok")) {
					JsonElement userData = result.get("userdata");
					cb = CBUser.fromJson(new Gson().toJson(userData));
				} else {
					String message = result.get("message").getAsString();
					event.setCancelled(true);
					TextComponent c = new TextComponent(message);
					connection.disconnect(c);
					return;
				}
			} catch (Exception e) {
			}
			if (cb == null) {
				event.setCancelled(true);
				TextComponent c = new TextComponent("Your profile data is corrupted. Contact a staff member.");
				connection.disconnect(c);
				return;
			}
			user = cb;
			boolean isIpBanned = false;
			try {
				byte[] ipBytes = connection.getAddress().getAddress().getAddress();
				IP ip = null;
				if (ipBytes.length == 4) {
					ip = new IPv4(ipBytes);
				} else if (ipBytes.length == 16) {
					ip = new IPv6(ipBytes);
				}
				isIpBanned = APIUtil.isIPBanned(ip.toString());
			} catch (Exception e) {
			}
			ipBanned = isIpBanned;
		}
		session.user = user;
		playerInfo.updateFromCBUser(user);
		if (ipBanned) {
			event.setCancelled(true);
			TextComponent c = new TextComponent("Your network address (" + userIPAddress + ") is blacklisted.");
			connection.disconnect(c);
			return;
		}
		if (user.getUserData().banMessageString != null) {
			TextComponent c = new TextComponent(user.getUserData().banMessageString);
			connection.disconnect(c);
			return;
		}
		if (user.getUserData().permanentlyBanned) {
			event.setCancelled(true);
			// TextComponent c = new TextComponent("You have been suspended from CubeBuilders until 31 Dec 9999.");
			TextComponent c = new TextComponent("You are permanently banned from CubeBuilders. (Cannot be appealed)");
			connection.disconnect(c);
			return;
		}
		{
			String customNameBanMessage = playerInfo.getCustomNameBanMessage();
			if (isNameBanned(connection.getName())) {
				TextComponent c;
				event.setCancelled(true);
				if (customNameBanMessage != null) {
					c = new TextComponent(customNameBanMessage);
				} else {
					c = new TextComponent("Your username (" + connection.getName() + ") is blacklisted. Please visit https://accounts.mojang.com/me and change your name before coming back. Thanks! <3");
				}
				connection.disconnect(c);
				return;
			} else if (customNameBanMessage != null) {
				playerInfo.setCustomNameBanMessage(null);
			}
		}
		if (user.isBanned()) {
			event.setCancelled(true);
			youAreBanned(connection, user);
			return;
		}
		final String ip = event.getConnection().getAddress().getAddress().getHostAddress();
		String ipFS = ip.replace("[", "").replace("]", "").replace(":", "-");
		mcBans(connection, playerInfo, event, session);
		getProxy().getScheduler().runAsync(this, () -> {
			if (mcBansApiKey != null) {
				PlayerAccount pI = playerInfo;
				try {
					String searchUUID = uuid.toString().replaceAll("-", "").toLowerCase();
					if (searchUUID.startsWith("00000000") || !searchUUID.substring(12, 13).equals("4")) {
						return;
					}
					URLConnection urlc = new URL("http://api.mcbans.com/v3/" + mcBansApiKey + "/login/" + searchUUID + "/" + ip + "/4.3.5").openConnection();
					urlc.setConnectTimeout(5000);
					urlc.setReadTimeout(5000);
					InputStream in = urlc.getInputStream();
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte[] b = new byte[4096];
					int c = 0;
					while ((c = in.read(b, 0, b.length)) != -1) {
						out.write(b, 0, c);
					}
					in.close();
					String str = new String(out.toByteArray(), Charset.forName("utf8"));
					String[] parts = str.split(";");
					if (parts.length == 11) {
						String banstatus = parts[0];
						float reputation = Float.parseFloat(parts[2]);
						boolean mcbansMod = parts[4].equalsIgnoreCase("y");

						String otherServerBans[] = parts[7].split(",");
						ArrayList<MCBan> mcBanList = new ArrayList<>();
						for (String string : otherServerBans) {
							if (string.equals("")) {
								continue;
							}
							String[] banInfo = string.split("\\$");
							String reason = banInfo[0];
							String server = banInfo[1];
							String prosecutor = banInfo[2];
							mcBanList.add(new MCBan(uuid, reason, server, prosecutor));
						}
						pI = getPlayerInfo(uuid);
						pI.setMCBans(banstatus.equals("g"), mcbansMod, reputation, mcBanList.toArray(new MCBan[mcBanList.size()]));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(5000L);
				} catch (Exception e) {
				}
				ProxiedPlayer player = getProxiedPlayer(uuid);
				if (player != null) {
					mcBans(player, pI, null, session);
				} else {
					mcBans(connection, pI, null, session);
				}
			}
		});
	}

	private void mcBans(Connection connection, PlayerAccount playerInfo, LoginEvent event, PlayerSession session) {
		if (playerInfo.isMCBansExempt()) {
			return;
		}
		boolean moreThan4Hours = false;
		{
			OnTimePlayer otp = OnTime.getInstance().getPlayer(playerInfo.getPlayerUUID());
			OnTimeSessionRecord[] sessionRecords = otp.getSessionRecords();
			long totalTime = 0L;
			for (OnTimeSessionRecord record : sessionRecords) {
				totalTime += record.getTimeLoggedIn();
				if (totalTime > 3600L * 4L * 1000L) {
					moreThan4Hours = true;
					continue;
				}
			}
		}
		int banCount = playerInfo.getMCBanCount();
		float rep = playerInfo.getMCBansRep();
		if (playerInfo.isMCBansPBanned() || rep < 7.0 || banCount >= 3) {
			if (event != null) {
				event.setCancelled(true);
			}
			TextComponent component = new TextComponent("Entry into CubeBuilders by this account is blocked due to having a bad reputation on other servers. Visit MCBans.com to check your reputation.");
			connection.disconnect(component);
		} else if (!moreThan4Hours && banCount > 0) {
			if (!session.alertedMCBans) {
				session.alertedMCBans = true;
				BaseComponent mcBansAlert = new TextComponent("Warning: " + getUUIDCache().getNameFromUUID(playerInfo.getPlayerUUID()) + " has negative reputation on MCBans.com.");
				mcBansAlert.setColor(ChatColor.AQUA);

				Collection<ProxiedPlayer> playerCollection = getProxy().getPlayers();
				ProxiedPlayer[] players = playerCollection.toArray(new ProxiedPlayer[playerCollection.size()]);
				for (ProxiedPlayer player : players) {
					if (player.hasPermission("hk.siggi.bungeechat.punishmentalert")) {
						player.sendMessage(mcBansAlert);
					}
				}
			}
		}
	}

	@EventHandler
	public void logout(PlayerDisconnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		PlayerSession session = getSession(player);
		session.loggingOut();
		UUID uuid = player.getUniqueId();
		sessionMapWriteLock.lock();
		try {
			sessionMap.remove(player.getAddress());
		} finally {
			sessionMapWriteLock.unlock();
		}
		synchronized (playerInfo) {
			playerInfo.remove(uuid);
		}
		synchronized (messageReply) {
			messageReply.remove(player.getName().toLowerCase());
		}
		syncSiggiIO(player.getUniqueId(), "CubeBuilders", null);
	}

	@EventHandler
	public void chatEvent(ChatEvent event) {
		Connection sender = event.getSender();
		if (!(sender instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer player = (ProxiedPlayer) sender;
		resetAFKTimer(player);
		PlayerSession session = getSession(player);
		if (!session.didMineChatTeleport) {
			session.didMineChatTeleport = true;
			if (session.clientBrand.isEmpty() || session.clientBrand.equalsIgnoreCase("MineChat")) {
				session.isMineChat = true;
				// no client brand: we probably have an old version of MineChat!
				// client brand=MineChat: we definitely have MineChat!
				ServerInfo currentServer = player.getServer().getInfo();
				String currentServerName = currentServer.getName();
				PlayerAccount playerAcc = getPlayerInfo(player.getUniqueId());
				if (session.ontimeOnLogin < 1200000L) { // Total OnTime less than 20 minutes
					if (!playerAcc.gaveMineChatGift()) {
						playerAcc.setMineChatGiftOnNextLogin(true);

						TextComponent msg = new TextComponent("");

						TextComponent heyYou = new TextComponent("Hey! You're new here! ");
						heyYou.setColor(ChatColor.LIGHT_PURPLE);

						TextComponent rememberPlotServers = new TextComponent("Do you remember these servers with stupid plot worlds that only give you a small 64x64 block area to build on? ");
						rememberPlotServers.setColor(ChatColor.YELLOW);

						TextComponent cbIsntLikeThat = new TextComponent("CubeBuilders is not like that! At CubeBuilders, everyone gets their own UNLIMITED superflat world! ");
						cbIsntLikeThat.setColor(ChatColor.LIGHT_PURPLE);

						TextComponent comeBackOnPC = new TextComponent("Join us later on when you return to your PC! Our address is ");
						comeBackOnPC.setColor(ChatColor.GOLD);

						TextComponent ourAddr = new TextComponent("cubebuilders.net");
						ourAddr.setColor(ChatColor.AQUA);

						TextComponent andYoullGetAGift = new TextComponent("! We'll even give you a MineChat exclusive gift so you can get started faster if you prefer playing Factions or Skyblock! SCREENSHOT this now so you remember! <3");
						andYoullGetAGift.setColor(ChatColor.GOLD);

						msg.addExtra(heyYou);
						msg.addExtra(rememberPlotServers);
						msg.addExtra(cbIsntLikeThat);
						msg.addExtra(comeBackOnPC);
						msg.addExtra(ourAddr);
						msg.addExtra(andYoullGetAGift);

						TextComponent blank = new TextComponent("");

						player.sendMessage(blank);
						player.sendMessage(blank);
						player.sendMessage(msg);
						player.sendMessage(blank);
						player.sendMessage(blank);
					}
				}
				if (currentServerName.equals("hub")) {
					ServerInfo inf = getProxy().getServerInfo("creative");
					if (inf != null) {
						player.connect(inf);
					}
				}
			}
		}
		String commandName = event.getMessage().split(" ")[0];
		if (commandName.equalsIgnoreCase("/stop") || commandName.equalsIgnoreCase("/end")) {
			BaseComponent message = new TextComponent("This command can only be used via the console.");
			message.setColor(ChatColor.RED);
			player.sendMessage(message);
			event.setCancelled(true);
			return;
		}
		if ((event.getMessage().startsWith("/bukkit:") || (commandName.startsWith("/") && commandName.contains(":"))) && !player.hasPermission("hk.siggi.bungeechat.bypassbukkitblock")) {
			BaseComponent message = new TextComponent("Stop it! No! You can't hack me! Scram before I tell Siggi!");
			message.setColor(ChatColor.RED);
			player.sendMessage(message);
			event.setCancelled(true);
			return;
		}
		PlayerAccount playerInfo = getPlayerInfo(player.getUniqueId());
		if (event.getMessage().toLowerCase().startsWith("/me ")) {
			String ip = player.getAddress().getAddress().getHostAddress();
			NetworkController controller = NetworkController.get(ip);
			if (!controller.chat(player, event.getMessage())) {
				event.setCancelled(true);
				return;
			} else if (session.user.isMuted()) {
				youAreMuted(player, session.user);
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void switchServer(net.md_5.bungee.api.event.ServerConnectedEvent event) {
		ProxiedPlayer p = event.getPlayer();
		Server server = event.getServer();
		sendInfoUpdate(p, server);
		updateStatus(p, server.getInfo());
		PlayerSession session = getSession(p);
		session.travelTo(server.getInfo());
		session.clearHotbar();
	}

	public void updateStatus(ProxiedPlayer p) {
		Server server = p.getServer();
		if (server != null) {
			updateStatus(p, server.getInfo());
		}
	}

	public void updateStatus(ProxiedPlayer p, ServerInfo server) {
		if (isVanished(p)) {
			server = null;
		}
		PlayerSession session = getSession(p);
		if (session.loggingOut) {
			return;
		}
		session.lastAppStatusUpdate = System.currentTimeMillis();
		String status = null;
		if (server != null) {
			String serverName = server.getName();
			if (serverName.startsWith("hub")) {
				status = "At the lobby";
			} else if (serverName.startsWith("factions")) {
				status = "Playing Factions";
			} else if (serverName.startsWith("personalspace")) {
				status = "Personal Space";
			} else if (serverName.startsWith("creative")) {
				status = "Creative";
			} else if (serverName.startsWith("skins")) {
				status = "Skin Wardrobe";
			} else if (serverName.startsWith("skyblock")) {
				status = "Playing Skyblock";
			} else if (serverName.startsWith("minigames")) {
				status = "At the minigames lobby";
			} else if (serverName.startsWith("colorshuffle")) {
				status = "Playing Color Shuffle";
			} else if (serverName.startsWith("ffapvp")) {
				status = "FFA PvP";
			} else if (serverName.startsWith("mobarena")) {
				status = "Playing Mob Arena";
			} else if (serverName.startsWith("quake")) {
				status = "Playing Quake";
			} else if (serverName.startsWith("splurge")) {
				status = "Playing Splurge";
			}
		}
		syncSiggiIO(p.getUniqueId(), "CubeBuilders", status);
	}

	@EventHandler
	public void playerSpeeding(PlayerSpeedingEvent event) {
		ProxiedPlayer player = event.getPlayer();
		TextComponent speedAlert = new TextComponent("");
		{
			TextComponent extra = new TextComponent("[!!] ");
			extra.setColor(ChatColor.RED);
			speedAlert.addExtra(extra);
		}
		{
			TextComponent extra = new TextComponent(player.getName());
			extra.setColor(ChatColor.AQUA);
			speedAlert.addExtra(extra);
		}
		{
			TextComponent extra = new TextComponent(" is speeding! Speed: ");
			extra.setColor(ChatColor.RED);
			speedAlert.addExtra(extra);
		}
		{
			TextComponent extra = new TextComponent(doubleToString(event.getSpeed3D(), 2) + " bps");
			extra.setColor(ChatColor.AQUA);
			speedAlert.addExtra(extra);
		}
		{
			TextComponent extra = new TextComponent(", Vertical speed: ");
			extra.setColor(ChatColor.RED);
			speedAlert.addExtra(extra);
		}
		{
			TextComponent extra = new TextComponent(doubleToString(event.getSpeedY(), 2) + " bps");
			extra.setColor(ChatColor.AQUA);
			speedAlert.addExtra(extra);
		}
		if (event.isFlying()) {
			TextComponent extra = new TextComponent(", flying");
			extra.setColor(ChatColor.RED);
			speedAlert.addExtra(extra);
		}
		if (event.isGliding()) {
			TextComponent extra = new TextComponent(", gliding");
			extra.setColor(ChatColor.RED);
			speedAlert.addExtra(extra);
		}
		{
			TextComponent extra = new TextComponent(", GameMode: ");
			extra.setColor(ChatColor.RED);
			speedAlert.addExtra(extra);
		}
		{
			TextComponent extra = new TextComponent(Integer.toString(event.getGameMode()));
			extra.setColor(ChatColor.AQUA);
			speedAlert.addExtra(extra);
		}
		{
			TextComponent extra = new TextComponent(".");
			extra.setColor(ChatColor.RED);
			speedAlert.addExtra(extra);
		}
		for (ProxiedPlayer p : getProxy().getPlayers()) {
			if (p.hasPermission("hk.siggi.bungeechat.speedalert")) {
				p.sendMessage(speedAlert);
			}
		}
	}

	public void sendInfoUpdate(ProxiedPlayer p, Server server) {
		try {
			PlayerAccount info = getPlayerInfo(p.getUniqueId());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(baos);
			out.writeUTF("CBInfo");
			Collection<String> groups = p.getGroups();
			String groupsStr = "";
			for (String group : groups) {
				if (!groupsStr.isEmpty()) {
					groupsStr += ",";
				}
				groupsStr += group;
			}
			out.writeUTF(groupsStr);
			String nickname = info.getNickname();
			out.writeUTF(nickname == null ? "" : nickname);
			out.flush();
			byte[] data = baos.toByteArray();
			server.sendData("BungeeCord", data);
		} catch (Exception e) {
		}
	}

	public void youAreMuted(ProxiedPlayer player, CBUser user) {
		long expires = user.getExpiry(Punishment.PunishmentAction.MUTE);
		String muteReason = "null";
		long now = System.currentTimeMillis();
		Punishment[] punishments = user.getUserData().getPunishments();
		long startTime = now;
		for (Punishment p : punishments) {
			if (p.isCancelled()) {
				continue;
			}
			if (p.getAction() == Punishment.PunishmentAction.MUTE) {
				muteReason = p.getReason();
				startTime = p.getStartTime();
			}
		}
		if (expires == -1L) {
			BaseComponent message = new TextComponent("You have been permanently muted for breaking the rules.");
			message.setColor(ChatColor.RED);
			player.sendMessage(message);

			message = new TextComponent("Reason: ");
			message.setColor(ChatColor.RED);
			BaseComponent extra = new TextComponent(muteReason);
			extra.setColor(ChatColor.WHITE);
			message.addExtra(extra);
			player.sendMessage(message);

			/*message = new TextComponent("You can ");
			 message.setColor(ChatColor.RED);
			 extra = new TextComponent("appeal your offence to be unmuted.");
			 extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://cubebuilders.net/index.php/topic,3.0.html"));
			 extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click here to appeal")}));
			 extra.setColor(ChatColor.AQUA);
			 message.addExtra(extra);
			 player.sendMessage(message);*/
		} else {
			BaseComponent message = new TextComponent("You have been temporarily muted for breaking the rules.");
			message.setColor(ChatColor.RED);
			player.sendMessage(message);

			message = new TextComponent("Reason: ");
			message.setColor(ChatColor.RED);
			BaseComponent extra = new TextComponent(muteReason);
			extra.setColor(ChatColor.WHITE);
			message.addExtra(extra);
			player.sendMessage(message);

			if (now - startTime < 500L) {
				now = startTime;
			}
			long timeLeft = expires - now;
			String timeLeftString = TimeUtil.timeToString(timeLeft, 2);
			if (timeLeftString.equals("0 seconds")) {
				timeLeftString = "Your mute will expire shortly.";
			} else {
				timeLeftString = "Your mute will expire on " + formatDate(expires, getSession(player).user.getUserData().getTimeZone()) + " (in " + timeLeftString + ").";
			}

			message = new TextComponent(timeLeftString);
			message.setColor(ChatColor.RED);
			player.sendMessage(message);

			/*message = new TextComponent("You can ");
			 message.setColor(ChatColor.RED);
			 extra = new TextComponent("appeal your offence to be unmuted earlier.");
			 extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://cubebuilders.net/index.php/topic,3.0.html"));
			 extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click here to appeal")}));
			 extra.setColor(ChatColor.AQUA);
			 message.addExtra(extra);
			 player.sendMessage(message);*/
		}
	}

	public void youAreBanned(Connection player, CBUser user) {
		long expires = user.getExpiry(Punishment.PunishmentAction.BAN);
		String banReason = "null";
		long now = System.currentTimeMillis();
		Punishment[] punishments = user.getUserData().getPunishments();
		long startTime = now;
		for (Punishment p : punishments) {
			if (p.isCancelled()) {
				continue;
			}
			if (p.getAction() == Punishment.PunishmentAction.BAN) {
				banReason = p.getReason();
				startTime = p.getStartTime();
			}
		}
		String prefix = "";
		if (player instanceof ProxiedPlayer) {
			getSession((ProxiedPlayer) player).lightningStrike();
			prefix = "Congratulations! You have unlocked an achievement:\nYou have been banned!\n\n";
		}
		if (expires == -1L) {
			BaseComponent message = new TextComponent(prefix + "You have been banned from CubeBuilders permanently.\n");
			message.setColor(ChatColor.RED);

			BaseComponent extra = new TextComponent("Reason: ");
			message.addExtra(extra);
			extra = new TextComponent(banReason + "\n");
			extra.setColor(ChatColor.WHITE);
			message.addExtra(extra);

			extra = new TextComponent("Visit the CubeBuilders website to appeal - https://cubebuilders.net - and then click Offences\n\nReview the game rules and terms of service at these links:\nhttps://cubebuilders.net/rules\nhttps://cubebuilders.net/tos");
			message.addExtra(extra);

			if (!(player instanceof ProxiedPlayer)) {
				String secretCode = APIUtil.genSecretCode(user.getUUID());
				if (secretCode != null) {
					extra = new TextComponent("\n\nCubeBuildersGirl's Secret Code: " + secretCode + "\n\n" + (user.getUserId() > 0 ? "If you forgot your password on the website, you can use the secret code above to reset it." : "You need to register on the website before you can appeal. Use the secret code above on the register page.") + "\n\nA staff member will NEVER ask for this secret code!");
					message.addExtra(extra);
				}
			}

			player.disconnect(message);
		} else {
			/*if (expires == -1) {
			 expires = 253402318799999L;
			 }*/
			if (now > startTime && now - startTime < 10000L) {
				now = startTime;
			}
			long timeLeft = expires - now;
			String timeLeftString = TimeUtil.timeToString(timeLeft, 2);
			if (timeLeftString.equals("0 seconds")) {
				timeLeftString = "shortly.";
			} else {
				timeLeftString = "in " + timeLeftString + ".";
			}

			String dateStr = formatDate(expires, user.getUserData().getTimeZone());
			BaseComponent message = new TextComponent(prefix + "You have been banned from CubeBuilders until " + dateStr + " (" + timeLeftString + ").\n");
			message.setColor(ChatColor.RED);

			BaseComponent extra = new TextComponent("Reason: ");
			message.addExtra(extra);
			extra = new TextComponent(banReason + "\n");
			extra.setColor(ChatColor.WHITE);
			message.addExtra(extra);

			extra = new TextComponent("Visit the CubeBuilders website to appeal - https://cubebuilders.net - and then click Offences\n\nReview the game rules and terms of service at these links:\nhttps://cubebuilders.net/rules\nhttps://cubebuilders.net/tos");
			message.addExtra(extra);

			if (!(player instanceof ProxiedPlayer)) {
				String secretCode = APIUtil.genSecretCode(user.getUUID());
				if (secretCode != null) {
					extra = new TextComponent("\n\nCubeBuildersGirl's Secret Code: " + secretCode + "\n\n" + (user.getUserId() > 0 ? "If you forgot your password on the website, you can use the secret code above to reset it." : "You need to register on the website before you can appeal. Use the secret code above on the register page.") + "\n\nA staff member will NEVER ask for this secret code!");
					message.addExtra(extra);
				}
			}

			player.disconnect(message);
		}
	}

	public String getLastMessage(String sender) {
		synchronized (messageReply) {
			return messageReply.getProperty(sender.toLowerCase());
		}
	}

	public void setLastMessage(String p1, String p2) {
		synchronized (messageReply) {
			messageReply.setProperty(p1.toLowerCase(), p2);
			messageReply.setProperty(p2.toLowerCase(), p1);
		}
	}

	public void text(final UUID player, final String message) {
		new Thread(() -> {
			textSync(player, message);
		}).start();
	}

	public String textSync(UUID player, String message) {
		String username = getUUIDCache().getNameFromUUID(player);
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("http://127.0.0.1:8895/twilio/cubebuilders/sendtext?username=" + URLEncoder.encode(username) + "&uuid=" + URLEncoder.encode(player.toString().toLowerCase().replaceAll("-", "")) + "&message=" + URLEncoder.encode(message)).openConnection().getInputStream()));
			return reader.readLine();
		} catch (Exception e) {
			return "ServiceUnavailable";
		}
	}

	public void prowl(UUID player, String header, String message) {
		String prowlApiKey = getPlayerInfo(player).getProwlApiKey();
		if (prowlApiKey == null) {
			return;
		}
		Prowl.sendNotification(prowlApiKey, "CubeBuilders", header, message);
	}

	private final Object variableServerLock = new Object();
	private VariableServerConnection variableServer = null;
	private final ArrayList<VariableListener> variableListeners = new ArrayList<>();

	@Override
	public void receivedVariable(String variable, String value) {
		if (variable.equalsIgnoreCase("@addserver")) {
			int eqPos = value.indexOf("=");
			if (eqPos != -1) {
				String server = value.substring(0, eqPos).trim();
				String addr = value.substring(eqPos + 1).trim();
				eqPos = addr.indexOf(":");
				if (eqPos != -1) {
					try {
						int port = Integer.parseInt(addr.substring(eqPos + 1).trim());
						addr = addr.substring(0, eqPos).trim();
						addServer(server, addr, port);
					} catch (Exception e) {
					}
				}
			}
		}
		if (variable.equalsIgnoreCase("@delserver")) {
			delServer(value);
		}
		if (variable.equalsIgnoreCase("@servergroup")) {
			int eqPos = value.indexOf("=");
			if (eqPos != -1) {
				String serverGroup = value.substring(0, eqPos).trim();
				String server = value.substring(eqPos + 1).trim();
				try {
					commandList.addAdditionalServer(serverGroup, server);
				} catch (Exception e) {
				}
			}
		}
		if (variable.startsWith("@publicchatgroup.")) {
			String server = variable.substring(17);
			String chatgroup = value;
			if (chatgroup.isEmpty()) {
				chatgroup = null;
			}
			getServerPublicChatGroupController().setGroup(server, chatgroup);
		}
		VariableListener[] listeners;
		synchronized (variableListeners) {
			listeners = variableListeners.toArray(new VariableListener[variableListeners.size()]);
		}
		for (int i = 0; i < listeners.length; i++) {
			try {
				listeners[i].receivedVariable(variable, value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void receivedMessage(String from, byte[] message) {

	}

	public void addVariableListener(VariableListener listener) {
		synchronized (variableListeners) {
			if (!variableListeners.contains(listener)) {
				variableListeners.add(listener);
			}
		}
	}

	public void removeVariableListener(VariableListener listener) {
		synchronized (variableListeners) {
			if (variableListeners.contains(listener)) {
				variableListeners.remove(listener);
			}
		}
	}
	private final Object chatLogLock = new Object();
	public static final long chatLogRotationInterval = 21600000L; // every 6 hours

	public static final long getChatLogNumber(long time) {
		return (time - (time % chatLogRotationInterval)) / chatLogRotationInterval;
	}
	private File chatLogDir = null;
	private long lastChatLogNumber = -1L;
	private File lastChatLogFile = null;
	private OutputStream chatLogOutputStream = null;

	private void closeChatLogOutputStream() {
		try {
			if (chatLogOutputStream != null) {
				chatLogOutputStream.close();
			}
		} catch (Exception e) {
		} finally {
			chatLogOutputStream = null;
			if (lastChatLogFile != null) {
				Util.compressFile(lastChatLogFile);
			}
		}
	}

	public void logChat(final String value) {
		getScheduler().runAsync(this, new Runnable() {
			@Override
			public void run() {
				synchronized (chatLogLock) {
					if (processChatLine(value)) {
						if (chatLogDir == null) {
							chatLogDir = new File(getDataFolder(), "chatlogs");
						}
						if (!chatLogDir.exists()) {
							chatLogDir.mkdirs();
						}
						long now = System.currentTimeMillis();
						long logNumber = getChatLogNumber(now);
						if (chatLogOutputStream == null || logNumber != lastChatLogNumber) {
							if (chatLogOutputStream != null) {
								closeChatLogOutputStream();
							}
							try {
								File f = new File(chatLogDir, logNumber + ".txt");
								chatLogOutputStream = new FileOutputStream(f, true);
								lastChatLogNumber = logNumber;
								lastChatLogFile = f;
							} catch (Exception e) {
							}
						}
						try {
							chatLogOutputStream.write((now + ":" + value + "\n").getBytes());
						} catch (Exception e) {
							e.printStackTrace();
							try {
								chatLogOutputStream.close();
							} catch (Exception e2) {
							} finally {
								chatLogOutputStream = null;
							}
						}
					}
				}
			}
		});
	}

	private boolean processChatLine(String line) {
		ChatLogLine c = ChatLogLine.parseLine(System.currentTimeMillis() + ":" + line);
		if (c == null) {
			return false;
		}
		if (c instanceof PublicChatLog) {
			PublicChatLog cc = (PublicChatLog) c;
			sendPublicSpy(cc.server, cc.sender.username, cc.message);
			return true;
		} else if (c instanceof FactionChatLog) {
			FactionChatLog cc = (FactionChatLog) c;
			ChatLogUser[] witnesses = cc.getWitnesses();
			String[] recipients = new String[witnesses.length];
			for (int i = 0; i < recipients.length; i++) {
				recipients[i] = witnesses[i].username;
			}
			sendFactionSpy(cc.server, cc.sender.username, recipients, cc.message);
			return true;
		} else if (c instanceof PrivateChatLog) {
			PrivateChatLog cc = (PrivateChatLog) c;
			sendPrivateSpy(cc.sender.username, cc.recipient.username, cc.message);
			return true;
		} else if (c instanceof MailChatLog) {
			MailChatLog cc = (MailChatLog) c;
			sendMailSpy(cc.sender.username, cc.recipient.username, cc.message);
			return true;
		} else if (c instanceof GroupChatLog) {
			GroupChatLog cc = (GroupChatLog) c;
			GroupChat gc = chatController.getChat(cc.groupUUID);
			ChatLogUser[] witnesses = cc.getWitnesses();
			String[] recipients = new String[witnesses.length];
			for (int i = 0; i < recipients.length; i++) {
				recipients[i] = witnesses[i].username;
			}
			String chatName = cc.groupName;
			if (gc != null) {
				chatName = gc.getName();
			}
			sendGroupSpy(gc, chatName, cc.groupName, cc.sender.username, recipients, cc.message);
			return true;
		} else if (c instanceof StaffChatLog) {
			StaffChatLog cc = (StaffChatLog) c;
			return true;
		}
		return false;
	}

	public void sendPublicSpy(String serverKind, String from, String line) {
		BaseComponent msg = new TextComponent(">>C ");
		msg.setColor(ChatColor.GRAY);
		BaseComponent extra = new TextComponent(serverKind + ": ");
		extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to join " + serverKind)}));
		extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + serverKind));
		msg.addExtra(extra);
		extra = new TextComponent(from + ": ");
		extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("View Punishment History for " + from)}));
		extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/phistory " + from));
		msg.addExtra(extra);
		extra = new TextComponent(line);
		msg.addExtra(extra);
		ArrayList<ProxiedPlayer> except = new ArrayList<>();
		getProxy().getPlayers().stream().forEach((p) -> {
			try {
				PlayerSession session = getSession(p);
				if (session.getPublicChatGroup().equals(serverKind)) {
					except.add(p);
				}
			} catch (Exception e) {
			}
		});
		sendSpyMessage(msg, except);
	}

	public void sendPrivateSpy(String from, String to, String line) {
		ProxiedPlayer pFrom = getProxy().getPlayer(from);
		ProxiedPlayer pTo = getProxy().getPlayer(to);
		PlayerAccount accountFrom = getPlayerInfo(getUUIDCache().getUUIDFromName(from));
		PlayerAccount accountTo = getPlayerInfo(getUUIDCache().getUUIDFromName(to));
		if ((accountFrom != null && accountFrom.isAntiSpy())
				|| (accountTo != null && accountTo.isAntiSpy())) {
			return;
		}

		ArrayList<ProxiedPlayer> exceptPlayers = new ArrayList<>();
		{
			if (pFrom != null) {
				exceptPlayers.add(pFrom);
			}
			if (pTo != null) {
				exceptPlayers.add(pTo);
			}
		}

		BaseComponent msg = new TextComponent(">>PM ");
		msg.setColor(ChatColor.YELLOW);
		BaseComponent extra = new TextComponent(from + "->");
		extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("View Punishment History for " + from)}));
		extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/phistory " + from));
		msg.addExtra(extra);
		extra = new TextComponent(to + ": ");
		extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("View Punishment History for " + to)}));
		extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/phistory " + to));
		msg.addExtra(extra);
		extra = new TextComponent(line);
		extra.setColor(ChatColor.WHITE);
		msg.addExtra(extra);
		sendSpyMessage(msg, exceptPlayers);
	}

	public void sendMailSpy(String from, String to, String line) {
		ProxiedPlayer pFrom = getProxy().getPlayer(from);
		ProxiedPlayer pTo = getProxy().getPlayer(to);
		PlayerAccount accountFrom = getPlayerInfo(getUUIDCache().getUUIDFromName(from));
		PlayerAccount accountTo = getPlayerInfo(getUUIDCache().getUUIDFromName(to));
		if ((accountFrom != null && accountFrom.isAntiSpy())
				|| (accountTo != null && accountTo.isAntiSpy())) {
			return;
		}

		ArrayList<ProxiedPlayer> exceptPlayers = new ArrayList<>();
		{
			if (pFrom != null) {
				exceptPlayers.add(pFrom);
			}
			if (pTo != null) {
				exceptPlayers.add(pTo);
			}
		}

		BaseComponent msg = new TextComponent(">>M ");
		msg.setColor(ChatColor.YELLOW);
		BaseComponent extra = new TextComponent(from + "->");
		extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("View Punishment History for " + from)}));
		extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/phistory " + from));
		msg.addExtra(extra);
		extra = new TextComponent(to + ": ");
		extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("View Punishment History for " + to)}));
		extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/phistory " + to));
		msg.addExtra(extra);
		extra = new TextComponent(line);
		extra.setColor(ChatColor.WHITE);
		msg.addExtra(extra);
		sendSpyMessage(msg, exceptPlayers);
	}

	public void sendGroupSpy(GroupChat gc, String chatName, String canonicalName, String from, String[] to, String line) {
		ArrayList<ProxiedPlayer> exceptPlayers = new ArrayList<>();
		{
			for (UUID uuid : gc.getUsers()) {
				ProxiedPlayer p = getProxiedPlayer(uuid);
				if (p != null) {
					exceptPlayers.add(p);
				}
			}
		}

		BaseComponent msg = new TextComponent(">>GC ");
		msg.setColor(ChatColor.GRAY);
		BaseComponent extra = new TextComponent(canonicalName + ": ");
		extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to join " + canonicalName)}));
		extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/group unmute " + canonicalName));
		msg.addExtra(extra);
		extra = new TextComponent(from + ": ");
		extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("View Punishment History for " + from)}));
		extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/phistory " + from));
		msg.addExtra(extra);
		extra = new TextComponent(line);
		String seenBy;
		if (to.length == 0) {
			seenBy = "This message was not seen by anyone.";
		} else {
			seenBy = "This message was seen by: ";
			for (int i = 0; i < to.length; i++) {
				seenBy += (i == 0 ? "" : ", ") + to[i];
			}
		}
		extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(seenBy)}));
		msg.addExtra(extra);
		sendSpyMessage(msg, exceptPlayers);
	}

	public void sendFactionSpy(String serverKind, String from, String[] to, String line) {
		ArrayList<ProxiedPlayer> exceptPlayers = new ArrayList<>();
		{
			ProxiedPlayer fr = getProxy().getPlayer(from);
			if (fr != null) {
				exceptPlayers.add(fr);
			}
			for (String s : to) {
				if (s == null) {
					continue;
				}
				fr = getProxy().getPlayer(s);
				if (fr != null) {
					exceptPlayers.add(fr);
				}
			}
		}

		BaseComponent msg = new TextComponent(">>FC ");
		msg.setColor(ChatColor.GRAY);
		BaseComponent extra = new TextComponent(serverKind + ": ");
		extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to join " + serverKind)}));
		extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + serverKind));
		msg.addExtra(extra);
		extra = new TextComponent(from + ": ");
		extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("View Punishment History for " + from)}));
		extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/phistory " + from));
		msg.addExtra(extra);
		extra = new TextComponent(line);
		String seenBy;
		if (to.length == 0) {
			seenBy = "This message was not seen by anyone.";
		} else {
			seenBy = "This message was seen by: ";
			for (int i = 0; i < to.length; i++) {
				seenBy += (i == 0 ? "" : ", ") + to[i];
			}
		}
		extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent(seenBy)}));
		msg.addExtra(extra);
		sendSpyMessage(msg, exceptPlayers);
	}

	public void sendSpyMessage(BaseComponent message, List<ProxiedPlayer> exceptPlayers) {
		for (ProxiedPlayer p : getProxy().getPlayers()) {
			if (exceptPlayers.contains(p)) {
				continue;
			}
			if (p.hasPermission("hk.siggi.bungeechat.spy")) {
				boolean iAmASpy;
				PlayerAccount pp = getPlayerInfo(p.getUniqueId());
				iAmASpy = !pp.isNoSpy();
				if (iAmASpy) {
					p.sendMessage(message);
				}
			}
		}
	}

	private void connectVariableServer() {
		variableServer = new VariableServerConnection(variableServerAddress);
		variableServer.addListener(this);
		variableServer.setName(System.getProperty("cubebuildersserver"));
	}

	@Override
	public void disconnectedVariableServer(VariableServerConnection vServer) {
		if (vServer == null) {
			return;
		}
		synchronized (variableServerLock) {
			if (variableServer != vServer) {
				return;
			}
			variableServer = null;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(2000L);
					} catch (Exception e) {
					}
					if (!pEnabled) {
						return;
					}
					connectVariableServer();
				}
			}).start();
		}
	}

	public boolean isVanished(ProxiedPlayer player) {
		PlayerAccount info = getPlayerInfo(player.getUniqueId());
		return info.isVanished();
	}

	public void setVanished(ProxiedPlayer player, boolean vanish) {
		long now = System.currentTimeMillis();
		StringBuilder vanishString = new StringBuilder("");
		PlayerAccount info = getPlayerInfo(player.getUniqueId());
		info.setVanished(vanish);
		PlayerSession session = getSession(player);
		if (vanish) {
			if (now - session.loginTime < 60000L) {
				if (info.getFakeLastOnline() == 0L) {
					info.setFakeLastOnline(OnTime.getInstance().getPlayer(player.getUniqueId()).getLastOnline(true));
				}
			} else {
				if (info.getFakeLastOnline() == 0L) {
					info.setFakeLastOnline(now);
				}
			}
		} else {
			info.setFakeLastOnline(0L);
		}
		boolean added = false;
		for (ProxiedPlayer pl : getProxy().getPlayers()) {
			PlayerAccount info2 = getPlayerInfo(pl.getUniqueId());
			if (!info2.isVanished()) {
				continue;
			}
			if (added) {
				vanishString.append(",");
			}
			added = true;
			vanishString.append(pl.getName());
		}
		String vList = vanishString.toString();
		variableServer.updateVariable("vanishlist", vList);
		if (vanish) {
			syncSiggiIO(player.getUniqueId(), "CubeBuilders", null);
		} else {
			updateStatus(player);
		}
	}

	public void fakeJoin(String player) {
		ProxiedPlayer pp = getProxy().getPlayer(player);
		if (pp == null) {
			return;
		}
		PlayerSession session = getSession(pp);
		if (session == null) {
			return;
		}
		session.fakeJoin(null);
	}

	public void fakeLeave(String player) {
		ProxiedPlayer pp = getProxy().getPlayer(player);
		if (pp == null) {
			return;
		}
		PlayerSession session = getSession(pp);
		if (session == null) {
			return;
		}
		session.fakeLeave(null);
	}

	public void vanish(ProxiedPlayer player, String[] args) {
		if (args.length == 0) {
			if (isVanished(player)) {
				args = new String[]{"s"};
			} else {
				args = new String[]{"h"};
			}
		}
		String user = player.getName();
		ProxiedPlayer p = player;
		if (args.length >= 2) {
			user = args[1];
			p = getProxy().getPlayer(user);
			if (p == null) {
				Pattern pattern = Pattern.compile(".*" + user + ".*");
				Collection<ProxiedPlayer> playerCollection = getProxy().getPlayers();
				ProxiedPlayer[] players = playerCollection.toArray(new ProxiedPlayer[playerCollection.size()]);
				for (ProxiedPlayer pl : players) {
					Matcher matcher = pattern.matcher(pl.getName());
					if (matcher.matches()) {
						p = pl;
						break;
					}
				}
			}
			if (p == null) {
				BaseComponent message = new TextComponent("The player ");
				message.setColor(ChatColor.RED);
				BaseComponent extra = new TextComponent(user);
				extra.setColor(ChatColor.WHITE);
				message.addExtra(extra);
				if (BungeeChat.getInstance().getUUIDCache().getUUIDFromName(user) != null) {
					extra = new TextComponent(" was not found. ");
					message.addExtra(extra);
				} else {
					extra = new TextComponent(" is currently offline. ");
					message.addExtra(extra);
				}
				player.sendMessage(message);
				return;
			}
		}
		if (args[0].equalsIgnoreCase("s") || args[0].equalsIgnoreCase("show")) {
			setVanished(p, false);
			BaseComponent msg1 = new TextComponent("You are now visible!");
			msg1.setColor(ChatColor.AQUA);
			if (p != player) {
				p.sendMessage(msg1);
				BaseComponent msg2 = new TextComponent("Making " + p.getName() + " visible!");
				msg2.setColor(ChatColor.AQUA);
				player.sendMessage(msg2);
			} else {
				player.sendMessage(msg1);
			}
		}
		if (args[0].equalsIgnoreCase("h") || args[0].equalsIgnoreCase("hide")) {
			setVanished(p, true);
			BaseComponent msg1 = new TextComponent("You are now hidden!");
			msg1.setColor(ChatColor.AQUA);
			if (p != player) {
				p.sendMessage(msg1);
				BaseComponent msg2 = new TextComponent("Making " + p.getName() + " hidden!");
				msg2.setColor(ChatColor.AQUA);
				player.sendMessage(msg2);
			} else {
				player.sendMessage(msg1);
			}
		}
		if (args[0].equalsIgnoreCase("j") || args[0].equalsIgnoreCase("join")) {
			fakeJoin(p.getName());
		}
		if (args[0].equalsIgnoreCase("l") || args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("q") || args[0].equalsIgnoreCase("quit")) {
			fakeLeave(p.getName());
		}
	}

	public final String cubeTokensServer = "127.0.0.1";

	private void updateCT(UUID uuid) {
		try {
			ProxiedPlayer pp = getProxy().getPlayer(uuid);
			if (pp == null) {
				return;
			}
			Server s = pp.getServer();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(baos);
			out.writeUTF("CubeTokens");
			out.writeUTF("Update");
			out.writeUTF(uuid.toString().replace("-", "").toLowerCase());
			out.flush();
			s.sendData("BungeeCord", baos.toByteArray());
		} catch (Exception e) {
		}
	}

	public void syncSiggiIO(final UUID uuid, final String app, final String status) {
		String secret = "Siggi is awesome, and Siggi loves Asia more than America";

		final String userIdentifier = "mc=" + Util.uuidToString(uuid);
		final long date = System.currentTimeMillis();
		final String dateStr = Long.toString(date);
		final String correctSecretCode = Util.bytesToHex(Util.sha1(secret + "/" + userIdentifier + "/" + app + "/" + status + "/" + dateStr));
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Properties props = new Properties();
					props.setProperty("uid", userIdentifier);
					props.setProperty("app", app);
					if (status != null) {
						props.setProperty("status", status);
					}
					props.setProperty("date", dateStr);
					props.setProperty("secretcode", correctSecretCode);
					HttpURLConnection postSiggiIOAPI = Util.postSiggiIOAPI("apistatus", props);
					byte[] resultBytes = Util.readFullyToArray(postSiggiIOAPI.getInputStream());
					String result = new String(resultBytes);
					JsonParser parser = new JsonParser();
					JsonObject resultObject = (JsonObject) parser.parse(result);
					JsonElement error = resultObject.get("error");
					if (error != null) {
						String errorMsg = error.getAsString();
						ProxiedPlayer p = getProxy().getPlayer(uuid);
						if (p != null) {
							PlayerSession session = getSession(p);
							session.setSiggiIOAPIResult(null);
						}
					} else {
						ProxiedPlayer p = getProxy().getPlayer(uuid);
						if (p != null) {
							PlayerSession session = getSession(p);
							session.setSiggiIOAPIResult(resultObject);
						}
					}
				} catch (Exception ex) {
				}
			}
		}).start();
	}

	public void updateGeolocationForOnlinePlayers() {
		for (ProxiedPlayer p : getProxy().getPlayers()) {
			InetSocketAddress address = p.getAddress();
			Geolocation geolocation = getGeolocation(address.getAddress().getHostAddress());
			if (geolocation != null) {
				getSession(p).geolocation = geolocation;
			}
		}
	}

	private final Set<String> bannedNames = new HashSet<>();
	private long lastBannedNamesModified = 0L;
	private long lastBannedNamesLoaded = 0L;

	private void loadBannedNamesIfNeeded() {
		long now = System.currentTimeMillis();
		if (now - lastBannedNamesLoaded < 60000L) {
			return;
		}
		lastBannedNamesLoaded = now;
		File bannedNamesF = new File("banned-names.txt");
		if (lastBannedNamesModified == bannedNamesF.lastModified()) {
			return;
		}
		LinkedList<String> loaded = new LinkedList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(bannedNamesF))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("#")) {
					line = line.substring(0, line.indexOf("#"));
				}
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				loaded.add(line.toLowerCase());
			}
		} catch (Exception e) {
		}
		bannedNames.clear();
		bannedNames.addAll(loaded);
	}

	public void banName(String username, UUID issuerUUID, String issuerName) {
		File bannedNamesF = new File("banned-names.txt");
		bannedNames.add(username.toLowerCase());
		try (FileOutputStream fos = new FileOutputStream(bannedNamesF, true)) {
			fos.write((username + " # banned by " + issuerName + " + (" + issuerUUID + ")\n").getBytes());
		} catch (Exception e) {
		}
		lastBannedNamesModified = bannedNamesF.lastModified();
	}

	public boolean isNameBanned(String username) {
		loadBannedNamesIfNeeded();
		if (username == null) {
			return false;
		}
		return bannedNames.contains(username.toLowerCase());
	}

	private ServerPublicChatGroupController serverPublicChatGroupController;

	public ServerPublicChatGroupController getServerPublicChatGroupController() {
		return serverPublicChatGroupController;
	}

	public boolean addPlus(UUID pl, String paymentRef, long time) {
		try {
			URL url = new URL("http://127.0.0.1:2823/api/addplus?uuid=" + pl.toString() + "&ref=" + Util.urlEncode(paymentRef) + "&time=" + time);
			URLConnection urlc = url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
			return reader.readLine().equals("OK");
		} catch (Exception e) {
		}
		return false;
	}

	public IPtoISP getIpToIsp() {
		return ipToIsp;
	}

	private void sendSkin(UUID user, String value) {
		new Thread(() -> {
			try {
				sendSkin0(user, value);
			} catch (Exception e) {
			}
		}, "UpdateSkin").start();
	}

	private void sendSkin0(UUID user, String value) {
		if (value == null) {
			value = "null";
		}
		try {
			byte[] decode = Base64.getDecoder().decode(value);
			String skinJson = new String(decode, "UTF-8");
			JsonParser jsonParser = new JsonParser();
			JsonElement rootElement = jsonParser.parse(skinJson);
			JsonObject rootObject = rootElement.getAsJsonObject();
			JsonObject texturesObject = rootObject.getAsJsonObject("textures");
			JsonObject skinsObject = texturesObject.getAsJsonObject("SKIN");
			String skinUrl = skinsObject.get("url").getAsString();
			String skinUrlEncoded = URLEncoder.encode(skinUrl, "UTF-8");
			URL url = new URL("http://127.0.0.1:2823/api/updateskin?uuid=" + (user.toString().replace("-", "").toLowerCase()) + "&url=" + skinUrlEncoded);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			int responseCode = connection.getResponseCode();
		} catch (Exception e) {
		}
	}

	private final Set<String> mineWatchServers = new HashSet<>();

	private void mineWatch(ProxiedPlayer player, ServerInfo server, String worldName, int x, int y, int z, String ore, int count, int lightLevel) {
		getProxy().getPluginManager().callEvent(new MineWatchEvent(player, server, worldName, x, y, z, ore, count, lightLevel));
		if (mineWatchServers.contains(server.getName())) {
			String oreColor = "";
			switch (ore) {
				case "iron":
					oreColor = "&7";
					break;
				case "gold":
					oreColor = "&6";
					break;
				case "coal":
					oreColor = "&8";
					break;
				case "lapis":
					oreColor = "&9";
					break;
				case "diamond":
					oreColor = "&b";
					break;
				case "redstone":
					oreColor = "&4";
					break;
				case "emerald":
					oreColor = "&a";
					break;
				case "quartz":
					oreColor = "&f";
					break;
			}
			String countStr = count >= 100000 ? "100000+" : (count + "x");
			TextComponent component = unify(processChat(null, "&c[!] &e" + player.getName() + "&c found &e" + countStr + " " + oreColor + ore + "&c in light level &e" + lightLevel + "/15&c" + " (" + server.getName() + ")"));
			for (ProxiedPlayer p : getProxy().getPlayers()) {
				if (p.hasPermission("hk.siggi.bungeechat.minewatch")) {
					p.sendMessage(component);
				}
			}
		}
	}

	@EventHandler
	public void pingRequest(ProxyPingEvent event) {
		String motd = randomMotd();
		if (motd == null) {
			return;
		}
		ServerPing response = event.getResponse();
		response.setDescription(motd);
		event.setResponse(response);
	}

	private String randomMotd() {
		return null;
	}
}
