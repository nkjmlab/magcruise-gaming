package org.magcruise.gaming.manager.session;

import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.manager.process.NoGameScenarioGameProcessBuilder;
import org.magcruise.gaming.manager.process.ProcessId;

@SuppressWarnings("serial")
public class NoGameScenarioGameSession extends GameSession {

	public NoGameScenarioGameSession() {
		super(ResourceLoader.class);
	}

	public NoGameScenarioGameSession(Class<? extends ResourceLoader> clazz) {
		super(clazz);
	}

	@Override
	public ProcessId start() {
		checkExecuted();
		buildIfNotBuilt();
		process = new NoGameScenarioGameProcessBuilder(getGameBuilder()).build();
		process.start();
		executed = true;
		return getProcessId();
	}

	public Object eval(String sexper) {
		return SchemeEnvironment.eval(getEnvironmentName(), sexper);
	}

}
