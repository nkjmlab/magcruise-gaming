package org.magcruise.gaming.model.def.scenario.stage;

import java.util.Arrays;
import java.util.List;
import org.magcruise.gaming.model.def.scenario.task.DefCall;
import org.magcruise.gaming.model.def.scenario.task.DefTask;
import org.magcruise.gaming.model.def.scenario.task.StageElement;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class DefSequentialStage extends DefStage {

	public DefSequentialStage() {
		super();
	}

	public DefSequentialStage(Symbol name) {
		super(name);
	}

	public DefSequentialStage(String name) {
		super(Symbol.parse(name));
	}

	public DefSequentialStage(Symbol name, List<StageElement> defStageElements) {
		super(name, defStageElements);
	}

	public DefSequentialStage(List<StageElement> defStageElements) {
		super(defStageElements);
	}

	public DefSequentialStage(Symbol name, StageElement... defStageElements) {
		super(name, Arrays.asList(defStageElements));
	}

	public DefSequentialStage(StageElement... defStageElements) {
		super(Arrays.asList(defStageElements));
	}

	public DefSequentialStage(Symbol name, List<DefTask> defTasks, List<DefCall> defBeforeFilters,
			List<DefCall> defAfterFilters) {
		super(name, defTasks, defBeforeFilters, defAfterFilters);
	}
}
