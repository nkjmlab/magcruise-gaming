package org.magcruise.gaming.webui.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.magcruise.gaming.webui.row.Assignment;
import org.magcruise.gaming.webui.row.GameSession;
import org.magcruise.gaming.webui.row.Progress;

public class GameSessionWithAsignmentAndProgress {

  private GameSession gameSession;
  private Assignment assignment;
  private Progress progress;

  public GameSessionWithAsignmentAndProgress() {}

  public GameSessionWithAsignmentAndProgress(
      GameSession session, Assignment assignment, Progress progress) {
    this.gameSession = session;
    this.assignment = assignment;
    this.progress = progress;
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

  public void setAssign(Assignment asignment) {
    this.assignment = asignment;
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
    } else if (!(o instanceof GameSessionWithAsignmentAndProgress)) {
      return false;
    }
    GameSessionWithAsignmentAndProgress other = (GameSessionWithAsignmentAndProgress) o;
    return new EqualsBuilder().append(this.gameSession, other.gameSession).build();
  }
}
