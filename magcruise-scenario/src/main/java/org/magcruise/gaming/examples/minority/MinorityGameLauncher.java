package org.magcruise.gaming.examples.minority;

import org.magcruise.gaming.examples.minority.resource.MinorityGameResourceLoader;
import org.magcruise.gaming.manager.session.GameSession;

public class MinorityGameLauncher {

  public static void main(String[] args) {

    GameSession launcher = new GameSession(MinorityGameResourceLoader.class);
    launcher.addGameDefinitionInResource("game-definition.scm");
    launcher.start();

  }

}
