package org.magcruise.broker.websocket;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.logging.log4j.LogManager;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.magcruise.broker.jsonrpc.RequestToUIController;
import org.magcruise.gaming.executor.api.RequestToUIService;
import org.magcruise.gaming.ui.api.message.RequestToUI;
import org.nkjmlab.util.jackson.JacksonMapper;
import org.nkjmlab.util.java.json.JsonMapper;

public class RequestToUIManager {
  private static final org.apache.logging.log4j.Logger log = LogManager.getLogger();

  protected static Map<String, ScheduledFuture<?>> workers = new ConcurrentHashMap<>();
  protected static ScheduledExecutorService pool = Executors.newScheduledThreadPool(20);
  protected static RequestToUIService uiService = new RequestToUIController();
  protected static Map<String, Map<String, Session>> sessions = new ConcurrentHashMap<>();

  private static final JsonMapper mapper = JacksonMapper.getDefaultMapper();

  private static String getPlayerId(Session session) {
    return WebsocketSessionUtils.extractParameterValue(session, "playerName").orElseThrow();
  }

  private static String getProcessId(Session session) {
    return WebsocketSessionUtils.extractParameterValue(session, "processId").orElseThrow();
  }

  public void onConnect(Session session) {
    String processId = getProcessId(session);
    String playerName = getPlayerId(session);
    log.info("OPEN WEBSOCKET={},{},{}", processId, playerName, session.hashCode());
    if (workers.get(String.valueOf(session.hashCode())) != null) {
      log.warn("session {} has been already registered.", session.hashCode());
      return;
    }

    sessions.putIfAbsent(processId, new ConcurrentHashMap<>());
    sessions.get(processId).put(playerName, session);

    RemoteEndpoint b = session.getRemote();

    AtomicLong lastMessageId = new AtomicLong(-1);
    AtomicLong lastRequestId = new AtomicLong(-1);
    ScheduledFuture<?> f =
        pool.scheduleWithFixedDelay(
            () -> {
              try {
                if (Thread.interrupted()) {
                  return;
                }
                b.sendPing(ByteBuffer.wrap(new byte[0]));

                List<RequestToUI> reqs = new ArrayList<>();

                {
                  long lastMessageIdTmp = lastMessageId.get();
                  for (RequestToUI e : uiService.getMessages(processId, playerName)) {
                    if (e.getId() <= lastMessageId.get()) {
                      continue;
                    }
                    reqs.add(e);
                    lastMessageIdTmp = e.getId();
                  }
                  lastMessageId.set(lastMessageIdTmp);
                }
                {
                  long lastRequestIdTmp = lastRequestId.get();
                  for (RequestToUI e : uiService.getNewRequestsToInput(processId, playerName)) {
                    if (e.getId() <= lastRequestId.get()) {
                      continue;
                    }
                    reqs.add(e);
                    lastRequestIdTmp = e.getId();
                  }
                  lastRequestId.set(lastRequestIdTmp);
                }
                if (reqs.size() > 0) {
                  synchronized (b) {
                    b.sendString(JacksonMapper.getDefaultMapper().toJson(reqs));
                  }
                }
              } catch (Exception e) {
                log.error("Target session is {}", session.hashCode());
                log.error(e, e);
              }
            },
            0,
            1,
            TimeUnit.SECONDS);

    workers.put(String.valueOf(session.hashCode()), f);
  }

  public void onMessage(String message, Session session) {
    String processId = getProcessId(session);
    String playerName = getPlayerId(session);
    if (sessions.get(processId) == null) {
      onConnect(session);
      return;
    }
    pool.submit(
        () -> {
          List<String> presses = new ArrayList<>();
          presses.add("keyup");
          presses.add(playerName);
          new ArrayList<>(sessions.get(processId).keySet())
              .forEach(
                  pName -> {
                    try {
                      Session s = sessions.get(processId).get(pName);
                      if (s == null || s.getRemote() == null || pName.equals(playerName)) {
                        return;
                      }
                      synchronized (s) {
                        s.getRemote().sendString(mapper.toJson(presses));
                      }
                    } catch (IllegalStateException e) {
                      log.warn(e.getMessage());
                      sessions.get(processId).remove(pName);
                    } catch (Exception e) {
                      log.warn(e, e);
                    }
                  });
        });
  }

  public void onClose(Session session, int statusCode, String reason) {
    String processId = getProcessId(session);
    String playerName = getPlayerId(session);
    Map<String, Session> s = sessions.get(processId);
    if (s != null) {
      s.remove(playerName);
    }
    synchronized (workers) {
      ScheduledFuture<?> f = workers.remove(String.valueOf(session.hashCode()));
      if (f != null) {
        f.cancel(true);
      }
    }
    session.close();
    log.info("Session {} is closed.", session.hashCode());
  }

  public void onError(Session session, Throwable cause) {
    log.warn(cause.getMessage());
  }
}
