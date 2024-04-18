package org.magcruise.gaming.model.def.boot;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import gnu.kawa.io.Path;

@SuppressWarnings("serial")
public class BootstrapProperties implements SConstructive {

	private List<DefClasspath> classpaths = new ArrayList<>();
	private List<Path> gameScripts = new ArrayList<>();
	private boolean remoteDebugMode = false;

	public BootstrapProperties(List<DefClasspath> classpaths, List<Path> gameScripts,
			boolean remoteDebugMode) {
		this.classpaths.addAll(classpaths);
		this.gameScripts.addAll(gameScripts);
		this.remoteDebugMode = remoteDebugMode;
	}

	public BootstrapProperties(BootstrapProperties props) {
		this(props.classpaths, props.gameScripts, props.remoteDebugMode);
	}

	@Override
	public SConstructor<?> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, this.getClass(), classpaths, gameScripts,
				remoteDebugMode);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public List<DefClasspath> getClasspaths() {
		return classpaths;
	}

	public List<Path> getGameScripts() {
		return gameScripts;
	}

	public boolean isRemoteDebugMode() {
		return remoteDebugMode;
	}

}
