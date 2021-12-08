package hk.siggi.bungeecord.bungeechat.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class ReaderAsIterator<T> implements Iterator<T> {

	private T next = null;
	private boolean nextWasDetermined = false;
	private boolean noSuchElement = false;

	protected abstract T getNext() throws NoSuchElementException;

	private void determineNext() {
		if (nextWasDetermined) {
			return;
		}
		noSuchElement = false;
		try {
			next = getNext();
		} catch (NoSuchElementException nsee) {
			noSuchElement = true;
		}
		nextWasDetermined = true;
	}

	@Override
	public boolean hasNext() {
		determineNext();
		return !noSuchElement;
	}

	@Override
	public T next() {
		determineNext();
		if (noSuchElement) {
			throw new NoSuchElementException();
		}
		T obj = next;
		next = null;
		nextWasDetermined = false;
		return obj;
	}

}
