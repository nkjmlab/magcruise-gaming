package org.magcruise.gaming.model.def.sys;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.executor.api.GameInteractionService;
import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class DefUIService implements DefUI {

  protected static Logger log = LogManager.getLogger();

  protected URL url;

  public DefUIService(String uiService) {
    try {
      uiService = uiService.contains(GameInteractionService.DEFAULT_PATH)
          || uiService.contains("BackendAPIService") || uiService.contains("WebUiService")
              ? uiService
              : uiService + GameInteractionService.DEFAULT_PATH;
      this.url = new URI(uiService).toURL();
    } catch (MalformedURLException | URISyntaxException e) {
      log.error(e, e);
    }
  }

  @Override
  public SConstructor<? extends DefUIService> toConstructor(ToExpressionStyle style) {
    return new SConstructor<>(
        getIndent(style) + SConstructor.toConstructor(style, getClass(), url.toString()));
  }

  public URL getUrl() {
    return url;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
