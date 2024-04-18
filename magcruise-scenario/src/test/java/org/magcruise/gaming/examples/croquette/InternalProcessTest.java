package org.magcruise.gaming.examples.croquette;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;
import org.magcruise.gaming.examples.croquette.resource.CroquetteGameResourceLoader;
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
    GameSession session = new GameSession(CroquetteGameResourceLoader.class);
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
    GameSession session = new GameSession(CroquetteGameResourceLoader.class);
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
    GameSession session = new GameSession(CroquetteGameResourceLoader.class);
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
    Path revertCode = getReverCode(suspendround);
    ProcessId pid = restart(revertCode, suspendround);
    ResultValidator.checkResult(pid, suspendround);
  }

  private static ProcessId restart(Path revertCode, int suspendround) {
    GameSession session = new GameSession(CroquetteGameResourceLoader.class);
    session.addGameDefinitionsInResource("game-definition.scm", "agent-players-definition.scm");
    session.addGameDefinition(revertCode);
    session.setLogConfiguration(Level.INFO, true);
    session.useAutoInput();
    session.build();
    log.info(session.toDefBootstrap());
    ProcessId pid = session.startAndWaitForFinish();
    return pid;
  }

  private static Path getReverCode(int suspendround) {
    GameSession session = new GameSession(CroquetteGameResourceLoader.class);
    session.addGameDefinitionsInResource("game-definition.scm", "agent-players-definition.scm");
    session.setFinalRound(suspendround);
    session.setLogConfiguration(Level.INFO, true);
    session.useAutoInput();
    session.startAndWaitForFinish();
    log.info("Before revert laucher pid ={}", session.getProcessId());
    log.info("Before revert laucher={}", session);
    return session.getRevertScriptPath();
  }
}
