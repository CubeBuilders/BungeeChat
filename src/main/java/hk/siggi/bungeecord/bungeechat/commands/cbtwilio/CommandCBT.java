package hk.siggi.bungeecord.bungeechat.commands.cbtwilio;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.player.PlayerAccount;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandCBT extends Command {

	public final BungeeChat plugin;

	public CommandCBT(BungeeChat plugin) {
		super("cbt", null);
		this.plugin = plugin;
	}

	public String fixServiceNumber(String serviceNumber) {
		if (serviceNumber.equals("+18664082823")) {
			return "+1-866-408-CUBE (2823)";
		}
		if (serviceNumber.equals("+16505132823")) {
			return "+1-650-513-CUBE (2823)";
		}
		return serviceNumber;
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
		PlayerAccount pi = plugin.getPlayerInfo(issuer);
		try {
			if (args.length == 0) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("http://127.0.0.1:8895/twilio/cubebuilders/getnumber?username=" + URLEncoder.encode(player.getName()) + "&uuid=" + URLEncoder.encode(player.getUniqueId().toString().toLowerCase().replaceAll("-", ""))).openConnection().getInputStream()));
				String result = reader.readLine();
				if (result.startsWith("OK:")) {
					String number = result.substring(3);
					String serviceNumber = number.substring(number.indexOf("/") + 1);
					number = number.substring(0, number.indexOf("/"));
					String savedNumber = pi.getPhoneNumber();
					if (savedNumber == null || !savedNumber.equals(number)) {
						pi.setPhoneNumber(savedNumber);
					}
					BaseComponent message = new TextComponent("CubeBuilders Texts: Registered to: ");
					message.setColor(ChatColor.AQUA);
					BaseComponent extra = new TextComponent(number);
					extra.setColor(ChatColor.WHITE);
					message.addExtra(extra);
					MessageSender.sendMessage(player, message);
					message = new TextComponent("To cancel service, text UNLINK to " + fixServiceNumber(serviceNumber) + " from your phone.  To change to a new number, you need to cancel service first and then start again with the new number.  If you no longer have access to this phone number, contact a staff member for assistance.");
					message.setColor(ChatColor.AQUA);
				} else if (result.equalsIgnoreCase("ServiceNotActive")) {
					BaseComponent message = new TextComponent("CubeBuilders Texts: Not registered yet");
					message.setColor(ChatColor.AQUA);
					MessageSender.sendMessage(player, message);
					message = new TextComponent("To register, text START to +1-866-408-CUBE (2823). This service is available to users in United States and Canada only. Other countries are not supported.");
					message.setColor(ChatColor.AQUA);
					MessageSender.sendMessage(player, message);
				}
				BaseComponent message = new TextComponent("Some features (such as mail via text, or raid detector alerts) require a paid membership with CubeBuilders. Additional message and data rates may apply depending your service plan with your cell phone carrier.");
				message.setColor(ChatColor.AQUA);
				MessageSender.sendMessage(player, message);
			} else if (args[0].equalsIgnoreCase("confirm")) {
				if (args.length < 2) {
					BaseComponent message = new TextComponent("Usage: ");
					message.setColor(ChatColor.AQUA);
					BaseComponent extra = new TextComponent("/cbt confirm <code>");
					extra.setColor(ChatColor.WHITE);
					message.addExtra(extra);
					MessageSender.sendMessage(player, message);
					return;
				}
				String code = args[1];
				BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("http://127.0.0.1:8895/twilio/cubebuilders/register?username=" + URLEncoder.encode(player.getName()) + "&uuid=" + URLEncoder.encode(player.getUniqueId().toString().toLowerCase().replaceAll("-", "")) + "&pincode=" + URLEncoder.encode(code)).openConnection().getInputStream()));
				String result = reader.readLine();
				if (result.startsWith("OK:")) {
					String number = result.substring(3);
					String serviceNumber = number.substring(number.indexOf("/") + 1);
					number = number.substring(0, number.indexOf("/"));
					pi.setPhoneNumber(number);
					BaseComponent message = new TextComponent("Successfully registered ");
					message.setColor(ChatColor.AQUA);
					BaseComponent extra = new TextComponent(number);
					extra.setColor(ChatColor.AQUA);
					message.addExtra(extra);
					extra = new TextComponent(" to your account!");
					message.addExtra(extra);
					MessageSender.sendMessage(player, message);
					message = new TextComponent("Some features (such as mail via text, or raid detector alerts) require a paid membership with CubeBuilders. Additional message and data rates may apply depending on your service plan with your cell phone carrier.");
					message.setColor(ChatColor.AQUA);
					MessageSender.sendMessage(player, message);
				} else if (result.equalsIgnoreCase("ServiceAlreadyActive")) {
					BaseComponent message = new TextComponent("You already have a phone number registered. For information, type: ");
					message.setColor(ChatColor.RED);
					BaseComponent extra = new TextComponent("/cbt");
					extra.setColor(ChatColor.AQUA);
					extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click for information")}));
					extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cbt"));
					message.addExtra(extra);
					MessageSender.sendMessage(player, message);
					message = new TextComponent("To link a new phone, unlink the old first by typing: ");
					message.setColor(ChatColor.RED);
					extra = new TextComponent("/cbt unlink");
					extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cbt unlink"));
					extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{new TextComponent("Click to unlink your phone from CubeBuilders")}));
					message.addExtra(extra);
					MessageSender.sendMessage(player, message);
				} else if (result.equalsIgnoreCase("InvalidCode")) {
					BaseComponent message = new TextComponent("You entered an invalid confirmation code! Try again.");
					message.setColor(ChatColor.RED);
					MessageSender.sendMessage(player, message);
				}
			} else if (args[0].equalsIgnoreCase("unlink")) {
				BungeeChat.getInstance().text(issuer, "CubeBuilders: Texting service has been deactivated for " + player.getName() + " by in-game command. No more texts will be sent until you resubscribe by replying START. If you didn't do this, email Siggi at siggi@cubebuilders.net immediately, and change your Minecraft account password ASAP.");
				try {
					Thread.sleep(2000L);
				} catch (Exception e) {
				}
				BufferedReader reader = new BufferedReader(new InputStreamReader(new URL("http://127.0.0.1:8895/twilio/cubebuilders/deregister?username=" + URLEncoder.encode(player.getName()) + "&uuid=" + URLEncoder.encode(player.getUniqueId().toString().toLowerCase().replaceAll("-", ""))).openConnection().getInputStream()));
				pi.setPhoneNumber(null);
				String result = reader.readLine();
				if (result.equalsIgnoreCase("OK")) {
					BaseComponent message = new TextComponent("Texting service has been deactivated.");
					message.setColor(ChatColor.GOLD);
					MessageSender.sendMessage(player, message);
				} else if (result.equalsIgnoreCase("ServiceNotActive")) {
					BaseComponent message = new TextComponent("Texting service is not active for your account.");
					message.setColor(ChatColor.RED);
					MessageSender.sendMessage(player, message);
				}
			}
		} catch (Exception e) {
		}
	}
}
