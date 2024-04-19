package org.magcruise.gaming.examples.ultimatum;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;
import org.magcruise.gaming.examples.ultimatum.resource.UltimatumGameResourceLoader;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.manager.session.GameSession;
import org.magcruise.gaming.util.SystemEnvironmentUtils;
import org.nkjmlab.util.java.net.UrlUtils;
import gnu.kawa.io.Path;

public class InternalProcessTest {
  private static org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  /** 内部プロセスとして実行．BrokerとUIは使っていない． */
  @Test
  public void testInternalProcess() {
    GameSession session = new GameSession(UltimatumGameResourceLoader.class);
    session.addGameDefinitionsInResource("game-definition.scm", "agent-players-definition.scm");
    session.setLogConfiguration(Level.INFO, true);
    session.useAutoInput();
    session.build();
    log.info(session.toDefBootstrap());
    ProcessId pid = session.startAndWaitForFinish();
    log.debug(pid);
    ResultValidator.validate(pid);
  }

  /** 内部プロセスとして実行．Brokerを使っている． */
  @Test
  public void testInternalProcessWithBroker() {
    GameSession session = new GameSession(UltimatumGameResourceLoader.class);
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
   * 内部プロセスとして実行．BrokerとWebUIを使っている． <a
   * href="http://localhost:6789/app/all-sessions.html">セッション履歴</a> から見ることが出来る．
   */
  @Test
  public void testInternalProcessWithBrokerAndWebUi() {
    GameSession session = new GameSession(UltimatumGameResourceLoader.class);
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
  public void testRevert() {
    int suspendround = 4;
    GameSession revLauncher = new GameSession(UltimatumGameResourceLoader.class);
    revLauncher.addGameDefinitionInResource("game-definition.scm");
    revLauncher.addGameDefinitionInResource("agent-players-definition.scm");
    revLauncher.useAutoInput();
    revLauncher.setFinalRound(suspendround);
    revLauncher.startAndWaitForFinish();
    Path revertCode = revLauncher.getRevertScriptPath();
    GameSession launcher = new GameSession(UltimatumGameResourceLoader.class);
    launcher.setBootstrapInResource("game-definition.scm");
    launcher.addGameDefinitionInResource("agent-players-definition.scm");
    launcher.addGameDefinition(revertCode);
    launcher.useAutoInput();
    ProcessId pid = launcher.startAndWaitForFinish();
    ResultValidator.validate(pid, suspendround + 1, 10);
  }
}
