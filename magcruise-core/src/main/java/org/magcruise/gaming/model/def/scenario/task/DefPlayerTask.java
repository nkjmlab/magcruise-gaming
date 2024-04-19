package org.magcruise.gaming.model.def.scenario.task;

import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.ActorName;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class DefPlayerTask extends DefSingleTask {

	public DefPlayerTask(Object o1, Object o2) {
		super(null, null);
	}

	public DefPlayerTask(ActorName playerName, Symbol procName) {
		super(playerName, procName);
	}

	public DefPlayerTask(Symbol playerName, Symbol procName) {
		super(ActorName.of(playerName), procName);
	}

	public DefPlayerTask(String playerName, String procName) {
		super(ActorName.of(playerName), Symbol.parse(procName));
	}

	@Override
	public SConstructor<? extends DefPlayerTask> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), playerName, procName);
	}

}
