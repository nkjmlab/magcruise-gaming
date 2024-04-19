package org.magcruise.gaming.model.def.scenario.stage;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.def.scenario.round.RoundElement;
import org.magcruise.gaming.model.def.scenario.task.DefCall;
import org.magcruise.gaming.model.def.scenario.task.DefTask;
import org.magcruise.gaming.model.def.scenario.task.MultipleDefTasks;
import org.magcruise.gaming.model.def.scenario.task.StageElement;
import org.magcruise.gaming.model.game.Context;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public abstract class DefStage implements RoundElement, DefTask {
	protected static Logger log = LogManager.getLogger();

	protected Symbol name;
	protected List<DefTask> defTasks = new ArrayList<>();
	protected List<DefCall> defBeforeCalls = new ArrayList<>();
	protected List<DefCall> defAfterCalls = new ArrayList<>();

	public DefStage() {
		this.name = Symbol.parse("stage-" + String.valueOf(hashCode()));
	}

	public DefStage(Symbol name) {
		this.name = name;
	}

	public DefStage(List<StageElement> defStageElements) {
		this();
		defStageElements.forEach(e -> procStageElement(e));
	}

	public DefStage(Symbol name, List<StageElement> defStageElements) {
		this(name);
		defStageElements.forEach(e -> procStageElement(e));
	}

	public DefStage(Symbol name, List<DefTask> defTasks, List<DefCall> defBeforeFilters,
			List<DefCall> defAfterFilters) {
		this(name);
		this.defTasks.addAll(defTasks);
		this.defBeforeCalls.addAll(defBeforeFilters);
		this.defAfterCalls.addAll(defAfterFilters);
	}

	@Override
	public SConstructor<? extends DefStage> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(
				(style == ToExpressionStyle.MULTI_LINE ? System.lineSeparator() + "      " : "")
						+ SConstructor.toConstructor(style, getClass(), name, defTasks,
								defBeforeCalls,
								defAfterCalls));
	}

	private void procStageElement(StageElement e) {
		if (e instanceof MultipleDefStages) {
			MultipleDefStages me = (MultipleDefStages) e;
			for (DefStage d : me.values()) {
				procStageElement(d);
			}
		} else if (e instanceof MultipleDefTasks) {
			MultipleDefTasks me = (MultipleDefTasks) e;
			this.defTasks.addAll(me.values());
		} else if (e instanceof DefTask) {
			this.defTasks.add((DefTask) e);
		} else if (e instanceof DefCall) {
			if (defTasks.isEmpty()) {
				this.defBeforeCalls.add((DefCall) e);
			} else {
				this.defAfterCalls.add((DefCall) e);
			}
		} else {
			throw new IllegalArgumentException(e + " is not supported.");
		}

	}

	public void add(DefTask e) {
		defTasks.add(e);
	}

	public void add(DefCall e) {
		defBeforeCalls.add(e);
	}

	public StageElement getTask(Context ctx, int elementNum) {
		return this.getActiveTasks(ctx).get(elementNum);
	}

	/**
	 * elementNum番目の要素にアクセスしたときに要素が獲得できるか．
	 *
	 * @param elementNum
	 * @return
	 */
	public boolean hasTask(Context ctx, int elementNum) {
		return elementNum < getActiveTasks(ctx).size();
	}

	/**
	 * TODO event-driven-taskをsequential-stage内で許すならば，それを前に集める様にソートするか， そのような順序で記述するように明記しなければならない．
	 *
	 * @param ctx
	 * @return
	 */
	public List<DefTask> getActiveTasks(Context ctx) {
		return this.defTasks;
	}

	public List<DefTask> getTasks() {
		return this.defTasks;
	}

	public List<DefCall> getBeforeCalls() {
		return this.defBeforeCalls;
	}

	public List<DefCall> getAfterCalls() {
		return this.defAfterCalls;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public Symbol getName() {
		return name;
	}

}
