package org.magcruise.gaming.model.def.scenario.task;

import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.message.GameEvent;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class DefEventDrivenTask extends DefPlayerTask {

	private Class<? extends GameEvent> handleEventClass;

	public DefEventDrivenTask(ActorName playerName, Symbol procName,
			Class<? extends GameEvent> handleEventClass) {
		super(playerName, procName);
		this.handleEventClass = handleEventClass;
	}

	public Class<? extends GameEvent> getHandleEventClass() {
		return handleEventClass;
	}

	@Override
	public SConstructor<? extends DefPlayerTask> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), playerName, procName,
				handleEventClass);
	}
}
