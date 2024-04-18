package org.magcruise.gaming.webui.model;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.magcruise.gaming.webui.row.Assignment;
import org.magcruise.gaming.webui.row.GameSession;
import org.magcruise.gaming.webui.row.Progress;

public class GameSessionWithAsignmentsAndProgress {
  private GameSession gameSession;
  private List<Assignment> assignments;
  private Progress progress;

  public GameSessionWithAsignmentsAndProgress() {}

  public GameSessionWithAsignmentsAndProgress(
      GameSession session, List<Assignment> assignments, Progress progress) {
    this.gameSession = session;
    this.assignments = new ArrayList<>(assignments);
    this.progress = progress;
  }

  public GameSession getGameSession() {
    return gameSession;
  }

  public void setGameSession(GameSession gameSession) {
    this.gameSession = gameSession;
  }

  public List<Assignment> getAssignments() {
    return assignments;
  }

  public void setAssignments(List<Assignment> assignments) {
    this.assignments = assignments;
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
    } else if (!(o instanceof GameSessionWithAsignmentsAndProgress)) {
      return false;
    }
    GameSessionWithAsignmentsAndProgress other = (GameSessionWithAsignmentsAndProgress) o;
    return new EqualsBuilder().append(this.gameSession, other.gameSession).build();
  }
}
