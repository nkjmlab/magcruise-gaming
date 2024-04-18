package org.magcruise.gaming.manager.session;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.def.GameBuilder;
import org.magcruise.gaming.model.def.actor.DefContextProperty;
import org.magcruise.gaming.model.def.boot.DefBootstrapProperty;
import org.magcruise.gaming.model.def.boot.DefBootstrapScript;
import org.magcruise.gaming.model.def.boot.DefGameDefinition;
import org.magcruise.gaming.model.def.boot.DefGameScript;
import org.magcruise.gaming.model.def.boot.DefResource;
import org.magcruise.gaming.model.def.boot.DefResourceLoader;
import org.magcruise.gaming.model.def.common.DefScript;
import org.magcruise.gaming.model.def.scenario.DefGameScenarioProperty;
import org.magcruise.gaming.model.def.sys.DefGameSystemProperty;
import gnu.kawa.io.Path;

@SuppressWarnings("serial")
public class ResourceLoader implements SConstructive {
	protected static Logger log = LogManager.getLogger();
	protected ProcessId processId = ProcessId.getNewProcessId();
	protected GameBuilder gameBuilder;
	private String bootstrapFile;
	private List<Pair<String, String>> gameDefinitions = new ArrayList<>();
	private List<DefBootstrapProperty> defBootstrapProperties = new ArrayList<>();
	private List<DefGameSystemProperty> defGameSystemProperties = new ArrayList<>();
	private List<DefContextProperty> defContextProperties = new ArrayList<>();
	private List<DefGameScenarioProperty> defGameScenarioProperties = new ArrayList<>();
	private List<DefGameScript> defGameScripts = new ArrayList<>();

	public ResourceLoader() {
	}

