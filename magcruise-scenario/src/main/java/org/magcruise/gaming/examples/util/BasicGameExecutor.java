package org.magcruise.gaming.examples.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.magcruise.gaming.manager.session.GameSession;
import org.magcruise.gaming.manager.session.GameSessionCore;
import org.magcruise.gaming.manager.session.GameSessionOnExternalProcess;
import org.magcruise.gaming.manager.session.GameSessionOnServer;
import org.magcruise.gaming.manager.session.ResourceLoader;

public class BasicGameExecutor {
  static org.apache.logging.log4j.Logger log = LogManager.getLogger();

  private static String loginId = "admin";
  private static int maxAutoResponseTime = 5;
  private static final Level LOG_LEVEL = Level.INFO;

  private final GameSessionCore<?> session;


  /**
   * Swingはややこしくなるので，ここに入れない．
   *
   * @author nkjm
   *
   */
  public enum GameExecutionMode {
    LOCAL_INTERNAL_SESSION_WITH_LOCAL_WEBUI, LOCAL_EXTERNAL_SESSION_WITH_LOCAL_WEBUI, LOCAL_BROKER_SESSION_WITH_LOCAL_WEBUI, PUBLIC_BROKER_SESSION_WITH_LOCAL_WEBUI, IMSE_BROKER_SESSION_WITH_LOCAL_WEBUI, LOCAL_INTERNAL_SESSION_WITH_PUBLIC_WEBUI, LOCAL_EXTERNAL_SESSION_WITH_PUBLIC_WEBUI, LOCAL_BROKER_SESSION_WITH_PUBLIC_WEBUI, PUBLIC_BROKER_SESSION_WITH_PUBLIC_WEBUI, IMSE_BROKER_SESSION_WITH_PUBLIC_WEBUI, LOCAL_INTERNAL_SESSION_WITH_IMSE_WEBUI, LOCAL_EXTERNAL_SESSION_WITH_IMSE_WEBUI, LOCAL_BROKER_SESSION_WITH_IMSE_WEBUI, PUBLIC_BROKER_SESSION_WITH_IMSE_WEBUI, IMSE_BROKER_SESSION_WITH_IMSE_WEBUI;

    public GameSessionCore<?> createSession(Class<? extends ResourceLoader> clazz, String loginId) {
      GameSessionCore<?> session = toSession(clazz);
      setBroker(session);
      setWebUi(session, loginId);
      return session;
    }

    private GameSessionCore<?> toSession(Class<? extends ResourceLoader> clazz) {
      if (name().contains("INTERNAL_SESSION")) {
        return new GameSession(clazz);
      } else if (name().contains("EXTERNAL_SESSION")) {
        return new GameSessionOnExternalProcess(clazz);
      } else {
        return new GameSessionOnServer(clazz);
      }
    }

    private void setBroker(GameSessionCore<?> session) {
      if (name().contains("INTERNAL_SESSION") || name().contains("EXTERNAL_SESSION")
          || name().contains("LOCAL_BROKER_SESSION")) {
        session.useDefaultLocalBroker();
      } else if (name().contains("PUBLIC_BROKER_SESSION")) {
        session.useDefaultPublicBroker();
      } else if (name().contains("IMSE_BROKER_SESSION")) {
        session.setBrokerHost(GameExecutorConsts.IMSE_BROKER_HOST);
      } else {
        throw new RuntimeException();
      }
    }

    private void setWebUi(GameSessionCore<?> session, String loginId) {
      if (name().contains("LOCAL_WEBUI")) {
        session.useDefaultLocalWebUI(loginId);
      } else if (name().contains("PUBLIC_WEBUI")) {
        session.useDefaultPublicWebUI(loginId);
      } else if (name().contains("IMSE_WEBUI")) {
        session.useWebUI(GameExecutorConsts.IMSE_WEBUI_URL, loginId);
      } else {
        throw new RuntimeException();
      }

    }

  }



  public void run() {
    session.startAndWaitForFinish();
  }

  public BasicGameExecutor(PlayerMode playerMode, GameExecutionMode gameExecMode,
      boolean useAutoInput, boolean useRoundValidation, Class<? extends ResourceLoader> clazz) {

    this.session = gameExecMode.createSession(clazz, loginId);


    session.addGameDefinitionInResource(GameExecutorConsts.GAME_DEFINITION);

    session.setLogConfiguration(LOG_LEVEL);

    if (useAutoInput) {
      session.useAutoInput();
    }
    if (useRoundValidation) {
      session.useRoundValidation();
    }


    switch (playerMode) {
      case AGENTS -> {
        session.useAutoInput(maxAutoResponseTime);
        session.addGameDefinitionInResource(GameExecutorConsts.AGENTS_SCENARIO);
      }
      case SINGLE_PLAYER -> session
          .addGameDefinitionInResource(GameExecutorConsts.SINGLE_PLAYER_SCENARIO);
      case TWO_PLAYERS -> session
          .addGameDefinitionInResource(GameExecutorConsts.TWO_PLAYERS_SCENARIO);
      case THREE_PLAYERS -> session
          .addGameDefinitionInResource(GameExecutorConsts.THREE_PLAYERS_SCENARIO);
      default -> {
      }
    }
  }


}
