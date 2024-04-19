package org.magcruise.gaming.webui.json;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class StartGameSessionJson {

  private String rootBrokerUrl;
  private String scriptId;
  private String additionalScript;
  private String sessionName;
  private String description;
  private String className;

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public String getRootBrokerUrl() {
    return rootBrokerUrl;
  }

  public void setRootBrokerUrl(String rootBrokerUrl) {
    this.rootBrokerUrl = rootBrokerUrl;
  }

  public String getScriptId() {
    return scriptId;
  }

  public void setScriptId(String scriptId) {
    this.scriptId = scriptId;
  }

  public String getAdditionalScript() {
    return additionalScript;
  }

  public void setAdditionalScript(String additionalScript) {
    this.additionalScript = additionalScript;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getSessionName() {
    return sessionName;
  }

  public void setSessionName(String sessionName) {
    this.sessionName = sessionName;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }
}
