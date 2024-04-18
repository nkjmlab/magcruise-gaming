package org.magcruise.broker.websocket;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jetty.websocket.api.Session;

public class WebsocketSessionUtils {

  public static List<String> extractParameterValues(Session session, String key) {
    List<String> params = session.getUpgradeRequest().getParameterMap().get(key);
    if (params == null) {
      return Collections.emptyList();
    }
    return params;
  }

  public static Optional<String> extractParameterValue(Session session, String key) {
    List<String> params = extractParameterValues(session, key);
    return params.size() == 0 ? Optional.empty() : Optional.of(params.get(0));
  }
}
