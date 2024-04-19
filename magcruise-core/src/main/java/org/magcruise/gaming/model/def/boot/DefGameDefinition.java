package org.magcruise.gaming.model.def.boot;

import java.net.URI;
import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class DefGameDefinition extends DefPath implements DefBootstrapProperty {

	public DefGameDefinition(String path) {
		super(path);
	}

	public DefGameDefinition(URI path) {
		super(path);
	}

	@Override
	public SConstructor<? extends DefGameDefinition> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(
				getIndent(style) + SConstructor.toConstructor(style, getClass(), path));
	}

}
