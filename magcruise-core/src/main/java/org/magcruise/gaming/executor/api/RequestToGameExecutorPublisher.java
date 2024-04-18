package org.magcruise.gaming.executor.api;

import org.magcruise.gaming.executor.api.message.RequestToGameExecutorTransferObject;

public interface RequestToGameExecutorPublisher {

	public RequestToGameExecutorTransferObject[] getNewRequestsToGameExecutor(String processId);

}
