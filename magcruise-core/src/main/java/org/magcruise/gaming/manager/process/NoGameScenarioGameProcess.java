package org.magcruise.gaming.manager.process;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.manager.GameExecutorManager;
import org.magcruise.gaming.model.def.GameBuilder;

public class NoGameScenarioGameProcess extends InternalGameProcess {
	protected static Logger log = LogManager.getLogger();

	public NoGameScenarioGameProcess(GameBuilder gameBuilder) {
		super(gameBuilder);
	}

	@Override
	public void start() {
		executor = GameExecutorManager.getInstance().createAndRegisterGameExecutor(gameBuilder);
		executor.beforeStart();
	}

}
