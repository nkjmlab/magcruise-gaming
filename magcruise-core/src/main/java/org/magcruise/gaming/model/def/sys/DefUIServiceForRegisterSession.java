package org.magcruise.gaming.model.def.sys;

import java.net.URL;
import org.magcruise.gaming.lang.SConstructor;
import org.nkjmlab.util.java.net.UrlUtils;

@SuppressWarnings("serial")
public class DefUIServiceForRegisterSession extends DefUIService {
  protected String userId;
  private URL brokerUrl;
  private String sessionName = "";
  private String description = "";

  public DefUIServiceForRegisterSession(String uiServiceUrl, String userId,
      String callbackBrokerUrl) {
    this(uiServiceUrl, userId, callbackBrokerUrl, "", "");
  }

  public DefUIServiceForRegisterSession(String uiServiceUrl, String userId,
      String callbackBrokerUrl, String sessionName, String description) {
    super(uiServiceUrl);
    this.userId = userId;
    this.brokerUrl = UrlUtils.of(callbackBrokerUrl);
    this.sessionName = sessionName;
    this.description = description;
  }

  @Override
  public SConstructor<? extends DefUIService> toConstructor(ToExpressionStyle style) {
    return new SConstructor<>(getIndent(style) + SConstructor.toConstructor(style, getClass(),
        url.toString(), userId, brokerUrl.toString(), sessionName, description));
  }

  public String getUserId() {
    return userId;
  }

  public String getSessionName() {
    return sessionName;
  }

  public String getDescription() {
    return description;
  }

  public URL getBrokerUrl() {
    return brokerUrl;
  }

  @Override
  public String toString() {
    return "DefUIServiceForRegisterSession [userId=" + userId + ", brokerUrl=" + brokerUrl
        + ", sessionName=" + sessionName + ", description=" + description + ", url=" + url + "]";
  }


}
