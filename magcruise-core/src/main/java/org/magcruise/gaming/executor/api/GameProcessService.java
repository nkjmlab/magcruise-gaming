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
package org.magcruise.gaming.executor.api;

import org.magcruise.gaming.executor.aws.AwsServersSetting;
import org.magcruise.gaming.manager.process.ProcessInfo;
import org.magcruise.gaming.manager.process.ProcessInfoJson;

/**
 * MAGCruiseCoreプロセスに関係するインタフェース. MAGCruiseBrokerがGUIからのリクエストに応答する形で使われるフロントエンドのサービス．
 *
 * @author nakaguchi, nkjm
 *
 */
public interface GameProcessService {

  String DEFAULT_PATH = "/json/GameProcessService";

  String createGameProcess(String bootstrapScript);

  String createGameProcess(String processId, String bootstrapScript);

  void sendScript(String processId, String script);

  ProcessInfo[] listProcesses();

  void stopProcess(String processId);

  void stopAllProcesses();

  void cleanUpProcess(String processId);

  void cleanUpAllProcesses();

  String getStdOut(String processId);

  String getStdErr(String processId);

  ProcessInfoJson[] listActiveProcesses();

  boolean isFinished(String processId);

  String getLatestRecord(String processId);

  String requestToCreateGameProcess(AwsServersSetting awsServersSetting, String bootstrapScript);

  void scheduleStopInstanceIfNeeded(AwsServersSetting awsSettings, String instanceId);

  void cleanUpIdleProcess(int idleProcessTTLMinutes);

  Object evalSExpression(String sexper);

  Object evalSExpression(String processId, String sexper);

  /**
   * 引数は1に固定
   *
   * @param className
   * @param methodName
   * @param arg
   */
  void invokeStaticMethod(String className, String methodName, String arg);

  /**
   * 引数は1に固定
   *
   * @param className
   * @param methodName
   * @param arg
   */
  void invokeStaticMethod(String processId, String className, String methodName, String arg);

}
