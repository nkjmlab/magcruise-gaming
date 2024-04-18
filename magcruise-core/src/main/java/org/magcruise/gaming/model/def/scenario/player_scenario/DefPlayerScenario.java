package org.magcruise.gaming.model.def.scenario.player_scenario;

import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gnu.lists.Pair;
import gnu.mapping.Symbol;

public class DefPlayerScenario {

	protected static Logger log = LogManager.getLogger();

	private List<DefScene> defScenes = new ArrayList<>();

	private Symbol scenarioName;

	public DefPlayerScenario(Symbol sceneName, Pair scenes) {
		this.scenarioName = sceneName;
		for (Object ds : scenes) {
			this.defScenes.add((DefScene) ds);
		}
	}

	public Symbol getScenarioName() {
		return scenarioName;
	}

	public List<DefScene> getDefScenes() {
		return defScenes;
	}

}
