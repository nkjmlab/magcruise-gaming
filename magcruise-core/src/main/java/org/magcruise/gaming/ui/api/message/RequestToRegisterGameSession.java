package org.magcruise.gaming.ui.api.message;

import org.apache.commons.lang3.builder.ToStringBuilder;

@SuppressWarnings("serial")
public class RequestToRegisterGameSession extends RequestToUI {

	private String processId;
	private String operatorId;
	private String sessionName;
	private String description;
	private String brokerUrl;
	private String bootstrapScript;

	public RequestToRegisterGameSession() {

	}

	public RequestToRegisterGameSession(String processId, String brokerUrl, String operatorId,
			String sessionName, String description, String bootstrapScript) {
		this.processId = processId;
		this.brokerUrl = brokerUrl;
		this.operatorId = operatorId;
		this.sessionName = sessionName;
		this.description = description;
		this.bootstrapScript = bootstrapScript;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getSessionName() {
		return sessionName;
	}

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public String getBrokerUrl() {
		return brokerUrl;
	}

	public void setBrokerUrl(String brokerUrl) {
		this.brokerUrl = brokerUrl;
	}

	public String getBootstrapScript() {
		return bootstrapScript;
	}

	public void setBootstrapScript(String bootstrapScript) {
		this.bootstrapScript = bootstrapScript;
	}

}
