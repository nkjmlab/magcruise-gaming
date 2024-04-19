package org.magcruise.gaming.model.def.boot;

import java.net.URI;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import gnu.kawa.io.Path;

@SuppressWarnings("serial")
public class DefPath implements SConstructive {
	protected String path;

	public DefPath(String path) {
		this.path = path;
	}

	public DefPath(URI path) {
		this.path = path.toString();
	}

	public Path getPath() {
		Path path = Path.coerceToPathOrNull(this.path);
		if (path.isPlainFile()) {
			path = Path.coerceToPathOrNull(path.toFile());
		}
		return path;
	}

	public String getPathString() {
		return path;
	}

	@Override
	public SConstructor<?> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), path);
	}

}
