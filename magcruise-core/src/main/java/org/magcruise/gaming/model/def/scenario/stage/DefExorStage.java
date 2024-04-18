package org.magcruise.gaming.model.def.scenario.stage;

import java.util.ArrayList;
import java.util.List;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.model.def.scenario.task.DefCall;
import org.magcruise.gaming.model.def.scenario.task.DefTask;
import org.magcruise.gaming.model.def.scenario.task.StageElement;
import org.magcruise.gaming.model.game.Context;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class DefExorStage extends DefSequentialStage {

	protected Symbol condProcName;

	public DefExorStage(Symbol name, Symbol condProcName) {
		super(name);
		this.condProcName = condProcName;
	}

	public DefExorStage(Symbol name, Symbol condProcName, List<StageElement> defStageElements) {
		super(name, defStageElements);
		this.condProcName = condProcName;
	}

	public DefExorStage(Symbol name, List<DefTask> defTasks, List<DefCall> defBeforeFilters,
			List<DefCall> defAfterFilters) {
		super(name, defTasks, defBeforeFilters, defAfterFilters);
	}

	@Override
	public SConstructor<? extends DefSequentialStage> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), name, condProcName, defTasks);
	}

	@Override
	public List<DefTask> getActiveTasks(Context ctx) {
		List<DefTask> result = new ArrayList<>();
		try {
			if ((boolean) SchemeEnvironment.applyProcedure(ctx.getEnvironmentName(),
					condProcName.toString(), ctx)) {
				result.add(this.defTasks.get(0));
			} else {
				result.add(this.defTasks.get(1));
			}
		} catch (Throwable e) {
			log.error(e, e);
		}
		return result;
	}

}
