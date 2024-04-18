package org.magcruise.gaming.model.def.sys;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.util.SystemEnvironmentUtils;

@SuppressWarnings("serial")
public class GameSystemPropertiesBuilder implements SConstructive {
	private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger();

	private GameSystemProperties props;

	private List<DefGameSystemProperty> defProperties = new ArrayList<>();

	public GameSystemPropertiesBuilder() {
	}

	public GameSystemPropertiesBuilder(List<DefGameSystemProperty> defProperties) {
		this.defProperties.addAll(defProperties);
	}

	public GameSystemPropertiesBuilder addProperties(DefGameSystemProperty... defProperties) {
		this.defProperties.addAll(Arrays.asList(defProperties));
		return this;
	}

	@Override
	public SConstructor<? extends GameSystemPropertiesBuilder> toConstructor(
			ToExpressionStyle style) {
		return new SConstructor<>(
				(style == ToExpressionStyle.MULTI_LINE ? System.lineSeparator() + "  " : "")
						+ SConstructor.toConstructor(style, getClass(), defProperties));
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public GameSystemProperties build(String processId) {
		if (props != null) {
			return new GameSystemProperties(props);
		}
		Path dbFile = null;
		List<DefUI> defUIs = new ArrayList<>();
		DefRequestToGameExecutorPublisherService defFetchRequestToGameExecutor = null;
		DefLog4jConfig defLog4jConfig = null;

		log.info("def:setup-services is defined? = {}",
				SchemeEnvironment.isDefined(processId, "def:setup-services"));
		SchemeEnvironment.applyProcedureIfDefined(processId, "def:setup-services", this);
		for (DefGameSystemProperty p : defProperties) {
			if (p instanceof DefGameLogDb) {
				dbFile = ((DefGameLogDb) p).getJdbcUrlPath();
			} else if (p instanceof DefUI) {
				defUIs.add(((DefUI) p));
			} else if (p instanceof DefRequestToGameExecutorPublisherService) {
				defFetchRequestToGameExecutor = (DefRequestToGameExecutorPublisherService) p;
			} else if (p instanceof DefLog4jConfig) {
				defLog4jConfig = (DefLog4jConfig) p;
			}
		}

		if (dbFile == null) {
			dbFile = SystemEnvironmentUtils.getDefaultDbFilePath();
		}

		props = new GameSystemProperties(getWorkingDirectory(), dbFile, defUIs,
				defFetchRequestToGameExecutor, defLog4jConfig);
		return props;
	}

	public Path getWorkingDirectory() {
		for (DefGameSystemProperty p : defProperties) {
			if (p instanceof DefWorkingDirectory) {
				return ((DefWorkingDirectory) p).getPath();
			}
		}
		return SystemEnvironmentUtils.getDefaultWorkingDirectoryPath();
	}

	public List<String> getJavaOptions() {
		for (DefGameSystemProperty p : defProperties) {
			if (p instanceof DefJavaOptions) {
				return ((DefJavaOptions) p).getOptions();
			}
		}
		return SystemEnvironmentUtils.getDefaultJavaOptions();
	}

}
