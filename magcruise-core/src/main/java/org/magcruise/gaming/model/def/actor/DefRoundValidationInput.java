package org.magcruise.gaming.model.def.actor;

import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.def.common.DefMode;

@SuppressWarnings("serial")
public class DefRoundValidationInput extends DefMode implements DefContextProperty {

	public DefRoundValidationInput(boolean mode) {
		super(mode);
	}

	@Override
	public SConstructor<? extends DefRoundValidationInput> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(
				getIndent(style) + SConstructor.toConstructor(style, getClass(), getFlag()));
	}

}
