package org.magcruise.gaming.executor.web;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.nkjmlab.util.jackson.JacksonMapper;
import org.nkjmlab.util.jsonrpc.JsonRpcClientFactory;

public class GameInteractionServiceClient implements GameInteractionService {
  private static final Logger log = LogManager.getLogger();
  private URL url;
  private GameInteractionService client;

  public GameInteractionServiceClient(URL brokerUrl) {
    this(brokerUrl.toString());
  }

  public GameInteractionServiceClient(String brokerUrl) {
    try {
      this.url = new URI(brokerUrl + GameInteractionService.DEFAULT_PATH).toURL();
      this.client = JsonRpcClientFactory.create(JacksonMapper.getIgnoreUnknownPropertiesMapper(),
          GameInteractionService.class, this.url);
    } catch (MalformedURLException | URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public void joinInGame(String processId, String playerName) {
    client.joinInGame(processId, playerName);
  }

  @Override
  public void sendAssignment(String processId, String playerName, String operatorId) {
    client.sendAssignment(processId, playerName, operatorId);

  }

  @Override
  public void sendInput(String processId, String playerName, long requestId, int roundnum,
      InputFromWebUI[] inputs) {
    client.sendInput(processId, playerName, requestId, roundnum, inputs);
  }

  @Override
  public void sendAutoInput(String processId, String playerName, long requestId, int roundnum,
      InputFromWebUI[] inputs, int maxAutoResponseTime) {
    log.debug("Send to [{}]. [{},{},{},{},{},{}]", url, processId, playerName, requestId, roundnum,
        inputs, maxAutoResponseTime);
    client.sendAutoInput(processId, playerName, requestId, roundnum, inputs, maxAutoResponseTime);
  }

  @Override
  public void request(ProcessId processId, RequestToGameExecutor message) throws Throwable {
    client.request(processId, message);
  }

  @Override
  public RequestToGameExecutorTransferObject[] getNewRequestsToGameExecutor(String processId) {
    return client.getNewRequestsToGameExecutor(processId);
  }

  @Override
  public Serializable requestToRegisterGameSession(GameSessionJson json) {
    return client.requestToRegisterGameSession(json);
  }

  @Override
  public Serializable requestToAssign(String processId, String playerName, String operatorId) {
    return client.requestToAssign(processId, playerName, operatorId);
  }

  @Override
  public Serializable requestToInput(String processId, String playerName, long requestId,
      String label, int roundnum, InputToUI[] inputs) {
    return client.requestToInput(processId, playerName, requestId, label, roundnum, inputs);
  }

  @Override
  public Serializable requestToInputAndAutoResponse(String processId, String playerName,
      long requestId, String label, int roundnum, InputToUI[] inputs, int maxAutoResponseTime) {
    return client.requestToInputAndAutoResponse(processId, playerName, requestId, label, roundnum,
        inputs, maxAutoResponseTime);
  }

  @Override
  public Serializable showMessage(String processId, String playerId, long messageId, int roundnum,
      MessageToUI message) {
    return client.showMessage(processId, playerId, messageId, roundnum, message);
  }

  @Override
  public Serializable notifyStartOfGame(String processId) {
    return client.notifyStartOfGame(processId);
  }

  @Override
  public Serializable notifyStartOfRound(String processId, int roundnum) {
    return client.notifyStartOfRound(processId, roundnum);
  }

  @Override
  public Serializable notifyEndOfRound(String processId, int roundnum) {
    return client.notifyEndOfRound(processId, roundnum);
  }

  @Override
  public Serializable notifyGameRecord(String processId, int roundnum, String record) {
    return client.notifyGameRecord(processId, roundnum, record);
  }

  @Override
  public Serializable notifyEndOfGame(String processId) {
    return client.notifyEndOfGame(processId);
  }

  @Override
  public Serializable notifyJoinInGame(String processId, String playerName) {
    return notifyJoinInGame(processId, playerName);
  }

  @Override
  public RequestToRegisterGameSession[] getRegisteredGameSessions() {
    return client.getRegisteredGameSessions();
  }

  @Override
  public RequestToAssignOperator[] getNewAssignmentRequests(String processId) {
    return client.getNewAssignmentRequests(processId);
  }

  @Override
  public RequestToShowMessage[] getMessages(String processId, String playerName) {
    return client.getMessages(processId, playerName);
  }

  @Override
  public RequestToInput[] getNewRequestsToInput(String processId, String playerName) {
    return client.getNewRequestsToInput(processId, playerName);
  }

  @Override
  public GameProgress[] getProgresses(String processId) {
    return client.getProgresses(processId);
  }

  @Override
  public PlayersState getPlayersState(String processId) {
    return client.getPlayersState(processId);
  }

}
