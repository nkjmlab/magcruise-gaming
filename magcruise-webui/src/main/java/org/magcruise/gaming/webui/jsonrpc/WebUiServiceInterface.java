package org.magcruise.gaming.webui.jsonrpc;

import org.magcruise.gaming.executor.aws.AwsServersSetting;
import org.magcruise.gaming.ui.api.RequestToUIRegistry;
import org.magcruise.gaming.ui.api.message.RequestToAssignOperator;
import org.magcruise.gaming.ui.model.input.InputFromWebUI;
import org.magcruise.gaming.webui.json.GameScriptJson;
import org.magcruise.gaming.webui.json.StartGameSessionJson;
import org.magcruise.gaming.webui.json.StartGameSessionsJson;
import org.nkjmlab.webui.service.user.UserAccountServiceInterface;

public interface WebUiServiceInterface extends UserAccountServiceInterface, RequestToUIRegistry {

  int deleteScript(String scriptId);

  boolean deleteUser(String userId);

  boolean updateScript(GameScriptJson script);

  boolean addScript(GameScriptJson script);

  boolean addBrokerUrls(String[] urls);

  String sendStartDemoGameSession(StartGameSessionJson json);

  String sendStartGameSession(StartGameSessionJson json);

  String sendStartGameSessions(StartGameSessionsJson json);

  String requestToCreateGameProcesses(
      AwsServersSetting awsServersSetting, StartGameSessionsJson json);

  boolean sendAssignment(String processId, String playerName, String userId);

  boolean sendInput(
      String processId, String playerName, long requestId, int roundnum, InputFromWebUI[] inputs);

  RequestToAssignOperator[] getNewAssignmentRequests(String processId);

  String resume(String processId);

  String resumeFrom(String processId, int roundnum);

  boolean sendLog(String errorLevel, String location, String msg, String options);

  boolean invokeMain(StartGameSessionJson json);
}
