package org.magcruise.gaming.model.def.scenario.task;

import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.ActorName;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class DefContextTask extends DefSingleTask {

	public DefContextTask(Symbol procName) {
		super(ActorName.NON_PLAYER, procName);
	}

	@Override
	public SConstructor<? extends DefContextTask> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), procName);
	}

}