	@Override
	public SConstructor<?> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructorWithSetter(style, getClass(),
				new String[] { "setBooatstrapFile", "setGameDefinitions",
						"setDefBootstrapProperties",
						"setDefGameScripts" },
				bootstrapFile, gameDefinitions, defBootstrapProperties, defGameScripts);
	}

	public ResourceLoader addGameDefinitionInResource(String gameDefinitionFile) {
		gameDefinitions.add(Pair.of("resource", gameDefinitionFile));
		return this;
	}

	public ResourceLoader addGameDefinition(Path path) {
		gameDefinitions.add(Pair.of("file", path.toURIString()));
		return this;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public ProcessId getProcessId() {
		return processId;
	}

	public GameBuilder getGameBuilder() {
		if (gameBuilder == null) {
			throw new IllegalStateException("gameBuilder not builded yet.");
		}
		return gameBuilder;
	}

	GameBuilder build() {
		if (gameBuilder != null) {
			throw new IllegalStateException("build method sould be call just one time.");
		}

		this.gameBuilder = new GameBuilder(processId);
		for (Pair<String, String> gameDefinition : gameDefinitions) {
			switch (gameDefinition.getLeft()) {
			case "file":
				gameBuilder.getBootstrapBuilder()
						.addProperties(new DefGameDefinition(gameDefinition.getRight()));
				break;
			case "resource":
				gameBuilder.getBootstrapBuilder()
						.addProperties(
								new DefGameDefinition(getResourceAsURI(gameDefinition.getRight())));
				break;
			default:
				break;
			}

		}

		defGameScripts.forEach(def -> gameBuilder.getBootstrapBuilder().addProperties(def));

		if (bootstrapFile != null) {
			gameBuilder.getBootstrapBuilder()
					.setBootstrapScript(getResource(bootstrapFile).toURI());
		}
		defBootstrapProperties
				.forEach(p -> getGameBuilder().getBootstrapBuilder().addProperties(p));

		defGameSystemProperties
				.forEach(p -> getGameBuilder().getGameSystemPropertiesBuilder().addProperties(p));

		defContextProperties.forEach(p -> getGameBuilder().getContextBuilder().addProperties(p));

		defGameScenarioProperties
				.forEach(p -> getGameBuilder().getGameScenarioBuilder().addProperties(p));
		return gameBuilder;
	}

	public DefBootstrapScript toDefBootstrap() {
		if (getDefBootstrapScript() != null) {
			return getDefBootstrapScript();
		}

		ToExpressionStyle style = ToExpressionStyle.MULTI_LINE;
		StringBuilder result = new StringBuilder();
		result.append(
				System.lineSeparator()
						+ "(define (def:setup-bootstrap-builder builder ::BootstrapBuilder)"
						+ System.lineSeparator());
		result.append("  (builder:addProperties" + System.lineSeparator() + "    ");

		List<DefResource> resources = new ArrayList<>();
		for (Pair<String, String> gameDefinition : gameDefinitions) {
			switch (gameDefinition.getLeft()) {
			case "file":
				resources.add(new DefResource(gameDefinition.getRight()));
				break;
			case "resource":
				resources.add(new DefResource(gameDefinition.getRight()));
				break;
			default:
				break;
			}

		}
		result.append(
				new DefResourceLoader(getClass().getName(), resources.toArray(new DefResource[0]))
						.toConstructor(style).getExpression(style));

		defBootstrapProperties.forEach(def -> {
			if (def instanceof DefScript) {
				result.append(
						def.toConstructor(ToExpressionStyle.SINGLE)
								.getExpression(ToExpressionStyle.SINGLE));
			} else {
				result.append(def.toConstructor(style).getExpression(style));
			}
		});

		defGameScripts.forEach(
				def -> result
						.append(def.toConstructor(style).getExpression(ToExpressionStyle.SINGLE)));

		result.append("))");

		if (defGameSystemProperties.size() > 0) {
			result.append(System.lineSeparator()
					+ "(define (def:setup-game-system-properties-builder builder ::GameSystemPropertiesBuilder)"
					+ System.lineSeparator() + "  (builder:addProperties");
			defGameSystemProperties
					.forEach(def -> result.append(def.toConstructor(style).getExpression(style)));
			result.append("))");
		}
		if (defContextProperties.size() > 0) {
			result.append(
					System.lineSeparator()
							+ "(define (def:setup-context-builder builder ::ContextBuilder)"
							+ System.lineSeparator() + "  (builder:addProperties");
			defContextProperties
					.forEach(def -> result.append(def.toConstructor(style).getExpression(style)));
			result.append("))");
		}
		if (defGameScenarioProperties.size() > 0) {
			result.append(System.lineSeparator()
					+ "(define (def:setup-game-scenario-builder builder ::GameScenarioBuilder)"
					+ System.lineSeparator() + "  (builder:addProperties");
			defGameScenarioProperties
					.forEach(def -> result.append(def.toConstructor(style).getExpression(style)));
			result.append("))");
		}
		result.append(System.lineSeparator());
		setDefBootstrapScript(new DefBootstrapScript(result.toString()));
		return toDefBootstrap();
	}

	/**
	 *
	 * @param pathToResource jarファイル中では，"../"のように親を辿ることはできないことに注意．
	 * @return
	 */
	public Path getResource(String pathToResource) {
		return Path.coerceToPathOrNull(getResourceAsURI(pathToResource));
	}

	public URI getResourceAsURI(String pathToResource) {
		if (pathToResource.contains("../")) {
			String msg = "pathToResource should not include ../ . " + pathToResource;
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}

		URL url = getClass().getResource(pathToResource);
		if (url == null) {
			String msg = pathToResource + " is not found with " + getClass().getName();
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}
		try {
			return url.toURI();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 *
	 * @param pathToResource jarファイル中では，"../"のように親を辿ることはできないことに注意．
	 * @return
	 */
	public InputStream getResourceAsStream(String pathToResource) {
		if (pathToResource.contains("../")) {
			String msg = "pathToResource should not include ../ . = " + pathToResource;
			log.error(msg);
			throw new IllegalArgumentException(msg);
		}

		return getClass().getResourceAsStream(pathToResource);
	}

	public ResourceLoader setBootstrapInResource(String bootstrapFile) {
		this.bootstrapFile = bootstrapFile;
		return this;
	}

	public void setBootstrapFile(String bootstrapFile) {
		this.bootstrapFile = bootstrapFile;
	}

	public void setGameDefinitions(List<Pair<String, String>> gameDefinitions) {
		this.gameDefinitions = gameDefinitions;
	}

	public void setDefBootstrapScript(DefBootstrapScript defBootstrapScript) {
		for (DefBootstrapProperty p : defBootstrapProperties) {
			if (p instanceof DefBootstrapScript) {
				throw new IllegalStateException("defBootstrapScript can be set just one time.");
			}
		}
		this.defBootstrapProperties.add(defBootstrapScript);
	}

	public DefBootstrapScript getDefBootstrapScript() {
		for (DefBootstrapProperty p : defBootstrapProperties) {
			if (p instanceof DefBootstrapScript) {
				return (DefBootstrapScript) p;
			}
		}
		return null;
	}

	public void setDefBootstrapProperties(DefBootstrapProperty... defBootstrapProperties) {
		this.defBootstrapProperties = new ArrayList<>(Arrays.asList(defBootstrapProperties));
	}

	public void addDefBootstrapProperties(DefBootstrapProperty... defs) {
		Arrays.asList(defs).forEach(def -> this.defBootstrapProperties.add(def));
	}

	public void addDefContextProperties(DefContextProperty... defs) {
		Arrays.asList(defs).forEach(def -> this.defContextProperties.add(def));
	}

	public void addDefGameSystemProperties(DefGameSystemProperty... defs) {
		Arrays.asList(defs).forEach(def -> this.defGameSystemProperties.add(def));
	}

	public void addDefGameScenarioProperties(DefGameScenarioProperty... defs) {
		Arrays.asList(defs).forEach(def -> this.defGameScenarioProperties.add(def));

	}

	public void addDefGameScript(DefGameScript defGameScript) {
		defGameScripts.add(defGameScript);
	}

	public void setDefGameScripts(List<DefGameScript> defGameScripts) {
		this.defGameScripts = defGameScripts;
	}

	public boolean isBuilt() {
		if (gameBuilder == null) {
			return false;
		}
		return true;
	}

	public void buildIfNotExists() {
		if (isBuilt()) {
			return;
		}
		build();
	}

	public void setDefGameSystemProperties(DefGameSystemProperty... props) {
		defGameSystemProperties.addAll(Arrays.asList(props));
	}

	public void setProcessId(ProcessId processId) {
		this.processId = processId;
	}

}
