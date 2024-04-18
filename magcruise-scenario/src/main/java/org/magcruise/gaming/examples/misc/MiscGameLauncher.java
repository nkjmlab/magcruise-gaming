package org.magcruise.gaming.examples.misc;

import org.magcruise.gaming.examples.misc.resource.MiscGameResourceLoader;
import org.magcruise.gaming.manager.session.GameSession;

public class MiscGameLauncher {

  public static void main(String[] args) {
    GameSession session = new GameSession(MiscGameResourceLoader.class);
    // launcher.addGameDefinitionInResource("sample-workflow.scm");
    session.useDefaultPublicBroker();
    session.useDefaultPublicWebUI("nishino");
    session.addGameDefinitionInResource("gui-test.scm");
    session.startAndWaitForFinish();

  }

}
