package org.magcruise.gaming.webui.row;

import java.net.URL;
import java.util.Date;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.executor.api.GameInteractionService;
import org.magcruise.gaming.webui.relation.GameSessionsTable;
import org.nkjmlab.sorm4j.annotation.OrmTable;
import org.nkjmlab.util.java.net.UrlUtils;

@OrmTable(GameSessionsTable.TABLE_NAME)
public class GameSession {

  private String processId;
  private Date createdAt = new Date();
  private String userId;
  private String sessionName;
  private String description;
  private String rootBrokerUrl;
  private String brokerUrl;
  private String bootstrapScript;

  public GameSession() {}

  public GameSession(
      String processId,
      String userId,
      String sessionName,
      String description,
      String rootBrokerUrl,
      String brokerUrl,
      String bootstrapScript) {
    this(processId, rootBrokerUrl, bootstrapScript);
    setSessionName(sessionName);
    setDescription(description);
    this.userId = userId;
    this.brokerUrl = brokerUrl;
  }

  public GameSession(String processId, String rootBrokerUrl, String bootstrapScript) {
    this.processId = processId;
    this.rootBrokerUrl = rootBrokerUrl;
    this.bootstrapScript = bootstrapScript;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(processId).toHashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof GameSession)) {
      return false;
    }
    GameSession other = (GameSession) o;
    return new EqualsBuilder().append(this.processId, other.processId).build();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
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
    this.sessionName = sessionName == null ? "" : sessionName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description == null ? "" : description;
  }

  public String getBootstrapScript() {
    return bootstrapScript;
  }

  public void setBootstrapScript(String script) {
    this.bootstrapScript = script;
  }

  public String toWebsocketUrl(String playerName) {
    URL url = UrlUtils.of(getBrokerUrl());
    return (url.getProtocol().equals("https") ? "wss" : "ws")
        + "://"
        + url.getHost()
        + (url.getPort() == -1 ? "" : ":" + url.getPort())
        + url.getPath()
        + "/websocket/requestToUI?processId="
        + getProcessId()
        + "&playerName="
        + playerName;
  }

  public String toGameInteractionServiceUrl() {
    return getBrokerUrl() + GameInteractionService.DEFAULT_PATH;
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
}
