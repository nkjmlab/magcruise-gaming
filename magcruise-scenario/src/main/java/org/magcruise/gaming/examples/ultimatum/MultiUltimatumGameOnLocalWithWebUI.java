package org.magcruise.gaming.examples.ultimatum;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.examples.ultimatum.resource.UltimatumGameResourceLoader;
import org.magcruise.gaming.manager.session.GameSession;

public class MultiUltimatumGameOnLocalWithWebUI {
  protected static Logger log = LogManager.getLogger();

  private static String loginId = "admin";
  @SuppressWarnings("unused")
  private static int maxAutoResponseTime = 5;

  public static void main(String[] args) {
    GameSession session = new GameSession(UltimatumGameResourceLoader.class);
    // session.useDefaultPublicBrokerAndWebUI(loginId);
    session.useDefaultPublicWebUI(loginId);
    session.addGameDefinitionInResource("game-definition.scm");
    session.startAndWaitForFinish();
  }
}
