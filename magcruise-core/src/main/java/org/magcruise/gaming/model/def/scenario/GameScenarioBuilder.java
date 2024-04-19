package org.magcruise.gaming.model.def.scenario;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.model.def.scenario.round.DefRound;
import org.magcruise.gaming.model.def.scenario.round.MultipleDefRounds;
import org.magcruise.gaming.model.def.scenario.stage.DefRestage;
import org.magcruise.gaming.model.def.scenario.stage.DefStage;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class GameScenarioBuilder implements SConstructive {

	private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger();

	private List<Object> defRounds = new ArrayList<>();
	private List<DefGameScenarioProperty> properties = new ArrayList<>();

	public GameScenarioBuilder() {
	}

	public GameScenarioBuilder(List<DefRound> defRounds, List<DefGameScenarioProperty> props) {
		this.defRounds.addAll(defRounds);
		this.properties.addAll(props);
	}

	@Override
	public SConstructor<? extends GameScenarioBuilder> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(
				(style == ToExpressionStyle.MULTI_LINE ? System.lineSeparator() + "  " : "")
						+ SConstructor.toConstructor(style, getClass(), defRounds, properties));
	}

	public GameScenarioBuilder addProperties(DefGameScenarioProperty... props) {
		Arrays.asList(props).forEach(p -> {
			if (p instanceof DefRound) {
				this.defRounds.add(p);
			} else {
				properties.add(p);
			}

		});
		return this;
	}

	public void addRounds(Object... defRounds) {
		this.defRounds.addAll(Arrays.asList(defRounds));
	}

	private void addRound(List<DefRound> result, Map<Symbol, DefStage> defStages,
			DefRound defRound) {
		result.add(defRound);
		defRound.getDefStages().forEach(defStage -> {
			if (defStage instanceof DefRestage) {
				// rastageが現れたら，最初だけ同名のstageの要素を追加する．繰り返すと繰り返しただけ要素が追加されてしまうことに注意．
				if (defStage.getTasks().size() > 0) {
					return;
				}
				DefStage registeredStage = defStages.get(defStage.getName());
				((DefRestage) defStage).setOriginalClass(registeredStage.getClass());
				registeredStage.getTasks().forEach(e -> {
					defStage.add(e);
				});
			} else {
				defStages.put(defStage.getName(), defStage);
			}
		});

	}

	private List<DefRound> createDefRounds() {
		List<DefRound> result = new ArrayList<>();
		Map<Symbol, DefStage> defStages = new HashMap<>();
		createDefRounds(result, defStages, new ArrayList<>(defRounds));
		return result;
	}

	private void createDefRounds(List<DefRound> result, Map<Symbol, DefStage> defStages,
			Object defRound) {
		SchemeEnvironment.acceptOnTemporalEnvironment((environmentName) -> {
			if (defRound instanceof MultipleDefRounds) {
				((MultipleDefRounds) defRound).values()
						.forEach(r -> createDefRounds(result, defStages, r));
			} else if (defRound instanceof DefRound) {
				try {
					SConstructor<? extends DefRound> tmp = ((DefRound) defRound).toConstructor();
					DefRound df = tmp.makeInstance(environmentName);
					addRound(result, defStages, new DefRound(df.getName(), df.getAllStages()));
				} catch (Exception e) {
					log.error(e, e);
				}
			} else if (defRound instanceof Collection) {
				((Collection<?>) defRound).forEach(dr -> {
					createDefRounds(result, defStages, dr);
				});
			} else {
				throw new IllegalArgumentException(
						"A class in args is invalid. " + defRound.getClass().getName());
			}
		});
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public GameScenario build() {
		GameScenario scenario = new GameScenario(createDefRounds(), getFinalRound());
		return scenario;
	}

	public int getFinalRound() {
		for (DefGameScenarioProperty p : properties) {
			if (p instanceof DefFinalRound) {
				return ((DefFinalRound) p).getRoundnum();
			}
		}
		return -1;
	}

}
