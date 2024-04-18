package org.magcruise.gaming.ui.web;

import java.net.URL;
import org.magcruise.gaming.ui.api.RequestToUIPublisher;
import org.magcruise.gaming.ui.api.message.GameProgress;
import org.magcruise.gaming.ui.api.message.PlayersState;
import org.magcruise.gaming.ui.api.message.RequestToAssignOperator;
import org.magcruise.gaming.ui.api.message.RequestToInput;
import org.magcruise.gaming.ui.api.message.RequestToRegisterGameSession;
import org.magcruise.gaming.ui.api.message.RequestToShowMessage;
import org.nkjmlab.util.jackson.JacksonMapper;
import org.nkjmlab.util.jsonrpc.JsonRpcClientFactory;

public class RequestToUIPublisherClient implements RequestToUIPublisher {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  private RequestToUIPublisher client;

  private URL url;

  public RequestToUIPublisherClient(URL url) {
    this.url = url;
    this.client = JsonRpcClientFactory.create(JacksonMapper.getIgnoreUnknownPropertiesMapper(),
        RequestToUIPublisher.class, this.url);
    if (client == null) {
      throw new RuntimeException(url + " is not valid.");
    }
    log.debug("{} is set. it requests to {}", getClass().getName(), url);
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
    return getPlayersState(processId);
  }

}
