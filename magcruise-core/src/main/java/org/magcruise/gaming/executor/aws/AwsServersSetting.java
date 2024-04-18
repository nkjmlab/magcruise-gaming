package org.magcruise.gaming.executor.aws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class AwsServersSetting {

	private String profileName;

	private List<AwsSettingJson> awsSettings = new ArrayList<>();

	private int waitingStopInstanceMinutes = 30;
	private int idleProcessTTLMinutes = 30;

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public List<AwsSettingJson> getAwsSettings() {
		return awsSettings;
	}

	public void setAwsSettings(AwsSettingJson[] awsSetting) {
		this.awsSettings.addAll(Arrays.asList(awsSetting));
	}

	public int getWaitingStopInstanceMinutes() {
		return waitingStopInstanceMinutes;
	}

	public void setWaitingStopInstanceMinutes(int waitingStopInstanceMinutes) {
		this.waitingStopInstanceMinutes = waitingStopInstanceMinutes;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public int getIdleProcessTTLMinutes() {
		return idleProcessTTLMinutes;
	}

	public void setIdleProcessTTLMinutes(int idleProcessTTLMinutes) {
		this.idleProcessTTLMinutes = idleProcessTTLMinutes;
	}

}
