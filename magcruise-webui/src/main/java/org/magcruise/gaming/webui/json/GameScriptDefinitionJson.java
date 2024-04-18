package org.magcruise.gaming.webui.json;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class GameScriptDefinitionJson {

  private String id;
  private String userId;
  private String name = "";
  private String description = "";
  private String sourceUrl = "";
  private String className = "";
  private String scriptFile;
  private String additionalScriptFile;

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getScriptFile() {
    return scriptFile;
  }

  public void setScriptFile(String file) {
    this.scriptFile = file;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAdditionalScriptFile() {
    return additionalScriptFile;
  }

  public void setAdditionalScriptFile(String additionalScriptFile) {
    this.additionalScriptFile = additionalScriptFile;
  }

  public String getSourceUrl() {
    return sourceUrl;
  }

  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }
}
