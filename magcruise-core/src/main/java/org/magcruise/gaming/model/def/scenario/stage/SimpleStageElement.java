package org.magcruise.gaming.model.def.scenario.stage;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.def.scenario.task.StageElement;

@SuppressWarnings("serial")
public abstract class SimpleStageElement implements StageElement {

	@Override
	public SConstructor<? extends StageElement> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass());
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
