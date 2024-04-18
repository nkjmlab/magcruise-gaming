package org.magcruise.gaming.model.def.actor;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.model.game.ActorName;

@SuppressWarnings("serial")
public abstract class DefActorElements<T> implements DefContextProperty {

	protected ActorName name;
	protected Class<? extends T> instanceClass;
	protected Object[] args;

	public DefActorElements(ActorName name, Class<? extends T> clazz, Object... args) {
		if (name == null) {
			throw new IllegalArgumentException("name should not be null.");
		}
		this.name = name;
		this.instanceClass = clazz;
		this.args = args;
	}

	public Class<?> getInstanceClass() {
		return instanceClass;
	}

	public Object[] getArgs() {
		return args;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public ActorName getName() {
		return name;
	}

}
