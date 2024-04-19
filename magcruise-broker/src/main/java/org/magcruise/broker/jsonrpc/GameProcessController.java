/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 MAGCruise
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.magcruise.broker.jsonrpc;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.SystemUtils;
import org.magcruise.gaming.executor.api.GameProcessService;
import org.magcruise.gaming.executor.aws.AwsServersSetting;
import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.manager.process.ExternalGameProcess;
import org.magcruise.gaming.manager.process.ExternalGameProcessManager;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.manager.process.ProcessInfo;
import org.magcruise.gaming.manager.process.ProcessInfoJson;
import org.magcruise.gaming.model.def.GameBuilder;
import org.magcruise.gaming.model.def.sys.DefCallbackBrokerUrl;
import org.magcruise.gaming.model.def.sys.DefWorkingDirectory;
import org.magcruise.gaming.util.SystemEnvironmentUtils;
import org.nkjmlab.util.java.concurrent.ForkJoinPoolUtils;

public class GameProcessController implements GameProcessService {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();
  protected static RequestToUIController uiControler = new RequestToUIController();

  private static int coreForCreateGameProcess =
      (int) Math.max(ForkJoinPoolUtils.availableProcessors() * 2.0 / 3, 1.0);

  private static ScheduledExecutorService createGameProcessExecutorService =
      Executors.newScheduledThreadPool(coreForCreateGameProcess);
  private static Queue<Runnable> createGameProcessExecutorTasks = new ConcurrentLinkedQueue<>();

  static {
    log.info(
        "Available processors = {}, Core for create game proecess is {}",
        ForkJoinPoolUtils.availableProcessors(),
        coreForCreateGameProcess);
    createGameProcessExecutorService.scheduleWithFixedDelay(
        () -> {
          if (createGameProcessExecutorTasks.isEmpty()) {
            return;
          }
          ForkJoinPoolUtils.executeWith(
              coreForCreateGameProcess,
              () -> {
                for (int p = 0; p < coreForCreateGameProcess; p++) {
                  Runnable task = createGameProcessExecutorTasks.poll();
                  if (task == null) {
                    break;
                  }
                  task.run();
                }
              });
        },
        0,
        5,
        TimeUnit.SECONDS);
  }

  public static void main(String[] args) throws InterruptedException {
    AtomicInteger a = new AtomicInteger();
    for (int j = 0; j < 100; j++) {
      createGameProcessExecutorTasks.add(() -> System.out.println(a.incrementAndGet()));
    }
    Thread.sleep(10000);
  }

  protected final File appRootDir;
  private final String brokerUrl;

  protected File getRealPath(String path) {
    return new File(appRootDir, path);
  }

  public GameProcessController(File appRootDir, String brokerUrl) {
    this.appRootDir = appRootDir;
    this.brokerUrl = brokerUrl;
  }

  @Override
  public synchronized String requestToCreateGameProcess(
      AwsServersSetting awsServersSetting, String bootstrapScript) {
    throw new NotImplementedException();
  }

  @Override
  public Object evalSExpression(String sexper) {
    return SchemeEnvironment.eval(ProcessId.getNewProcessId().toString(), sexper);
  }

  @Override
  public Object evalSExpression(String processId, String sexper) {
    return SchemeEnvironment.eval(processId, sexper);
  }

  @Override
  public void invokeStaticMethod(String className, String methodName, String arg) {
    invokeStaticMethod(ProcessId.getNewProcessId().toString(), className, methodName, arg);
  }

  @Override
  public void invokeStaticMethod(
      String processId, String className, String methodName, String arg) {
    log.info("{},{},{},{}", processId, className, methodName, arg);
    ExecutorService service = Executors.newSingleThreadExecutor();
    service.submit(
        () -> {
          try {
            Method m = Class.forName(className).getMethod(methodName, String[].class);
            m.invoke(null, new Object[] {new String[] {arg}});
          } catch (Exception e) {
            log.error(e, e);
            throw new RuntimeException(e);
          }
        });
    service.shutdown();
  }

  @Override
  public String createGameProcess(String bootstrapScript) {
    String ret = createGameProcess(ProcessId.getNewProcessId().toString(), bootstrapScript);
    return ret;
  }

