package org.magcruise.gaming.ui;

import org.magcruise.gaming.executor.api.message.ReceiveInput;
import org.magcruise.gaming.manager.GameExecutorManager;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.ui.api.message.RequestToInput;

public class DefaultAutoInputer {

	public static void requestAutoInput(ProcessId processId, RequestToInput rmsg) {
		GameExecutorManager.getInstance().request(processId,
				new ReceiveInput(rmsg.getId(), rmsg.getPlayerId(), rmsg.getInputToUIs()));

	}

}
