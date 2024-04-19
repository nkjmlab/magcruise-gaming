package org.magcruise.gaming.webui.row;

import java.time.LocalDateTime;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Assignment {

  private long id;
  private LocalDateTime createdAt = LocalDateTime.now();
  private String processId;
  private String playerName;
  private String userId;
  private boolean joined;

  public Assignment() {}

  public Assignment(String processId, String playerName, String userId, boolean joined) {
    this.processId = processId;
    this.playerName = playerName;
    this.userId = userId;
    this.joined = joined;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public String getProcessId() {
    return processId;
  }

  public void setProcessId(String processId) {
    this.processId = processId;
  }

  public String getPlayerName() {
    return playerName;
  }

  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String operatorId) {
    this.userId = operatorId;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime created) {
    this.createdAt = created;
  }

  public boolean getJoined() {
    return joined;
  }

  public void setJoined(boolean joined) {
    this.joined = joined;
  }
}
