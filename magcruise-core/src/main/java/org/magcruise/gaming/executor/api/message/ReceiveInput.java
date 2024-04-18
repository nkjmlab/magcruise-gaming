package org.magcruise.gaming.executor.api.message;

import org.magcruise.gaming.lang.Parameters;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.message.SimpleMessage;
import org.magcruise.gaming.ui.model.input.InputFromWebUI;

@SuppressWarnings("serial")
public class ReceiveInput extends SimpleMessage implements RequestToGameExecutor {

	private long requestId;
	private String playerName;
	private InputFromWebUI[] inputs;

	public ReceiveInput() {

	}

	public ReceiveInput(long requestId, String playerName, InputFromWebUI[] inputs) {
		this.requestId = requestId;
		this.playerName = playerName;
		this.inputs = inputs;
	}

	public long getRequestId() {
		return requestId;
	}

	public Parameters getParameters() {
		return Parameters.parse(inputs);
	}

	public String getPlayerName() {
		return this.playerName;
	}

	@Override
	public SConstructor<? extends ReceiveInput> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), requestId, playerName, inputs);
	}

}
