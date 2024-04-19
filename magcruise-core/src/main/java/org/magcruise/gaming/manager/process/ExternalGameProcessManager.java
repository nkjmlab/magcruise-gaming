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
package org.magcruise.gaming.manager.process;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.executor.api.RequesterToGameExecutor;
import org.magcruise.gaming.executor.api.message.RequestToGameExecutor;
import org.magcruise.gaming.lang.Message;
import org.magcruise.gaming.lang.SimpleSExpression;
import org.magcruise.gaming.model.def.GameBuilder;
import org.nkjmlab.util.java.lang.MethodInvokerInfoUtils;

/**
 *
 * @author nkjm
 *
 */
public class ExternalGameProcessManager implements RequesterToGameExecutor {

  protected static Logger log = LogManager.getLogger();

  private static ExternalGameProcessManager instance;

  private static String JAVA_CMD = "java";

  private ExternalGameProcessManager() {

  }

  public static ExternalGameProcessManager getInstance() {
    if (instance == null) {
      instance = new ExternalGameProcessManager();
    }
    return instance;
  }

  private static IdToProcess idToProcess = new IdToProcess();

  public static class IdToProcess {
    private Map<ProcessId, Pair<ExternalGameProcess, ProcessInfo>> idToProcMap =
        new ConcurrentHashMap<>();

    public Collection<? extends Pair<ExternalGameProcess, ProcessInfo>> values() {
      return idToProcMap.values();
    }

    public void put(ProcessId id, Pair<ExternalGameProcess, ProcessInfo> pair) {
      idToProcMap.put(id, pair);
    }

    public Pair<ExternalGameProcess, ProcessInfo> get(ProcessId processId) {
      return idToProcMap.get(processId);
    }

    public void remove(ProcessId processId) {
      this.remove(processId);
    }

    public Set<ProcessId> keySet() {
      return idToProcMap.keySet();
    }

    public Set<Entry<ProcessId, Pair<ExternalGameProcess, ProcessInfo>>> entrySet() {
      return idToProcMap.entrySet();
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

  }

  public Collection<Pair<ExternalGameProcess, ProcessInfo>> listProcesses() {
    Collection<Pair<ExternalGameProcess, ProcessInfo>> ret =
        new TreeSet<>(new Comparator<Pair<ExternalGameProcess, ProcessInfo>>() {
          @Override
          public int compare(Pair<ExternalGameProcess, ProcessInfo> o1,
              Pair<ExternalGameProcess, ProcessInfo> o2) {
            return o1.getRight().getProcessId().compareTo(o2.getRight().getProcessId());
          }
        });
    ret.addAll(idToProcess.values());
    return ret;
  }

  public List<Pair<ExternalGameProcess, ProcessInfo>> listActiveProcesses() {
    return listProcesses().stream().filter(pair -> pair.getRight().isRunning())
        .collect(Collectors.toList());
  }

  public ExternalGameProcess createProcess(String classpath, GameBuilder gameBuilder) {
    return createProcess(null, classpath, gameBuilder);
  }

  public ExternalGameProcess createProcess(String javaHome, String classpath,
      GameBuilder gameBuilder) {

    ExternalGameProcessBuilder processBuilder = new ExternalGameProcessBuilder(gameBuilder);
    log.info("GameBuilder = {}", gameBuilder);

    processBuilder.setJavaCommand(getJavaCommand(javaHome));
    processBuilder.setClassPath(classpath);

    log.info("ProcessBuilder ={}", processBuilder);

    ExternalGameProcess process = processBuilder.build();

    ProcessId processId = process.getProcessId();
    registerProcessInfo(processId, process);

    log.info("ExternalGameProcess {} is created.", processId);
    log.info("StdOut and StdErr of the process are redirected to {} and {}.",
        process.getStdOutFile(), process.getStdErrFile());
    return process;
  }

  public void registerProcessInfo(ProcessId processId, ExternalGameProcess process) {
    ProcessInfo pi = new ProcessInfo(processId, System.currentTimeMillis(), true);
    idToProcess.put(processId, Pair.of(process, pi));

  }

  private String getJavaCommand(String javaHome) {
    if (javaHome == null) {
      return JAVA_CMD;
    }
    return new File(javaHome, "/bin/" + JAVA_CMD).getPath();
  }

  /**
   * プロセスを停止する。プロセス管理テーブルのエントリは残る。
   *
   * @param processId プロセスID
   */
  public void stopProcess(ProcessId processId) {
    ExternalGameProcess proc = getProcess(processId);
    if (proc == null) {
      return;
    }
    stop(getProcess(processId), getProcessInfo(processId));
    log.debug("Process " + processId + " stopped.");
  }