  @Override
  public synchronized String createGameProcess(String processId, String bootstrapScript) {
    try {
      log.info("in requestToCreateGameProcess {}", processId);
      // logger.info("requestUrl={}, pid={},{}", getRequestUrl(), processId, bootstrapScript);

      GameBuilder gameBuilder = new GameBuilder(new ProcessId(processId));
      gameBuilder
          .getBootstrapBuilder()
          .setBootstrapScript(
              SystemEnvironmentUtils.toTmpScriptFile(
                      "bootstrap",
                      ".scm",
                      gameBuilder.getProcessId(),
                      bootstrapScript.replace(" (", System.lineSeparator() + " ("))
                  .toUri());

      // WorkingDirectoryをappRootDirに決め打ちしている．しかし，指定がある場合は，指定を優先する方が正しい．
      // それをするには，一回gameBuilderをbuildしないとだめ．
      // GameBuilderを一度bootstrapまでビルドして捨てるまでを簡単にできると便利かな．

      Path workingDirectory = appRootDir.toPath();
      gameBuilder
          .getGameSystemPropertiesBuilder()
          .addProperties(new DefWorkingDirectory(workingDirectory));
      DefCallbackBrokerUrl callbackBrokerUrl = new DefCallbackBrokerUrl(brokerUrl);
      log.info("[new] DefCallbackBrokerUrl is created. {}", callbackBrokerUrl);
      log.info("workind directory={}", workingDirectory);
      gameBuilder.getGameSystemPropertiesBuilder().addProperties(callbackBrokerUrl);
      log.info("[{}], {}", gameBuilder.getProcessId(), gameBuilder.getPlayerNames());

      ExternalGameProcess p =
          ExternalGameProcessManager.getInstance()
              .createProcess(
                  SystemUtils.getJavaHome().toString(),
                  new ClasspathBuilder(workingDirectory).build(),
                  gameBuilder);
      log.info("External process is {}", p);
      createGameProcessExecutorTasks.add(
          () -> {
            p.start();
            uiControler.notifyStartOfGameProcess(processId);
            if (!processId.equals(p.getProcessId().toString())) {
              throw new RuntimeException("procId does not match");
            }
          });
      return processId;
    } catch (Throwable e) {
      log.error(e, e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public ProcessInfoJson[] listActiveProcesses() {
    ProcessInfoJson[] result =
        ExternalGameProcessManager.getInstance().listProcesses().stream()
            .map(processInfoPair -> processInfoPair.getRight())
            .filter(process -> process.isRunning())
            .map(pi -> new ProcessInfoJson(pi))
            .collect(Collectors.toList())
            .toArray(new ProcessInfoJson[0]);
    return result;
  }

  @Override
  public ProcessInfo[] listProcesses() {
    return ExternalGameProcessManager.getInstance().listProcesses().stream()
        .map(processInfoPair -> processInfoPair.getRight())
        .collect(Collectors.toList())
        .toArray(new ProcessInfo[0]);
  }

  @Override
  public void stopProcess(String processId) {
    ExternalGameProcessManager.getInstance().stopProcess(new ProcessId(processId));
  }

  @Override
  public void stopAllProcesses() {
    ExternalGameProcessManager.getInstance().stopAllProcesses();
  }

  @Override
  public void cleanUpProcess(String processId) {
    ExternalGameProcessManager.getInstance().cleanUpProcess(new ProcessId(processId));
  }

  @Override
  public void cleanUpAllProcesses() {
    ExternalGameProcessManager.getInstance().cleanUpAllProcess();
  }

  @Override
  public String getStdOut(String processId) {
    return SystemEnvironmentUtils.getStdOutContent(new ProcessId(processId));
  }

  @Override
  public String getStdErr(String processId) {
    return SystemEnvironmentUtils.getStdErrContent(new ProcessId(processId));
  }

  @Override
  public void sendScript(String processId, String script) {
    try {
      ExternalGameProcessManager.getInstance().sendScript(new ProcessId(processId), script);
    } catch (Throwable e) {
      log.error(e, e);
      throw e;
    }
  }

  class ClasspathBuilder {

    private Path workingDirectory;

    public ClasspathBuilder(Path workingDirectory) {
      this.workingDirectory = workingDirectory;
    }

    public String build() {
      List<String> classpathBuilder = new ArrayList<>();
      addClassDirectory(classpathBuilder, workingDirectory.toFile());

      File appRootLibs = new File(appRootDir, "lib");
      addJars(classpathBuilder, appRootLibs, new String[] {"*"});
      addJars(classpathBuilder, new File(appRootDir.getParentFile(), "lib"), new String[] {"*"});
      addJars(
          classpathBuilder,
          new File(appRootDir.getParentFile().getParentFile(), "lib"),
          new String[] {"*"});

      log.info("classpath is {}", classpathBuilder);
      return String.join(File.pathSeparator, classpathBuilder);
    }

    private void addJars(List<String> builder, File libDir, String[] jars) {
      Arrays.asList(jars)
          .forEach(
              jar -> {
                builder.add(new File(libDir, jar).getAbsolutePath().toString());
              });
    }

    private void addClassDirectory(List<String> builder, File projectHome) {
      builder.add(new File(projectHome, "bin").getAbsolutePath().toString());
    }
  }

  @Override
  public boolean isFinished(String processId) {
    return ExternalGameProcessManager.getInstance().isFinished(new ProcessId(processId));
  }

  @Override
  public String getLatestRecord(String processId) {
    return SystemEnvironmentUtils.getLatestRecord(new ProcessId(processId));
  }

  @Override
  public void scheduleStopInstanceIfNeeded(AwsServersSetting awsSettings, String instanceId) {
    throw new NotImplementedException();
  }

  @Override
  public void cleanUpIdleProcess(int idleProcessTTLMinutes) {
    ExternalGameProcessManager.getInstance().cleanUpIdleProcess(idleProcessTTLMinutes);
  }
}
