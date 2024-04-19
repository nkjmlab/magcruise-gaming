/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 MAGCruise
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.magcruise.broker.jsonrpc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.magcruise.broker.common.PlayerMessageBoxes;
import org.magcruise.gaming.executor.api.RequestToUIService;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.game.LRUMessageBoxes;
import org.magcruise.gaming.model.game.MessageBoxes;
import org.magcruise.gaming.ui.api.GameSessionJson;
import org.magcruise.gaming.ui.api.message.GameProgress;
import org.magcruise.gaming.ui.api.message.NotifyEndOfGame;
import org.magcruise.gaming.ui.api.message.NotifyEndOfRound;
import org.magcruise.gaming.ui.api.message.NotifyGameRecord;
import org.magcruise.gaming.ui.api.message.NotifyJoinInGame;
import org.magcruise.gaming.ui.api.message.NotifyStartOfGameProcess;
import org.magcruise.gaming.ui.api.message.PlayersState;
import org.magcruise.gaming.ui.api.message.RequestToAssignOperator;
import org.magcruise.gaming.ui.api.message.RequestToInput;
import org.magcruise.gaming.ui.api.message.RequestToRegisterGameSession;
import org.magcruise.gaming.ui.api.message.RequestToShowMessage;
import org.magcruise.gaming.ui.api.message.RequestToUI;
import org.magcruise.gaming.ui.api.message.StartOfGameEvent;
import org.magcruise.gaming.ui.api.message.StartOfRoundEvent;
import org.magcruise.gaming.ui.model.input.InputToUI;
import org.magcruise.gaming.ui.model.message.MessageToUI;
import org.nkjmlab.util.java.concurrent.RetryUtils;

public class RequestToUIController implements RequestToUIService {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();
  private static final int MAX_PROCESSES_SIZE = 200;

  protected static MessageBoxes<ProcessId, RequestToUI> gameMessageQueues =
      new LRUMessageBoxes<>(MAX_PROCESSES_SIZE);
  protected static PlayerMessageBoxes playerMessageQueues =
      new PlayerMessageBoxes(MAX_PROCESSES_SIZE);

  @Override
  public RequestToRegisterGameSession[] getRegisteredGameSessions() {
    return gameMessageQueues
        .values(RequestToRegisterGameSession.class)
        .toArray(new RequestToRegisterGameSession[0]);
  }

  @Override
  public GameProgress[] getProgresses(String processId) {
    return gameMessageQueues
        .values(new ProcessId(processId), GameProgress.class)
        .toArray(new GameProgress[0]);
  }

  @Override
  public RequestToShowMessage[] getMessages(String processId, String playerId) {
    List<RequestToShowMessage> result = new ArrayList<>();

    for (RequestToShowMessage e :
        playerMessageQueues.get(RequestToShowMessage.class, new ProcessId(processId), playerId)) {
      result.add(e);
    }

    result.sort((a, b) -> (int) (a.getId() - b.getId()));
    return result.toArray(new RequestToShowMessage[0]);
  }

  @Override
  public RequestToInput[] getNewRequestsToInput(String processId, String playerName) {

    List<RequestToInput> result = new ArrayList<>();

    for (RequestToInput e :
        playerMessageQueues.get(RequestToInput.class, new ProcessId(processId), playerName)) {
      if (!e.getFinished()) {
        result.add(e);
      }
    }
    return result.toArray(new RequestToInput[0]);
  }

  /** 未割当の割当リクエストを取得する */
  @Override
  public RequestToAssignOperator[] getNewAssignmentRequests(String processId) {

    List<RequestToAssignOperator> result = new ArrayList<>();
    for (RequestToAssignOperator e : getAllAssignmentRequests(processId)) {
      if (!e.getAssigned()) {
        result.add(e);
      }
    }
    return result.toArray(new RequestToAssignOperator[0]);
  }

  public List<RequestToAssignOperator> getAllAssignmentRequests(String processId) {
    return gameMessageQueues.values(new ProcessId(processId), RequestToAssignOperator.class);
  }

  @Override
  public PlayersState getPlayersState(String processId) {
    List<RequestToAssignOperator> assinments = getAllAssignmentRequests(processId);

    PlayersState result = new PlayersState();
    for (RequestToAssignOperator assignment : assinments) {
      result.addAssignment(assignment);
      for (RequestToInput r :
          playerMessageQueues.get(
              RequestToInput.class, new ProcessId(processId), assignment.getPlayerName())) {
        result.addRequestToInput(r);
      }
    }
    return result;
  }

