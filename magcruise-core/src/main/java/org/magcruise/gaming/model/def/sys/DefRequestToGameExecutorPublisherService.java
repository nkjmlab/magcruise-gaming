package org.magcruise.gaming.model.def.sys;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.executor.api.GameInteractionService;
import org.magcruise.gaming.lang.SConstructor;
import org.nkjmlab.util.java.net.UrlUtils;

@SuppressWarnings("serial")
public class DefRequestToGameExecutorPublisherService implements DefGameSystemProperty {

  protected static Logger log = LogManager.getLogger();

  private URL brokerUrl;
  private boolean useWebsocket;

  public DefRequestToGameExecutorPublisherService(String url, boolean useWebsocket) {
    try {
      this.brokerUrl = new URI(url).toURL();
      this.useWebsocket = useWebsocket;
    } catch (MalformedURLException | URISyntaxException e) {
      log.error(e, e);
    }
  }

  public DefRequestToGameExecutorPublisherService(String url) {
    this(url, false);
  }

  @Override
  public SConstructor<? extends DefRequestToGameExecutorPublisherService> toConstructor(
      ToExpressionStyle style) {
    return new SConstructor<>(getIndent(style)
        + SConstructor.toConstructor(style, getClass(), brokerUrl.toString(), useWebsocket));
  }

  public URL getBrokerUrl() {
    URL serviceUrl =
        useWebsocket ? getWsUrl() : UrlUtils.of(brokerUrl + GameInteractionService.DEFAULT_PATH);
    return serviceUrl;
  }

  private URL getWsUrl() {

    try {
      return URI.create(brokerUrl.getProtocol() + "://" + brokerUrl.getHost()
          + (brokerUrl.getPort() == -1 ? "" : ":" + brokerUrl.getPort())
          + "/app/websocket/requestToGameExecutor").toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean getUseWebsocket() {
    return useWebsocket;
  }

}
