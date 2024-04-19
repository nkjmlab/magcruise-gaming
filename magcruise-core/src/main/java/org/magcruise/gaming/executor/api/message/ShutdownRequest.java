package org.magcruise.gaming.executor.api.message;

import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.message.SimpleMessage;

@SuppressWarnings("serial")
public class ShutdownRequest extends SimpleMessage implements RequestToGameExecutor {

	@Override
	public SConstructor<? extends ShutdownRequest> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass());
	}

}
