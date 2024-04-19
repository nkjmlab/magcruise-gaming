package org.magcruise.gaming.executor;

import java.io.IOException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.executor.api.message.JoinInGame;
import org.magcruise.gaming.executor.web.RequestToGameExecutorFetcher;
import org.magcruise.gaming.executor.web.RequestToGameExecutorFetcherWithWebSocket;
import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.def.GameBuilder;
import org.magcruise.gaming.model.def.sys.GameSystemProperties;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.model.game.Game;
import org.magcruise.gaming.model.game.Player;
import org.magcruise.gaming.model.game.message.GameEvent;
import org.magcruise.gaming.ui.api.RequesterToUI;
import org.magcruise.gaming.ui.api.message.NotifyJoinInGame;
import org.magcruise.gaming.util.SystemEnvironmentUtils;
import org.nkjmlab.sorm4j.util.h2.server.H2TcpServerProcess;
import org.nkjmlab.sorm4j.util.h2.server.H2TcpServerProperties;
import org.nkjmlab.util.java.concurrent.BasicThreadFactory;
import org.nkjmlab.util.java.concurrent.ExecutorServiceUtils;
import org.nkjmlab.util.java.concurrent.ThreadUtils;
import org.nkjmlab.util.java.lang.RuntimeUtils;

/**
 *
 * @author nkjm
 *
 */
public class GameExecutor {

  static {
    new H2TcpServerProcess(H2TcpServerProperties.builder().build()).awaitStart();
  }


  private static Logger log = LogManager.getLogger();

  public Context context;

  private GameRecorder recorder;

  private GameMessenger messenger;

  private RequesterToUI requesters;

  private ProcessId processId;

  private GameExecutorHelpers gameExecutorHelpers;
  private GameScenarioInterpreter scenarioInterpreter;
  private ExecutorService scenarioInterpreterExecutorService;
  private ScheduledExecutorService activeThreadsCheckerExecutorService =
      Executors.newSingleThreadScheduledExecutor(
          BasicThreadFactory.builder("check-active-threads", false).build());

  public GameExecutor(GameBuilder builder) {
    log.info("Start of constructor of GameManager.");

    Game game = builder.build();
    log.debug("GameBuilder after build = {}", builder);

    this.processId = builder.getProcessId();
    this.context = game.getContext();

    String bootstrapScript = "";
    try {
      bootstrapScript = builder.getBootstrapBuilder().getBootstrapScriptPath() == null ? ""
          : String.join(System.lineSeparator(),
              Files.readAllLines(builder.getBootstrapBuilder().getBootstrapScriptPath().toNPath()));
    } catch (IOException | FileSystemNotFoundException e) {
      log.error(e, e);
    }

    this.requesters = this.context.getGameSystemProperties().createRequesters(bootstrapScript);

    // File dbPath = context.getGameSystemProperties().getDbFilePath().toFile();

    log.info("dbPath is ignored because it is osbolete");

    boolean useDb = true;
    this.recorder = new GameRecorder(SystemEnvironmentUtils.getDefaultDataSource(), useDb);

    scenarioInterpreter = new GameScenarioInterpreter(game.getScenario(), context, recorder);
    this.messenger = new GameMessenger(this, processId, context, recorder, requesters);
    context.setMessenger(messenger);
    context.getPlayers().forEach(player -> {
      player.setMessenger(messenger);
    });
    scenarioInterpreter.setMessenger(messenger);
    activeThreadsCheckerExecutorService.scheduleWithFixedDelay(() -> {
      log.info("Memory usege of this process is {}", RuntimeUtils.getMemoryUsege());
      log.info("Number of active threads in {} is {}, {}.", processId,
          ThreadUtils.getActiveThreadNames().size(), ThreadUtils.getActiveThreadNames());
    }, 0, 15, TimeUnit.MINUTES);

    log.debug("End of constructor of GameManager.");
    log.info("ProcessId is ={}", processId);

  }

