package org.magcruise.gaming.executor.api.message;

import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.message.SimpleMessage;

@SuppressWarnings("serial")
public class JoinInGame extends SimpleMessage implements RequestToGameExecutor {

	private ActorName playerName;
	private String operatorId;

	public JoinInGame() {
	}

	public JoinInGame(ActorName playerName, String operatorId) {
		this.playerName = playerName;
		this.operatorId = operatorId;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public ActorName getPlayerName() {
		return playerName;
	}

	@Override
	public SConstructor<? extends JoinInGame> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), playerName, operatorId);
	}

}
