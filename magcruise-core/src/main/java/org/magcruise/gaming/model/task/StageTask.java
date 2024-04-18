package org.magcruise.gaming.model.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.executor.GameMessenger;
import org.magcruise.gaming.executor.api.message.UnexpectedShutdownRequest;
import org.magcruise.gaming.model.def.scenario.stage.DefParallelStage;
import org.magcruise.gaming.model.def.scenario.stage.DefRestage;
import org.magcruise.gaming.model.def.scenario.stage.DefSequentialStage;
import org.magcruise.gaming.model.def.scenario.stage.DefStage;
import org.magcruise.gaming.model.def.scenario.task.DefCall;
import org.magcruise.gaming.model.def.scenario.task.DefContextTask;
import org.magcruise.gaming.model.def.scenario.task.DefEventDrivenTask;
import org.magcruise.gaming.model.def.scenario.task.DefPlayerScenarioTask;
import org.magcruise.gaming.model.def.scenario.task.DefPlayerTask;
import org.magcruise.gaming.model.def.scenario.task.StageElement;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.model.game.message.GameEvent;
import org.nkjmlab.util.java.concurrent.BasicThreadFactory;
import org.nkjmlab.util.java.concurrent.ExecutorServiceUtils;
import org.nkjmlab.util.java.lang.reflect.ReflectionMethodInvoker;

public class StageTask implements Task {

  protected static Logger log = LogManager.getLogger();
  private EventDrivenTasks rootEventDrivenTasks;
  private EventDrivenTasks eventDrivenTasksInThisStage;

  private Context context;
  private DefStage stage;
  private CountDownLatch parentLatch;
  private GameMessenger messenger;
  protected volatile boolean finished = false;

  public StageTask(GameMessenger messenger, CountDownLatch latch,
      EventDrivenTasks rootEventDrivenTasks, Context context, DefStage stage) {
    this.messenger = messenger;
    this.parentLatch = latch;
    this.context = context;
    this.stage = stage;
    this.rootEventDrivenTasks = rootEventDrivenTasks;
    this.eventDrivenTasksInThisStage = new EventDrivenTasks();
  }

  private void execStageParlallelly() {
    ExecutorService executorService = Executors
        .newCachedThreadPool(BasicThreadFactory.builder("stage-" + stage.getName(), true).build());
    CountDownLatch latch = new CountDownLatch(stage.getActiveTasks(context).size());

    List<Future<?>> fs = new ArrayList<>();
    callFilters(stage.getBeforeCalls());

    for (StageElement se : stage.getActiveTasks(context)) {
      procStageElement(executorService, latch, se).ifPresent(f -> fs.add(f));
    }
    log.debug("Start await pararel stage={}, latch={}", stage.getName(), latch);
    awaitLatchAndGetFuture(latch, fs);
    ExecutorServiceUtils.shutdownAndAwaitTermination(executorService, 3, TimeUnit.SECONDS);

    callFilters(stage.getAfterCalls());
    parentLatch.countDown();
    log.debug("ParentLatch countdowned={}", parentLatch);

  }

  private static void awaitLatchAndGetFuture(CountDownLatch latch, List<Future<?>> fs) {
    while (!awaitResponseAux(latch, fs)) {
    }
  }

  private void callFilters(List<DefCall> filters) {
    filters.forEach(filter -> {
      log.info("START of filter: {}", filter);
      ReflectionMethodInvoker.invoke(context.getRegisteredObject(filter.getName()),
          filter.getMethod().toString(), context, stage);
      log.info("END of filter: {}", filter);
    });
  }

