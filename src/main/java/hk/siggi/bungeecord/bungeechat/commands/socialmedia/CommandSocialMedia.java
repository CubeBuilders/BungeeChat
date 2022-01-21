package hk.siggi.bungeecord.bungeechat.commands.socialmedia;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.processChat;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.unify;
import java.util.Arrays;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandSocialMedia extends Command implements TabExecutor {

	private final BungeeChat plugin;

	public CommandSocialMedia(BungeeChat plugin) {
		super("social", null, "socialmedia", "share", "officialpage", "officialpages");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender cs, String[] args) {
		MessageSender.sendMessage(cs, "&6Check out our social media pages!");
		MessageSender.sendMessage(cs, "<https://www.youtube.com/channel/UCsRSHw2Fay1wEiYVaT4bTzw><&bClick for CubeBuilders on YouTube!>");
		MessageSender.sendMessage(cs, "<https://www.youtube.com/channel/UCBbW5ZcYN_2fVbEsJZqFXqw><&dClick for IcelandicAsian on YouTube! (Siggi's channel)>");
		MessageSender.sendMessage(cs, "<https://www.instagram.com/cube.builders/><&eClick for our Instagram page!> &6(<https://www.instagram.com/cube.builders/><&e@cube.builders>)");
		MessageSender.sendMessage(cs, "<https://www.facebook.com/CubeBuilders/><&5Click for our Facebook page!>");
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender cs, String[] args) {
		return Arrays.asList(new String[0]);
	}
}
