package org.magcruise.gaming.webui.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.magcruise.gaming.webui.row.Assignment;
import org.magcruise.gaming.webui.row.GameSession;

public class GameSessionWithAsignment {

  private GameSession gameSession;
  private Assignment assignment;

  public GameSessionWithAsignment() {}

  public GameSessionWithAsignment(GameSession session, Assignment assignment) {
    this.gameSession = session;
    this.assignment = assignment;
  }

  public GameSession getGameSession() {
    return gameSession;
  }

  public void setGameSession(GameSession gameSession) {
    this.gameSession = gameSession;
  }

  public Assignment getAssignment() {
    return assignment;
  }

  public void setAssignment(Assignment playerName) {
    this.assignment = playerName;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(gameSession.hashCode()).toHashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof GameSessionWithAsignment)) {
      return false;
    }
    GameSessionWithAsignment other = (GameSessionWithAsignment) o;
    return new EqualsBuilder().append(this.gameSession, other.gameSession).build();
  }
}
