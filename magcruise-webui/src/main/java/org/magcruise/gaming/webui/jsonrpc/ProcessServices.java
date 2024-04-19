package org.magcruise.gaming.webui.jsonrpc;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.eclipse.jetty.http.HttpStatus;
import org.magcruise.gaming.executor.web.GameProcessServiceClient;
import org.magcruise.gaming.manager.process.ProcessInfoJson;
import org.magcruise.gaming.util.SystemEnvironmentUtils;
import org.magcruise.gaming.webui.relation.GameSessionsTable;
import org.magcruise.gaming.webui.row.GameSession;
import org.nkjmlab.util.java.net.BasicHttpClient;

public class ProcessServices {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();
  private static final Map<String, GameProcessServiceClient> processServices =
      new ConcurrentHashMap<>();
  private static final List<String> keepBrokerUrls = Arrays.asList();

  // private static final List<String> keepBrokerUrls = Arrays.asList(
  // SystemEnvironmentUtils.getDefaultLocalBrokerUrl(),
  // SystemEnvironmentUtils.getDefaultPublicBrokerUrl());

  static {
    Arrays.asList(
            SystemEnvironmentUtils.getDefaultLocalBrokerUrl(),
            SystemEnvironmentUtils.getDefaultPublicBrokerUrl())
        .forEach(brokerUrl -> addBrokerUrl(brokerUrl));
  }

  public static List<String> getWatchingBrokerUrls() {
    return new ArrayList<>(processServices.keySet());
  }

  static void addBrokerUrl(String brokerUrl) {
    processServices.computeIfAbsent(brokerUrl, k -> new GameProcessServiceClient(brokerUrl));
  }

  public static GameProcessServiceClient get(String brokerUrl) {
    return processServices.get(brokerUrl);
  }

  public static List<GameSession> getActiveGameProcesses(GameSessionsTable gameSessionsTable) {
    processServices.values().parallelStream()
        .forEach(processService -> removeInactiveService(processService));
    List<GameSession> result =
        processServices.values().parallelStream()
            .flatMap(s -> getActiveGameProcesses(s, gameSessionsTable).stream())
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    return result;
  }

  private static void removeInactiveService(GameProcessServiceClient processService) {
    try {
      if (new BasicHttpClient(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build())
              .getResponse(processService.getProcessServiceUrl().toURI())
              .statusCode()
          != HttpStatus.OK_200) {
        log.warn("can not access to {}", processService.getProcessServiceUrl());
        removeProcessService(processService);
      }
    } catch (Throwable e) {
      log.warn("can not access to {}", processService.getProcessServiceUrl());
      removeProcessService(processService);
    }
  }

  public static void removeProcessService(GameProcessServiceClient processService) {
    log.warn("can not access to {}", processService.getBrokerUrl());
    if (!keepBrokerUrls.contains(processService.getBrokerUrl())) {
      processServices.remove(processService.getBrokerUrl());
    }
  }

  private static List<GameSession> getActiveGameProcesses(
      GameProcessServiceClient processService, GameSessionsTable gameSessionsTable) {
    try {
      ProcessInfoJson[] pInfo = processService.listActiveProcesses();
      if (pInfo == null) {
        return Collections.emptyList();
      }
      return Arrays.asList(pInfo).stream()
          .map(p -> gameSessionsTable.selectByPrimaryKey(p.getProcessId().toString()))
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    } catch (Throwable e) {
      log.warn(e, e);
      log.warn(
          "could not get information about active game processes at {}",
          processService.getBrokerUrl());
      removeProcessService(processService);
      return Collections.emptyList();
    }
  }

  public static Set<String> getActiveGameProcessId(GameSessionsTable gameSessionsTable) {
    return getActiveGameProcesses(gameSessionsTable).stream()
        .map(s -> s.getProcessId())
        .collect(Collectors.toSet());
  }
}
