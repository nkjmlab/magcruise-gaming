package org.magcruise.gaming.model.def.actor;

import java.util.HashMap;
import java.util.Map;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class RegisteredObjects implements SConstructive {

	private Map<Symbol, SConstructive> objects = new HashMap<>();

	public RegisteredObjects(Map<Symbol, SConstructive> objects) {
		this.objects.putAll(objects);
	}

	@Override
	public SConstructor<? extends RegisteredObjects> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), objects);
	}

	public SConstructive get(Symbol name) {
		return objects.get(name);
	}

}
