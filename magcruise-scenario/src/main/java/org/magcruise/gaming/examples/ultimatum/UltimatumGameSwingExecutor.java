package org.magcruise.gaming.examples.ultimatum;

import org.magcruise.gaming.examples.ultimatum.actor.FirstPlayer;
import org.magcruise.gaming.examples.ultimatum.actor.SecondPlayer;
import org.magcruise.gaming.examples.ultimatum.resource.UltimatumGameResourceLoader;
import org.magcruise.gaming.manager.session.GameSession;
import org.magcruise.gaming.model.def.actor.DefPlayer;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.Player.PlayerType;
import org.nkjmlab.sorm4j.util.h2.server.H2TcpServerProcess;
import org.nkjmlab.sorm4j.util.h2.server.H2TcpServerProperties;

public class UltimatumGameSwingExecutor {

  static {
    new H2TcpServerProcess(H2TcpServerProperties.builder().build()).awaitStart();
  }

  public static void main(String[] args) {
    GameSession session = new GameSession(UltimatumGameResourceLoader.class);
    // launcher.setBootstrapInResource("bootstrap.scm");
    session.addGameDefinitionInResource("game-definition.scm");
    session.addDefPlayer(
        new DefPlayer(ActorName.of("FirstPlayer"), PlayerType.AGENT, FirstPlayer.class));
    session.addDefPlayer(
        new DefPlayer(ActorName.of("SecondPlayer"), PlayerType.AGENT, SecondPlayer.class));
    session.useSwingGui();
    // launcher.useAutoInput();
    session.start();
  }

}
