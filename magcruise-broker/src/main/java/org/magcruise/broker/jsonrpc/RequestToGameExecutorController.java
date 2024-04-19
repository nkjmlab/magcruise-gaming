/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 MAGCruise
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.magcruise.broker.jsonrpc;

import java.util.ArrayList;
import java.util.List;
import org.magcruise.gaming.executor.api.RequestToGameExecutorService;
import org.magcruise.gaming.executor.api.message.JoinInGame;
import org.magcruise.gaming.executor.api.message.ReceiveInput;
import org.magcruise.gaming.executor.api.message.RequestToGameExecutor;
import org.magcruise.gaming.executor.api.message.RequestToGameExecutorTransferObject;
import org.magcruise.gaming.lang.Message;
import org.magcruise.gaming.manager.process.ExternalGameProcessManager;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.def.actor.DefAssignmentRequest;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.LRUMessageBoxes;
import org.magcruise.gaming.model.game.MessageBoxes;
import org.magcruise.gaming.ui.api.message.RequestToAssignOperator;
import org.magcruise.gaming.ui.model.input.InputFromWebUI;

/**
 * @author nkjm
 */
public class RequestToGameExecutorController implements RequestToGameExecutorService {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  private static final int MAX_PROCESSES_SIZE = 200;
  protected static MessageBoxes<ProcessId, RequestToGameExecutor> requestToGameExecutorQueues =
      new LRUMessageBoxes<>(MAX_PROCESSES_SIZE);
  protected static RequestToUIController uiControler = new RequestToUIController();

  public RequestToGameExecutorController() {
    // TODO 自動生成されたコンストラクター・スタブ
  }

  @Override
  public RequestToGameExecutorTransferObject[] getNewRequestsToGameExecutor(String processId) {
    try {
      List<RequestToGameExecutor> tmp =
          requestToGameExecutorQueues.pollAll(new ProcessId(processId));
      if (tmp.size() == 0) {
        return tmp.toArray(new RequestToGameExecutorTransferObject[0]);
      }

      List<RequestToGameExecutorTransferObject> result = new ArrayList<>();

      for (RequestToGameExecutor e : tmp) {
        RequestToGameExecutorTransferObject e2 =
            new RequestToGameExecutorTransferObject(e.toConstructor());
        result.add(e2);
      }

      return result.toArray(new RequestToGameExecutorTransferObject[0]);
    } catch (Exception e) {
      log.error(e, e);
      throw e;
    }
  }

  @Override
  public void sendAssignment(String processId, String playerName, String operatorId) {}

  @Override
  public void joinInGame(String processId, String playerName) {
    log.info("{},{}", processId, playerName);

    String operatorId = DefAssignmentRequest.ANONYMOUS_OPERATOR;
    for (RequestToAssignOperator r : uiControler.getAllAssignmentRequests(processId)) {
      if (r.getPlayerName().equals(playerName)) {
        operatorId = r.getOperatorId();
      }
    }
    requestToGameExecutorQueues.offer(
        new ProcessId(processId), new JoinInGame(ActorName.of(playerName), operatorId));
    sendMessageToProcess(processId, new JoinInGame(ActorName.of(playerName), operatorId));

    uiControler.notifyJoinInGame(processId, playerName);
  }

  private void sendMessageToProcess(String processId, Message msg) {
    try {
      if (!ExternalGameProcessManager.getInstance().exists(new ProcessId(processId))) {
        return;
      }
      ExternalGameProcessManager.getInstance().sendMessage(new ProcessId(processId), msg);
    } catch (Exception e) {
      log.error(e, e);
      throw e;
    }
  }

  @Override
  public void sendInput(
      String processId, String playerName, long requestId, int roundnum, InputFromWebUI[] inputs) {
    log.info("{} in {} on round {} to {}, {}", requestId, processId, roundnum, playerName, inputs);
    ReceiveInput r = new ReceiveInput(requestId, playerName, inputs);
    requestToGameExecutorQueues.offer(new ProcessId(processId), r);
    sendMessageToProcess(processId, r);
  }

  /** UIと連携する必要があるので，GameInteractionControllerに任せる． */
  @Override
  public void sendAutoInput(
      String processId,
      String playerName,
      long requestId,
      int roundnum,
      InputFromWebUI[] inputs,
      int maxResponseTime) {
    sendInput(processId, playerName, requestId, roundnum, inputs);
  }

  @Override
  public void request(ProcessId processId, RequestToGameExecutor message) {
    try {
      sendMessageToProcess(processId.toString(), message);
    } catch (Exception e) {
      log.error(e, e);
      throw e;
    }
  }
}
