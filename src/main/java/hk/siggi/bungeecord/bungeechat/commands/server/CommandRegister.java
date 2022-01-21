package hk.siggi.bungeecord.bungeechat.commands.server;

import hk.siggi.bungeecord.bungeechat.BungeeChat;
import hk.siggi.bungeecord.bungeechat.MessageSender;
import hk.siggi.bungeecord.bungeechat.PlayerSession;
import hk.siggi.bungeecord.bungeechat.util.APIUtil;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.processChat;
import static hk.siggi.bungeecord.bungeechat.util.ChatUtil.unify;
import net.cubebuilders.user.CBUser;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandRegister extends Command {

	private final BungeeChat plugin;

	public CommandRegister(BungeeChat plugin) {
		super("register");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender cs, String[] strings) {
		if (!(cs instanceof ProxiedPlayer)) {
			return;
		}
		ProxiedPlayer p = (ProxiedPlayer) cs;
		PlayerSession session = BungeeChat.getSession(p);
		CBUser user = session.user;
		String em = strings.length > 0 ? strings[0] : null;
		if (em != null && em.equalsIgnoreCase("resend")) {
			if (user.isEmailVerified()) {
				MessageSender.sendMessage(p, "&6Your email is already confirmed!");
				return;
			}
			if (session.didResendEmail) {
				MessageSender.sendMessage(p, "&6The email was already resent!");
				return;
			}
			if (APIUtil.resendEmail(p.getUniqueId())) {
				session.didResendEmail = true;
				MessageSender.sendMessage(p, "&6A new email has been sent to &b"+user.getEmail()+"&6!");
			} else {
				MessageSender.sendMessage(p, "&6Something went wrong! Please try again!");
			}
			return;
		}
		if (em != null && em.contains("@")) {
			if (!session.emailNeedsConfirmation && user.getEmail() != null && em.equalsIgnoreCase(user.getEmail())) {
				MessageSender.sendMessage(p, "&6Your email address is already set to &b" + user.getEmail() + "&6! :)");
			} else if (session.emailNeedsConfirmation && em.equalsIgnoreCase(user.getEmail())) {
				session.emailNeedsConfirmation = false;
				if (APIUtil.setEmail(p.getUniqueId(), em)) {
					MessageSender.sendMessage(p, "&6Thanks for keeping your information up to date! <3");
				} else {
					MessageSender.sendMessage(p, "&6Something went wrong! Please try again!");
				}
			} else if (session.changeEmailTo != null && session.changeEmailTo.equals(em)) {
				if (APIUtil.setEmail(p.getUniqueId(), em)) {
					if (user.getUserData().isMember) {
						MessageSender.sendMessage(p, "&6Email set! Please check your email for a link to confirm the change!");
					} else {
						MessageSender.sendMessage(p, "&6Email set! Please check your email for a link to confirm it! You must confirm your email to become a member!");
					}
				} else {
					MessageSender.sendMessage(p, "&6Could not set email! Please try again!");
				}
			} else {
				session.changeEmailTo = em;
				MessageSender.sendMessage(p, "&6Is this your email? &b" + strings[0]);
				MessageSender.sendMessage(p, "&6Please double-check and type it one more time to confirm it!");
			}
		} else {
			if (user.getUserData().isMember) {
				MessageSender.sendMessage(p, "&b/register [email] &6- Update your Email address!");
			} else {
				MessageSender.sendMessage(p, "&b/register [email] &6- Register your Email to become a member!");
			}
		}
	}
}
