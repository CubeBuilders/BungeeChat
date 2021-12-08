package hk.siggi.bungeecord.bungeechat.commands.socialmedia;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.processChat;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.unify;
import java.util.Arrays;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class CommandVote extends Command implements TabExecutor {

	private final BungeeChat plugin;

	public CommandVote(BungeeChat plugin) {
		super("vote");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender cs, String[] args) {
		cs.sendMessage(unify(processChat(null, "&6----")));
		cs.sendMessage(unify(processChat(null, "&6To vote for us, <https://cubebuilders.net/vote><Click here!>")));
		cs.sendMessage(unify(processChat(null, "&6----")));
		cs.sendMessage(unify(processChat(null, "&6Subscribe to us on YouTube!")));
		cs.sendMessage(unify(processChat(null, "<https://www.youtube.com/channel/UCsRSHw2Fay1wEiYVaT4bTzw><&bClick for CubeBuilders on YouTube!>")));
		cs.sendMessage(unify(processChat(null, "<https://www.youtube.com/channel/UCBbW5ZcYN_2fVbEsJZqFXqw><&dClick for IcelandicAsian on YouTube! (Siggi's channel)>")));
		cs.sendMessage(unify(processChat(null, "&6----")));
		cs.sendMessage(unify(processChat(null, "&6Don't forget to follow us on Instagram too!")));
		cs.sendMessage(unify(processChat(null, "<https://www.instagram.com/cube.builders/><&eClick here for our Instagram page!> &6(<https://www.instagram.com/cube.builders/><&e@cube.builders>)")));
		cs.sendMessage(unify(processChat(null, "&6----")));
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender cs, String[] args) {
		return Arrays.asList(new String[0]);
	}
}
