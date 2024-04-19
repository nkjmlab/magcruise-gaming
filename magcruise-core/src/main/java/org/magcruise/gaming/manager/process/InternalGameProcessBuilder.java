package org.magcruise.gaming.manager.process;

import org.magcruise.gaming.model.def.GameBuilder;

public class InternalGameProcessBuilder extends GameProcessBuilder {

	public InternalGameProcessBuilder(GameBuilder gameBuilder) {
		super(gameBuilder);
	}

	@Override
	public InternalGameProcess build() {
		InternalGameProcess process = new InternalGameProcess(gameBuilder);
		return process;
	}

}
