package org.magcruise.gaming.model.def.actor;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.ActorName;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class DefAssignmentRequest implements DefContextProperty {

	public static final String ANONYMOUS_OPERATOR = "ANONYMOUS_OPERATOR";

	public static final String AGENT_OPERATOR = "AGENT_OPERATOR";

	private ActorName playerName;

	private String operatorId;

	public DefAssignmentRequest(Symbol playerName, String operatorId) {
		this(ActorName.of(playerName), operatorId);
	}

	public DefAssignmentRequest(ActorName playerName, String operatorId) {
		this.playerName = playerName;
		this.operatorId = operatorId;
	}

	public ActorName getPlayerName() {
		return playerName;
	}

	public String getOperatorId() {
		return operatorId;
	}

	@Override
	public SConstructor<? extends DefAssignmentRequest> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), playerName, operatorId);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
