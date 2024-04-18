package org.magcruise.gaming.ui.api;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class GameSessionJson {
	private String processId;
	private String userId;
	private String sessionName;
	private String description;
	private String rootBrokerUrl;
	private String brokerUrl;
	private String bootstrapScript;

	public GameSessionJson() {
	}

	public GameSessionJson(String processId, String userId, String sessionName, String description,
			String rootBrokerUrl, String brokerUrl, String bootstrapScript) {
		this.processId = processId;
		this.userId = userId;
		this.sessionName = sessionName;
		this.description = description;
		this.rootBrokerUrl = rootBrokerUrl;
		this.brokerUrl = brokerUrl;
		this.bootstrapScript = bootstrapScript;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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

	public String getBrokerUrl() {
		return brokerUrl;
	}

	public void setBrokerUrl(String brokerUrl) {
		this.brokerUrl = brokerUrl;
	}

	public String getRootBrokerUrl() {
		return rootBrokerUrl;
	}

	public void setRootBrokerUrl(String rootBrokerUrl) {
		this.rootBrokerUrl = rootBrokerUrl;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public String getBootstrapScript() {
		return bootstrapScript;
	}

	public void setBootstrapScript(String bootstrapScript) {
		this.bootstrapScript = bootstrapScript;
	}

}
