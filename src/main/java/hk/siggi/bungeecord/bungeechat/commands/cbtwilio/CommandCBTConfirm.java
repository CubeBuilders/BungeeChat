package hk.siggi.bungeecord.bungeechat.commands.cbtwilio;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.Endpoints;
import java.io.*;
import java.util.*;
import java.net.*;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandCBTConfirm extends Command {
	public final BungeeChat plugin;
	public CommandCBTConfirm(BungeeChat plugin) {
		super("cbtconfirm", null);
		this.plugin = plugin;
	}
	@Override
	public void execute(CommandSender sender, String[] args) {
		UUID issuer;
		String issuerName;
		ProxiedPlayer player;
		if (sender instanceof ProxiedPlayer) {
			player = ((ProxiedPlayer) sender);
			issuer = player.getUniqueId();
			issuerName = player.getName();
		} else {
			MessageSender.sendMessage(sender, "This command can only be used in-game.");
			return;
		}
		if (args.length < 1) {
			BaseComponent message = new TextComponent("Usage: ");
			message.setColor(ChatColor.AQUA);
			BaseComponent extra = new TextComponent("/cbtconfirm <code>");
			extra.setColor(ChatColor.WHITE);
			message.addExtra(extra);
			MessageSender.sendMessage(player, message);
			return;
		}
		String code = args[0];
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(Endpoints.get("twilio") + "/twilio/cubebuilders/register?username=" + URLEncoder.encode(player.getName()) + "&uuid=" + URLEncoder.encode(player.getUniqueId().toString().toLowerCase().replaceAll("-", "")) + "&pincode=" + URLEncoder.encode(code)).openConnection().getInputStream()));
			String result = reader.readLine();
			if (result.startsWith("OK:")) {
				String number = result.substring(3);
				String serviceNumber = number.substring(number.indexOf("/") + 1);
				number = number.substring(0, number.indexOf("/"));
				BaseComponent message = new TextComponent("Successfully registered ");
				message.setColor(ChatColor.AQUA);
				BaseComponent extra = new TextComponent(number);
				extra.setColor(ChatColor.AQUA);
				message.addExtra(extra);
				extra = new TextComponent(" to your account!");
				message.addExtra(extra);
				MessageSender.sendMessage(player, message);
				message = new TextComponent("This service is provided free of charge by CubeBuilders. Texting rates may apply depending on your carrier and service plan.");
				message.setColor(ChatColor.AQUA);
				MessageSender.sendMessage(player, message);
			} else if (result.equalsIgnoreCase("ServiceAlreadyActive")) {
				BaseComponent message = new TextComponent("You already have a phone number registered. Info: ");
				message.setColor(ChatColor.RED);
				BaseComponent extra = new TextComponent("/cbt");
				extra.setColor(ChatColor.AQUA);
				extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("Click for information")}));
				extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cbt"));
				message.addExtra(extra);
				MessageSender.sendMessage(player, message);
				message = new TextComponent("If you no longer have access to your old phone, contact Siggi88 to remove it from your account.");
				message.setColor(ChatColor.RED);
				MessageSender.sendMessage(player, message);
			} else if (result.equalsIgnoreCase("InvalidCode")) {
				BaseComponent message = new TextComponent("You entered an invalid confirmation code!");
				message.setColor(ChatColor.RED);
				MessageSender.sendMessage(player, message);
			}
		} catch (Exception e) {
			BaseComponent message = new TextComponent("An error has occurred. :/");
			message.setColor(ChatColor.RED);
			MessageSender.sendMessage(player, message);
		}
	}
}
