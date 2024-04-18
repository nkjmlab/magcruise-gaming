package org.magcruise.gaming.model.def.scenario.player_scenario;

import java.util.ArrayList;
import java.util.List;
import org.magcruise.gaming.lang.exception.ApplyException;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.model.game.Player;
import org.magcruise.gaming.model.game.message.ScenarioEvent;
import gnu.lists.Pair;
import gnu.mapping.Symbol;

public class DefScene {

	private Symbol name;
	private List<DefBehavior> defBehaviors = new ArrayList<DefBehavior>();

	public DefScene(Symbol name, Pair behaviors) {
		this.name = name;
		for (Object db : behaviors) {
			this.defBehaviors.add((DefBehavior) db);
		}

	}

	public DefBehavior interpretScene(Context context, Player player, ScenarioEvent msg)
			throws ApplyException {
		for (DefBehavior behavior : defBehaviors) {
			if (behavior.interpretBehavior(context, player, msg)) {
				return behavior;
			}
		}

		return null;
	}

	public Symbol getName() {
		return name;
	}

	@Override
	public String toString() {
		return name + "," + defBehaviors;
	}

}
