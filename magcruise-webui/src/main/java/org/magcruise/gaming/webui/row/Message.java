package org.magcruise.gaming.webui.row;

import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.ui.model.attr.Attribute;
import org.magcruise.gaming.webui.relation.MessagesTable;
import org.nkjmlab.sorm4j.annotation.OrmTable;

@OrmTable(MessagesTable.TABLE_NAME)
public class Message {

  private long id;
  private Date createdAt;
  private String processId;
  private int roundnum;
  private String playerName;
  private long messageId;
  private String message;
  private String messageAttrs;

  public Message() {}

  public Message(
      String processId,
      int roundnum,
      String playerName,
      long messageId,
      String message,
      Attribute[] attrs) {
    this.processId = processId;
    this.roundnum = roundnum;
    this.playerName = playerName;
    this.messageId = messageId;
    this.message = message;
    this.messageAttrs =
        String.join(",", Arrays.stream(attrs).map(a -> a.toString()).collect(Collectors.toList()));
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

  public long getMessageId() {
    return messageId;
  }

  public void setMessageId(long messageId) {
    this.messageId = messageId;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getMessageAttrs() {
    return messageAttrs;
  }

  public void setMessageAttrs(String attrs) {
    this.messageAttrs = attrs;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }
}
