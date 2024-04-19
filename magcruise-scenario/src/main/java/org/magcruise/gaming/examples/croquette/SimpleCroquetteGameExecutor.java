package org.magcruise.gaming.examples.croquette;

import org.magcruise.gaming.examples.croquette.resource.CroquetteGameResourceLoader;
import org.magcruise.gaming.examples.util.GameExecutorConsts;
import org.magcruise.gaming.manager.session.GameSession;
import org.magcruise.gaming.manager.session.GameSessionCore;
import org.magcruise.gaming.manager.session.GameSessionOnExternalProcess;

public class SimpleCroquetteGameExecutor {
  static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private static int maxAutoResponseTime = 5;

  private final GameSessionCore<?> session;

  private enum ExecMode {
    EXT, INTERNAL
  }

  public static void main(String[] args) {

    SimpleCroquetteGameExecutor executor = new SimpleCroquetteGameExecutor(ExecMode.INTERNAL);
    executor.run();
  }

  public SimpleCroquetteGameExecutor(ExecMode execMode) {
    this.session =
        execMode == ExecMode.INTERNAL ? new GameSession(CroquetteGameResourceLoader.class)
            : new GameSessionOnExternalProcess(CroquetteGameResourceLoader.class);
    session.addGameDefinitionInResource(GameExecutorConsts.GAME_DEFINITION);
    session.addGameDefinitionInResource(GameExecutorConsts.SINGLE_PLAYER_SCENARIO);
    session.useRoundValidation();
    session.useAutoInput(maxAutoResponseTime);
  }

  private void run() {
    session.start();

  }

}
