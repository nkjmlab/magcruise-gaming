package org.magcruise.gaming.model.def.common;

import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class DefMode implements SConstructive {
	private boolean flag;

	public DefMode(boolean flag) {
		this.flag = flag;
	}

	public boolean getFlag() {
		return flag;
	}

	@Override
	public SConstructor<? extends DefMode> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), flag);
	}

}
