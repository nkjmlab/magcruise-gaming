package org.magcruise.gaming.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.util.SystemEnvironmentUtils;
import org.nkjmlab.util.java.concurrent.BasicThreadFactory;
import org.nkjmlab.util.java.concurrent.ExecutorServiceUtils;

public class KawaRepl implements Runnable {

  protected static Logger log = LogManager.getLogger();

  private ProcessId processId;
  private ExecutorService replExecutorService =
      Executors.newSingleThreadExecutor(BasicThreadFactory.builder("kawa-repl", false).build());

  private ScheduledExecutorService checkFileExecutorService = Executors
      .newSingleThreadScheduledExecutor(BasicThreadFactory.builder("check-exit", false).build());

  public KawaRepl(String processId) {
    this.processId = new ProcessId(processId);
  }

  /**
   * 外部プロセスから呼び出すときの呼び出し口となる．
   *
   * @param args
   */
  public static void main(String[] args) {
    new KawaRepl(args[0]).start();
  }

  public void start() {
    replExecutorService.submit(() -> {
      try {
        this.run();
      } catch (Throwable e) {
        log.error(e, e);
      }
    });
    replExecutorService.shutdown();
    checkFileExecutorService.scheduleWithFixedDelay(() -> {
      if (String.join(" ", SystemEnvironmentUtils.getTempDir(processId).toFile().list())
          .contains("exit")) {
        log.info("kawa-repl will be shutdownNow.");
        ExecutorServiceUtils.shutdownNowAfterAwaiting(replExecutorService, 10,
            TimeUnit.MILLISECONDS);
        log.info("check-exit will be shutdownNow.");
        ExecutorServiceUtils.shutdownNowAfterAwaiting(checkFileExecutorService, 1,
            TimeUnit.MILLISECONDS);
      }
    }, 1, 1, TimeUnit.SECONDS);
  }

  @Override
  public void run() {
    log.debug("environmentName for KawaRepl is {}.", processId);
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
      while (true) {
        if (reader.ready()) {
          String line = reader.readLine();
          if (line == null || line.length() == 0) {
            continue;
          }
          log.debug(line);
          Object obj = SchemeEnvironment.eval(processId.toString(), line);
          if (obj != null) {
            log.debug(obj + System.lineSeparator());
          }
        } else {
          if (Thread.interrupted()) {
            log.debug("Interrupted!!");
            throw new InterruptedException();
          }
        }
        Thread.sleep(1);
      }
    } catch (InterruptedException e) {
      log.info(e);
    } catch (Throwable e) {
      log.error(e, e);
    }
  }

}
