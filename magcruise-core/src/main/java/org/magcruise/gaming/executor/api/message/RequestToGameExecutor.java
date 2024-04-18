package org.magcruise.gaming.executor.api.message;

import org.magcruise.gaming.lang.Message;
import org.magcruise.gaming.lang.SConstructor;

public interface RequestToGameExecutor extends Message {

	@Override
	public SConstructor<? extends RequestToGameExecutor> toConstructor(ToExpressionStyle style);

	@Override
	public default SConstructor<? extends RequestToGameExecutor> toConstructor() {
		return toConstructor(ToExpressionStyle.DEFAULT);
	}

}
