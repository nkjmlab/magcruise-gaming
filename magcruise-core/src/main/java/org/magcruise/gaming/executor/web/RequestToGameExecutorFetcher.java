package org.magcruise.gaming.executor.web;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.executor.GameExecutorHelper;
import org.magcruise.gaming.executor.GameMessenger;
import org.magcruise.gaming.executor.api.message.RequestToGameExecutor;
import org.magcruise.gaming.executor.api.message.RequestToGameExecutorTransferObject;
import org.magcruise.gaming.executor.api.message.ShutdownRequest;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.manager.GameExecutorManager;
import org.magcruise.gaming.manager.process.ProcessId;
import org.nkjmlab.util.java.concurrent.BasicThreadFactory;
import org.nkjmlab.util.java.concurrent.ExecutorServiceUtils;

@SuppressWarnings("serial")
public class RequestToGameExecutorFetcher implements GameExecutorHelper, Runnable {

  protected static Logger log = LogManager.getLogger();

  private RequestToGameExecutorPublisherClient client;
  private ProcessId processId;
  private URL url;

  private ScheduledExecutorService executorService;

  public RequestToGameExecutorFetcher(ProcessId processId, URL url) {
    this.url = url;
    this.processId = processId;
    this.client = new RequestToGameExecutorPublisherClient(url);
    this.executorService = Executors.newSingleThreadScheduledExecutor(
        BasicThreadFactory.builder(getClass().getSimpleName(), false).build());
  }

  private List<RequestToGameExecutor> getRequests() {
    RequestToGameExecutorTransferObject[] transferedRequests =
        client.getNewRequestsToGameExecutor(processId.toString());
    List<RequestToGameExecutor> result = new ArrayList<>();
    if (transferedRequests == null || transferedRequests.length == 0) {
      return result;
    }
    log.info("Receive response from {} = {}", url, Arrays.asList(transferedRequests));
    for (RequestToGameExecutorTransferObject request : transferedRequests) {
      if (request == null || request.getSConstructor() == null
          || request.getSConstructor().getExpression() == null) {
        log.error("request is invalid {}.", request);
        continue;
      }
      result.add(request.makeInstance(processId.getValue()));
    }
    return result;
  }

  @Override
  public SConstructor<? extends RequestToGameExecutorFetcher> toConstructor(
      ToExpressionStyle style) {
    return SConstructor.toConstructor(style, getClass(), processId, url);
  }

  @Override
  public void run() {
    try {
      List<RequestToGameExecutor> requests = getRequests();
      if (requests.size() == 0) {
        return;
      }
      GameMessenger messenger =
          GameExecutorManager.getInstance().getGameExecutor(processId).getMessenger();
      requests.forEach(r -> messenger.sendMessage(r));
    } catch (Throwable e) {
      log.error("url={}", url);
      log.error(e, e);
      log.error("Send shutdown request");
      GameExecutorManager.getInstance().getGameExecutor(processId).getMessenger()
          .sendMessage(new ShutdownRequest());
    }
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public void start() {
    executorService.scheduleWithFixedDelay(this, 0, 1, TimeUnit.SECONDS);
  }

  @Override
  public List<Runnable> shutdownForceAfterAwating() {
    return ExecutorServiceUtils.shutdownNowAfterAwaiting(executorService, 10, TimeUnit.SECONDS);
  }
}
