package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.PlayerSession;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandCTReward extends Command {

	public final BungeeChat plugin;

	public CommandCTReward(BungeeChat plugin) {
		super("ctreward", null);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			return;
		}
		
		ProxiedPlayer player = (ProxiedPlayer) sender;
		
		PlayerSession session = BungeeChat.getSession(player);
		long currentSessionLength = (System.currentTimeMillis() - session.loginTime);
		long timeInLast2Weeks = session.timeInLast2Weeks + currentSessionLength;
		double hoursInLast2Weeks = ((double) timeInLast2Weeks) / 3600000L;
		
		TextComponent msg = new TextComponent("");
		MessageSender.sendMessage(player, msg);
		
		TextComponent ctRewards = new TextComponent("CubeTokens for being on CubeBuilders:");
		ctRewards.setColor(ChatColor.YELLOW);
		msg.addExtra(ctRewards);
		MessageSender.sendMessage(player, msg);
		
		msg = new TextComponent("");
		TextComponent infoText = new TextComponent("A base amount calculated based on the # of players online:");
		infoText.setColor(ChatColor.GOLD);
		msg.addExtra(infoText);
		MessageSender.sendMessage(player, msg);

		msg = new TextComponent("");
		baseCTText = new TextComponent("Base CT: ");
		formulaText = new TextComponent("3");
		baseCTText.setColor(ChatColor.GOLD);
		formulaText.setColor(ChatColor.YELLOW);
		msg.addExtra(baseCTText);
		msg.addExtra(formulaText);
		MessageSender.sendMessage(player, msg);
		
		msg = new TextComponent("");
		infoText = new TextComponent("A multiplier calculated based on the number of hours you were online in the last two weeks:");
		infoText.setColor(ChatColor.GOLD);
		msg.addExtra(infoText);
		MessageSender.sendMessage(player, msg);
		
		msg = new TextComponent("");
		TextComponent multiplierText = new TextComponent("Multiplier: ");
		formulaText = new TextComponent("min(23, 1 + (hours / 5))");
		multiplierText.setColor(ChatColor.GOLD);
		formulaText.setColor(ChatColor.AQUA);
		msg.addExtra(multiplierText);
		msg.addExtra(formulaText);
		MessageSender.sendMessage(player, msg);
		
		msg = new TextComponent("");
		multiplierText = new TextComponent("Multiplier: ");
		formulaText = new TextComponent("min(23, 1 + (" + doubleToString(hoursInLast2Weeks) + " / 5))");
		multiplierText.setColor(ChatColor.GOLD);
		formulaText.setColor(ChatColor.AQUA);
		msg.addExtra(multiplierText);
		msg.addExtra(formulaText);
		MessageSender.sendMessage(player, msg);
		
		msg = new TextComponent("");
		multiplierText = new TextComponent("Multiplier: ");
		formulaText = new TextComponent("min(23, " + doubleToString(1 + (hoursInLast2Weeks / 5)) + ")");
		multiplierText.setColor(ChatColor.GOLD);
		formulaText.setColor(ChatColor.AQUA);
		msg.addExtra(multiplierText);
		msg.addExtra(formulaText);
		MessageSender.sendMessage(player, msg);
		
		msg = new TextComponent("");
		multiplierText = new TextComponent("Multiplier: ");
		formulaText = new TextComponent(doubleToString(Math.min(23, (1 + (hoursInLast2Weeks / 5)))));
		multiplierText.setColor(ChatColor.GOLD);
		formulaText.setColor(ChatColor.YELLOW);
		msg.addExtra(multiplierText);
		msg.addExtra(formulaText);
		MessageSender.sendMessage(player, msg);
		
		msg = new TextComponent("");
		infoText = new TextComponent("Combine the base by the multiplier, and this is what you get every 15 minutes.");
		infoText.setColor(ChatColor.GOLD);
		msg.addExtra(infoText);
		MessageSender.sendMessage(player, msg);
		
		msg = new TextComponent("");
		TextComponent finalText = new TextComponent("Final Amount: ");
		formulaText = new TextComponent("round(Base CT * Multiplier)");
		finalText.setColor(ChatColor.GOLD);
		formulaText.setColor(ChatColor.AQUA);
		msg.addExtra(finalText);
		msg.addExtra(formulaText);
		MessageSender.sendMessage(player, msg);
		
		msg = new TextComponent("");
		finalText = new TextComponent("Final Amount: ");
		formulaText = new TextComponent("round(" + doubleToString(plugin.baseCT()) + " * " + doubleToString(Math.min(23, (1 + (hoursInLast2Weeks / 5)))) + ")");
		finalText.setColor(ChatColor.GOLD);
		formulaText.setColor(ChatColor.AQUA);
		msg.addExtra(finalText);
		msg.addExtra(formulaText);
		MessageSender.sendMessage(player, msg);
		
		double finalAmount = plugin.baseCT() * Math.min(23, (1 + (hoursInLast2Weeks / 5)));
		int finalInt = (int) Math.round(finalAmount);
		if (finalInt < 1) finalInt = 1;
		
		msg = new TextComponent("");
		finalText = new TextComponent("Final Amount: ");
		formulaText = new TextComponent(Integer.toString(finalInt));
		finalText.setColor(ChatColor.GOLD);
		formulaText.setColor(ChatColor.YELLOW);
		msg.addExtra(finalText);
		msg.addExtra(formulaText);
		MessageSender.sendMessage(player, msg);
		
		msg = new TextComponent("");
		infoText = new TextComponent("So spend more time on CubeBuilders, you'll get more CubeTokens as a reward!");
		infoText.setColor(ChatColor.GOLD);
		msg.addExtra(infoText);
		MessageSender.sendMessage(player, msg);
	}
	
	public String doubleToString(double number) {
		return Double.toString(Math.round(number * 1000.0) / 1000.0);
	}
}
