package org.magcruise.gaming.model.def.boot;

import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class DefClasspath extends DefPath implements DefBootstrapProperty {

	public DefClasspath(String path) {
		super(path);
	}

	@Override
	public SConstructor<? extends DefClasspath> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(
				getIndent(style) + SConstructor.toConstructor(style, getClass(), path));
	}

}
