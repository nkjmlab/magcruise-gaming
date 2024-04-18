package org.magcruise.gaming.manager.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.common.KawaRepl;
import org.magcruise.gaming.lang.Message;
import org.magcruise.gaming.lang.SExpression;
import org.magcruise.gaming.lang.SExpression.ToExpressionStyle;
import org.magcruise.gaming.manager.GameExecutorManager;
import org.magcruise.gaming.util.SystemEnvironmentUtils;
import org.nkjmlab.sorm4j.internal.util.Try;
import org.nkjmlab.util.java.lang.MethodInvokerInfoUtils;
import org.nkjmlab.util.java.lang.ParameterizedStringFormatter;

public class ExternalGameProcess implements GameProcess {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  private ProcessBuilder processBuilder;
  private Process process;
  private Writer writer;
  private ProcessId processId;

  private File stdOutFile;
  private File stdErrFile;

  private SExpression initialScript;

  public ExternalGameProcess(List<String> javaCommands, List<String> classPaths,
      ProcessId processId, SExpression initialScript) {
    this.processId = processId;
    this.initialScript = initialScript;

    List<String> args = new ArrayList<>();
    args.addAll(javaCommands);
    args.addAll(classPaths);
    args.add(KawaRepl.class.getName());
    args.add(processId.getValue());
    processBuilder = new ProcessBuilder(args);
    redirect();
  }

  @Override
  public Object call() throws Exception {
    start();
    return null;
  }

  @Override
  public void start() {
    try {
      log.info("ProcessBuilder command is {}", processBuilder.command().toString());
      info("ProcessBuilder command is " + processBuilder.command().toString());
      log.info("Working directory is {}", processBuilder.directory());
      process = processBuilder.start();
      writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
      log.info("Create and register GameExecutor {}",
          initialScript.getExpression(ToExpressionStyle.MULTI_LINE));
      info("Working directory is " + processBuilder.directory());
      info("Create and register GameExecutor "
          + initialScript.getExpression(ToExpressionStyle.MULTI_LINE));
      writeStringToGameProcess(initialScript.getExpression());
    } catch (Exception e) {
      log.error(e, e);
      throw new RuntimeException(e);
    }
  }

  public void directory(Path workingDirectory) {
    processBuilder.directory(workingDirectory.toFile());
  }

  public void redirectOutput(File out) {
    this.stdOutFile = out;
    processBuilder.redirectOutput(Redirect.appendTo(stdOutFile));

    log.info("Working direcroy for this process in temporal directory is {}",
        SystemEnvironmentUtils.getTempDir(processId));
    info("Working direcroy for this process in temporal directory is "
        + SystemEnvironmentUtils.getTempDir(processId));

    log.info("StdOut file is {}", stdOutFile.toString());
    info("StdOut file is " + stdOutFile.toString());
  }

  public void redirectError(File err) {
    this.stdErrFile = err;
    processBuilder.redirectError(Redirect.appendTo(stdErrFile));
    log.warn("[Test for error reporting] "
        + "This message is showed in console for game creation process. "
        + "The error messages for the external game process are written in {}.", stdOutFile);
    warn("Test for error reporting. StdErr file is " + stdErrFile.toString());
  }

  protected void redirect() {
    redirectOutput(SystemEnvironmentUtils.createTempStdOutFile(processId).toFile());
    redirectError(SystemEnvironmentUtils.createTempStdErrFile(processId).toFile());
  }

  public void writeStringToGameProcess(String script) {
    if (script == null || script.length() == 0) {
      return;
    }

    try {
      String formedLine = script;
      if (!formedLine.endsWith(System.lineSeparator())) {
        formedLine = formedLine + System.lineSeparator();
      }
      log.debug(formedLine);
      writer.write(formedLine);
      writer.flush();
    } catch (Exception e) {
      log.error(script);
      log.error(e, e);
      process.destroy();
    }

  }

  public void writeMessageToGameProcess(Message message) {
    writeStringToGameProcess(GameExecutorManager.getInstance()
        .getSendMessageSMethodCall(processId, message).getExpression());

  }

  public void trace(String message, Object... params) {
    writeToStdOut(
        MethodInvokerInfoUtils.getInvokerLogMessage(3, "TRACE", new Throwable().getStackTrace())
            + " " + ParameterizedStringFormatter.DEFAULT.format((String) message, params)
            + System.lineSeparator());
  }

  /**
   * Writing debug logger with code location information and a line separator.
   *
   * @param message
   */
  public void debug(String message, Object... params) {
    writeToStdOut(
        MethodInvokerInfoUtils.getInvokerLogMessage(3, "DEBUG", new Throwable().getStackTrace())
            + " " + ParameterizedStringFormatter.DEFAULT.format((String) message, params)
            + System.lineSeparator());
  }

  public void info(String message, Object... params) {
    writeToStdOut(
        MethodInvokerInfoUtils.getInvokerLogMessage(3, "INFO", new Throwable().getStackTrace())
            + " " + ParameterizedStringFormatter.DEFAULT.format((String) message, params)
            + System.lineSeparator());
  }

  public void warn(String message, Object... params) {
    writeToStdError(
        MethodInvokerInfoUtils.getInvokerLogMessage(3, "WARN", new Throwable().getStackTrace())
            + " " + ParameterizedStringFormatter.DEFAULT.format((String) message, params)
            + System.lineSeparator());
  }

  public void error(String message, Object... params) {
    writeToStdError(
        MethodInvokerInfoUtils.getInvokerLogMessage(3, "ERROR", new Throwable().getStackTrace())
            + " " + ParameterizedStringFormatter.DEFAULT.format((String) message, params)
            + System.lineSeparator());
  }

  /**
   * Writing message to stdout. {@code start} method should be called before use this.
   *
   * @param message
   */
  protected void writeToStdOut(String message) {
    try {
      Files.writeString(stdOutFile.toPath(), message);
    } catch (IOException e) {
      throw Try.rethrow(e);
    }
  }

  /**
   * Writing message to stderr. {@code start} method should be called before use this. @see
   * {@link #warn}, {@link #error}
   *
   * @param message
   */
  protected void writeToStdError(String message) {
    try {
      Files.writeString(stdErrFile.toPath(), message);
    } catch (IOException e) {
      throw Try.rethrow(e);
    }
  }

  public void destroy() {
    process.destroy();
  }

  public File getStdOutFile() {
    return stdOutFile;
  }

  public File getStdErrFile() {
    return stdErrFile;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public ProcessId getProcessId() {
    return processId;
  }

  @Override
  public int waitFor() {
    while (true) {
      try {
        process.waitFor(5, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
      }
      if (SystemEnvironmentUtils.existsTempFile(processId, "exit_0")) {
        log.info("{} is finished", processId);
        return 0;
      }
    }
  }

  public boolean isFinished() {
    if (process == null) {
      return false;
    }

    return !process.isAlive();
  }

  @Override
  public String getLatestRecord(ProcessId processId) {
    return SystemEnvironmentUtils.getLatestRecord(processId);
  }

}
