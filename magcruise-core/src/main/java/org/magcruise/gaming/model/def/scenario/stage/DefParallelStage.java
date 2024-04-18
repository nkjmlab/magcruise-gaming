package org.magcruise.gaming.model.def.scenario.stage;

import java.util.Arrays;
import java.util.List;
import org.magcruise.gaming.model.def.scenario.task.DefCall;
import org.magcruise.gaming.model.def.scenario.task.DefTask;
import org.magcruise.gaming.model.def.scenario.task.StageElement;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class DefParallelStage extends DefStage {

	public DefParallelStage() {
		super();
	}

	public DefParallelStage(Symbol name) {
		super(name);
	}

	public DefParallelStage(List<StageElement> defStageElements) {
		super(defStageElements);
	}

	public DefParallelStage(StageElement... defStageElements) {
		this(Arrays.asList(defStageElements));
	}

	public DefParallelStage(Symbol name, List<StageElement> defStageElements) {
		super(name, defStageElements);
	}

	public DefParallelStage(Symbol name, List<DefTask> defTasks, List<DefCall> defBeforeFilters,
			List<DefCall> defAfterFilters) {
		super(name, defTasks, defBeforeFilters, defAfterFilters);
	}

}
