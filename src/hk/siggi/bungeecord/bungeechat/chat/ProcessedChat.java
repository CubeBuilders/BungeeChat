package hk.siggi.bungeecord.bungeechat.chat;

import java.util.ArrayList;
import net.md_5.bungee.api.chat.BaseComponent;

public class ProcessedChat {
	public final String message;
	public final ArrayList<BaseComponent> uncensored;
	public final ArrayList<BaseComponent> censored;
	public final ArrayList<BaseComponent> semiCensored;

	ProcessedChat(String message, ArrayList<BaseComponent> uncensored, ArrayList<BaseComponent> censored, ArrayList<BaseComponent> semiCensored) {
		this.message = message;
		this.uncensored = uncensored;
		this.censored = censored;
		this.semiCensored = semiCensored;
	}
	
}
