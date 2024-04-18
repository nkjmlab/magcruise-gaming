package org.magcruise.gaming.lang;

public interface SConstructive extends SExpression {

	public SConstructor<?> toConstructor(ToExpressionStyle style);

	public default SConstructor<?> toConstructor() {
		return toConstructor(ToExpressionStyle.DEFAULT);
	}

	@Override
	public default String getExpression(ToExpressionStyle style) {
		return toConstructor(style).getExpression(style);
	}

	@Override
	public default String getExpression() {
		return toConstructor().getExpression();
	}

}
