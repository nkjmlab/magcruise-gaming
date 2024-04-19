package org.magcruise.gaming.ui.web;

import java.io.Serializable;
import java.net.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.executor.RequestToShowImage;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.ui.api.GameSessionJson;
import org.magcruise.gaming.ui.api.RequesterToUI;
import org.magcruise.gaming.ui.api.message.NotifyEndOfGame;
import org.magcruise.gaming.ui.api.message.NotifyEndOfRound;
import org.magcruise.gaming.ui.api.message.NotifyGameRecord;
import org.magcruise.gaming.ui.api.message.NotifyJoinInGame;
import org.magcruise.gaming.ui.api.message.NotifyStartOfGame;
import org.magcruise.gaming.ui.api.message.NotifyStartOfRound;
import org.magcruise.gaming.ui.api.message.RequestToAssignOperator;
import org.magcruise.gaming.ui.api.message.RequestToInput;
import org.magcruise.gaming.ui.api.message.RequestToRegisterGameSession;
import org.magcruise.gaming.ui.api.message.RequestToShowMessage;
import org.magcruise.gaming.ui.api.message.RequestToUI;

@SuppressWarnings("serial")
public class RequesterToWebUI implements RequesterToUI {

  protected static Logger log = LogManager.getLogger();

  protected URL uiServiceUrl;
  protected RequestToUIRegistryClient client;

  RequesterToWebUI(URL uiServiceUrl) {
    this.uiServiceUrl = uiServiceUrl;
    this.client = new RequestToUIRegistryClient(this.uiServiceUrl);
  }

  @Override
  public String toString() {
    return super.toString() + ", " + uiServiceUrl == null ? "" : uiServiceUrl.toString();
  }

  @Override
  public SConstructor<? extends RequesterToUI> toConstructor(ToExpressionStyle style) {
    return SConstructor.toConstructor(style, getClass(), uiServiceUrl);
  }

  @Override
  public void initialize() {}

  @Override
  public Serializable request(ProcessId processId, RequestToUI request) {
    log.info("{} is sent to {}", request, uiServiceUrl);
    try {
      if (request instanceof RequestToRegisterGameSession) {
        RequestToRegisterGameSession req = (RequestToRegisterGameSession) request;
        // TODO rootBrokerの処理をあきらめて，brokerにしている．
        GameSessionJson j = new GameSessionJson(processId.toString(), req.getOperatorId(),
            req.getSessionName(), req.getDescription(), req.getBrokerUrl(), req.getBrokerUrl(),
            req.getBootstrapScript());
        log.info("[new] GameSessionJson is created. {}", j);
        client.requestToRegisterGameSession(j);
      } else if (request instanceof RequestToAssignOperator) {
        RequestToAssignOperator req = (RequestToAssignOperator) request;
        client.requestToAssign(processId.toString(), req.getPlayerName(), req.getOperatorId());
      } else if (request instanceof RequestToInput) {
        RequestToInput req = (RequestToInput) request;
        if (req.isAutoInput()) {
          client.requestToInputAndAutoResponse(processId.toString(), req.getPlayerId(), req.getId(),
              req.getLabel(), req.getRoundnum(), req.getInputs(), req.getMaxAutoResponseTime());
        } else {
          client.requestToInput(processId.toString(), req.getPlayerId(), req.getId(),
              req.getLabel(), req.getRoundnum(), req.getInputs());
        }
      } else if (request instanceof RequestToShowMessage) {
        RequestToShowMessage req = (RequestToShowMessage) request;
        client.showMessage(processId.toString(), req.getPlayerName(), req.getId(),
            req.getRoundnum(), req.getMessage());
      } else if (request instanceof RequestToShowImage) {
        RequestToShowImage req = (RequestToShowImage) request;
        client.showMessage(processId.toString(), req.getPlayerName(), req.getId(),
            req.getRoundnum(), req.getMessage());
      } else if (request instanceof NotifyStartOfGame) {
        client.notifyStartOfGame(processId.toString());
      } else if (request instanceof NotifyStartOfRound) {
        client.notifyStartOfRound(processId.toString(),
            ((NotifyStartOfRound) request).getRoundnum());
      } else if (request instanceof NotifyEndOfRound) {
        client.notifyEndOfRound(processId.toString(), ((NotifyEndOfRound) request).getRoundnum());
      } else if (request instanceof NotifyJoinInGame) {
        client.notifyJoinInGame(processId.toString(), ((NotifyJoinInGame) request).getPlayerName());
      } else if (request instanceof NotifyGameRecord) {
        client.notifyGameRecord(processId.toString(), ((NotifyGameRecord) request).getRoundnum(),
            ((NotifyGameRecord) request).getContext());
      } else if (request instanceof NotifyEndOfGame) {
        client.notifyEndOfGame(processId.toString());
      } else {
        log.warn("Pass through request... {}", request);
      }
    } catch (Exception e) {
      log.error("{},{},{},{},{}", processId, request, uiServiceUrl);
      throw e;
    }
    return null;
  }

  @Override
  public void requestToRegisterGameSession(ProcessId processId) {}

}