  @Override
  public Serializable showMessage(
      String processId, String playerName, long messageId, int roundnum, MessageToUI message) {
    log.info("{} in {} on round {} to {}, ", messageId, processId, roundnum, playerName, message);
    playerMessageQueues.offer(
        new ProcessId(processId),
        playerName,
        new RequestToShowMessage(messageId, playerName, roundnum, message));
    return null;
  }

  @Override
  public Serializable requestToInput(
      String processId,
      String playerName,
      long requestId,
      String label,
      int roundnum,
      InputToUI[] inputs) {
    log.info("{} in {} to {}, {}", requestId, processId, playerName, label);
    playerMessageQueues.offer(
        new ProcessId(processId),
        playerName,
        new RequestToInput(requestId, label, roundnum, playerName, inputs, false));
    return null;
  }

  @Override
  public Serializable requestToInputAndAutoResponse(
      String processId,
      String playerName,
      long requestId,
      String label,
      int roundnum,
      InputToUI[] inputs,
      int maxResponseTime) {
    log.warn("This method is valid in GameInteraction Controller.");
    return null;
  }

  @Override
  public Serializable requestToAssign(String processId, String playerName, String operatorId) {
    log.info("{} is requested to assign {} in {}", operatorId, playerName, processId);
    gameMessageQueues.offer(
        new ProcessId(processId), new RequestToAssignOperator(playerName, operatorId));
    return null;
  }

  /**
   * 入力を受けとった時の処理．
   *
   * @param processId
   * @param requestId
   * @param playerName
   */
  public void receiveInput(String processId, long requestId, String playerName) {
    try {
      RetryUtils.retry(
          () -> {
            for (RequestToInput r :
                playerMessageQueues.get(
                    RequestToInput.class, new ProcessId(processId), playerName)) {
              if (r.getId() == requestId) {
                r.setFinished(true);
                return true;
              }
            }
            return false;
          },
          (result) -> {
            return result == true;
          },
          20,
          500,
          TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      log.warn("requestId {} is not match", requestId);
      log.warn(
          "registered request ids = {}",
          playerMessageQueues
              .get(RequestToInput.class, new ProcessId(processId), playerName)
              .stream()
              .map(r -> r.getId())
              .collect(Collectors.toList()));
    }
  }

  /**
   * joinInGameを受けとった時の処理．
   *
   * @param processId
   * @param playerName
   */
  public void receiveJoinInGame(String processId, String playerName) {
    for (RequestToAssignOperator r : getAllAssignmentRequests(processId)) {
      if (r.getPlayerName().equals(playerName)) {
        r.setJoined(true);
      }
    }
  }

  public void receiveAssignment(String processId, String playerName, String operatorId) {
    for (RequestToAssignOperator r : getNewAssignmentRequests(processId)) {
      if (r.getPlayerName().equals(playerName)) {
        r.setOperatorId(operatorId);
        r.setAssigned(true);
      }
    }
  }

  @Override
  public Serializable requestToRegisterGameSession(GameSessionJson json) {
    gameMessageQueues.offer(
        new ProcessId(json.getProcessId()),
        new RequestToRegisterGameSession(
            json.getProcessId(),
            json.getBrokerUrl(),
            json.getUserId(),
            json.getSessionName(),
            json.getDescription(),
            json.getBootstrapScript()));
    return null;
  }

  @Override
  public Serializable notifyStartOfGame(String processId) {
    gameMessageQueues.offer(new ProcessId(processId), new StartOfGameEvent());
    return null;
  }

  @Override
  public Serializable notifyEndOfGame(String processId) {
    gameMessageQueues.offer(new ProcessId(processId), new NotifyEndOfGame());
    return null;
  }

  @Override
  public Serializable notifyStartOfRound(String processId, int roundnum) {
    gameMessageQueues.offer(new ProcessId(processId), new StartOfRoundEvent(roundnum));
    return null;
  }

  @Override
  public Serializable notifyEndOfRound(String processId, int roundnum) {
    gameMessageQueues.offer(new ProcessId(processId), new NotifyEndOfRound(roundnum));
    return null;
  }

  @Override
  public Serializable notifyGameRecord(String processId, int roundnum, String context) {
    gameMessageQueues.offer(new ProcessId(processId), new NotifyGameRecord(roundnum, context));
    return null;
  }

  public Serializable notifyStartOfGameProcess(String processId) {
    gameMessageQueues.offer(
        new ProcessId(processId), new NotifyStartOfGameProcess(new ProcessId(processId)));
    return null;
  }

  @Override
  public Serializable notifyJoinInGame(String processId, String playerName) {
    gameMessageQueues.offer(new ProcessId(processId), new NotifyJoinInGame(playerName));
    return null;
  }
}
