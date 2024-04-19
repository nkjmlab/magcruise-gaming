package org.magcruise.gaming.model.def.sys;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class DefGameInteractionService implements DefGameSystemProperty {

  protected static Logger log = LogManager.getLogger();

  private URL url;

  public DefGameInteractionService(String url) {
    try {
      this.url = new URI(url).toURL();
    } catch (MalformedURLException | URISyntaxException e) {
      log.error(e, e);
    }
  }

  @Override
  public SConstructor<? extends DefGameInteractionService> toConstructor(ToExpressionStyle style) {
    return SConstructor.toConstructor(style, getClass(), url.toString());
  }

  public URL getUrl() {
    return url;
  }

}
