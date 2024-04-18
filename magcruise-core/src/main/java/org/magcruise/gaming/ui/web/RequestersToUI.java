package org.magcruise.gaming.ui.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.def.sys.DefCallbackBrokerUrl;
import org.magcruise.gaming.model.def.sys.DefNoGui;
import org.magcruise.gaming.model.def.sys.DefSwingGui;
import org.magcruise.gaming.model.def.sys.DefUI;
import org.magcruise.gaming.model.def.sys.DefUIService;
import org.magcruise.gaming.model.def.sys.DefUIServiceForRegisterSession;
import org.magcruise.gaming.ui.DefaultAutoInputer;
import org.magcruise.gaming.ui.api.RequesterToUI;
import org.magcruise.gaming.ui.api.message.RequestToInput;
import org.magcruise.gaming.ui.api.message.RequestToUI;
import org.magcruise.gaming.ui.swing.SwingGui;

@SuppressWarnings("serial")
public class RequestersToUI implements RequesterToUI {
  protected static org.apache.logging.log4j.Logger log = LogManager.getLogger();

  private List<RequesterToUI> requesters = new ArrayList<>();
  private List<DefUI> defUIs = new ArrayList<>();

  public RequestersToUI(List<DefUI> defUIs, String bootstrapScript) {
    for (DefUI def : defUIs) {
      if (def instanceof DefSwingGui && !containsNoGui(defUIs)) {
        requesters.add(new SwingGui());
      } else if (def instanceof DefUIServiceForRegisterSession) {
        DefUIServiceForRegisterSession defUiRegister = (DefUIServiceForRegisterSession) def;
        for (DefUI defUi : new ArrayList<>(defUIs)) {
          if (defUi instanceof DefCallbackBrokerUrl) {
            defUiRegister = new DefUIServiceForRegisterSession(defUiRegister.getUrl().toString(),
                defUiRegister.getUserId(), ((DefCallbackBrokerUrl) defUi).getUrl().toString(),
                defUiRegister.getSessionName(), defUiRegister.getDescription());
            log.info("[new] DefUIServiceForRegisterSession is created and set. {}", defUiRegister);
          }
        }

        RequesterToWebUIWithRegister register = new RequesterToWebUIWithRegister(
            defUiRegister.getUrl(), defUiRegister.getUserId(), defUiRegister.getBrokerUrl(),
            defUiRegister.getSessionName(), defUiRegister.getDescription(), bootstrapScript);
        log.info("[new] RequesterToWebUIWithRegister is created. {}", register);
        requesters.add(register);

      } else if (def instanceof DefUIService) {
        requesters.add(new RequesterToWebUI(((DefUIService) def).getUrl()));
      }
    }
    this.defUIs.addAll(defUIs);
  }

  private boolean containsNoGui(List<DefUI> defUIs) {
    for (DefUI def : defUIs) {
      if (def instanceof DefNoGui) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Serializable request(ProcessId processId, RequestToUI message) {
    if (requesters.size() == 0) {
      if (message instanceof RequestToInput) {
        DefaultAutoInputer.requestAutoInput(processId, (RequestToInput) message);
      }
      return null;
    }

    for (RequesterToUI r : requesters) {
      r.request(processId, message);
    }
    return null;
  }

  @Override
  public void initialize() {
    for (RequesterToUI r : requesters) {
      r.initialize();
    }
  }

  @Override
  public SConstructor<? extends RequesterToUI> toConstructor(ToExpressionStyle style) {
    return SConstructor.toConstructor(style, getClass(), defUIs);
  }

  @Override
  public void requestToRegisterGameSession(ProcessId processId) {
    for (RequesterToUI r : requesters) {
      r.requestToRegisterGameSession(processId);
    }
  }

  @Override
  public String toString() {
    return defUIs.toString();
  }

}
