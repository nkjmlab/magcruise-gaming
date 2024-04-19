package org.magcruise.gaming.ui.api.message;

import org.magcruise.gaming.manager.process.ProcessId;

@SuppressWarnings("serial")
public class NotifyStartOfGameProcess extends SimpleGameProgress {

	private ProcessId processId;

	public NotifyStartOfGameProcess(ProcessId processId) {
		super(Integer.MIN_VALUE);
		this.setProcessId(processId);
	}

	public ProcessId getProcessId() {
		return processId;
	}

	public void setProcessId(ProcessId processId) {
		this.processId = processId;
	}

}
