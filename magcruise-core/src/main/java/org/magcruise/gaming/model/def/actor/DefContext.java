package org.magcruise.gaming.model.def.actor;

import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.Context;

@SuppressWarnings("serial")
public class DefContext extends DefActorElements<Context> {

	public DefContext(Class<? extends Context> clazz, Object... args) {
		super(ActorName.DEFAULT_CONTEXT_NAME, clazz, args);
	}

	public DefContext(ActorName name, Class<? extends Context> clazz, Object... args) {
		super(name, clazz, args);
	}

	@Override
	public Class<? extends Context> getInstanceClass() {
		return instanceClass;
	}

	@Override
	public SConstructor<? extends DefContext> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), name, instanceClass, args);
	}

}
