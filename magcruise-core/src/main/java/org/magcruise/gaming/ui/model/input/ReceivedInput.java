package org.magcruise.gaming.ui.model.input;

import java.sql.Timestamp;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.Parameters;
import org.magcruise.gaming.lang.SExpression.ToExpressionStyle;
import org.magcruise.gaming.manager.process.ProcessId;
import gnu.mapping.Symbol;

public class ReceivedInput {

	private long id;
	private Date createdAt = new Timestamp(new Date().getTime());
	private long requestId;
	private String processId;
	private int roundnum;
	private String playerName;
	private String parameters;

	public ReceivedInput() {
	}

	public ReceivedInput(long requestId, ProcessId processId, int roundnum, Symbol playerName,
			Parameters params) {
		this.requestId = requestId;
		this.processId = processId.getValue();
		this.roundnum = roundnum;
		this.playerName = playerName.toString();
		this.parameters = params.toConstructor(ToExpressionStyle.MULTI_LINE).toString();
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

	public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String pid) {
		this.processId = pid;
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

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
