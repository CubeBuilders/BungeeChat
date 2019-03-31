package hk.siggi.bungeecord.bungeechat;

public interface VariableListener {
	public void receivedVariable(String variable, String value);
	public void receivedMessage(String from, byte[] data);
}
