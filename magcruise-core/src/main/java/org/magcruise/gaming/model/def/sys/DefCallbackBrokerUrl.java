package org.magcruise.gaming.model.def.sys;

import java.net.URL;
import org.magcruise.gaming.executor.api.GameInteractionService;
import org.magcruise.gaming.executor.api.GameProcessService;
import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class DefCallbackBrokerUrl extends DefUrl implements DefUI {
  protected static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  public DefCallbackBrokerUrl(String brokerUrl) {
    super(brokerUrl.replaceAll(GameProcessService.DEFAULT_PATH, "")
        .replaceAll(GameInteractionService.DEFAULT_PATH, ""));
  }

  public DefCallbackBrokerUrl(URL url) {
    this(url.toString());
    log.info(url);
  }

  @Override
  public SConstructor<? extends DefCallbackBrokerUrl> toConstructor(ToExpressionStyle style) {
    return SConstructor.toConstructor(style, getClass(), getUrl().toString());
  }

}
