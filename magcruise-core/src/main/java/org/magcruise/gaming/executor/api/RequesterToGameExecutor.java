package org.magcruise.gaming.executor.api;

import org.magcruise.gaming.executor.api.message.RequestToGameExecutor;
import org.magcruise.gaming.manager.process.ProcessId;

/**
 *
 * Executorへの依頼を行う
 *
 */
public interface RequesterToGameExecutor {

	public void request(ProcessId processId, RequestToGameExecutor message) throws Throwable;

}
