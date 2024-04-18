package org.magcruise.gaming.model.def.sys;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.Level;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.def.boot.DefBootstrapProperty;

@SuppressWarnings("serial")
public class DefLog4jConfig implements DefBootstrapProperty {

	private String level;
	private boolean location;

	public DefLog4jConfig(Level level, boolean location) {
		this(level.name(), location);
	}

	public DefLog4jConfig(String level, boolean location) {
		this.level = level;
		this.location = location;
	}

	@Override
	public SConstructor<? extends DefLog4jConfig> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(
				getIndent(style) + SConstructor.toConstructor(style, getClass(), level, location));
	}

	public boolean getLocation() {
		return location;
	}

	public Level getLevel() {
		return Level.valueOf(level);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
