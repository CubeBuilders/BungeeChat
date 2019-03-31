package hk.siggi.bungeecord.bungeechat;

public abstract class RunnableWithReturn<O> implements Runnable {

	private final Object lock = new Object();
	private boolean didRun = false;
	private O returnValue = null;

	@Override
	public final void run() {
		returnValue = doRun();
		synchronized (lock) {
			didRun = true;
			lock.notifyAll();
		}
	}

	protected abstract O doRun();

	public final O getReturnValue() throws InterruptedException {
		synchronized (lock) {
			while (!didRun) {
				lock.wait();
			}
			return returnValue;
		}
	}
}
