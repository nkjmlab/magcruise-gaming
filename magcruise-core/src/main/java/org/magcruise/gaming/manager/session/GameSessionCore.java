package org.magcruise.gaming.manager.session;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.executor.db.GameRecordsTable;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.manager.process.GameProcess;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.def.GameBuilder;
import org.magcruise.gaming.model.def.actor.DefAssignmentRequest;
import org.magcruise.gaming.model.def.actor.DefAutoInput;
import org.magcruise.gaming.model.def.actor.DefMaxAutoResponseTime;
import org.magcruise.gaming.model.def.actor.DefPlayer;
import org.magcruise.gaming.model.def.actor.DefRoundValidationInput;
import org.magcruise.gaming.model.def.boot.DefBootstrapScript;
import org.magcruise.gaming.model.def.boot.DefClasspath;
import org.magcruise.gaming.model.def.boot.DefGameScript;
import org.magcruise.gaming.model.def.boot.DefRemoteDebug;
import org.magcruise.gaming.model.def.scenario.DefFinalRound;
import org.magcruise.gaming.model.def.scenario.round.DefRound;
import org.magcruise.gaming.model.def.sys.DefGameLogDb;
import org.magcruise.gaming.model.def.sys.DefLog4jConfig;
import org.magcruise.gaming.model.def.sys.DefUI;
import org.magcruise.gaming.model.def.sys.DefUIServiceForRegisterSession;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.model.game.SimpleContext;
import org.magcruise.gaming.util.SystemEnvironmentUtils;
import org.nkjmlab.util.java.lang.SystemPropertyUtils;
import org.nkjmlab.util.java.net.UrlUtils;
import gnu.kawa.io.Path;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public abstract class GameSessionCore<T extends GameProcess> implements SConstructive {
  protected static Logger log = LogManager.getLogger();

  private static ResourceLoader createResourceLoader(Class<? extends ResourceLoader> clazz) {
    try {
      return clazz.getDeclaredConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException | SecurityException e) {
      throw new IllegalArgumentException((clazz != null ? clazz.toString() : "") + " is invalid.",
          e);
    }
  }

  protected ResourceLoader resourceLoader;

  protected T process;
  protected boolean executed = false;

  protected boolean built = false;

  protected URL brokerUrl;

  protected String loginId;

  protected String webUIUrl;

  protected String sessionName = "";
  protected String description = "";

  protected boolean useWebsocket = true;

  protected GameRecordsTable gameRecordsTable =
      new GameRecordsTable(SystemEnvironmentUtils.getDefaultDataSource());

  public GameSessionCore(Class<? extends ResourceLoader> clazz) {
    SystemPropertyUtils.setUseSystemProxies();
    this.resourceLoader = createResourceLoader(clazz);
  }

  public void addClasspath(String jarPath) {
    checkBuilt();
    resourceLoader.addDefBootstrapProperties(new DefClasspath(jarPath));
  }

  public void addDefGameScript(DefGameScript defGameScript) {
    checkBuilt();
    resourceLoader.addDefGameScript(defGameScript);
  }

  public void addDefUI(DefUI defUI) {
    checkBuilt();
    resourceLoader.addDefGameSystemProperties(defUI);
  }

  public void addGameDefinition(Path path) {
    checkBuilt();
    resourceLoader.addGameDefinition(path);
  }

  public void addGameDefinitions(Path... paths) {
    Arrays.asList(paths).forEach(path -> addGameDefinition(path));
  }

  public void addGameDefinitionInResource(String gameDefinitionFile) {
    checkBuilt();
    resourceLoader.addGameDefinitionInResource(gameDefinitionFile);
  }

  public void addGameDefinitionsInResource(String... gameDefinitionFile) {
    Arrays.asList(gameDefinitionFile).forEach(f -> addGameDefinitionInResource(f));
  }

  public void addDefPlayer(DefPlayer defPlayer) {
    checkBuilt();
    resourceLoader.addDefContextProperties(defPlayer);
  }

  public void addAssignmentRequests(List<Symbol> playerNames, List<Symbol> operatorIds) {
    checkBuilt();
    for (int i = 0; i < playerNames.size(); i++) {
      resourceLoader.addDefContextProperties(new DefAssignmentRequest(
          ActorName.of(playerNames.get(i)), operatorIds.get(i).toString()));
    }
  }

  public void addDefRound(DefRound defRound) {
    checkBuilt();
    resourceLoader.addDefGameScenarioProperties(defRound);
  }

  public GameBuilder build() {
    if (resourceLoader.isBuilt()) {
      String msg = "buildGameBuilder method sould be call just one time.";
      log.error(msg);
      throw new IllegalStateException(msg);
    }
    if (webUIUrl != null) {
      if (brokerUrl == null) {
        useDefaultPublicBroker();
      }
      addDefUI(new DefUIServiceForRegisterSession(webUIUrl, loginId, brokerUrl.toString(),
          sessionName, description));
    }
    GameBuilder builder = resourceLoader.build();
    built = true;
    return builder;
  }

  public void buildIfNotBuilt() {
    if (resourceLoader.isBuilt()) {
      return;
    }
    build();
  }

  protected void checkBuilt() {
    if (built) {
      throw new RuntimeException("can not set value after build.");
    }
  }

  public URL getBrokerUrl() {
    return brokerUrl;
  }

  public String getEnvironmentName() {
    return getProcessId().getValue();
  }

  public GameBuilder getGameBuilder() {
    return resourceLoader.getGameBuilder();
  }

  public Context getLatestContext() {
    return getLatestContext(SimpleContext.class);
  }

  public <S extends Context> S getLatestContext(Class<S> clazz) {
    return new SConstructor<S>(getLatestRecord()).makeInstance(getProcessId().toString());
  }

  public String getLatestRecord() {
    return process.getLatestRecord(getProcessId());
  }

  public T getProcess() {
    return process;
  }

  public ProcessId getProcessId() {
    return resourceLoader.getProcessId();
  }

  public ResourceLoader getResourceLoader() {
    return resourceLoader;
  }

  public Path getRevertScriptPath() {
    java.nio.file.Path p = SystemEnvironmentUtils.toTmpScriptFile("revert", ".scm", getProcessId(),
        getLatestContext().toSExpressionForRevert(ToExpressionStyle.MULTI_LINE));
    return gnu.kawa.io.Path.coerceToPathOrNull(p.toFile());
  }

  public void checkExecuted() {
    if (executed) {
      String msg = "Session can start just one time.";
      log.error(msg);
      throw new IllegalStateException(msg);
    }
  }

  public void setAutoInputMode(boolean use) {
    checkBuilt();
    resourceLoader.addDefContextProperties(new DefAutoInput(use));
  }

  public void setBootstrapInResource(String bootstrapFile) {
    checkBuilt();
    resourceLoader.setBootstrapInResource(bootstrapFile);
  }

  public void setBrokerHost(String brokerHostname) {
    checkBuilt();
    String protocol = brokerHostname.contains("localhost") ? "http://" : "https://";
    setBrokerUrl(UrlUtils.of(protocol + brokerHostname + "/app"));
  }

  public void setBrokerUrl(URL brokerUrl) {
    checkBuilt();
    this.brokerUrl = brokerUrl;
  }

  public void setDefBootstrapScript(DefBootstrapScript defBootstrapScript) {
    checkBuilt();
    resourceLoader.setDefBootstrapScript(defBootstrapScript);
  }

  public void setFinalRound(int roundnum) {
    checkBuilt();
    resourceLoader.addDefGameScenarioProperties(new DefFinalRound(roundnum));
  }

  public void setLogConfiguration(Level level) {
    checkBuilt();
    setLogConfiguration(level, true);
  }

  public void setLogConfiguration(Level level, boolean location) {
    checkBuilt();
    resourceLoader.addDefBootstrapProperties(new DefLog4jConfig(level, location));
  }

  /**
   *
   * @param maxAutoResponseTime sec.
   */
  public void setMaxAutoResponseTime(int maxAutoResponseTime) {
    checkBuilt();
    resourceLoader.addDefContextProperties(new DefMaxAutoResponseTime(maxAutoResponseTime));
  }

  public void setStartFromEndOf(ProcessId pid, int roundnum) {
    checkBuilt();
    String record = gameRecordsTable.readBy(pid, roundnum).getRecord();
    if (record == null) {
      throw new IllegalArgumentException(
          "pid or roundnum is invalid. pid=" + pid + ",roundnum=" + roundnum);
    }

    String expr = String.format("(define (def:revert-context game-builder ::GameBuilder)"
        + " (game-builder:setStartFromEndOfRound %s %d))", record, roundnum);
    java.nio.file.Path path =
        SystemEnvironmentUtils.toTmpScriptFile("revert", ".scm", getProcessId(), expr);
    addGameDefinition(gnu.kawa.io.Path.coerceToPathOrNull(path.toFile()));
  }

  public void useDefaultPublicWebUI(String loginId) {
    checkBuilt();
    useWebUI(SystemEnvironmentUtils.getDefaultPublicWebUI(), loginId);
  }

  public void useDefaultLocalWebUI(String loginId) {
    checkBuilt();
    useWebUI(SystemEnvironmentUtils.getDefaultLocalWebUI(), loginId);
  }

  public void useWebUI(String webUIUrl, String loginId) {
    checkBuilt();
    this.webUIUrl = webUIUrl;
    this.loginId = loginId;
  }

  public abstract ProcessId start();

  public ProcessId startAndWaitForFinish() {
    start();
    process.waitFor();
    return getProcessId();
  }

  @Override
  public SConstructor<?> toConstructor(ToExpressionStyle style) {
    return SConstructor.toConstructor(style, getClass(), resourceLoader);
  }

  public DefBootstrapScript toDefBootstrap() {
    if (!resourceLoader.isBuilt()) {
      String msg = "call build method before toDefBootstrap.";
      log.error(msg);
      throw new IllegalStateException(msg);
    }
    return resourceLoader.toDefBootstrap();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public void useAutoInput() {
    checkBuilt();
    setAutoInputMode(true);
  }

  /**
   *
   * @param maxAutoResponseTime sec.
   */
  public void useAutoInput(int maxAutoResponseTime) {
    checkBuilt();
    setAutoInputMode(true);
    setMaxAutoResponseTime(maxAutoResponseTime);
  }

  public void useRemoteDebug() {
    checkBuilt();
    resourceLoader.addDefBootstrapProperties(new DefRemoteDebug(true));
  }

  public void useRoundValidation() {
    checkBuilt();
    resourceLoader.addDefContextProperties(new DefRoundValidationInput(true));
  }

  public void useDefaultPublicBroker() {
    checkBuilt();
    this.brokerUrl = UrlUtils.of(SystemEnvironmentUtils.getDefaultPublicBrokerUrl());
  }

  public void useDefaultLocalBroker() {
    checkBuilt();
    this.brokerUrl = UrlUtils.of(SystemEnvironmentUtils.getDefaultLocalBrokerUrl());
  }

  public void useDefaultPublicBrokerAndWebUI(String loginId) {
    useDefaultPublicBroker();
    useDefaultPublicWebUI(loginId);
  }

  public void useDefaultLocalBrokerAndWebUI(String loginId) {
    useDefaultLocalBroker();
    useDefaultLocalWebUI(loginId);
  }

  public void disableWebsocketFetcher() {
    checkBuilt();
    this.useWebsocket = false;
  }

  public void setLogDb(String parentPath, String dbName) {
    resourceLoader.setDefGameSystemProperties(new DefGameLogDb(Paths.get(parentPath), dbName));
  }

  public String getSessionName() {
    return sessionName;
  }

  public void setSessionName(String sessionName) {
    this.sessionName = sessionName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
