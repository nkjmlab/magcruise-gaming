package org.magcruise.gaming.model.def.boot;

import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.def.common.DefMode;

@SuppressWarnings("serial")
public class DefRemoteDebug extends DefMode implements DefBootstrapProperty {

	public DefRemoteDebug(boolean mode) {
		super(mode);
	}

	@Override
	public SConstructor<? extends DefRemoteDebug> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(getIndent(style) + super.toConstructor(style));
	}
}
