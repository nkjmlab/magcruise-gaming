package org.magcruise.gaming.manager.process;

import java.util.concurrent.Callable;

public interface GameProcess extends Callable<Object> {

	int waitFor();

	void start();

	ProcessId getProcessId();

	String getLatestRecord(ProcessId processId);

}
