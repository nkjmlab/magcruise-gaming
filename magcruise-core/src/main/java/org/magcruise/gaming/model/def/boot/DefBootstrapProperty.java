package org.magcruise.gaming.model.def.boot;

import org.magcruise.gaming.lang.SConstructive;

public interface DefBootstrapProperty extends SConstructive {

	default String getIndent(ToExpressionStyle style) {
		return (style == ToExpressionStyle.MULTI_LINE ? System.lineSeparator() + "      " : "");
	}

}
