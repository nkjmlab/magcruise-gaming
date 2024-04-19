package org.magcruise.gaming.executor.web;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.executor.api.GameProcessService;
import org.magcruise.gaming.executor.aws.AwsServersSetting;
import org.magcruise.gaming.manager.process.ProcessInfo;
import org.magcruise.gaming.manager.process.ProcessInfoJson;
import org.nkjmlab.util.jackson.JacksonMapper;
import org.nkjmlab.util.java.concurrent.RetryUtils;
import org.nkjmlab.util.java.net.UrlUtils;
import org.nkjmlab.util.jsonrpc.JsonRpcClientFactory;

public class GameProcessServiceClient implements GameProcessService {
  protected static Logger log = LogManager.getLogger();

  private String brokerUrl;
  private GameProcessService client;

  public GameProcessServiceClient(URL url) {
    this(url.toString());
  }

  public GameProcessServiceClient(String brokerUrl) {
    this.brokerUrl = brokerUrl;
    this.client = JsonRpcClientFactory.create(JacksonMapper.getIgnoreUnknownPropertiesMapper(),
        GameProcessService.class, getProcessServiceUrl());
  }

  @Override
  public String toString() {
    return "brokerUrl=" + brokerUrl;
  }

  public URL getProcessServiceUrl() {
    return UrlUtils.of(brokerUrl + GameProcessService.DEFAULT_PATH);
  }

  public String getBrokerUrl() {
    return brokerUrl;
  }

  @Override
  public void sendScript(String processId, String script) {
    client.sendScript(processId, script);
  }

  @Override
  public ProcessInfo[] listProcesses() {
    return client.listProcesses();
  }

  @Override
  public void stopProcess(String processId) {
    client.stopProcess(processId);

  }

  @Override
  public void stopAllProcesses() {
    client.stopAllProcesses();
  }

  @Override
  public void cleanUpProcess(String processId) {
    client.cleanUpProcess(processId);
  }

  @Override
  public void cleanUpAllProcesses() {
    client.cleanUpAllProcesses();
  }

  @Override
  public String getStdOut(String processId) {
    return client.getStdOut(processId);
  }

  @Override
  public String getStdErr(String processId) {
    return client.getStdErr(processId);
  }

  @Override
  public ProcessInfoJson[] listActiveProcesses() {
    return client.listActiveProcesses();
  }

  @Override
  public boolean isFinished(String processId) {
    return client.isFinished(processId);
  }

  @Override
  public String getLatestRecord(String processId) {
    return client.getLatestRecord(processId);
  }

  @Override
  public String requestToCreateGameProcess(AwsServersSetting awsServersSetting,
      String bootstrapScript) {
    return RetryUtils.retry(() -> {
      return client.requestToCreateGameProcess(awsServersSetting, bootstrapScript);
    }, 3, 1, TimeUnit.SECONDS);
  }

  @Override
  public String createGameProcess(String bootstrapScript) {
    return RetryUtils.retry(() -> {
      log.info("CreateGameProcess {}", bootstrapScript);
      return client.createGameProcess(bootstrapScript);
    }, 3, 1, TimeUnit.SECONDS);
  }

  @Override
  public String createGameProcess(String processId, String bootstrapScript) {
    return RetryUtils.retry(() -> {
      String pid = client.createGameProcess(processId, bootstrapScript);
      log.info("success to create game process {}", processId);
      return pid;
    }, 3, 1, TimeUnit.SECONDS);

  }

  @Override
  public void scheduleStopInstanceIfNeeded(AwsServersSetting awsSettings, String instanceId) {
    RetryUtils.retry(() -> {
      client.scheduleStopInstanceIfNeeded(awsSettings, instanceId);
      log.info("success to schedule stop instance {}", instanceId);
      return null;
    }, 2, 2, TimeUnit.SECONDS);

  }

  @Override
  public void cleanUpIdleProcess(int idleProcessTTLMinutes) {
    client.cleanUpIdleProcess(idleProcessTTLMinutes);
  }

  @Override
  public Object evalSExpression(String sexper) {
    return client.evalSExpression(sexper);
  }

  @Override
  public Object evalSExpression(String processId, String sexper) {
    return client.evalSExpression(processId, sexper);
  }

  @Override
  public void invokeStaticMethod(String className, String methodName, String arg) {
    client.invokeStaticMethod(className, methodName, arg);
  }

  @Override
  public void invokeStaticMethod(String processId, String className, String methodName,
      String arg) {
    client.invokeStaticMethod(className, methodName, arg);
  }

}
