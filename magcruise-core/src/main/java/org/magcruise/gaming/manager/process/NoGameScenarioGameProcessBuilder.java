package org.magcruise.gaming.manager.process;

import org.magcruise.gaming.model.def.GameBuilder;

public class NoGameScenarioGameProcessBuilder extends GameProcessBuilder {

	public NoGameScenarioGameProcessBuilder(GameBuilder gameBuilder) {
		super(gameBuilder);
	}

	@Override
	public NoGameScenarioGameProcess build() {
		NoGameScenarioGameProcess process = new NoGameScenarioGameProcess(gameBuilder);
		return process;
	}

}
