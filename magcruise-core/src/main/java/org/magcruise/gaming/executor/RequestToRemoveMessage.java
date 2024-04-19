package org.magcruise.gaming.executor;

import org.magcruise.gaming.ui.api.message.RequestToUI;

@SuppressWarnings("serial")
public class RequestToRemoveMessage extends RequestToUI {

	private long removeMessageId;

	public RequestToRemoveMessage(long msgId) {
		this.removeMessageId = msgId;
	}

	public long getRemoveMessageId() {
		return removeMessageId;
	}

}
