package org.magcruise.gaming.model.task;

import org.magcruise.gaming.model.game.ActorName;

public class EventDrivenTaskLatch extends SingleTaskLatch {
	private EventDrivenTask<?> task;

	public EventDrivenTaskLatch(SingleTaskLatch latch, EventDrivenTask<?> task,
			EventDrivenTasks rootEventDrivenTasks) {
		super(latch.stageTask, latch.executorService, latch.eventDrivenTasksInThisStage,
				latch.latch);
		this.task = task;
		this.rootEventDrivenTasks = rootEventDrivenTasks;
	}

	@Override
	public synchronized void finish(ActorName name) {
		super.finish(name);
		this.eventDrivenTasksInThisStage.remove(task);
		this.rootEventDrivenTasks.remove(task);
	}

}
