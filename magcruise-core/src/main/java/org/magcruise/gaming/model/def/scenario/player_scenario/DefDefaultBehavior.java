package org.magcruise.gaming.model.def.scenario.player_scenario;

import org.magcruise.gaming.lang.exception.ApplyException;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.model.game.Player;
import org.magcruise.gaming.model.game.message.ScenarioEvent;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class DefDefaultBehavior extends DefBehavior {

	public DefDefaultBehavior(Symbol cue, Symbol action, Symbol nextSceneName) {
		super(cue, action, nextSceneName);
	}

	@Override
	public boolean interpretBehavior(Context context, Player self, ScenarioEvent msg)
			throws ApplyException {
		procAction(self, context, msg);
		return true;
	}

}
