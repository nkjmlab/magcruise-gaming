package org.magcruise.gaming.model.def.sys;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class DefGameLogDb implements DefGameSystemProperty {

	private Path parentPath;
	private String name;

	public DefGameLogDb(Path parentPath, String name) {
		this.parentPath = parentPath;
		this.name = name;
	}

	public Path getJdbcUrlPath() {
		return Paths.get(parentPath.toString(), name);
	}

	@Override
	public SConstructor<? extends DefGameLogDb> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), parentPath, name);
	}

}
