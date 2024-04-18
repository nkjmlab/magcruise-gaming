package org.magcruise.gaming.model.task;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.Player;
import org.magcruise.gaming.model.game.Players;
import org.nkjmlab.util.java.concurrent.ExecutorServiceUtils;

public class SingleTaskLatch implements TaskLatch {

	protected static Logger log = LogManager.getLogger();

	protected StageTask stageTask;
	protected ExecutorService executorService;
	protected EventDrivenTasks rootEventDrivenTasks;
	protected EventDrivenTasks eventDrivenTasksInThisStage;
	protected CountDownLatch latch;

	protected Players<? extends Player> players;
	protected volatile boolean finished = false;

	private final long firstCount;

	public SingleTaskLatch(StageTask stageTask, ExecutorService executorService,
			EventDrivenTasks eventDrivenTasksInThisStage, CountDownLatch latch) {
		this.stageTask = stageTask;
		this.executorService = executorService;
		this.eventDrivenTasksInThisStage = eventDrivenTasksInThisStage;
		this.latch = latch;
		this.firstCount = latch.getCount();
	}

	@Override
	public synchronized void countDown(ActorName name) {
		if (finished) {
			return;
		}
		this.latch.countDown();
		stageTask.countDownedBy(name);
		finished = true;
	}

	@Override
	public synchronized void finish(ActorName name) {
		if (finished) {
			return;
		}
		countDown(name);
	}

	public void setPlayers(Players<? extends Player> players) {
		this.players = players;
	}

	@Override
	public synchronized void finishStage(ActorName name) {
		if (finished) {
			return;
		}
		finish(name);
		if (stageTask.isFinishied()) {
			return;
		}
		eventDrivenTasksInThisStage.finishAll(name);
		ExecutorServiceUtils.shutdownAndAwaitTermination(executorService, 10, TimeUnit.MILLISECONDS);
		while (!repeatCountDown()) {

		}
		stageTask.setFinished(true);
	}

	private boolean repeatCountDown() {
		try {
			while (!latch.await(0, TimeUnit.NANOSECONDS)) {
				latch.countDown();
			}
			return true;
		} catch (InterruptedException e) {
			return false;
		}

	}

	public boolean isFinishied() {
		return finished;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("latch", latch).append("firstCount", firstCount)
				.append("count", latch.getCount())
				.append("countDownedBy", stageTask.getCountDownedBy())
				.toString();
	}

}
