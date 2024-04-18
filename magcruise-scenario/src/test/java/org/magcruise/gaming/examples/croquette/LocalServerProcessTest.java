package org.magcruise.gaming.examples.croquette;

import java.util.Arrays;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.magcruise.gaming.examples.croquette.resource.CroquetteGameResourceLoader;
import org.magcruise.gaming.lang.SExpression.ToExpressionStyle;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.manager.session.GameSessionOnServer;
import org.magcruise.gaming.model.def.boot.DefBootstrapScript;
import org.magcruise.gaming.model.def.boot.DefGameScript;
import org.magcruise.gaming.util.SystemEnvironmentUtils;
import org.nkjmlab.util.java.net.UrlUtils;

import gnu.mapping.Symbol;

@Tag("local_server")
public class LocalServerProcessTest {
  private static org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  private static int maxAutoResponseTime = 0;

  private static int suspendRound = 3;

  /** サーバプロセスとして実行．Brokerを使っている． */
  @Test
  public void testServerProcessWithBroker() {
    GameSessionOnServer session = new GameSessionOnServer(CroquetteGameResourceLoader.class);
    session.useDefaultLocalBroker();
    session.addGameDefinitionsInResource("game-definition.scm", "agent-players-definition.scm");
    session.setLogConfiguration(Level.INFO, true);
    session.useAutoInput();
    session.build();
    log.info(session.toDefBootstrap());
    ProcessId pid = session.startAndWaitForFinish();
    log.debug(pid);
    ResultValidator.validate(pid);
  }

  /**
   * LocalBrokerとLocalUIを動かした状態でテストする必要がある． <a href=
   * "http://localhost:6789/app/active-sessions.html">http://localhost:6789/app/active-sessions.html</a>にadminでログインすると表示される．
   */
  @Test
  public void testServerProcessWithBrokerAndWebUi() {
    GameSessionOnServer session = new GameSessionOnServer(CroquetteGameResourceLoader.class);
    session.setBrokerUrl(UrlUtils.of(SystemEnvironmentUtils.getDefaultLocalBrokerUrl()));
    session.useDefaultLocalBroker();
    session.useDefaultLocalWebUI("admin");
    session.addGameDefinitionsInResource("game-definition.scm", "agent-players-definition.scm");
    session.setLogConfiguration(Level.INFO, true);
    session.useAutoInput();
    session.build();
    log.info(session.toDefBootstrap());
    ProcessId pid = session.startAndWaitForFinish();
    log.debug(pid);
    ResultValidator.validate(pid);
  }

  @Test
  public void testRunOnServerWithBootstrapScript() {
    GameSessionOnServer session = new GameSessionOnServer(CroquetteGameResourceLoader.class);
    session.setBrokerUrl(UrlUtils.of(SystemEnvironmentUtils.getDefaultLocalBrokerUrl()));
    DefBootstrapScript bootstrapScript =
        createSession(
                SystemEnvironmentUtils.getDefaultLocalBrokerUrl(),
                SystemEnvironmentUtils.getDefaultLocalWebUI())
            .toDefBootstrap();
    session.setDefBootstrapScript(bootstrapScript);
    session.startAndWaitForFinish();
    ResultValidator.getLatestContextAndValidateResult(session);
  }

  private static GameSessionOnServer createSession(String brokerUrl, String webUiUrl) {
    GameSessionOnServer session = new GameSessionOnServer(CroquetteGameResourceLoader.class);
    session.setBrokerUrl(UrlUtils.of(brokerUrl));
    session.useWebUI(webUiUrl, "admin");
    session.addGameDefinitionsInResource("game-definition.scm", "agent-players-definition.scm");
    session.setLogConfiguration(Level.INFO, true);
    session.useAutoInput();
    session.build();
    log.info(session.toDefBootstrap());
    return session;
  }

  @Test
  public void testRevertOnServer() {
    String brokerHost = "localhost:5678";
    GameSessionOnServer session = new GameSessionOnServer(CroquetteGameResourceLoader.class);
    session.setBrokerHost(brokerHost);
    session.useDefaultLocalWebUI("admin");
    session.addGameDefinitionsInResource("game-definition.scm", "agent-players-definition.scm");
    session.addDefGameScript(getRerverScript(brokerHost));
    session.setLogConfiguration(Level.INFO, true);
    session.useAutoInput(maxAutoResponseTime);
    session.build();
    session
        .getGameBuilder()
        .addAssignmentRequests(
            Arrays.asList(Symbol.parse("Factory"), Symbol.parse("Shop1"), Symbol.parse("Shop2")),
            Arrays.asList(Symbol.parse("user1"), Symbol.parse("user1"), Symbol.parse("user1")));
    session.startAndWaitForFinish();
    ResultValidator.getLatestContextAndValidateResult(session);
  }

  private static DefGameScript getRerverScript(String brokerHost) {
    GameSessionOnServer session = new GameSessionOnServer(CroquetteGameResourceLoader.class);
    session.setBrokerHost(brokerHost);
    session.useDefaultLocalWebUI("admin");
    session.addGameDefinitionInResource("game-definition.scm");
    session.addGameDefinitionInResource("agent-players-definition.scm");
    session.setLogConfiguration(Level.INFO);
    session.useAutoInput();
    session.setFinalRound(suspendRound);
    ProcessId pid = session.startAndWaitForFinish();
    log.info(pid);
    DefGameScript def =
        new DefGameScript(
            session.getLatestContext().toSExpressionForRevert(ToExpressionStyle.MULTI_LINE));
    log.info(def);
    return def;
  }
}
