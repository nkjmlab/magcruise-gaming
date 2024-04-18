package org.magcruise.gaming.manager.process;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.executor.api.GameProcessService;
import org.magcruise.gaming.executor.web.GameProcessServiceClient;
import org.magcruise.gaming.model.def.boot.DefBootstrapScript;

public class GameOnServerProcess implements GameProcess {

	protected static Logger log = LogManager.getLogger();

	protected GameProcessService processServiceClient;
	protected DefBootstrapScript defBootstrapScript;

	protected ProcessId processId;

	public GameOnServerProcess(String brokerUrl, DefBootstrapScript defBootstrapScript) {
		this.processServiceClient = new GameProcessServiceClient(brokerUrl);
		this.defBootstrapScript = defBootstrapScript;
	}

	@Override
	public Object call() throws Exception {
		start();
		return null;
	}

	@Override
	public int waitFor() {
		while (!processServiceClient.isFinished(getProcessId().toString())) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.warn(e, e);
			}
		}
		return 0;
	}

	@Override
	public void start() {
		processId = new ProcessId(
				processServiceClient
						.createGameProcess(defBootstrapScript.getScript()));
	}

	@Override
	public ProcessId getProcessId() {
		return processId;
	}

	@Override
	public String getLatestRecord(ProcessId processId) {
		return processServiceClient.getLatestRecord(processId.toString());
	}

}
