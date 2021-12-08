package hk.siggi.bungeecord.bungeechat.util;

import java.util.Iterator;

public class SimpleIterable<T> implements Iterable<T> {

	private final Iterator<T> iterator;

	public SimpleIterable(Iterator<T> iterator) {
		this.iterator = iterator;
	}

	@Override
	public Iterator<T> iterator() {
		return iterator;
	}
}
