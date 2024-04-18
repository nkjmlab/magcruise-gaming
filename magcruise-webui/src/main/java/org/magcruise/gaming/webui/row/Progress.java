package org.magcruise.gaming.webui.row;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.webui.relation.ProgressesTable;
import org.nkjmlab.sorm4j.annotation.OrmTable;

@OrmTable(ProgressesTable.TABLE_NAME)
public class Progress {

  private long id;
  private String processId;
  private Date createdAt;
  private int roundnum;
  private String status;

  public Progress() {}

  public Progress(String processId, int roundnum, String status) {
    this.processId = processId;
    this.roundnum = roundnum;
    this.status = status;
    this.createdAt = new Date();
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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getRoundnum() {
    return roundnum;
  }

  public void setRoundnum(int roundnum) {
    this.roundnum = roundnum;
  }
}
