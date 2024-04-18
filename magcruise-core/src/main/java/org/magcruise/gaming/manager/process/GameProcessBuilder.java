package org.magcruise.gaming.manager.process;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.model.def.GameBuilder;

public abstract class GameProcessBuilder {

	protected static Logger log = LogManager.getLogger();

	protected GameBuilder gameBuilder;

	public abstract GameProcess build();

	public GameProcessBuilder(GameBuilder gameBuilder) {
		this.gameBuilder = gameBuilder;
	}

	public ProcessId getProcessId() {
		return gameBuilder.getProcessId();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
