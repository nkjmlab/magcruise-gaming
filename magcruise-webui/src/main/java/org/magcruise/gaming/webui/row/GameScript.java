package org.magcruise.gaming.webui.row;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.webui.relation.GameScriptsTable;
import org.nkjmlab.sorm4j.annotation.OrmTable;

@OrmTable(GameScriptsTable.TABLE_NAME)
public class GameScript {

  private String id;
  private Date createdAt;
  private String userId;
  private String name;
  private String className;
  private String script;
  private String additionalScript;
  private String description;
  private String sourceUrl;

  public GameScript() {}

  public GameScript(
      String id,
      String userId,
      String name,
      String description,
      String sourceUrl,
      String className,
      String additionalScript,
      String script) {
    this.id = id;
    this.userId = userId;
    this.name = name;
    this.description = description;
    this.sourceUrl = sourceUrl;
    this.className = className;
    this.additionalScript = additionalScript;
    this.script = script;
    this.createdAt = new Date();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public String getScript() {
    return script;
  }

  public void setScript(String processId) {
    this.script = processId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String operatorId) {
    this.userId = operatorId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date created) {
    this.createdAt = created;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAdditionalScript() {
    return additionalScript;
  }

  public void setAdditionalScript(String additionalScript) {
    this.additionalScript = additionalScript;
  }

  public String getSourceUrl() {
    return sourceUrl;
  }

  public void setSourceUrl(String source) {
    this.sourceUrl = source;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }
}
