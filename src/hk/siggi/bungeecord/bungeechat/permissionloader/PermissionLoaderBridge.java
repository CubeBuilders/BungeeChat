package hk.siggi.bungeecord.bungeechat.permissionloader;

interface PermissionLoaderBridge {
	public boolean checkPermission(String permission);
	public void clearPermissions();
	public void setParent(String child, String...parent);
	public void addPermission(String group, String permission);
	public void finishUp();
	public String getValueOfVariable(String variable);
	public boolean checkForPlugin(String condition);
	public void setupTimerTask(PermissionLoader loader);
}
