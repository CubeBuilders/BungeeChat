package hk.siggi.bungeecord.bungeechat.chat.web;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hk.siggi.bungeecord.bungeechat.BungeeChat;
import io.siggi.http.HTTPWebSocket;
import io.siggi.http.HTTPWebSocketListener;
import io.siggi.http.HTTPWebSocketMessage;
import java.io.IOException;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class WebChatClient implements HTTPWebSocketListener {

	private final WebChat webchat;
	final UUID uuid;
	final HTTPWebSocket socket;
	private boolean started = false;
	private final Gson gson;
	private final JsonParser jsonParser;
	private boolean valid = false;
	private String publicChatChannel = "hub";

	WebChatClient(WebChat webchat, UUID uuid, HTTPWebSocket socket) {
		this.webchat = webchat;
		this.uuid = uuid;
		this.socket = socket;
		this.gson = new Gson();
		this.jsonParser = new JsonParser();
	}

	public UUID getUUID() {
		return uuid;
	}

	void start() throws IOException {
		if (started) {
			return;
		}
		started = true;
		socket.accept();
		socket.addListener(this);
		socket.useNonBlockingMode();
		valid = true;
		JsonObject welcomeMsg = new JsonObject();
		welcomeMsg.addProperty("result", "ok");
		welcomeMsg.addProperty("uuid", uuid.toString());
		welcomeMsg.addProperty("name", webchat.plugin.getPlayerNameHandler().getNameByPlayer(uuid));
		HTTPWebSocketMessage welcome = HTTPWebSocketMessage.create(gson.toJson(welcomeMsg));
		socket.send(welcome);
		webchat.addClient(this);
	}

	@Override
	public void receivedMessage(HTTPWebSocket socket, HTTPWebSocketMessage message) {
		try {
			String json = message.getText();
			JsonElement parse = jsonParser.parse(json);
			JsonObject root = (JsonObject) parse;
			String type = root.get("type").getAsString();
			if (type.equals("line")) {
				String text = root.get("text").getAsString();
				if (text.isEmpty())return;
				receivedLine(text);
			}
		} catch (Exception e) {
		}
	}

	private void receivedLine(String line) {
		BungeeChat bc = BungeeChat.getInstance();
		if (line.startsWith("/")) {
			String args[] = line.substring(1).split(" ");
			switch (args[0]) {
				case "server":
					break;
				case "msg":
				case "tell":
				case "t":
				case "whisper":
				case "w":
				case "m":
					break;
				case "r":
					break;
				case "g":
					break;
				case "group":
					if (args.length >= 2) {
						switch (args[1]) {
							case "mute":
								break;
							case "unmute":
								break;
						}
					}
					break;
				default:
					sendMessage(bc.unify(bc.processChat(null, "&6Unknown command")));
					break;
			}
		} else {
			sendMessage(bc.unify(bc.processChat(null, line)));
		}
	}

	@Override
	public void socketClosed(HTTPWebSocket socket) {
		valid = false;
		webchat.removeClient(this);
	}

	public void sendMessage(String line) {
		try {
			JsonObject lineObj = new JsonObject();
			lineObj.addProperty("type", "line");
			lineObj.addProperty("text", line);
			socket.send(HTTPWebSocketMessage.create(gson.toJson(lineObj)));
		} catch (Exception e) {
		}
	}

	public void sendMessage(BaseComponent line) {
		try {
			String lineJson = ComponentSerializer.toString(line);
			JsonObject lineObj = new JsonObject();
			lineObj.addProperty("type", "richline");
			lineObj.add("base", jsonParser.parse(lineJson));
			socket.send(HTTPWebSocketMessage.create(gson.toJson(lineObj)));
		} catch (Exception e) {
		}
	}

	public void kickOff(String kickMessage) {
		try {
			JsonObject lineObj = new JsonObject();
			lineObj.addProperty("type", "kick");
			lineObj.addProperty("message", kickMessage);
			socket.send(HTTPWebSocketMessage.create(gson.toJson(lineObj)));
		} catch (Exception e) {
		}
		try {
			valid = false;
			socket.close();
		} catch (Exception e) {
		}
	}

	public boolean isValid() {
		return valid;
	}

	public String getPublicChatChannel() {
		return publicChatChannel;
	}
}
