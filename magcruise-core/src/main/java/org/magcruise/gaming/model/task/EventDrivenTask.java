package org.magcruise.gaming.model.task;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.model.def.scenario.task.DefEventDrivenTask;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.model.game.Player;
import org.magcruise.gaming.model.game.message.GameEvent;
import org.nkjmlab.util.java.lang.reflect.ReflectionMethodInvoker;
import gnu.mapping.Procedure;
import gnu.mapping.Symbol;

public class EventDrivenTask<T extends GameEvent> extends SingleTask {

	protected Queue<T> events = new ConcurrentLinkedQueue<>();

	public EventDrivenTask(SingleTaskLatch latch, Context context, DefEventDrivenTask task,
			EventDrivenTasks rootEventDrivenTasks) {
		super(latch, context, task);
		this.latch = new EventDrivenTaskLatch(latch, this, rootEventDrivenTasks);
	}

	@SuppressWarnings("unchecked")
	public void offer(GameEvent event) {
		synchronized (events) {
			events.offer((T) event);
		}
	}

	@Override
	public Object call() throws Exception {
		try {
			if (latch.isFinishied()) {
				return null;
			}

			log.debug("START of " + task);
			latch.setPlayers(context.getPlayers());
			T msg;
			synchronized (events) {
				msg = events.poll();
			}
			callMethod(task.getProcName(), context.getPlayer(task.getPlayerName()), msg);
			log.debug("Latch countdowned={}, at task={}", latch, task);
			log.info("END of task: {}", task);
		} catch (Exception e) {
			log.error(System.lineSeparator() + "    Roundnum: {}. An exception occurred in: {}",
					context.getRoundnum(), this.task);
			log.error(e, e);
			throw e;
		}
		return null;
	}

	private void callMethod(Symbol procedureName, Player self, T msg) throws Exception {
		if (SchemeEnvironment.isDefined(context.getEnvironmentName(), procedureName.toString())) {
			Procedure p = (Procedure) SchemeEnvironment.eval(context.getEnvironmentName(),
					procedureName.toString());
			if (p.maxArgs() == 4) {
				SchemeEnvironment.applyProcedure(context.getEnvironmentName(),
						procedureName.toString(),
						context, self, msg, latch);
			} else {
				SchemeEnvironment.applyProcedure(context.getEnvironmentName(),
						procedureName.toString(),
						context, self, msg);
				latch.countDown(self.getName());
				log.debug("Latch countdowned={}, at task={}", latch, task);
			}
		} else {
			if (ReflectionMethodInvoker.isDefined(self, procedureName.toString(), context, msg,
					latch)) {
				ReflectionMethodInvoker.invoke(self, procedureName.toString(), context, msg, latch);
			} else {
				ReflectionMethodInvoker.invoke(self, procedureName.toString(), context, msg);
				latch.countDown(self.getName());
				log.debug("Latch countdowned={}, at task={}", latch, task);
			}
		}

	}

	protected EventDrivenTaskLatch getLatch() {
		return (EventDrivenTaskLatch) latch;
	}

	public Class<? extends GameEvent> getHandleEventClass() {
		DefEventDrivenTask t = (DefEventDrivenTask) task;
		return t.getHandleEventClass();
	}

	public void finish(ActorName name) {
		getLatch().finish(name);
	}

}
