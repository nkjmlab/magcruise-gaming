package org.magcruise.gaming.model.game;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class ActorName implements SConstructive, Comparable<ActorName> {

	public static final ActorName NON_PLAYER = ActorName.of("NON_PLAYER");
	public static final ActorName UNDEFINED = ActorName.of("UNDEFINED");
	public static final ActorName DEFAULT_CONTEXT_NAME = ActorName.of("Context");

	private final String actorName;

	public ActorName(String actorName) {
		this.actorName = actorName;
	}

	public static ActorName of(String actorName) {
		return new ActorName(actorName);
	}

	public static ActorName of(Symbol symbol) {
		return of(symbol.toString());
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return actorName;
	}

	public Symbol toSymbol() {
		return Symbol.parse(getName());
	}

	public String getName() {
		return actorName;
	}

	@Override
	public SConstructor<?> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), actorName);
	}

	public boolean compareToString(String o) {
		if (o == null) {
			return false;
		}
		return ActorName.of(o).equals(this);
	}

	@Override
	public int compareTo(ActorName o) {
		return actorName.compareTo(o.actorName);
	}

}
