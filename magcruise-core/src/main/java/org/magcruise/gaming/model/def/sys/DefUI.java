package org.magcruise.gaming.model.def.sys;

import org.magcruise.gaming.lang.SConstructor;

public interface DefUI extends DefGameSystemProperty {

	@Override
	default SConstructor<? extends DefUI> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(getIndent(style) + SConstructor.toConstructor(style, getClass()));
	}
}
