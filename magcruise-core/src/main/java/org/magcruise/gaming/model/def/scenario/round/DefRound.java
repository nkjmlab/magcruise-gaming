package org.magcruise.gaming.model.def.scenario.round;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.def.scenario.DefGameScenarioProperty;
import org.magcruise.gaming.model.def.scenario.stage.DefStage;
import org.magcruise.gaming.model.def.scenario.stage.MultipleDefStages;
import org.magcruise.gaming.model.def.scenario.task.StageElement;
import org.magcruise.gaming.model.game.Context;

@SuppressWarnings("serial")
public class DefRound implements DefScenarioElements, DefGameScenarioProperty {

	protected String name;
	protected List<DefStage> defStages = new ArrayList<>();

	public DefRound() {
		this.name = String.valueOf("round-" + hashCode());
	}

	public DefRound(String name) {
		this.name = name;
	}

	public DefRound(RoundElement... defStages) {
		this();
		addToDefStages(Arrays.asList(defStages));
	}

	public DefRound(String name, RoundElement... defStages) {
		this(name);
		addToDefStages(Arrays.asList(defStages));
	}

	public DefRound(String name, List<? extends RoundElement> defStages) {
		this(name);
		addToDefStages(defStages);
	}

	private void addToDefStages(List<? extends RoundElement> defStages) {
		defStages.forEach(re -> {
			if (re instanceof MultipleDefStages) {
				((MultipleDefStages) re).values().forEach(stage -> {
					this.defStages.add(stage);
				});

			} else if (re instanceof DefStage) {
				this.defStages.add((DefStage) re);
			}

		});

	}

	@Override
	public SConstructor<? extends DefRound> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(
				(style == ToExpressionStyle.MULTI_LINE ? System.lineSeparator() + "    " : "")
						+ SConstructor.toConstructor(style, getClass(), name, defStages));
	}

	@Override
	public SConstructor<? extends DefRound> toConstructor() {
		return toConstructor(ToExpressionStyle.DEFAULT);
	}

	public void add(RoundElement e) {
		if (e instanceof MultipleDefStages) {
			for (RoundElement r : ((MultipleDefStages) e).values()) {
				defStages.add((DefStage) r);
			}
		} else {
			defStages.add((DefStage) e);
		}
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public List<DefStage> getAllStages() {
		return this.defStages;
	}

	public DefStage getStage(int stageNum) {
		return this.defStages.get(stageNum);
	}

	public StageElement getStageElement(Context ctx, int stageNum, int elementNum) {
		return getStage(stageNum).getTask(ctx, elementNum);
	}

	public boolean hasNextStageElement(Context ctx, int stageNum, int elementNum) {
		return getStage(stageNum).hasTask(ctx, elementNum);
	}

	public boolean hasStage(int stageNum) {
		return stageNum < defStages.size();
	}

	public String getName() {
		return name;
	}

	public List<DefStage> getDefStages() {
		return defStages;
	}

}