  public void stopAllProcesses() {
    for (Pair<ExternalGameProcess, ProcessInfo> p : idToProcess.values()) {
      stop(p.getLeft(), p.getRight());
    }
    log.debug("All processes stopped.");
  }

  /**
   * プロセスを消去する。プロセスが実行中の場合、stopProcessが呼ばれる。 プロセス管理テーブルからプロセス情報が消去される。ログファイルは残る。
   *
   * @param processId プロセスID
   */
  public void cleanUpProcess(ProcessId processId) {
    ProcessInfo proc = getProcessInfo(processId);
    if (proc == null) {
      return;
    }
    if (proc.isRunning()) {
      stopProcess(processId);
    }
    idToProcess.remove(processId);
  }

  private ProcessInfo getProcessInfo(ProcessId processId) {
    Pair<ExternalGameProcess, ProcessInfo> proc = idToProcess.get(processId);
    if (proc == null) {
      log.debug("no process for id: " + processId);
      return null;
    }
    return proc.getRight();
  }

  public void cleanUpAllProcess() {
    List<ProcessId> ids = new ArrayList<>(idToProcess.keySet());
    for (ProcessId id : ids) {
      Pair<ExternalGameProcess, ProcessInfo> proc = idToProcess.get(id);
      if (proc.getRight().isRunning())
        continue;
      idToProcess.remove(id);
    }
  }

  public void cleanUpIdleProcess(int idleProcessTTLMinutes) {
    log.info("cleanUpIdleProcess with {} minutes as TTL.", idleProcessTTLMinutes);

    synchronized (idToProcess) {
      for (Entry<ProcessId, Pair<ExternalGameProcess, ProcessInfo>> entry : idToProcess
          .entrySet()) {
        ProcessInfo pi = entry.getValue().getRight();
        if (!pi.isRunning()) {
          continue;
        }
        log.info(
            "[{}] is running:{},TTL(MINUTES):{},arrived:{},current{},current-arrived:{},rest-ttl(MILLISEC):{}",
            pi.getProcessId(), pi.isRunning(), idleProcessTTLMinutes, pi.getLastMessageArrived(),
            System.currentTimeMillis(), System.currentTimeMillis() - pi.getLastMessageArrived(),
            pi.getLastMessageArrived() + TimeUnit.MINUTES.toMillis(idleProcessTTLMinutes)
                - System.currentTimeMillis());
        if (pi.getLastMessageArrived() + TimeUnit.MINUTES.toMillis(idleProcessTTLMinutes)
            - System.currentTimeMillis() < 0) {
          log.warn("STOP IDLE PROCESS: {}", pi.getProcessId());
          stop(entry.getValue().getLeft(), pi);
        }
      }
    }
  }

  public void sendMessage(ProcessId processId, Message message) {
    ExternalGameProcess proc = getProcess(processId);
    if (proc == null) {
      return;
    }
    proc.writeMessageToGameProcess(message);
  }

  private ExternalGameProcess getProcess(ProcessId processId) {
    Pair<ExternalGameProcess, ProcessInfo> proc = idToProcess.get(processId);
    if (proc == null) {
      log.debug("Process of {} does not exist on ExternalGameManager. "
          + "There is no problem when you start the game on local pc", processId);
      return null;
    }
    return proc.getLeft();
  }

  public boolean isFinished(ProcessId processId) {
    return getProcess(processId).isFinished();
  }

  public void sendScript(ProcessId processId, String script) {
    log.debug(processId);
    log.debug(script);
    log.debug(idToProcess);
    Pair<ExternalGameProcess, ProcessInfo> proc = idToProcess.get(processId);
    log.debug(proc);
    if (proc == null) {
      log.warn("no process for id {} ", processId);
      return;
    }
    proc.getLeft().writeStringToGameProcess(new SimpleSExpression(script).getExpression());
    proc.getRight().setLastMessageArrived(System.currentTimeMillis());
  }

  private static void stop(ExternalGameProcess process, ProcessInfo processInfo) {
    processInfo.setRunning(false);
    process.destroy();
  }

  @Override
  public void request(ProcessId processId, RequestToGameExecutor message) {
    sendMessage(processId, message);
  }

  public void debug(ProcessId processId, String message) {
    ExternalGameProcess proc = getProcess(processId);
    if (proc == null) {
      return;
    }
    proc.debug(
        MethodInvokerInfoUtils.getInvokerFileNameAndLineNumber(2, new Throwable().getStackTrace())
            + " " + message);
  }

  public boolean exists(ProcessId processId) {
    if (getProcess(processId) == null) {
      return false;
    }
    return true;
  }

}