  private void execStageSequentially() {
    ExecutorService executorService = Executors.newSingleThreadExecutor(
        BasicThreadFactory.builder("stage-" + stage.getName(), true).build());
    callFilters(stage.getBeforeCalls());

    for (StageElement se : stage.getActiveTasks(context)) {
      CountDownLatch latch = new CountDownLatch(1);
      procStageElement(executorService, latch, se).ifPresent(f -> {
        try {
          while (!f.isDone()) {
            Thread.sleep(1);
          }
          f.get();
        } catch (InterruptedException e) {
          log.info(e, e);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
      try {
        log.debug("Start await task: {}, latch: {}", se, latch);
        while (!latch.await(1, TimeUnit.SECONDS)) {

        }
      } catch (InterruptedException e) {
        log.info(e, e);
      }
    }
    ExecutorServiceUtils.shutdownAndAwaitTermination(executorService, 3, TimeUnit.SECONDS);

    callFilters(stage.getAfterCalls());
    parentLatch.countDown();
    log.debug("round:{}, stage:{}, ParentLatch countdowned={}", context.getRoundnum(),
        stage.getName(), parentLatch);
  }

  public boolean isFinishied() {
    return finished;
  }

  public void setFinished(boolean t) {
    this.finished = t;
  }

  private Optional<Future<?>> procStageElement(ExecutorService executorService,
      CountDownLatch latch, StageElement se) {
    SingleTaskLatch sl =
        new SingleTaskLatch(this, executorService, eventDrivenTasksInThisStage, latch);
    if (se instanceof DefPlayerScenarioTask) {
      DefPlayerScenarioTask defPlayerScenarioTask = (DefPlayerScenarioTask) se;
      PlayerScenarioTask task =
          new PlayerScenarioTask(sl, context, defPlayerScenarioTask, rootEventDrivenTasks);
      rootEventDrivenTasks.add(task);
      eventDrivenTasksInThisStage.add(task);
      return Optional.empty();
    } else if (se instanceof DefEventDrivenTask) {
      DefEventDrivenTask defEventDrivenTask = (DefEventDrivenTask) se;
      EventDrivenTask<? extends GameEvent> task =
          new EventDrivenTask<>(sl, context, defEventDrivenTask, rootEventDrivenTasks);
      rootEventDrivenTasks.add(task);
      eventDrivenTasksInThisStage.add(task);
      return Optional.empty();
    } else if (se instanceof DefContextTask) {
      return Optional.of(executorService.submit(new ContextTask(sl, context, (DefContextTask) se)));
    } else if (se instanceof DefPlayerTask) {
      return Optional.of(executorService.submit(new PlayerTask(sl, context, (DefPlayerTask) se)));
    } else if (se instanceof DefStage) {
      DefStage ste = (DefStage) se;
      log.debug("END OF execStage (roundnum={}, stage={}", context.getRoundnum(), ste.getName());
      log.debug("END OF execStage (roundnum={}, stage={}", context.getRoundnum(), ste.getName());
      return Optional.of(executorService
          .submit(new StageTask(messenger, latch, rootEventDrivenTasks, context, (DefStage) se)));
    } else {
      String msg = "StageElement type error. " + se;
      log.error(msg);
      throw new RuntimeException(msg);
    }
  }

  private static boolean awaitResponseAux(CountDownLatch latch, List<Future<?>> fs) {
    for (Future<?> f : fs) {
      try {
        f.get();
      } catch (ExecutionException e) {
        throw new RuntimeException(e);
      } catch (InterruptedException e) {
      }
    }
    try {
      return latch.await(200, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      log.debug(e);
      return false;
    }

  }

  @Override
  public Object call() throws Exception {
    try {
      if (stage instanceof DefParallelStage) {
        execStageParlallelly();
      } else if (stage instanceof DefSequentialStage) {
        execStageSequentially();
      } else if (stage instanceof DefRestage) {
        Class<? extends DefStage> originalClass = ((DefRestage) stage).getOriginalClass();
        if (originalClass.isAssignableFrom(DefParallelStage.class)) {
          execStageParlallelly();
        } else {
          execStageSequentially();
        }
      } else {
        throw new IllegalStateException(
            "The class is not supported yet. = " + stage.getClass().getName());
      }
    } catch (Exception e) {
      log.error(System.lineSeparator() + "    Roundnum: {} . An exception occurred in: {}",
          context.getRoundnum(), this.stage);
      log.error(e, e);
      messenger.sendMessage(new UnexpectedShutdownRequest());
      throw e;
    }
    return null;
  }

  private List<ActorName> countDownedBy = new CopyOnWriteArrayList<>();

  public void countDownedBy(ActorName name) {
    countDownedBy.add(name);
  }

  public List<ActorName> getCountDownedBy() {
    return countDownedBy;
  }

}
