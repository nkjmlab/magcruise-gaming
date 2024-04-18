package org.magcruise.gaming.model.def.scenario;

import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.def.actor.DefContextProperty;
import org.magcruise.gaming.model.game.Context;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class DefObjectRegistration implements DefContextProperty {

	private Symbol name;
	private Class<? extends SConstructive> instanceClass;
	private Object[] args;

	public DefObjectRegistration(Symbol name, Class<? extends Context> clazz, Object... args) {
		this.name = name;
		this.instanceClass = clazz;
		this.args = args;

	}

	@Override
	public SConstructor<? extends DefObjectRegistration> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), name, instanceClass, args);
	}

	public Symbol getName() {
		return name;
	}

	public Class<? extends SConstructive> getInstanceClass() {
		return instanceClass;
	}

	public Object[] getArgs() {
		return args;
	}

}
