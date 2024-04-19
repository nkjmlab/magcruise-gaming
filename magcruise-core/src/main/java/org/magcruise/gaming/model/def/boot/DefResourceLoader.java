package org.magcruise.gaming.model.def.boot;

import java.util.Arrays;
import java.util.List;
import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class DefResourceLoader implements DefBootstrapProperty {

	private String className;
	private List<DefResource> resources;

	public DefResourceLoader(String className, DefResource resource) {
		this(className, Arrays.asList(resource).toArray(new DefResource[0]));
	}

	public DefResourceLoader(String className, DefResource[] resources) {
		this.className = className;
		this.resources = Arrays.asList(resources);
	}

	public String getClassName() {
		return className;
	}

	public List<DefResource> getResources() {
		return resources;
	}

	@Override
	public SConstructor<? extends DefResourceLoader> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), className,
				resources.toArray(new DefResource[0]));
	}
}
