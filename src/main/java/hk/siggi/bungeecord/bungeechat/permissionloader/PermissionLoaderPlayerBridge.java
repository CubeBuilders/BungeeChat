package hk.siggi.bungeecord.bungeechat.permissionloader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PermissionLoaderPlayerBridge implements PermissionLoaderBridge {

	private final String serverName;
	private final List<String> playerGroups;
	private final List<String> plugins;
	private final Map<String, List<String>> permissionList = new HashMap<>();
	private final Map<String, List<String>> parentList = new HashMap<>();
	private final List<String> computedPermissions = new ArrayList<>();

	public PermissionLoaderPlayerBridge(String serverName, List<String> playerGroups, List<String> plugins) {
		this.serverName = serverName;
		List<String> pg = new ArrayList<>(playerGroups.size());
		pg.addAll(playerGroups);
		this.playerGroups = Collections.unmodifiableList(pg);
		List<String> p = new ArrayList<>(plugins.size());
		p.addAll(plugins);
		this.plugins = Collections.unmodifiableList(p);
	}

	@Override
	public boolean checkPermission(String permission) {
		for (String p : computedPermissions) {
			if (p.endsWith(".*")) {
				p = p.substring(0, p.length() - 1);
				if (permission.startsWith(p)) {
					return true;
				}
			} else if (permission.equals(p)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void clearPermissions() {
		permissionList.clear();
		parentList.clear();
		computedPermissions.clear();
	}

	@Override
	public void setParent(String child, String... parent) {
		List<String> parents = parentList.get(child);
		if (parents == null) {
			parentList.put(child, parents = new ArrayList<>());
		}
		parents.addAll(Arrays.asList(parent));
	}

	@Override
	public void addPermission(String group, String permission) {
		List<String> permissions = permissionList.get(group);
		if (permissions == null) {
			permissionList.put(group, permissions = new ArrayList<>());
		}
		permissions.add(permission);
	}

	@Override
	public void finishUp() {
		List<String> groupsDone = new ArrayList<>();
		for (String group : playerGroups) {
			addPermissionsFromGroup(groupsDone, group);
		}
	}

	private void addPermissionsFromGroup(List<String> groupsDone, String group) {
		if (groupsDone.contains(group)) {
			return;
		}
		groupsDone.add(group);
		List<String> perms = permissionList.get(group);
		if (perms != null) {
			for (String perm : perms) {
				computedPermissions.add(perm);
			}
		}
		List<String> parents = parentList.get(group);
		if (parents != null) {
			for (String group2 : parents) {
				addPermissionsFromGroup(groupsDone, group2);
			}
		}
	}

	@Override
	public String getValueOfVariable(String variable) {
		if (variable.equals("server")) {
			return serverName;
		}
		return "";
	}

	@Override
	public boolean checkForPlugin(String condition) {
		return plugins.contains(condition);
	}

	@Override
	public void setupTimerTask(PermissionLoader loader) {

	}

}
