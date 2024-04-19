package org.magcruise.gaming.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.magcruise.gaming.executor.api.message.ShutdownRequest;
import org.magcruise.gaming.executor.api.message.UnexpectedShutdownRequest;
import org.magcruise.gaming.lang.SExpression.ToExpressionStyle;
import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.model.def.scenario.GameScenario;
import org.magcruise.gaming.model.def.scenario.stage.DefStage;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.model.game.message.GameEvent;
import org.magcruise.gaming.model.task.EventDrivenTasks;
import org.magcruise.gaming.model.task.StageTask;
import org.magcruise.gaming.ui.api.message.NotifyEndOfGame;
import org.magcruise.gaming.ui.api.message.NotifyEndOfRound;
import org.magcruise.gaming.ui.api.message.NotifyGameRecord;
import org.magcruise.gaming.ui.api.message.NotifyStartOfGame;
import org.magcruise.gaming.ui.api.message.NotifyStartOfRound;
import org.nkjmlab.util.java.concurrent.BasicThreadFactory;

public class GameScenarioInterpreter implements Callable<Object> {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  private GameScenario scenario;

  private Context context;

  private EventDrivenTasks eventDrivenTasks = new EventDrivenTasks();

  private GameRecorder records;

  private GameMessenger messenger;

  GameScenarioInterpreter(GameScenario scenario, Context context, GameRecorder records) {
    this.scenario = scenario;
    this.context = context;
    this.records = records;
  }

  @Override
  public Object call() throws Exception {
    try {
      this.startGame();
      return null;
    } catch (Exception e) {
      log.error("Shutdown becauce " + e.getMessage(), e);
      messenger.sendMessage(new UnexpectedShutdownRequest());
      throw e;
    }
  }

  boolean hasRound() {
    return scenario.hasRound(getRoundnum());
  }

  void startGame() throws Exception {
    log.info("START OF GAME (roundnum={})", getRoundnum());
    messenger.sendMessage(new NotifyStartOfGame());
    log.debug("proc def:before-start-first-round");
    SchemeEnvironment.applyProcedureIfDefined(context.getEnvironmentName(),
        "def:before-start-first-round", context);
    startRound();
  }

  private ExecutorService eventDrivenTaskExecutorService =
      Executors.newCachedThreadPool(BasicThreadFactory.builder("proc-event").build());

  void notifyGameEvent(GameEvent event) {
    eventDrivenTasks.procEvent(eventDrivenTaskExecutorService, event);
  }

  private int getRoundnum() {
    return context.getRoundnum();
  }

  private void startRound() throws Exception {
    log.debug("START OF Round (roundnum=" + getRoundnum() + ")");
    if (!hasRound() || overFinalRound()) {
      eventDrivenTaskExecutorService.shutdown();
      messenger.sendMessage(new NotifyEndOfGame());
      context.setFinished(true);
      messenger.sendMessage(new ShutdownRequest());
      return;
    }

    log.info("START OF Round (roundnum={})", getRoundnum());
    messenger.sendMessage(new NotifyStartOfRound(getRoundnum()));
    execStages(scenario.getStages(getRoundnum()));

    log.info("START OF finalizeRound (roundnum=" + getRoundnum() + ")");
    context.finalizeRound(context.getRoundnum());
    records.finalizeRound(context);
    messenger.sendMessage(new NotifyGameRecord(context.getRoundnum(),
        context.toSExpressionForRevert(ToExpressionStyle.DEFAULT)));
    messenger.sendMessage(new NotifyEndOfRound(context.getRoundnum()));
    log.info("END OF finalizeRound (roundnum=" + getRoundnum() + ").");
    log.info("START OF roundValidation (roundnum=" + getRoundnum() + ")");
    context.roundValidation();
    log.info("END OF roundValidation (roundnum=" + getRoundnum() + ").");
    log.info("END OF Round (roundnum=" + getRoundnum() + ") Roundnum is incremented immediately.");
    context.incrementRoundnum();
    startRound();
  }

  private boolean overFinalRound() {
    return scenario.getFinalRound() < context.getRoundnum();
  }

  private void execStages(List<DefStage> stages) throws Exception {
    if (stages.size() == 0) {
      log.debug("END OF STAGES. ");
      return;
    } else if (stages.size() == 1) {
      execStage(stages.get(0));
      execStages(new ArrayList<DefStage>());
      return;
    }
    execStage(stages.get(0));
    execStages(stages.subList(1, stages.size()));
  }

  private void execStage(DefStage stage) throws Exception {
    log.debug("START OF execStage (roundnum={}, stage={}", context.getRoundnum(), stage.getName());
    CountDownLatch latch = new CountDownLatch(1);
    new StageTask(messenger, latch, eventDrivenTasks, context, stage).call();
    latch.await();
    log.debug("END OF execStage (roundnum={}, stage={}", context.getRoundnum(), stage.getName());
  }

  public void setMessenger(GameMessenger messenger) {
    this.messenger = messenger;
  }

}
