package org.magcruise.gaming.executor.api.message;

import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.message.SimpleMessage;

@SuppressWarnings("serial")
public class UnexpectedShutdownRequest extends SimpleMessage implements RequestToGameExecutor {
	@Override
	public SConstructor<? extends UnexpectedShutdownRequest> toConstructor(
			ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass());
	}

}
