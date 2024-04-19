package org.magcruise.gaming.model.game;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class MessageBoxes<K, T> {

	protected Map<K, MessageBox<T>> messageBoxes;

	public MessageBoxes() {
		messageBoxes = new ConcurrentHashMap<>();
	}

	public synchronized void offer(K key, T message) {
		getMessageBox(key).offer(message);
	}

	public synchronized List<T> values(K key) {
		return getMessageBox(key).values();
	}

	public synchronized <S> List<S> values(K key, Class<S> clazz) {
		return getMessageBox(key).values(clazz);
	}

	public synchronized <S> List<S> values(Class<S> clazz) {
		return messageBoxes.keySet().stream().map(key -> messageBoxes.get(key).values(clazz))
				.flatMap(l -> l.stream()).collect(Collectors.toList());
	}

	public synchronized List<T> pollAll(K key) {
		return getMessageBox(key).pollAll();
	}

	public synchronized <S> List<S> pollAll(K key, Class<S> clazz) {
		return getMessageBox(key).pollAll(clazz);
	}

	public synchronized <S> List<S> pollAll(Class<S> clazz) {
		return messageBoxes.keySet().stream().map(key -> messageBoxes.get(key).pollAll(clazz))
				.flatMap(l -> l.stream()).collect(Collectors.toList());
	}

	public synchronized MessageBox<T> getMessageBox(K key) {
		messageBoxes.putIfAbsent(key, new MessageBox<>());
		return messageBoxes.get(key);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public synchronized void putMessageBox(K name, MessageBox<T> msgbox) {
		messageBoxes.put(name, msgbox);
	}

	public void remove(K key) {
		messageBoxes.remove(key);
	}

}
