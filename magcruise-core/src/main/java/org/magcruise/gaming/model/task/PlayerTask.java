package org.magcruise.gaming.model.task;

import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.model.def.scenario.task.DefSingleTask;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.model.game.Player;
import org.magcruise.gaming.model.game.TaskInterruptedException;
import org.nkjmlab.util.java.lang.reflect.ReflectionMethodInvoker;
import gnu.mapping.Procedure;
import gnu.mapping.Symbol;

public class PlayerTask extends SingleTask {

	public PlayerTask(SingleTaskLatch latch, Context context, DefSingleTask task) {
		super(latch, context, task);
	}

	@Override
	public Object call() throws Exception {
		try {
			if (latch.isFinishied()) {
				return null;
			}
			log.info("START of task: {}", task);
			latch.setPlayers(context.getPlayers());
			callMethod(task.getProcName(), context.getPlayer(task.getPlayerName()));
			log.info("END of task: {}", task);
		} catch (Exception e) {
			if (e.getCause() instanceof TaskInterruptedException) {
				log.debug("Task is interrupted = {}", this);
			}
			log.error(System.lineSeparator() + "    Roundnum: {}. An exception occurred in: {}",
					context.getRoundnum(), this.task);
			log.error(e, e);
			throw e;
		}
		return null;
	}

	private void callMethod(Symbol procedureName, Player self) throws Exception {
		if (SchemeEnvironment.isDefined(context.getEnvironmentName(), procedureName.toString())) {
			Procedure p = (Procedure) SchemeEnvironment.eval(context.getEnvironmentName(),
					procedureName.toString());
			if (p.maxArgs() == 3) {
				SchemeEnvironment.applyProcedure(context.getEnvironmentName(),
						procedureName.toString(),
						context, self, latch);
			} else {
				SchemeEnvironment.applyProcedure(context.getEnvironmentName(),
						procedureName.toString(),
						context, self);
				latch.countDown(self.getName());
				log.debug("Latch countdowned={}, at task={}", latch, task);
			}
		} else {
			if (ReflectionMethodInvoker.isDefined(self, procedureName.toString(), context, latch)) {
				ReflectionMethodInvoker.invoke(self, procedureName.toString(), context, latch);
			} else {
				ReflectionMethodInvoker.invoke(self, procedureName.toString(), context);
				latch.countDown(self.getName());
				log.debug("Latch countdowned={}, at task={}", latch, task);
			}
		}
	}
}
