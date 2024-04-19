package org.magcruise.gaming.model.def.common;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class DefScript implements SConstructive {
	private String script;

	public DefScript(String script) {
		this.script = script;
	}

	public String getScript() {
		return script;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public SConstructor<? extends DefScript> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), getScript());
	}
}
