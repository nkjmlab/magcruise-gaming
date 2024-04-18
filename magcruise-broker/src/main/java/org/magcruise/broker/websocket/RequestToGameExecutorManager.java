package org.magcruise.broker.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.magcruise.broker.jsonrpc.RequestToGameExecutorController;
import org.magcruise.gaming.executor.api.RequestToGameExecutorService;
import org.magcruise.gaming.executor.api.message.RequestToGameExecutorTransferObject;
import org.nkjmlab.util.jackson.JacksonMapper;

public class RequestToGameExecutorManager {
  private org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger();

  protected static Map<String, ScheduledFuture<?>> workers = new ConcurrentHashMap<>();
  protected static ScheduledExecutorService pool = Executors.newScheduledThreadPool(20);
  protected static RequestToGameExecutorService gameExecutorService =
      new RequestToGameExecutorController();

  private static String getProcessId(Session session) {
    return WebsocketSessionUtils.extractParameterValue(session, "processId").orElseThrow();
  }

  public void onOpen(Session session) {

    String processId = getProcessId(session);

    if (workers.get(String.valueOf(session.hashCode())) != null) {
      log.warn("session {} has been already registered.", session.hashCode());
      return;
    }

    RemoteEndpoint b = session.getRemote();

    ScheduledFuture<?> f =
        pool.scheduleWithFixedDelay(
            () -> {
              try {
                if (Thread.interrupted()) {
                  return;
                }
                List<RequestToGameExecutorTransferObject> reqs = new ArrayList<>();
                for (RequestToGameExecutorTransferObject e :
                    gameExecutorService.getNewRequestsToGameExecutor(processId)) {
                  reqs.add(e);
                }
                if (reqs.size() > 0) {
                  b.sendString(JacksonMapper.getDefaultMapper().toJson(reqs));
                }
              } catch (Exception e) {
                log.error(e, e);
              }
            },
            0,
            1,
            TimeUnit.SECONDS);

    workers.put(String.valueOf(session.hashCode()), f);
  }

  public void onClose(Session session, int statusCode, String reason) {
    session.close();
    synchronized (workers) {
      ScheduledFuture<?> f = workers.remove(String.valueOf(session.hashCode()));
      if (f != null) {
        f.cancel(true);
      }
    }
  }

  public void onError(Session session, Throwable cause) {
    log.warn(cause.getMessage());
  }
}
