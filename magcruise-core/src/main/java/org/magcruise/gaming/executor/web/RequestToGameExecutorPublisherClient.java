package org.magcruise.gaming.executor.web;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.executor.api.RequestToGameExecutorPublisher;
import org.magcruise.gaming.executor.api.message.RequestToGameExecutorTransferObject;
import org.nkjmlab.util.jackson.JacksonMapper;
import org.nkjmlab.util.java.concurrent.RetryUtils;
import org.nkjmlab.util.jsonrpc.JsonRpcClientFactory;

public class RequestToGameExecutorPublisherClient implements RequestToGameExecutorPublisher {
  protected static Logger log = LogManager.getLogger();

  private URL url;
  private RequestToGameExecutorPublisher client;

  public RequestToGameExecutorPublisherClient(URL url) {
    this(url.toString());
  }

  public RequestToGameExecutorPublisherClient(String url) {
    try {
      this.url = new URI(url).toURL();
      this.client = JsonRpcClientFactory.create(JacksonMapper.getIgnoreUnknownPropertiesMapper(),
          RequestToGameExecutorPublisher.class, this.url);
    } catch (MalformedURLException | URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public RequestToGameExecutorTransferObject[] getNewRequestsToGameExecutor(String processId) {
    return RetryUtils.retry(() -> {
      RequestToGameExecutorTransferObject[] result = client.getNewRequestsToGameExecutor(processId);
      return result;
    }, 10, 500, TimeUnit.MILLISECONDS);
  }

}
