package hk.siggi.bungeecord.bungeechat.permissionloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class PermissionLoader {

	private final File permissionsFile;
	private final PermissionLoaderBridge bridge;
	private long lastModified = 0L;
	private long lastSize = 0L;

	public PermissionLoader(File permissionsFile) {
		this.permissionsFile = permissionsFile;
		bridge = new PermissionLoaderBridgeImpl();
		bridge.setupTimerTask(this);
	}
	
	private PermissionLoader(File permissionsFile, String serverName, List<String> playerGroups, List<String> plugins) {
		this.permissionsFile = permissionsFile;
		bridge = new PermissionLoaderPlayerBridge(serverName, playerGroups, plugins);
	}
	
	public PermissionLoader get(String serverName, List<String> playerGroups, List<String> plugins) {
		PermissionLoader pl = new PermissionLoader(permissionsFile, serverName, playerGroups, plugins);
		pl.loadPermissions();
		return pl;
	}

	public void loadPermissions() {
		if (!permissionsFile.exists()) {
			return;
		}
		try {
			lastSize = permissionsFile.length();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(permissionsFile)));
			bridge.clearPermissions();
			loadPermissions(reader, 0);
		} catch (Exception e) {
		} finally {
			bridge.finishUp();
			lastModified = permissionsFile.lastModified();
		}
	}

	private ReadLine loadPermissions(BufferedReader reader, int minSpaces) throws IOException {
		ReadLine line = null;
		while (true) {
			if (line == null) {
				if ((line = ReadLine.read(reader)) == null) {
					return null;
				}
			}
			if (line.spacesInFront < minSpaces) {
				return line;
			}
			ReadLine nextLine = null;
			try {
				switch (line.pieces[0]) {
					case "setparent":
					case "setparents": {
						String child = line.pieces[1];
						String[] parent = new String[line.pieces.length - 2];
						System.arraycopy(line.pieces, 2, parent, 0, parent.length);
						bridge.setParent(child, parent);
					}
					break;
					case "permit":
					case "permission": {
						String group = line.pieces[1];
						String permission = line.pieces[2];
						bridge.addPermission(group, permission);
					}
					break;
					case "if": {
						int ifConditionSpacesInFront = line.spacesInFront;
						String[] condition = new String[line.pieces.length - 1];
						System.arraycopy(line.pieces, 1, condition, 0, condition.length);
						if (testCondition(condition)) {
							nextLine = loadPermissions(reader, line.spacesInFront + 1);
							if (nextLine == null) {
								return null;
							}
							if (nextLine.spacesInFront < minSpaces) {
								return nextLine;
							}
							while (nextLine.pieces[0].equals("else")) { // skip over else blocks
								while (true) {
									nextLine = ReadLine.read(reader);
									if (nextLine == null) {
										return null;
									}
									if (nextLine.spacesInFront < minSpaces) {
										return nextLine;
									}
									if (nextLine.spacesInFront <= ifConditionSpacesInFront) {
										break;
									}
									if (nextLine.spacesInFront == ifConditionSpacesInFront) {
										break;
									}
								}
							}
						} else {
							while (true) {
								nextLine = ReadLine.read(reader);
								if (nextLine == null) {
									return null;
								}
								if (nextLine.spacesInFront < minSpaces) {
									return nextLine;
								}
								if (nextLine.spacesInFront <= ifConditionSpacesInFront) {
									break;
								}
								if (nextLine.spacesInFront == ifConditionSpacesInFront) {
									if (nextLine.pieces[0].equals("else")) { // if we hit an else, set it up to be evaluated in the next loop
										if (nextLine.pieces.length > 1 && nextLine.pieces[1].equals("if")) {
											nextLine = new ReadLine(nextLine.line.substring(5), nextLine.spacesInFront);
											break;
										} else {
											nextLine = new ReadLine("if 1", nextLine.spacesInFront);
											break;
										}
									} else {
										break;
									}
								}
							}
						}
					}
					break;
					case "quit":
					case "exit": {
						return new ReadLine("exit", 0);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			line = nextLine;
		}
	}

	private boolean testCondition(String[] condition) {
		if (condition.length == 0) {
			throw new RuntimeException("Empty condition");
		} else if (condition.length == 1) {
			return condition[0].equals("1") || condition[0].equals("true");
		} else if (condition.length == 2) {
			boolean result = false;
			boolean negate = false;
			if (condition[0].startsWith("!")) {
				condition[0] = condition[0].substring(1);
				negate = true;
			}
			switch (condition[0]) {
				case "plugin":
					result = bridge.checkForPlugin(condition[1]);
					break;
				default:
					throw new RuntimeException("Invalid condition " + join(condition));
			}
			return result ^ negate;
		} else if (condition.length >= 3) {
			boolean negate = false;
			boolean result = false;
			String variableToCheck = condition[0];
			String matchType = condition[1];
			if (matchType.startsWith("!")) {
				matchType = matchType.substring(1);
				negate = true;
			}
			String matchString = condition[2];
			for (int i = 3; i < condition.length; i++) {
				matchString += " " + condition[i];
			}
			String variableValue = getValueOfVariable(variableToCheck);
			switch (matchType) {
				case "=": {
					result = variableValue.equalsIgnoreCase(matchString);
				}
				break;
				case "==": {
					result = negate ? variableValue.equals(matchString) : variableValue.equalsIgnoreCase(matchString);
				}
				break;
				case "===": {
					result = variableValue.equals(matchString);
				}
				break;
				case "match":
				case "matches": {
					result = variableValue.matches(matchString);
				}
				break;
			}

			return result ^ negate;
		}
		return false;
	}

	private String join(String... pieces) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < pieces.length; i++) {
			if (i != 0) {
				sb.append(" ");
			}
			sb.append(pieces[i]);
		}
		return sb.toString();
	}

	private String getValueOfVariable(String variable) {
		return bridge.getValueOfVariable(variable);
	}

	private static class ReadLine {

		public static ReadLine read(BufferedReader reader) throws IOException {
			String line = "";
			while (line.trim().equals("")) {
				line = reader.readLine();
				if (line == null) {
					return null;
				}
				int idx = line.indexOf("#");
				if (idx >= 0) {
					line = line.substring(0, idx);
				}
			}
			int spacesInFront = 0;
			while (line.startsWith(" ", spacesInFront)) {
				spacesInFront += 1;
			}
			return new ReadLine(line.substring(spacesInFront), spacesInFront);
		}
		public final String line;
		public final String[] pieces;
		public final int spacesInFront;

		public ReadLine(String line, int spacesInFront) {
			this.line = line;
			this.pieces = line.split(" ");
			this.spacesInFront = spacesInFront;
		}
	}
	
	public boolean checkPermission(String permission) {
		return bridge.checkPermission(permission);
	}

	public void runLoop() {
		if (!permissionsFile.exists()) {
			return;
		}
		if (lastSize != permissionsFile.length() || lastModified != permissionsFile.lastModified()) {
			loadPermissions();
		}
	}
}
