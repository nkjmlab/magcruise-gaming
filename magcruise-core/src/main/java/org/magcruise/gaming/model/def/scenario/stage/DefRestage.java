package org.magcruise.gaming.model.def.scenario.stage;

import java.util.List;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.def.scenario.task.DefCall;
import org.magcruise.gaming.model.def.scenario.task.DefTask;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class DefRestage extends DefStage {

	private Class<? extends DefStage> originalClass;

	public DefRestage(Symbol name) {
		super(name);
	}

	public DefRestage(Symbol name, List<DefTask> defTasks, List<DefCall> defBeforeActions,
			List<DefCall> defAfterActions, Class<? extends DefStage> originalClass) {
		super(name, defTasks, defBeforeActions, defAfterActions);
		this.originalClass = originalClass;
	}

	@Override
	public SConstructor<? extends DefRestage> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), name, defTasks, defBeforeCalls,
				defAfterCalls, originalClass);
	}

	public void setOriginalClass(Class<? extends DefStage> clazz) {
		this.originalClass = clazz;
	}

	public Class<? extends DefStage> getOriginalClass() {
		return originalClass;
	}
}
