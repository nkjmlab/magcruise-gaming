package org.magcruise.gaming.model.def.actor;

import org.magcruise.gaming.lang.SConstructive;

public interface DefContextProperty extends SConstructive {

	default String getIndent(ToExpressionStyle style) {
		return (style == ToExpressionStyle.MULTI_LINE ? System.lineSeparator() + "      " : "");
	}
}
