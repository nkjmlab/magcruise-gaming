package org.magcruise.gaming.model.def.boot;

import java.net.URI;
import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class DefBootstrapPath extends DefPath implements DefBootstrapProperty {

	public DefBootstrapPath(String path) {
		super(path);
	}

	public DefBootstrapPath(URI path) {
		super(path);
	}

	@Override
	public SConstructor<? extends DefGameScript> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(
				getIndent(style) + SConstructor.toConstructor(style, getClass(), path));
	}

}
