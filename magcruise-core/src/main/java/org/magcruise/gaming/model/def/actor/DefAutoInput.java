package org.magcruise.gaming.model.def.actor;

import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.def.common.DefMode;

@SuppressWarnings("serial")
public class DefAutoInput extends DefMode implements DefContextProperty {

	public DefAutoInput(boolean mode) {
		super(mode);
	}

	@Override
	public SConstructor<? extends DefAutoInput> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(
				getIndent(style) + SConstructor.toConstructor(style, getClass(), getFlag()));
	}

}
