package org.magcruise.gaming.lang;

import java.io.Serializable;

public interface SExpression extends Serializable {

	enum ToExpressionStyle {
		DEFAULT, MULTI_LINE, SINGLE;
	}

	public String getExpression(ToExpressionStyle style);

	public default String getExpression() {
		return getExpression(ToExpressionStyle.DEFAULT);
	}

}
