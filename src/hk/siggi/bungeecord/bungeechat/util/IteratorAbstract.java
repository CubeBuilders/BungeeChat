package hk.siggi.bungeecord.bungeechat.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class IteratorAbstract<E> implements Iterator<E> {

	private boolean foundNext = false;
	private boolean hasNext = false;
	private E next = null;

	@Override
	public final boolean hasNext() {
		if (!foundNext) {
			foundNext = true;
			hasNext = (next = get()) != null;
		}
		return hasNext;
	}

	@Override
	public final E next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		} else {
			foundNext = hasNext = false;
			try {
				return next;
			} finally {
				next = null;
			}
		}
	}

	protected abstract E get();
}
