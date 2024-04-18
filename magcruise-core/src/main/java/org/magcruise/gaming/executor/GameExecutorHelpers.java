package org.magcruise.gaming.executor;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class GameExecutorHelpers implements SConstructive {

	protected static Logger log = LogManager.getLogger();

	private List<GameExecutorHelper> executorHelpers = new ArrayList<>();

	public GameExecutorHelpers() {
		this(new ArrayList<>());
	}

	public GameExecutorHelpers(List<GameExecutorHelper> executorHelpers) {
		this.executorHelpers.addAll(executorHelpers);
	}

	public void add(GameExecutorHelper executorHelper) {
		executorHelpers.add(executorHelper);
	}

	@Override
	public SConstructor<? extends GameExecutorHelpers> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), executorHelpers);
	}

	public void start() {
		executorHelpers.forEach(e -> e.start());
	}

	public void shutdownForceAfterAwaiting() {
		executorHelpers.forEach(e -> e.shutdownForceAfterAwating());
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
