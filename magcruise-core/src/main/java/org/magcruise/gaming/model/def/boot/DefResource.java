package org.magcruise.gaming.model.def.boot;

import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class DefResource extends DefPath implements DefBootstrapProperty {

	public DefResource(String fineName) {
		super(fineName);
	}

	@Override
	public SConstructor<? extends DefResource> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(
				getIndent(style) + SConstructor.toConstructor(style, getClass(), path));
	}
}
