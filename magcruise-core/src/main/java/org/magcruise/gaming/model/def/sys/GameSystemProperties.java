package org.magcruise.gaming.model.def.sys;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.ui.web.RequestersToUI;

@SuppressWarnings("serial")
public class GameSystemProperties implements SConstructive {

	private Path workingDirectory;
	private Path dbFile;
	private List<DefUI> defUIs;
	private DefRequestToGameExecutorPublisherService defFetchRequestToGameExecutor;
	private DefLog4jConfig defLog4jConfig;

	public GameSystemProperties(Path workingDirectory, Path dbFile, List<DefUI> defUIs,
			DefRequestToGameExecutorPublisherService defFetchRequestToGameExecutor,
			DefLog4jConfig defLog4jConfig) {
		this.workingDirectory = workingDirectory;
		this.dbFile = dbFile;
		this.defUIs = new ArrayList<>(defUIs);
		this.defFetchRequestToGameExecutor = defFetchRequestToGameExecutor;
		this.defLog4jConfig = defLog4jConfig;
	}

	public GameSystemProperties(GameSystemProperties props) {
		this(props.workingDirectory, props.dbFile, props.defUIs,
				props.defFetchRequestToGameExecutor,
				props.defLog4jConfig);
	}

	@Override
	public SConstructor<? extends GameSystemProperties> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), workingDirectory, dbFile, defUIs,
				defFetchRequestToGameExecutor, defLog4jConfig);
	}

	public Path getDbFilePath() {
		return dbFile;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public Path getWorkingDirectory() {
		return workingDirectory;
	}

	public RequestersToUI createRequesters(String bootstrapScript) {
		return new RequestersToUI(defUIs, bootstrapScript);
	}

	public DefRequestToGameExecutorPublisherService getDefFetchRequestToGameExecutor() {
		return defFetchRequestToGameExecutor;
	}

	public DefLog4jConfig getDefLog4jConfig() {
		return defLog4jConfig;
	}

}
