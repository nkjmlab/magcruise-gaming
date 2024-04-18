package org.magcruise.gaming.examples.croquette;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;
import org.magcruise.gaming.examples.croquette.resource.CroquetteGameResourceLoader;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.manager.session.GameSessionOnExternalProcess;
import org.magcruise.gaming.util.SystemEnvironmentUtils;
import org.nkjmlab.util.java.net.UrlUtils;

public class ExternalProcessTest {
  private static org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  /** 外部プロセスとして実行．BrokerとUIは使っていない． */
  @Test
  public void testExternalProcess() {
    GameSessionOnExternalProcess session =
        new GameSessionOnExternalProcess(CroquetteGameResourceLoader.class);
    session.addGameDefinitionsInResource("game-definition.scm", "agent-players-definition.scm");
    session.setLogConfiguration(Level.INFO, true);
    session.useAutoInput();
    session.build();
    log.info(session.toDefBootstrap());
    ProcessId pid = session.startAndWaitForFinish();
    log.debug(pid);
    ResultValidator.validate(pid);
  }

  /** 外部プロセスとして実行．Brokerを使っている． */
  @Test
  public void testExternalProcessWithBroker() {
    GameSessionOnExternalProcess session =
        new GameSessionOnExternalProcess(CroquetteGameResourceLoader.class);
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
   * 外部プロセスとして実行．BrokerとWebUIを使っている． <a
   * href="http://localhost:6789/app/all-sessions.html">セッション履歴</a> から見ることが出来る．
   */
  @Test
  public void testExternalProcessWithBrokerAndWebUi() {
    GameSessionOnExternalProcess session =
        new GameSessionOnExternalProcess(CroquetteGameResourceLoader.class);
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
}
