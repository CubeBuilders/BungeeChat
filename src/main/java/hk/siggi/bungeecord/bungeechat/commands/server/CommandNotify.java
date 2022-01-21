package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.notifications.LoginAlert;
import hk.siggi.bungeecord.bungeechat.notifications.MineWatchAlert;
import hk.siggi.bungeecord.bungeechat.notifications.NotificationTrigger;
import hk.siggi.bungeecord.bungeechat.notifications.Notifications;
import hk.siggi.bungeecord.bungeechat.notifications.PunishmentAlert;
import hk.siggi.bungeecord.bungeechat.notifications.SpeedAlert;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandNotify extends Command implements TabExecutor {

	private final BungeeChat plugin;
	private final Map<String, Class<? extends NotificationTrigger>> triggerTypes = new HashMap<>();
	private final List<String> commands = new LinkedList<>();

	public CommandNotify(BungeeChat plugin) {
		super("notifications", null, "notify");
		this.plugin = plugin;
		plugin.getNotifications().copyTypesTo(triggerTypes);
		commands.add("add");
		commands.add("list");
		commands.add("configure");
		commands.add("delete");
		commands.add("reload");
	}

	@Override
	public void execute(CommandSender sender, String[] split) {
		if (!(sender instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer p = (ProxiedPlayer) sender;
		Notifications notifications = plugin.getNotifications();
		if (split.length == 0) {
			MessageSender.sendMessage(p, "?");
		} else if (split[0].equalsIgnoreCase("add")) {
			Class<? extends NotificationTrigger> clazz = getTrigger(split[1]);
			if (clazz == LoginAlert.class) {
				String username = split[2];
				boolean sms = false;
				boolean prowl = false;
				int maxActivations = 0;
				UUID targetUUID = plugin.getPlayerNameHandler().getPlayerByName(username);
				for (int i = 3; i < split.length; i++) {
					if (split[i].equalsIgnoreCase("sms")) {
						sms = true;
					} else if (split[i].equalsIgnoreCase("prowl")) {
						prowl = true;
					} else {
						try {
							maxActivations = Integer.parseInt(split[i]);
						} catch (Exception e) {
						}
					}
				}
				if (!sms && !prowl) {
					PlayerAccount info = plugin.getPlayerInfo(p.getUniqueId());
					if (info.getProwlApiKey() != null) {
						prowl = true;
					} else {
						sms = true;
					}
				}
				notifications.addTrigger(new LoginAlert(notifications, UUID.randomUUID(), sms, prowl, p.getUniqueId(), 0, maxActivations, targetUUID));
			} else if (clazz == PunishmentAlert.class) {
				String username = split[2];
				boolean sms = false;
				boolean prowl = false;
				int maxActivations = 0;
				UUID targetUUID = username.equals("-") ? null : plugin.getPlayerNameHandler().getPlayerByName(username);
				for (int i = 3; i < split.length; i++) {
					if (split[i].equalsIgnoreCase("sms")) {
						sms = true;
					} else if (split[i].equalsIgnoreCase("prowl")) {
						prowl = true;
					} else {
						try {
							maxActivations = Integer.parseInt(split[i]);
						} catch (Exception e) {
						}
					}
				}
				if (!sms && !prowl) {
					PlayerAccount info = plugin.getPlayerInfo(p.getUniqueId());
					if (info.getProwlApiKey() != null) {
						prowl = true;
					} else {
						sms = true;
					}
				}
				notifications.addTrigger(new PunishmentAlert(notifications, UUID.randomUUID(), sms, prowl, p.getUniqueId(), 0, maxActivations, targetUUID));
			} else if (clazz == MineWatchAlert.class) {
				String server = split[2];
				boolean sms = false;
				boolean prowl = false;
				int maxActivations = 0;
				if (server.equals("-")) {
					server = null;
				}
				for (int i = 3; i < split.length; i++) {
					if (split[i].equalsIgnoreCase("sms")) {
						sms = true;
					} else if (split[i].equalsIgnoreCase("prowl")) {
						prowl = true;
					} else {
						try {
							maxActivations = Integer.parseInt(split[i]);
						} catch (Exception e) {
						}
					}
				}
				if (!sms && !prowl) {
					PlayerAccount info = plugin.getPlayerInfo(p.getUniqueId());
					if (info.getProwlApiKey() != null) {
						prowl = true;
					} else {
						sms = true;
					}
				}
				notifications.addTrigger(new MineWatchAlert(notifications, UUID.randomUUID(), sms, prowl, p.getUniqueId(), 0, maxActivations, server));
			} else if (clazz == SpeedAlert.class) {
				boolean firstOnly=split[2].equalsIgnoreCase("firstonly");
				boolean sms = false;
				boolean prowl = false;
				int maxActivations = 0;
				for (int i = 3; i < split.length; i++) {
					if (split[i].equalsIgnoreCase("sms")) {
						sms = true;
					} else if (split[i].equalsIgnoreCase("prowl")) {
						prowl = true;
					} else {
						try {
							maxActivations = Integer.parseInt(split[i]);
						} catch (Exception e) {
						}
					}
				}
				if (!sms && !prowl) {
					PlayerAccount info = plugin.getPlayerInfo(p.getUniqueId());
					if (info.getProwlApiKey() != null) {
						prowl = true;
					} else {
						sms = true;
					}
				}
				notifications.addTrigger(new SpeedAlert(notifications, UUID.randomUUID(), sms, prowl, p.getUniqueId(), 0, maxActivations, firstOnly));
			}
		} else if (split[0].equalsIgnoreCase("list")) {
			try {
				NotificationTrigger[] byNotifyee = notifications.getByNotifyee(p.getUniqueId());
				for (int i = 0; i < byNotifyee.length; i++) {
					NotificationTrigger trigger = byNotifyee[i];
					MessageSender.sendMessage(p, Integer.toString(i + 1) + ": " + trigger.toString());
				}
			} catch (Exception e) {
			}
		} else if (split[0].equalsIgnoreCase("configure")) {
			try {
				NotificationTrigger[] byNotifyee = notifications.getByNotifyee(p.getUniqueId());
				int idx = Integer.parseInt(split[1]) - 1;
				NotificationTrigger trigger = byNotifyee[idx];
				Class<? extends NotificationTrigger> clazz = trigger.getClass();
				List<String> oreList = new LinkedList<>();
				for (int i = 2; i < split.length; i++) {
					if (MineWatchAlert.allOres.contains(split[i].toLowerCase())) {
						oreList.add(split[i].toLowerCase());
					}
				}
				String[] ores = oreList.isEmpty() ? null : oreList.toArray(new String[oreList.size()]);
				if (clazz == MineWatchAlert.class) {
					MineWatchAlert mwa = (MineWatchAlert)trigger;
					mwa.setOres(ores);
				}
			} catch (Exception e) {
			}
		} else if (split[0].equalsIgnoreCase("delete")) {
			try {
				NotificationTrigger[] byNotifyee = notifications.getByNotifyee(p.getUniqueId());
				int idx = Integer.parseInt(split[1]) - 1;
				notifications.deleteTrigger(byNotifyee[idx]);
			} catch (Exception e) {
			}
		} else if (split[0].equalsIgnoreCase("reload")) {
			try {
				notifications.reloadData();
			} catch (Exception e) {
			}
		}
	}

	private Class<? extends NotificationTrigger> getTrigger(String type) {
		Class<? extends NotificationTrigger> clazz = triggerTypes.get(type);
		if (clazz == null) {
			for (String t : triggerTypes.keySet()) {
				if (t.equalsIgnoreCase(type)) {
					clazz = triggerTypes.get(t);
					break;
				}
			}
		}
		return clazz;
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender cs, String[] split) {
		if (!(cs instanceof ProxiedPlayer)) {
			return new LinkedList<>();
		}
		ProxiedPlayer p = (ProxiedPlayer) cs;
		Notifications notifications = plugin.getNotifications();
		List<String> result = new LinkedList<>();
		if (split.length == 1) {
			for (String command : commands) {
				if (command.toLowerCase().startsWith(split[0].toLowerCase())) {
					result.add(command);
				}
			}
		} else if (split.length == 2) {
			if (split[0].equalsIgnoreCase("add")) {
				for (String type : triggerTypes.keySet()) {
					if (type.toLowerCase().startsWith(split[1].toLowerCase())) {
						result.add(type);
					}
				}
			}
		} else if (split.length == 3) {
			if (split[0].equalsIgnoreCase("add")) {
				Class<? extends NotificationTrigger> clazz = getTrigger(split[1]);
				if (clazz == LoginAlert.class || clazz == PunishmentAlert.class) {
					result.addAll(plugin.getPlayerNameHandler().autocompletePlayers(split[2].toLowerCase()));
				} else if (clazz == MineWatchAlert.class) {
					for (ServerInfo s : plugin.getProxy().getServers().values()) {
						if (s.getName().toLowerCase().startsWith(split[2].toLowerCase())) {
							result.add(s.getName());
						}
					}
				} else if (clazz == SpeedAlert.class) {
					for (String s : new String[]{"firstOnly","all"}) {
						if (s.toLowerCase().startsWith(split[2].toLowerCase())) {
							result.add(s);
						}
					}
				}
			} else if (split[0].equalsIgnoreCase("configure")) {
				NotificationTrigger[] byNotifyee = notifications.getByNotifyee(p.getUniqueId());
				try {
					NotificationTrigger trigger = byNotifyee[Integer.parseInt(split[1]) - 1];
					Class<? extends NotificationTrigger> clazz = trigger.getClass();
					if (clazz == MineWatchAlert.class) {
						for (String s : MineWatchAlert.allOres) {
							if (s.toLowerCase().startsWith(split[2].toLowerCase())) {
								result.add(s);
							}
						}
					}
				} catch (Exception e) {
				}
			}
		}
		return result;
	}
}
