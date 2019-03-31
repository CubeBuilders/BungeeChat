package hk.siggi.bungeecord.bungeechat.permissionloader;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.config.ConfigurationAdapter;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

class PermissionLoaderBridgeImpl implements PermissionLoaderBridge {

	private final BungeeChat plugin;
	private final Map<String, String[]> parentMappings = new HashMap<>();

	private Map getMap() {
		try {
			ConfigurationAdapter configurationAdapter = plugin.getProxy().getConfigurationAdapter();
			Field mapField = configurationAdapter.getClass().getDeclaredField("config");
			mapField.setAccessible(true);
			Map map = (Map) mapField.get(configurationAdapter);
			return map;
		} catch (Exception e) {
		}
		return null;
	}

	private Collection<String> getGroups(UserConnection uc) {
		try {
			Field groupField = uc.getClass().getDeclaredField("groups");
			groupField.setAccessible(true);
			return (Collection<String>) groupField.get(uc);
		} catch (Exception e) {
		}
		return null;
	}

	private Collection<String> getPermissions(UserConnection uc) {
		try {
			Field permField = uc.getClass().getDeclaredField("permissions");
			permField.setAccessible(true);
			return (Collection<String>) permField.get(uc);
		} catch (Exception e) {
		}
		return null;
	}

	private void saveConfig() {
		try {
			ConfigurationAdapter configurationAdapter = plugin.getProxy().getConfigurationAdapter();
			Method saveMethod = configurationAdapter.getClass().getDeclaredMethod("save");
			saveMethod.setAccessible(true);
			saveMethod.invoke(configurationAdapter);
		} catch (Exception e) {
		}
	}

	public PermissionLoaderBridgeImpl() {
		plugin = BungeeChat.getInstance();
	}

	@Override
	public boolean checkPermission(String permission) {
		return false;
	}

	@Override
	public void clearPermissions() {
		parentMappings.clear();
		getMap().put("permissions", new HashMap());
	}

	@Override
	public void setParent(String child, String... parent) {
		parentMappings.put(child, parent);
	}

	@Override
	public void addPermission(String group, String permission) {
		Object get = getMap().get("permissions");
		if (!(get instanceof Map)) {
			getMap().put("permissions", get = new HashMap());
		}
		Map permissions = (Map) get;
		Object get1 = permissions.get(group);
		if (!(get1 instanceof Collection)) {
			permissions.put(group, get1 = new ArrayList());
		}
		Collection perms = (Collection) get1;
		if (!perms.contains(permission)) {
			perms.add(permission);
		}
	}

	@Override
	public void finishUp() {
		// because BungeeCord's permissions system is literally hitler, we have to do some post-processing.

		// copy parent's parents to children.
		{
			boolean added = true;
			while (added) {
				added = false;
				for (String child : parentMappings.keySet()) {
					String[] x = parentMappings.get(child);
					List<String> parents = new ArrayList<>();
					parents.addAll(Arrays.asList(x));
					for (String pp : x) {
						String[] y = parentMappings.get(pp);
						if (y != null) {
							for (String ppp : y) {
								if (!parents.contains(ppp)) {
									parents.add(ppp);
									added = true;
								}
							}
						}
					}
					parentMappings.put(child, parents.toArray(new String[parents.size()]));
				}
			}
		}

		try {
			// what we're doing here is copying permissions from parent groups to child groups.
			Map permissions = (Map) getMap().get("permissions");
			if (permissions == null) {
				getMap().put("permissions", permissions = new HashMap());
			}
			for (String child : parentMappings.keySet()) {
				try {
					Collection childPerms = (Collection) permissions.get(child);
					if (childPerms == null) {
						permissions.put(child, childPerms = new ArrayList());
					}
					String[] parents = parentMappings.get(child);
					for (String parent : parents) {
						try {
							Collection parentPerms = (Collection) ((Map) getMap().get("permissions")).get(parent);
							if (parentPerms == null) {
								continue;
							}
							for (Object perm : parentPerms) {
								if (perm instanceof String) {
									String permission = (String) perm;
									if (!childPerms.contains(permission)) {
										childPerms.add(permission);
									}
								}
							}
						} catch (Exception e) {
						}
					}
				} catch (Exception e) {
				}
			}

			// now we clear groups/permissions from all players and set them again.
			// we don't need to do this on startup, but if permissions are reloaded without restarting, this is needed
			for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
				try {
					UserConnection uc = (UserConnection) p;

					// backup the groups
					Collection<String> groupsC = p.getGroups();
					String[] groups = groupsC.toArray(new String[groupsC.size()]);

					// clear the groups and permissions
					getGroups(uc).clear();
					getPermissions(uc).clear();

					// set the groups again (BungeeCord will take care of permissions)
					p.addGroups(groups);
					plugin.getSession(p).updateBungeePermissionCache();
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
		}

		saveConfig();
	}

	@Override
	public String getValueOfVariable(String variable) {
		if (variable.equals("server")) {
			return "bungee";
		}
		return "";
	}

	@Override
	public boolean checkForPlugin(String plugin) {
		Plugin pp = this.plugin.getProxy().getPluginManager().getPlugin(plugin);
		return pp != null;
	}

	@Override
	public void setupTimerTask(final PermissionLoader loader) {
		plugin.getScheduler().schedule(plugin, new Runnable() {
			@Override
			public void run() {
				loader.runLoop();
			}
		}, 1000L, 1000L, TimeUnit.MILLISECONDS);
	}

}
