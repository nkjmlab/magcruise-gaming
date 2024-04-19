package org.magcruise.gaming.model.def.scenario.task;

import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.message.GameEvent;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class DefPlayerScenarioTask extends DefEventDrivenTask {

	public DefPlayerScenarioTask(ActorName playerName, Symbol procName,
			Class<? extends GameEvent> handleEventClass) {
		super(playerName, procName, handleEventClass);
	}
}