  public Context getContext() {
    return context;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public GameMessenger getMessenger() {
    return messenger;
  }

  public void kickStartGame() {
    try {
      log.info("GameExecutor is kickstarted");
      beforeStart();

      if (isAllPlayersJoinInGame()) {
        startGameScenarioInterpreter();
      } else {
        requestToAssignOperator();
      }
    } catch (Exception e) {
      log.error(e, e);
    }
  }

  public void beforeStart() {
    requesters.initialize();
    gameExecutorHelpers = createGameExecutorHelpers();
    gameExecutorHelpers.start();
    log.info("[call] requestToRegisterGameSession with processId={}", processId);
    requesters.requestToRegisterGameSession(processId);
    context.getPlayers().forEach(player -> {
      if (player.isAgent()) {
        synchronized (joinedPlayers) {
          joinedPlayers.add(player.getName());
          messenger.sendMessage(new NotifyJoinInGame(player.getName().getName()));
        }
      }
    });

    log.debug("start of def:before-start-game");
    SchemeEnvironment.applyProcedureIfDefined(context.getEnvironmentName(), "def:before-start-game",
        context);
    log.debug("end of def:before-start-game");
  }

  private void requestToAssignOperator() {
    for (Player player : context.getPlayers()) {
      String oid = context.getAssignRequestOperatorId(player.getName());
      player.requestToAssignOperator(oid);
      log.info("{} is assigned to {}", oid, player.getName().toString());
    }
    // keepThreadUntilScenarioInterpreterIsActivated();
  }

  @SuppressWarnings("unused")
  private void keepThreadUntilScenarioInterpreterIsActivated() {
    while (!isAllPlayersJoinInGame()) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
      }
    }
  }

  private Set<ActorName> joinedPlayers = ConcurrentHashMap.newKeySet();

  void joinInGame(JoinInGame msg) {
    synchronized (joinedPlayers) {
      Player player = context.getPlayer(msg.getPlayerName());
      if (joinedPlayers.contains(msg.getPlayerName())) {
        player.rejoinInGame(context, msg);
        return;
      }
      joinedPlayers.add(msg.getPlayerName());
      messenger.sendMessage(new NotifyJoinInGame(player.getName().getName()));
      player.setOperatorId(msg.getOperatorId());
      player.joinInGame(context, msg);
      if (isAllPlayersJoinInGame()) {
        startGameScenarioInterpreter();
      }
    }
  }

  /**
   * ゲームの実行環境が整ったとき，ゲームシナリオインタプリタを処理するタスクを登録する．
   */
  private void startGameScenarioInterpreter() {
    scenarioInterpreterExecutorService =
        Executors.newSingleThreadExecutor(BasicThreadFactory.builder("scn-interp").build());
    scenarioInterpreterExecutorService.submit(scenarioInterpreter);
  }

  private boolean isAllPlayersJoinInGame() {
    return joinedPlayers.size() == context.getPlayers().size();
  }

  public ProcessId getProcessId() {
    return processId;
  }

  public GameExecutorHelpers createGameExecutorHelpers() {
    GameSystemProperties props = context.getGameSystemProperties();
    GameExecutorHelpers executorHelpers = new GameExecutorHelpers();
    if (props.getDefFetchRequestToGameExecutor() != null) {
      if (props.getDefFetchRequestToGameExecutor().getUseWebsocket()) {
        executorHelpers.add(new RequestToGameExecutorFetcherWithWebSocket(processId,
            props.getDefFetchRequestToGameExecutor().getBrokerUrl()));
      } else {
        executorHelpers.add(new RequestToGameExecutorFetcher(processId,
            props.getDefFetchRequestToGameExecutor().getBrokerUrl()));
      }
    }
    return executorHelpers;
  }

  public void shutdown() {
    log.info(RuntimeUtils.getMemoryUsege());
    context.setFinished(true);
    gameExecutorHelpers.shutdownForceAfterAwaiting();
    ExecutorServiceUtils.shutdownNowAfterAwaiting(scenarioInterpreterExecutorService, 3,
        TimeUnit.SECONDS);
    activeThreadsCheckerExecutorService.shutdown();
    recorder.shutdown();
    SystemEnvironmentUtils.createTempFile(processId, "exit_0");
    log.info("Finish shutdown");
  }

  public void unexpectedShutdown() {
    context.setUnexpectedShutdown(true);
    log.info(RuntimeUtils.getMemoryUsege());
    context.setFinished(true);
    gameExecutorHelpers.shutdownForceAfterAwaiting();
    ExecutorServiceUtils.shutdownNowAfterAwaiting(scenarioInterpreterExecutorService, 3,
        TimeUnit.SECONDS);
    activeThreadsCheckerExecutorService.shutdown();
    recorder.shutdown();
    SystemEnvironmentUtils.createTempFile(processId, "exit_1");
    log.warn("Finish unexpected shutdown");
  }

  public void notifyStartOfGame() {
    log.info(RuntimeUtils.getMemoryUsege());
  }

  public void notifyGameEvent(GameEvent event) {
    scenarioInterpreter.notifyGameEvent(event);
  }

}
