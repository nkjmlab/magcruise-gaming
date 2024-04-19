package org.magcruise.gaming.executor.web;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.executor.GameExecutorHelper;
import org.magcruise.gaming.executor.GameMessenger;
import org.magcruise.gaming.executor.api.message.RequestToGameExecutorTransferObject;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.manager.GameExecutorManager;
import org.magcruise.gaming.manager.process.ProcessId;
import org.nkjmlab.sorm4j.internal.util.Try;
import org.nkjmlab.util.jackson.JacksonMapper;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;

@ClientEndpoint
@SuppressWarnings("serial")
public class RequestToGameExecutorFetcherWithWebSocket implements GameExecutorHelper {
  protected static Logger log = LogManager.getLogger();

  private ProcessId processId;
  private URI uri;
  private Session session;

  public RequestToGameExecutorFetcherWithWebSocket(ProcessId processId,
      URL websocketEndPointBaseUrl) {
    this.processId = processId;
    this.uri = URI.create(websocketEndPointBaseUrl.toString().replaceAll("http://", "ws://")
        .replaceAll("https://", "wss://") + "?processId=" + processId.getValue());
  }

  @Override
  public void start() {
    try {
      session = ContainerProvider.getWebSocketContainer().connectToServer(this, uri);
      if (session.isOpen()) {
        log.info("session is open to {}", uri);
      }
    } catch (DeploymentException | IOException e) {
      log.error("uri is {}", uri);
      log.error(e, e);
      throw new RuntimeException(e);
    }
  }

  @OnOpen
  public void onOpen(Session session) {
    log.info("onOpen={}", session);
  }

  @OnMessage
  public void onMessage(String message) {
    try {
      log.debug("messsage alived =>[{}]", message);
      // RequestToGameExecutorTransferObject[] transferRequests = JSON.decode(message,
      // RequestToGameExecutorTransferObject[].class);
      RequestToGameExecutorTransferObject[] transferRequests =
          JacksonMapper.getIgnoreUnknownPropertiesMapper().toObject(message,
              RequestToGameExecutorTransferObject[].class);
      log.debug("transferedMessage =>[{}]", (Object) transferRequests);
      if (transferRequests == null || transferRequests.length == 0) {
        log.warn("request is empty.");
        return;
      }
      GameMessenger messenger =
          GameExecutorManager.getInstance().getGameExecutor(processId).getMessenger();
      Arrays.asList(transferRequests).forEach(request -> {
        if (request == null || request.getSConstructor() == null
            || request.getSConstructor().getExpression() == null) {
          log.error("request is invalid {}.", request);
          return;
        }
        messenger.sendMessage(request.makeInstance(processId.getValue()));
      });
    } catch (Exception e) {
      log.error(e, e);
      throw Try.rethrow(e);
    }
  }

  @OnError
  public void onError(Throwable th) {
    log.error("onError", th);
  }

  @OnClose
  public void onClose(Session session) {
    log.warn("onClose");
    try {
      session.close();
      this.session.close();
    } catch (IOException e) {
      log.error(e, e);
    }
  }

  @Override
  public SConstructor<?> toConstructor(ToExpressionStyle style) {
    try {
      return SConstructor.toConstructor(style, getClass(), processId, uri.toURL());
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Runnable> shutdownForceAfterAwating() {
    try {
      session.close();
    } catch (IOException e) {
      log.error(e, e);
    }
    return Collections.emptyList();
  }

}
