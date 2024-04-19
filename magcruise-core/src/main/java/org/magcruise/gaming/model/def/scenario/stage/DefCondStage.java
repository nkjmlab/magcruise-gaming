package org.magcruise.gaming.model.def.scenario.stage;

import java.util.ArrayList;
import java.util.List;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.model.def.scenario.task.DefCall;
import org.magcruise.gaming.model.def.scenario.task.DefTask;
import org.magcruise.gaming.model.def.scenario.task.StageElement;
import org.magcruise.gaming.model.game.Context;
import gnu.lists.LList;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class DefCondStage extends DefSequentialStage {

	protected List<Symbol> condProcSymbols = new ArrayList<>();

	public DefCondStage(Symbol name, LList conds) {
		super(name);
		for (Object o : conds) {
			this.condProcSymbols.add((Symbol) o);
		}
	}

	public DefCondStage(Symbol name, LList conds, List<StageElement> defStageElements) {
		super(name, defStageElements);
		conds.forEach((cond) -> {
			this.condProcSymbols.add((Symbol) cond);
		});
	}

	public DefCondStage(Symbol name, List<DefTask> defTasks, List<DefCall> defBeforeFilters,
			List<DefCall> defAfterFilters) {
		super(name, defTasks, defBeforeFilters, defAfterFilters);
	}

	@Override
	public SConstructor<? extends DefSequentialStage> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), name, LList.makeList(condProcSymbols),
				defTasks);
	}

	@Override
	public List<DefTask> getActiveTasks(Context ctx) {
		int matchedIdx = condProcSymbols.size();
		try {

			for (int i = 0; i < condProcSymbols.size(); i++) {
				if ((boolean) SchemeEnvironment.applyProcedure(ctx.getEnvironmentName(),
						condProcSymbols.get(i).toString(), ctx)) {
					matchedIdx = i;
					break;
				}
			}
		} catch (Throwable e) {
			log.error(e, e);
		}
		List<DefTask> result = new ArrayList<>();
		result.add(this.defTasks.get(matchedIdx));
		return result;
	}

}
