package org.magcruise.gaming.model.def.common;

import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class DefNumber implements SConstructive {
	private Number number;

	public DefNumber(Number number) {
		this.number = number;
	}

	public Number getNumber() {
		return number;
	}

	@Override
	public SConstructor<? extends DefNumber> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), number);
	}

	public int intValue() {
		return number.intValue();
	}

}
