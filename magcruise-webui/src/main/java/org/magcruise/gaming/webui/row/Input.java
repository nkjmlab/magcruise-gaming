package org.magcruise.gaming.webui.row;

import java.time.LocalDateTime;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.webui.relation.InputsTable;
import org.nkjmlab.sorm4j.annotation.OrmTable;

@OrmTable(InputsTable.TABLE_NAME)
public class Input {

  private long id;
  private LocalDateTime createdAt;
  private String processId;
  private int roundnum;
  private String playerName;
  private long requestId;
  private String inputs;

  public Input() {}

  public Input(
      LocalDateTime createdAt,
      String processId,
      int roundnum,
      String playerName,
      long messageId,
      String inputs) {
    this.createdAt = createdAt;
    this.processId = processId;
    this.roundnum = roundnum;
    this.playerName = playerName;
    this.requestId = messageId;
    this.inputs = inputs;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getProcessId() {
    return processId;
  }

  public void setProcessId(String processId) {
    this.processId = processId;
  }

  public int getRoundnum() {
    return roundnum;
  }

  public void setRoundnum(int roundnum) {
    this.roundnum = roundnum;
  }

  public String getPlayerName() {
    return playerName;
  }

  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  public long getRequestId() {
    return requestId;
  }

  public void setRequestId(long messageId) {
    this.requestId = messageId;
  }

  public String getInputs() {
    return inputs;
  }

  public void setInputs(String message) {
    this.inputs = message;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
