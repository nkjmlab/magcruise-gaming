package org.magcruise.broker.jsonrpc;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.magcruise.gaming.executor.api.GameInteractionService;
import org.magcruise.gaming.executor.api.message.RequestToGameExecutor;
import org.magcruise.gaming.executor.api.message.RequestToGameExecutorTransferObject;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.ui.api.GameSessionJson;
import org.magcruise.gaming.ui.api.message.GameProgress;
import org.magcruise.gaming.ui.api.message.PlayersState;
import org.magcruise.gaming.ui.api.message.RequestToAssignOperator;
import org.magcruise.gaming.ui.api.message.RequestToInput;
import org.magcruise.gaming.ui.api.message.RequestToRegisterGameSession;
import org.magcruise.gaming.ui.api.message.RequestToShowMessage;
import org.magcruise.gaming.ui.model.input.InputFromWebUI;
import org.magcruise.gaming.ui.model.input.InputToUI;
import org.magcruise.gaming.ui.model.message.MessageToUI;
import org.nkjmlab.util.java.concurrent.BasicThreadFactory;

public class GameInteractionController implements GameInteractionService {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();
  protected static RequestToUIController uiController = new RequestToUIController();
  protected static RequestToGameExecutorController executorController =
      new RequestToGameExecutorController();

  private static final ScheduledExecutorService autoInputService =
      Executors.newSingleThreadScheduledExecutor(
          BasicThreadFactory.builder("auto-input", false).build());

  @Override
  public void sendAssignment(String processId, String playerName, String operatorId) {
    uiController.receiveAssignment(processId, playerName, operatorId);
  }

  @Override
  public void joinInGame(String processId, String playerName) {
    log.info("{},{}", processId, playerName);
    uiController.receiveJoinInGame(processId, playerName);
    executorController.joinInGame(processId, playerName);
  }

  private static ExecutorService sendInputLazyExexcutor = Executors.newFixedThreadPool(6);

  @Override
  public void sendInput(
      String processId, String playerName, long requestId, int roundnum, InputFromWebUI[] inputs) {
    sendInputLazyExexcutor.execute(
        () -> {
          uiController.receiveInput(processId, requestId, playerName);
          executorController.sendInput(processId, playerName, requestId, roundnum, inputs);
        });
  }

  @Override
  public void sendAutoInput(
      String processId,
      String playerName,
      long requestId,
      int roundnum,
      InputFromWebUI[] inputs,
      int maxResponseTime) {
    if (maxResponseTime == 0) {
      autoInputService.submit(
          () -> {
            uiController.receiveInput(processId, requestId, playerName);
            executorController.sendAutoInput(
                processId, playerName, requestId, roundnum, inputs, maxResponseTime);
          });
    } else {
      autoInputService.schedule(
          () -> {
            uiController.receiveInput(processId, requestId, playerName);
            executorController.sendAutoInput(
                processId, playerName, requestId, roundnum, inputs, maxResponseTime);
          },
          ThreadLocalRandom.current().nextInt(maxResponseTime),
          TimeUnit.SECONDS);
    }
  }

  @Override
  public void request(ProcessId processId, RequestToGameExecutor message) throws Throwable {
    executorController.request(processId, message);
  }

  @Override
  public RequestToShowMessage[] getMessages(String processId, String playerId) {
    return uiController.getMessages(processId, playerId);
  }

  @Override
  public RequestToInput[] getNewRequestsToInput(String processId, String playerName) {
    return uiController.getNewRequestsToInput(processId, playerName);
  }

  @Override
  public RequestToAssignOperator[] getNewAssignmentRequests(String processId) {
    return uiController.getNewAssignmentRequests(processId);
  }

  @Override
  public GameProgress[] getProgresses(String processId) {
    return uiController.getProgresses(processId);
  }

  @Override
  public Serializable requestToAssign(String processId, String playerName, String operatorId) {
    return uiController.requestToAssign(processId, playerName, operatorId);
  }

  @Override
  public Serializable requestToRegisterGameSession(GameSessionJson json) {
    return uiController.requestToRegisterGameSession(json);
  }

  @Override
  public Serializable requestToInput(
      String processId,
      String playerName,
      long requestId,
      String label,
      int roundnum,
      InputToUI[] inputs) {
    return uiController.requestToInput(processId, playerName, requestId, label, roundnum, inputs);
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
    requestToInput(processId, playerName, requestId, label, roundnum, inputs);
    sendAutoInput(
        processId,
        playerName,
        requestId,
        roundnum,
        InputToUI.toInputFromWebUIs(inputs),
        maxResponseTime);
    return null;
  }

  @Override
  public Serializable showMessage(
      String processId, String playerName, long messageId, int roundnum, MessageToUI message) {
    return uiController.showMessage(processId, playerName, messageId, roundnum, message);
  }

  @Override
  public Serializable notifyStartOfGame(String processId) {
    return uiController.notifyStartOfGame(processId);
  }

  @Override
  public Serializable notifyStartOfRound(String processId, int roundnum) {
    return uiController.notifyStartOfRound(processId, roundnum);
  }

  @Override
  public Serializable notifyEndOfRound(String processId, int roundnum) {
    return uiController.notifyEndOfRound(processId, roundnum);
  }

  @Override
  public Serializable notifyGameRecord(String processId, int roundnum, String record) {
    return uiController.notifyGameRecord(processId, roundnum, record);
  }

  @Override
  public Serializable notifyEndOfGame(String processId) {
    return uiController.notifyEndOfGame(processId);
  }

  @Override
  public Serializable notifyJoinInGame(String processId, String playerName) {
    return uiController.notifyJoinInGame(processId, playerName);
  }

  @Override
  public RequestToGameExecutorTransferObject[] getNewRequestsToGameExecutor(String processId) {
    RequestToGameExecutorTransferObject[] result =
        executorController.getNewRequestsToGameExecutor(processId);
    return result;
  }

  @Override
  public PlayersState getPlayersState(String processId) {
    return uiController.getPlayersState(processId);
  }

  @Override
  public RequestToRegisterGameSession[] getRegisteredGameSessions() {
    return uiController.getRegisteredGameSessions();
  }
}
