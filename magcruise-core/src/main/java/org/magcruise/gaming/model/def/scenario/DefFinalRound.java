package org.magcruise.gaming.model.def.scenario;

import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class DefFinalRound implements DefGameScenarioProperty {

	private int roundnum;

	public DefFinalRound(int roundnum) {
		this.roundnum = roundnum;
	}

	@Override
	public SConstructor<? extends DefFinalRound> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(
				getIndent(style) + SConstructor.toConstructor(style, getClass(), roundnum));
	}

	public int getRoundnum() {
		return roundnum;
	}

}
