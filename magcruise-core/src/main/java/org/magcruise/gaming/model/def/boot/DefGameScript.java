package org.magcruise.gaming.model.def.boot;

import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.def.common.DefScript;

@SuppressWarnings("serial")
public class DefGameScript extends DefScript implements DefBootstrapProperty {

	public DefGameScript(String script) {
		super(script);
	}

	@Override
	public SConstructor<? extends DefGameScript> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(
				getIndent(style) + SConstructor.toConstructor(style, getClass(), getScript()));
	}
}
