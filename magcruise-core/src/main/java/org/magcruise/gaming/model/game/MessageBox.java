package org.magcruise.gaming.model.game;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.message.SimpleMessage;
import gnu.lists.LList;
import gnu.lists.Pair;

@SuppressWarnings("serial")
public class MessageBox<T> implements SConstructive {
	protected static Logger log = LogManager.getLogger();

	protected final Queue<T> msgbox = new ConcurrentLinkedQueue<>();

	public MessageBox() {
	}

	@SafeVarargs
	public MessageBox(T... msgs) {
		for (T msg : msgs) {
			offer(msg);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized SConstructor<? extends T> toConstructor(ToExpressionStyle style) {
		return (SConstructor<? extends T>) SConstructor.toConstructor(style, getClass(),
				msgbox.toArray());
	}

	public synchronized void offer(T msg) {
		msgbox.offer(msg);
	}

	public synchronized T poll() {
		return msgbox.poll();
	}

	public synchronized T peek() {
		return msgbox.peek();
	}

	public synchronized <S> S poll(Class<S> clazz) {
		T v = values().stream().filter(m -> clazz.isAssignableFrom(m.getClass())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
						"The message of " + clazz + " is not present."));
		remove(v);
		return clazz.cast(v);
	}

	public synchronized List<T> pollAll() {
		List<T> t = values();
		clear();
		return t;
	}

	public synchronized <S> List<S> pollAll(Class<S> clazz) {
		return values().stream().filter(m -> clazz.isAssignableFrom(m.getClass())).map(m -> {
			S t = clazz.cast(m);
			remove(m);
			return t;
		}).collect(Collectors.toList());
	}

	public synchronized LList asLList() {
		return Pair.makeList(Arrays.asList(msgbox.toArray(new SimpleMessage[0])));
	}

	public synchronized void clear() {
		msgbox.clear();
	}

	public synchronized boolean isEmpty() {
		return msgbox.isEmpty();
	}

	public synchronized void forEach(Consumer<? super T> action) {
		msgbox.forEach(action);
	}

	public synchronized boolean remove(T m) {
		return msgbox.remove(m);
	}

	@SuppressWarnings("unchecked")
	public synchronized List<T> values() {
		return Arrays.stream(msgbox.toArray()).map(o -> (T) o).collect(Collectors.toList());
	}

	public synchronized <S> List<S> values(Class<S> clazz) {
		return values().stream().filter(m -> clazz.isAssignableFrom(m.getClass())).map(m -> {
			S t = clazz.cast(m);
			return t;
		}).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
