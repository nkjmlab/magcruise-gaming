package org.magcruise.gaming.model.def.scenario;

import org.magcruise.gaming.lang.SConstructive;

public interface DefGameScenarioProperty extends SConstructive {

	default String getIndent(ToExpressionStyle style) {
		return (style == ToExpressionStyle.MULTI_LINE ? System.lineSeparator() + "      " : "");
	}

}
