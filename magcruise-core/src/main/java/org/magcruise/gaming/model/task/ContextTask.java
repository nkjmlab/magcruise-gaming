package org.magcruise.gaming.model.task;

import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.model.def.scenario.task.DefSingleTask;
import org.magcruise.gaming.model.game.Context;
import org.nkjmlab.util.java.lang.reflect.ReflectionMethodInvoker;
import gnu.mapping.Procedure;
import gnu.mapping.Symbol;

public class ContextTask extends SingleTask {

	public ContextTask(SingleTaskLatch latch, Context context, DefSingleTask task) {
		super(latch, context, task);
	}

	@Override
	public Object call() throws Exception {
		try {
			if (latch.isFinishied()) {
				return null;
			}

			log.debug("start of eval non-player-task: " + task);
			latch.setPlayers(context.getPlayers());
			callMethod(task.getProcName());
			log.debug("end of eval non-player-task: " + task);

		} catch (Exception e) {
			log.error(System.lineSeparator() + "An exception occurred in: " + this.task
					+ System.lineSeparator());
			log.error(e, e);
			throw e;
		}
		return null;
	}

	private void callMethod(Symbol procedureName) throws Exception {
		if (SchemeEnvironment.isDefined(context.getEnvironmentName(), procedureName.toString())) {
			Procedure p = (Procedure) SchemeEnvironment.eval(context.getEnvironmentName(),
					procedureName.toString());
			if (p.maxArgs() == 2) {
				SchemeEnvironment.applyProcedure(context.getEnvironmentName(),
						procedureName.toString(),
						context, latch);
			} else {
				SchemeEnvironment.applyProcedure(context.getEnvironmentName(),
						procedureName.toString(),
						context);
				latch.countDown(context.getName());
				log.debug("Latch countdowned={}, at task={}", latch, task);
			}
		} else {
			if (ReflectionMethodInvoker.isDefined(context, procedureName.toString(), latch)) {
				ReflectionMethodInvoker.invoke(context, procedureName.toString(), latch);
			} else {
				ReflectionMethodInvoker.invoke(context, procedureName.toString());
				latch.countDown(context.getName());
				log.debug("Latch countdowned={}, at task={}", latch, task);
			}
		}

	}
}
