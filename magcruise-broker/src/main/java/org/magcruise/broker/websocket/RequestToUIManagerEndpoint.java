package org.magcruise.broker.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class RequestToUIManagerEndpoint {
  private org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger();

  private static RequestToUIManager manager = new RequestToUIManager();

  @OnWebSocketConnect
  public void onConnect(Session session) {
    try {
      manager.onConnect(session);
    } catch (Throwable e) {
      log.error(e, e);
    }
  }

  @OnWebSocketMessage
  public void onMessage(Session session, String message) {
    try {
      manager.onMessage(message, session);
    } catch (Throwable e) {
      log.error(e, e);
    }
  }

  @OnWebSocketClose
  public void onClose(Session session, int statusCode, String reason) {
    try {
      manager.onClose(session, statusCode, reason);
    } catch (Throwable e) {
      log.error(e, e);
    }
  }

  @OnWebSocketError
  public void onError(Session session, Throwable cause) {
    try {
      manager.onError(session, cause);
    } catch (Throwable e) {
      log.error(e, e);
    }
  }
}
