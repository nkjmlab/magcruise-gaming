package org.magcruise.gaming.model.def.sys;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class DefUrl implements DefGameSystemProperty {
  protected static Logger log = LogManager.getLogger();

  private URL url;

  public DefUrl(String url) {
    try {
      this.url = new URI(url).toURL();
    } catch (MalformedURLException | URISyntaxException e) {
      log.error(e, e);
    }
  }

  public DefUrl(URL url) {
    this.url = url;
  }

  @Override
  public SConstructor<? extends DefUrl> toConstructor(ToExpressionStyle style) {
    return SConstructor.toConstructor(style, getClass(), url.toString());
  }

  public URL getUrl() {
    return url;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
