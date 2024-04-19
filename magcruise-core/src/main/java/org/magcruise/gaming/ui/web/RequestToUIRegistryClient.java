package org.magcruise.gaming.ui.web;

import java.io.Serializable;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.magcruise.gaming.ui.api.GameSessionJson;
import org.magcruise.gaming.ui.api.RequestToUIRegistry;
import org.magcruise.gaming.ui.model.input.InputToUI;
import org.magcruise.gaming.ui.model.message.MessageToUI;
import org.nkjmlab.util.jackson.JacksonMapper;
import org.nkjmlab.util.java.concurrent.RetryUtils;
import org.nkjmlab.util.jsonrpc.JsonRpcClientFactory;

public class RequestToUIRegistryClient implements RequestToUIRegistry {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  private RequestToUIRegistry client;

  private URL url;

  public RequestToUIRegistryClient(URL url) {
    this.url = url;
    this.client = JsonRpcClientFactory.create(JacksonMapper.getIgnoreUnknownPropertiesMapper(),
        RequestToUIRegistry.class, url);
    if (client == null) {
      throw new RuntimeException(url + " is not valid.");
    }
    log.debug("{} is set. it requests to {}", getClass().getName(), url);
  }

  @Override
  public Serializable requestToAssign(String processId, String playerName, String operatorId) {
    try {
      return RetryUtils.retry(() -> {
        log.debug("{},{},{}, to {}", processId, playerName, operatorId, url);
        Serializable response = client.requestToAssign(processId, playerName, operatorId);
        if (response != null && response.toString().contains("error")) {
          log.warn(response);
        }
        return response;
      }, 10, 2, TimeUnit.SECONDS);
    } catch (Exception e) {
      log.warn("Command faild. Retry. {}, {}, {}, to {}", processId, playerName, operatorId, url);
      throw e;
    }
  }

  @Override
  public Serializable notifyStartOfGame(String processId) {
    return RetryUtils.retry(() -> {
      Serializable response = client.notifyStartOfGame(processId);
      if (response != null && response.toString().contains("error")) {
        log.warn(response);
      }
      return response;
    }, 10, 2, TimeUnit.SECONDS);

  }

  @Override
  public Serializable notifyStartOfRound(String processId, int roundnum) {
    return RetryUtils.retry(() -> {
      Serializable response = client.notifyStartOfRound(processId, roundnum);
      if (response != null && response.toString().contains("error")) {
        log.warn(response);
      }
      return response;
    }, 10, 2, TimeUnit.SECONDS);

  }

  @Override
  public Serializable requestToInput(String processId, String recipient, long requestId,
      String message, int roundnum, InputToUI[] inputsToWebUI) {
    return RetryUtils.retry(() -> {
      log.debug("requestId={}, toURL={}", requestId, url);
      Serializable response =
          client.requestToInput(processId, recipient, requestId, message, roundnum, inputsToWebUI);
      if (response != null && response.toString().contains("error")) {
        log.warn(response);
      }
      return response;
    }, 10, 2, TimeUnit.SECONDS);
  }

  @Override
  public Serializable requestToInputAndAutoResponse(String processId, String playerName,
      long requestId, String label, int roundnum, InputToUI[] inputs, int maxAutoResponseTime) {
    try {
      return RetryUtils.retry(() -> {
        Serializable response = client.requestToInputAndAutoResponse(processId, playerName,
            requestId, label, roundnum, inputs, maxAutoResponseTime);
        if (response != null && response.toString().contains("error")) {
          throw new RuntimeException(response.toString());
        }
        return response;
      }, 10, 2, TimeUnit.SECONDS);
    } catch (Exception e) {
      log.error("fail to request. requestId={}, toURL={}", requestId, url);
      throw e;
    }
  }

  @Override
  public Serializable showMessage(String processId, String playerId, long messageId, int round,
      MessageToUI message) {
    try {
      return RetryUtils.retry(() -> {
        Serializable response = client.showMessage(processId, playerId, messageId, round, message);
        if (response != null && response.toString().contains("error")) {
          log.warn(response);
        }
        return response;
      }, 10, 2, TimeUnit.SECONDS);
    } catch (Exception e) {
      log.error(
          "fail to request. processId={}, playerId={}, messageId={}, roundnum={}, message={}, toURL={}",
          processId, playerId, messageId, round, message, url);
      throw e;
    }
  }

  @Override
  public Serializable notifyEndOfRound(String processId, int roundnum) {
    return RetryUtils.retry(() -> {
      Serializable response = client.notifyEndOfRound(processId, roundnum);
      if (response != null && response.toString().contains("error")) {
        log.warn(response);
      }
      return response;
    }, 10, 2, TimeUnit.SECONDS);

  }

  @Override
  public Serializable notifyGameRecord(String processId, int roundnum, String state) {
    return RetryUtils.retry(() -> {
      Serializable response = client.notifyGameRecord(processId, roundnum, state);
      if (response != null && response.toString().contains("error")) {
        log.warn(response);
      }
      return response;
    }, 10, 2, TimeUnit.SECONDS);
  }

  @Override
  public Serializable notifyEndOfGame(String processId) {
    Serializable response = client.notifyEndOfGame(processId);
    if (response != null && response.toString().contains("error")) {
      log.warn(response);
    }
    return response;
  }

  @Override
  public Serializable notifyJoinInGame(String processId, String playerName) {
    return RetryUtils.retry(() -> {
      Serializable response = client.notifyJoinInGame(processId, playerName);
      if (response != null && response.toString().contains("error")) {
        log.warn(response);
      }
      return response;
    }, 10, 2, TimeUnit.SECONDS);
  }

  @Override
  public Serializable requestToRegisterGameSession(GameSessionJson json) {
    return RetryUtils.retry(() -> {
      log.info("{} is requested to register to {}", json, url);
      return client.requestToRegisterGameSession(json);
    }, 10, 2, TimeUnit.SECONDS);
  }

}
