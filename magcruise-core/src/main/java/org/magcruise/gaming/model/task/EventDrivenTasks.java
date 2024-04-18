package org.magcruise.gaming.model.task;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.message.GameEvent;

public class EventDrivenTasks {

	private Map<Class<? extends GameEvent>, List<EventDrivenTask<? extends GameEvent>>> eventDrivenTasks = new LinkedHashMap<>();
	private List<Future<?>> futures = new ArrayList<>();

	public EventDrivenTasks() {
	}

	public void add(EventDrivenTask<? extends GameEvent> task) {
		Class<? extends GameEvent> clazz = task.getHandleEventClass();
		get(clazz).add(task);

	}

	private List<EventDrivenTask<? extends GameEvent>> get(
			Class<? extends GameEvent> clazz) {
		eventDrivenTasks.putIfAbsent(clazz, new CopyOnWriteArrayList<>());

		return eventDrivenTasks.get(clazz);
	}

	public <T extends GameEvent> void remove(EventDrivenTask<T> task) {
		Class<? extends GameEvent> clazz = task.getHandleEventClass();
		get(clazz).remove(task);
	}

	public <T extends GameEvent> Iterable<EventDrivenTask<T>> getTasks(Class<T> clazz) {
		List<EventDrivenTask<T>> result = new ArrayList<>();
		for (EventDrivenTask<? extends GameEvent> t : get(clazz)) {
			@SuppressWarnings("unchecked")
			EventDrivenTask<T> t2 = (EventDrivenTask<T>) t;
			result.add(t2);
		}
		return result;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public void finishAll(ActorName name) {
		futures.forEach(f -> {
			if (f.isDone()) {
				return;
			}
			f.cancel(true);
		});
		eventDrivenTasks.values().forEach(tasks -> tasks.forEach(task -> task.finish(name)));
	}

	public void procEvent(ExecutorService executorService, GameEvent event) {
		futures.add(executorService.submit(() -> {
			for (EventDrivenTask<?> task : getTasks(event.getClass())) {
				if (event.getTo().equals(task.getPlayerName())) {
					task.offer(event);
					try {
						return task.call();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
			return null;
		}));

	}
}
