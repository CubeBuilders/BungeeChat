package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.PlayerSession;
import hk.siggi.bungeecord.bungeechat.ontime.OnTime;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.processChat;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.unify;
import hk.siggi.bungeecord.bungeechat.util.Util;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.cubebuilders.user.CBUser;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;

public class CommandImpersonate extends Command implements TabExecutor {

	private final BungeeChat plugin;

	public CommandImpersonate(BungeeChat plugin) {
		super("impersonate", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer p = (ProxiedPlayer) sender;
		if (!p.hasPermission("hk.siggi.bungeechat.impersonate")) {
			p.sendMessage(Util.randomNotPermittedMessage());
			return;
		}
		if (args.length == 0) {
			p.sendMessage(unify(processChat(null, "&6Usage: &b/impersonate [playername]")));
			return;
		}
		String name = args[0];
		UUID uuid = plugin.getPlayerNameHandler().getPlayerByName(name);
		if (uuid == null) {
			p.sendMessage(unify(processChat(null, "&4Unknown username &b" + name)));
			return;
		}
		if (!plugin.getPlayerInfo(p.getUniqueId()).isVanished()) {
			p.sendMessage(unify(processChat(null, "&4You have to be vanished to perform this action. Vanish, then impersonate, then change server, then unvanish.")));
			return;
		}
		if (doImpersonate(p, uuid)) {
			plugin.getPlayerInfo(uuid).setVanished(true);
			p.sendMessage(unify(processChat(null, "&6Switch servers before unvanishing, and so you don't give yourself away, make sure to &b/setgroup " + p.getName() + " fake [new groups]&6 before you unvanish!")));
		}
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		List<String> result = new LinkedList<>();
		if (args.length == 1) {
			result.addAll(plugin.getPlayerNameHandler().autocompletePlayers(args[0]));
		}
		return result;
	}

	private static Field uidF = null, nameF = null, nameUF = null, loginProfileF = null;
	private static BungeeCord bungeeCord;

	private static void setup() {
		if (uidF == null) {
			try {
				uidF = InitialHandler.class.getDeclaredField("uniqueId");
				uidF.setAccessible(true);

				nameF = InitialHandler.class.getDeclaredField("name");
				nameF.setAccessible(true);

				nameUF = UserConnection.class.getDeclaredField("name");
				Field modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(nameUF, nameUF.getModifiers() & ~Modifier.FINAL);
				nameUF.setAccessible(true);

				loginProfileF = InitialHandler.class.getDeclaredField("loginProfile");
				loginProfileF.setAccessible(true);

				bungeeCord = BungeeCord.getInstance();
			} catch (Exception e) {
			}
		}
	}

	public boolean doImpersonate(ProxiedPlayer p, UUID target) {
		setup();
		Map<UUID, PlayerAccount> playerInfo = plugin.getPlayerInfoMap(new ImpersonationLock());
		UUID oldUniqueId = p.getUniqueId();
		PlayerSession session = BungeeChat.getSession(p);
		UserConnection uc = ((UserConnection) p);
		InitialHandler ih = uc.getPendingConnection();

		LoginResult newLoginResult = plugin.reconstructLoginResultFromCache(target);
		if (newLoginResult == null) {
			p.sendMessage(unify(processChat(null, "&4Something went wrong! :/")));
			return false;
		}
		CBUser cbUser = BungeeChat.getUser(target);
		ProxiedPlayer targetCheck = BungeeChat.getProxiedPlayer(target);
		if (targetCheck != null) {
			p.sendMessage(unify(processChat(null, "&4Cannot impersonate a user who is currently online!")));
			return false;
		}

		OnTime.getInstance().invalidateSession(oldUniqueId);
		bungeeCord.removeConnection(uc);
		playerInfo.remove(p.getUniqueId());
		try {
			session.setUUID(target, new ImpersonationLock());
			session.user = cbUser;
			
			plugin.getGeolocation(p);

			uidF.set(ih, target);
			nameF.set(ih, newLoginResult.getName());
			nameUF.set(uc, newLoginResult.getName());
			loginProfileF.set(ih, newLoginResult);
			uc.setDisplayName(newLoginResult.getName());

			bungeeCord.addConnection(uc);

			p.sendMessage(unify(processChat(null, "&6You are now &b" + uc.getName() + "&6 (UUID: &b" + uc.getUniqueId() + "&6)")));

			session.addBungeeHotbar("impersonate", "Impersonating " + uc.getName());

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			p.disconnect(unify(processChat(null, "&4Something went wrong! :/")));
		}
		return false;
	}

	public class ImpersonationLock {

		private ImpersonationLock() {
		}
	}
}
