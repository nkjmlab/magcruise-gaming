package org.magcruise.gaming.webui.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.magcruise.gaming.webui.row.GameSession;
import org.magcruise.gaming.webui.row.Progress;

public class GameSessionWithProgress {

  private GameSession gameSession;
  private Progress progress;

  public GameSessionWithProgress() {}

  public GameSessionWithProgress(GameSession gameSession, Progress progress) {
    this.gameSession = gameSession;
    this.progress = progress;
  }

  public GameSession getGameSession() {
    return gameSession;
  }

  public void setGameSession(GameSession gameSession) {
    this.gameSession = gameSession;
  }

  public Progress getProgress() {
    return progress;
  }

  public void setProgress(Progress progress) {
    this.progress = progress;
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(gameSession.hashCode()).toHashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof GameSessionWithProgress)) {
      return false;
    }
    GameSessionWithProgress other = (GameSessionWithProgress) o;
    return new EqualsBuilder().append(this.gameSession, other.gameSession).build();
  }
}
