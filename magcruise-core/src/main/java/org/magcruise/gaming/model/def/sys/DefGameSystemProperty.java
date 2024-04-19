package org.magcruise.gaming.model.def.sys;

import org.magcruise.gaming.lang.SConstructive;

public interface DefGameSystemProperty extends SConstructive {

	default String getIndent(ToExpressionStyle style) {
		return (style == ToExpressionStyle.MULTI_LINE ? System.lineSeparator() + "      " : "");
	}
}
