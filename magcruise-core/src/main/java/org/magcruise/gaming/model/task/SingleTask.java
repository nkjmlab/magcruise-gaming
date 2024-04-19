package org.magcruise.gaming.model.task;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.model.def.scenario.task.DefSingleTask;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.Context;

public abstract class SingleTask implements Task {

	protected static Logger log = LogManager.getLogger();

	protected SingleTaskLatch latch;
	protected Context context;
	protected DefSingleTask task;

	public SingleTask(SingleTaskLatch latch, Context context, DefSingleTask task) {
		this.latch = latch;
		this.context = context;
		this.task = task;
	}

	public ActorName getPlayerName() {
		return task.getPlayerName();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
