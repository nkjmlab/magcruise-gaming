package org.magcruise.gaming.ui.web;

import java.net.URL;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.ui.api.message.RequestToRegisterGameSession;

@SuppressWarnings("serial")
public class RequesterToWebUIWithRegister extends RequesterToWebUI {

  private String operatorId;
  private URL brokerUrl;
  private String sessionName = "";
  private String description = "";
  private String bootstrapScript = "";

  public RequesterToWebUIWithRegister(URL url, String userId, URL brokerUrl, String sessionName,
      String description, String bootstrapScript) {
    super(url);
    this.operatorId = userId;
    this.brokerUrl = brokerUrl;
    this.sessionName = sessionName;
    this.description = description;
    this.bootstrapScript = bootstrapScript;
  }

  @Override
  public SConstructor<? extends RequesterToWebUI> toConstructor(ToExpressionStyle style) {
    return SConstructor.toConstructor(style, getClass(), uiServiceUrl, operatorId, brokerUrl,
        sessionName, description, bootstrapScript);
  }

  @Override
  public void requestToRegisterGameSession(ProcessId processId) {
    RequestToRegisterGameSession r =
        new RequestToRegisterGameSession(processId.getProcessId(), brokerUrl.toString(), operatorId,
            sessionName.length() == 0 ? processId.toString() : sessionName, description,
            bootstrapScript);
    log.info("[new] RequestToGameSession {}", r);
    request(processId, r);
  }

  public String getSessionName() {
    return sessionName;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return "RequesterToWebUIWithRegister [operatorId=" + operatorId + ", brokerUrl=" + brokerUrl
        + ", sessionName=" + sessionName + ", description=" + description + ", bootstrapScript="
        + bootstrapScript + ", uiServiceUrl=" + uiServiceUrl + "]";
  }

}
