package org.magcruise.gaming.executor.db;

import java.sql.Timestamp;
import java.util.Date;

public class GameRecord {

	private long id;
	private Date createdAt = new Timestamp(new Date().getTime());
	private String processId;
	private int roundnum;

	private String record;

	public GameRecord() {

	}

	public GameRecord(String processId, int roundnum, String record) {
		this.processId = processId;
		this.roundnum = roundnum;
		this.record = record;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getRecord() {
		return record;
	}

	public void setRecord(String record) {
		this.record = record;
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

	public void setCreatedAt(Date created) {
		this.createdAt = created;
	}

	public int getRoundnum() {
		return roundnum;
	}

	public void setRoundnum(int roundnum) {
		this.roundnum = roundnum;
	}

}
