package org.magcruise.gaming.examples.croquette;

import org.magcruise.gaming.examples.croquette.resource.CroquetteGameResourceLoader;
import org.magcruise.gaming.examples.util.BasicGameExecutor;
import org.magcruise.gaming.examples.util.BasicGameExecutor.GameExecutionMode;
import org.magcruise.gaming.examples.util.PlayerMode;

public class BasicCroquetteGameExecutor {

  public static void main(String[] args) {
    BasicGameExecutor game = new BasicGameExecutor(PlayerMode.SINGLE_PLAYER,
        GameExecutionMode.IMSE_BROKER_SESSION_WITH_IMSE_WEBUI, false, false,
        CroquetteGameResourceLoader.class);

    game.run();
  }


}
