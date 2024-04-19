package org.magcruise.gaming.executor.aws;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class AwsSettingJson {

	private String imageId;
	private String keyName;
	private String[] securityGroupIds;

	private String instanceId;
	private String instanceType;
	private String brokerUrl;
	private String hostname;
	private boolean stopAfterFinishing = false;

	public String getBrokerUrl() {
		return brokerUrl;
	}

	public void setBrokerUrl(String brokerUrl) {
		this.brokerUrl = brokerUrl;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getInstanceType() {
		return instanceType;
	}

	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public String[] getSecurityGroupIds() {
		return securityGroupIds;
	}

	public void setSecurityGroupIds(String[] securityGroupIds) {
		this.securityGroupIds = securityGroupIds;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostName) {
		this.hostname = hostName;
	}

	public boolean isStopAfterFinishing() {
		return stopAfterFinishing;
	}

	public void setStopAfterFinishing(boolean stopAfterFinishing) {
		this.stopAfterFinishing = stopAfterFinishing;
	}

}
