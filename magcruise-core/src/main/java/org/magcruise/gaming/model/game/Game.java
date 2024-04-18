package org.magcruise.gaming.model.game;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.def.scenario.GameScenario;

@SuppressWarnings("serial")
public class Game implements SConstructive {

	/**
	 * ゲームシナリオからアクセスしやすいように特別にpublicにしている．ゲームシナリオ以外からはgetterでアクセスすること．
	 */
	public Context context;
	/**
	 * ゲームシナリオからアクセスしやすいように特別にpublicにしている．ゲームシナリオ以外からはgetterでアクセスすること．
	 */
	public GameScenario scenario;

	public Game(Context context, GameScenario scenario) {
		this.context = context;
		this.scenario = scenario;
	}

	@Override
	public SConstructor<? extends Game> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), context, scenario);
	}

	public Context getContext() {
		return context;
	}

	public GameScenario getScenario() {
		return scenario;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
