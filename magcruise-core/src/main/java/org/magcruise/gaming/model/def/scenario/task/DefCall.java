package org.magcruise.gaming.model.def.scenario.task;

import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.def.scenario.stage.SimpleStageElement;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class DefCall extends SimpleStageElement {

	private Symbol name;
	private Symbol method;

	public DefCall(Symbol name, Symbol method) {
		this.name = name;
		this.method = method;
	}

	@Override
	public SConstructor<? extends DefCall> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), name, method);
	}

	public Symbol getName() {
		return name;
	}

	public Symbol getMethod() {
		return method;
	}

}
