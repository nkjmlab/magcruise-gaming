package org.magcruise.gaming.model.def.sys;

import java.util.ArrayList;
import java.util.List;
import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class DefJavaOptions implements DefGameSystemProperty {

	private List<String> options;

	public DefJavaOptions(List<String> options) {
		this.options = new ArrayList<>(options);
	}

	public List<String> getOptions() {
		return options;
	}

	@Override
	public SConstructor<? extends DefJavaOptions> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), options);
	}

}
