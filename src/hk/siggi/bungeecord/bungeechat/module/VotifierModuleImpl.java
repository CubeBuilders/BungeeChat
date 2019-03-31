package hk.siggi.bungeecord.bungeechat.module;

import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;
import hk.siggi.bungeecord.bungeechat.BungeeChat;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public final class VotifierModuleImpl implements VotifierModule, Listener {

	private BungeeChat plugin;

	@Override
	public void setBungeeChat(BungeeChat plugin) {
		if (plugin != null) {
			this.plugin = plugin;
		}
	}

	@EventHandler
	public void voted(VotifierEvent event) {
		Vote vote = event.getVote();
		logVote(vote);
		String name = vote.getUsername();
		UUID uuid = plugin.getUUIDCache().getUUIDFromName(name);
		plugin.getPlayerInfo(uuid).setLastVoted(System.currentTimeMillis());
		try {
			for (VoteHandler h : getHandlers()) {
				try {
					h.handleVote(uuid, plugin);
				} catch (Exception e) {
					System.err.print("Error handling vote: ");
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
		}
	}

	private List<VoteHandler> getHandlers() {
		File ff = new File(plugin.getDataFolder(), "votehandlers");
		LinkedList<VoteHandler> voteHandlers = new LinkedList<>();
		try {
			ClassLoader cl = new URLClassLoader(new URL[]{ff.toURI().toURL()}, getClass().getClassLoader());
			for (File f : ff.listFiles((File pathname) -> pathname.getName().endsWith(".class") && !pathname.getName().startsWith("."))) {
				String n = f.getName().substring(0, f.getName().length() - 6);
				try {
					Class<VoteHandler> loadClass = (Class<VoteHandler>) cl.loadClass(n);
					voteHandlers.add(loadClass.newInstance());
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
		}
		return voteHandlers;
	}

	private final Object voteSync = new Object();

	private void logVote(Vote vote) {
		synchronized (voteSync) {
			String name = vote.getUsername();
			UUID uuid = name == null || name.equals("") ? null : plugin.getUUIDCache().getUUIDFromName(name);
			String address = vote.getAddress();
			String timeStamp = vote.getTimeStamp();
			String serviceName = vote.getServiceName();
			long localTimestamp = vote.getLocalTimestamp();
			try (FileWriter writer = new FileWriter(new File(plugin.getDataFolder(), "votelog.txt"), true)) {
				writer.write(localTimestamp + "," + timeStamp + "," + name + "," + uuid + "," + address + "," + serviceName+"\n");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
