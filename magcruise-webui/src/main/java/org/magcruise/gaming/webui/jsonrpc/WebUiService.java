package org.magcruise.gaming.webui.jsonrpc;

import java.io.File;
import java.io.Serializable;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.magcruise.gaming.executor.aws.AwsServersSetting;
import org.magcruise.gaming.executor.db.GameRecord;
import org.magcruise.gaming.executor.db.GameRecordsTable;
import org.magcruise.gaming.executor.web.GameInteractionServiceClient;
import org.magcruise.gaming.executor.web.GameProcessServiceClient;
import org.magcruise.gaming.ui.api.GameSessionJson;
import org.magcruise.gaming.ui.api.message.RequestToAssignOperator;
import org.magcruise.gaming.ui.model.input.InputFromWebUI;
import org.magcruise.gaming.ui.model.input.InputToUI;
import org.magcruise.gaming.ui.model.message.MessageToUI;
import org.magcruise.gaming.webui.json.GameScriptJson;
import org.magcruise.gaming.webui.json.StartGameSessionJson;
import org.magcruise.gaming.webui.json.StartGameSessionsJson;
import org.magcruise.gaming.webui.relation.AssignmentsTable;
import org.magcruise.gaming.webui.relation.GameScriptsTable;
import org.magcruise.gaming.webui.relation.GameSessionsTable;
import org.magcruise.gaming.webui.relation.InputsTable;
import org.magcruise.gaming.webui.relation.MessagesTable;
import org.magcruise.gaming.webui.relation.ProgressesTable;
import org.magcruise.gaming.webui.row.Assignment;
import org.magcruise.gaming.webui.row.GameScript;
import org.magcruise.gaming.webui.row.GameSession;
import org.magcruise.gaming.webui.row.Input;
import org.magcruise.gaming.webui.row.Message;
import org.nkjmlab.util.jackson.JacksonMapper;
import org.nkjmlab.util.java.io.FileUtils;
import org.nkjmlab.util.java.io.SystemFileUtils;
import org.nkjmlab.util.java.lang.ParameterizedStringFormatter;
import org.nkjmlab.webui.service.user.UserAccount;
import org.nkjmlab.webui.service.user.UserAccountService;
import org.nkjmlab.webui.service.user.UserAccountsTable;

public class WebUiService implements WebUiServiceInterface {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  private final UserAccountService userAccountService;
  private final GameScriptsTable gameScriptsTable;
  private final AssignmentsTable assignmentsTable;
  private final GameSessionsTable gameSessionsTable;
  private final MessagesTable messagesTable;
  private final InputsTable inputsTable;
  private final ProgressesTable progressesTable;
  private final GameRecordsTable gameRecordsTable;

  private static final Map<String, GameInteractionServiceClient> interactionServices =
      new ConcurrentHashMap<>();

  private static final ExecutorService lazyExecutorService = Executors.newSingleThreadExecutor();

  public WebUiService(
      UserAccountService userAccountService,
      GameScriptsTable gameScriptsTable,
      AssignmentsTable assignmentsTable,
      GameSessionsTable gameSessionsTable,
      MessagesTable messagesTable,
      InputsTable inputsTable,
      ProgressesTable progressesTable,
      GameRecordsTable gameRecordsTable) {
    this.userAccountService = userAccountService;
    this.gameScriptsTable = gameScriptsTable;
    this.assignmentsTable = assignmentsTable;
    this.gameSessionsTable = gameSessionsTable;
    this.messagesTable = messagesTable;
    this.inputsTable = inputsTable;
    this.progressesTable = progressesTable;
    this.gameRecordsTable = gameRecordsTable;
  }

  @Override
  public boolean signup(UserAccount account) {
    if (isAdmin()) {
      return register(account);
    } else {
      account.setRole("USER");
      return userAccountService.signup(account);
    }
  }

  @Override
  public boolean register(UserAccount account) {
    checkAdmin();
    if (account.getRole() == null) {
      account.setRole("USER");
    }

    if (account.getLanguage() == null) {
      account.setLanguage("ja");
    }
    return userAccountService.register(account);
  }

  @Override
  public boolean merge(UserAccount account) {
    return true;
  }

  @Override
  public boolean update(UserAccount account) {
    userAccountService.update(account);
    return true;
  }

  @Override
  public boolean login(String userId, String password) {
    boolean result = userAccountService.login(userId, password);
    if (result) {
      log.info("{} success to login", userId);
    } else {
      log.warn("{} failed to login", userId);
    }
    return result;
  }

  @Override
  public boolean logout() {
    return userAccountService.logout();
  }

  @Override
  public boolean updatePassword(String userId, String oldPassword, String newPassword) {
    return userAccountService.updatePassword(userId, oldPassword, newPassword);
  }

  @Override
  public boolean updatePasswordByAdmin(String userId, String newPassword) {
    checkAdmin();
    return userAccountService.updatePasswordByAdmin(userId, newPassword);
  }

  @Override
  public boolean exists(String userId) {
    return userAccountService.exists(userId);
  }

  public String sendBootstrapScript(String brokerUrl, String bootstrapScript) {
    UserAccount ua = userAccountService.getCurrentUserAccount();
    if (!ua.isAdmin()) {
      List<GameSession> gss = gameSessionsTable.readListByUserId(ua.getUserId());
      gss.forEach(
          gs -> {
            // String bUrl = gs.getBrokerUrl() + "/json/GameProcessService";
            GameProcessServiceClient srv = getGameProcessService(brokerUrl);
            String pid = gs.getProcessId();
            srv.stopProcess(pid);
            log.info("Stop [{}] on [{}]", pid, brokerUrl);
          });
    }
    GameProcessServiceClient srv = getGameProcessService(brokerUrl);
    log.info("target brokerUrl={}", brokerUrl);
    log.info("target brokerService={}", srv);
    String pid = srv.createGameProcess(bootstrapScript);
    return pid;
  }

  private GameProcessServiceClient getGameProcessService(String brokerUrl) {
    checkOperator();
    ProcessServices.addBrokerUrl(brokerUrl);
    return ProcessServices.get(brokerUrl);
  }

  private void checkAdmin() {
    if (isAdmin()) {
      Object[] params = {userAccountService.getCurrentUserAccount()};
      throw new RuntimeException(
          ParameterizedStringFormatter.DEFAULT.format((String) "{} is not administrator", params));
    }
  }

  private boolean isAdmin() {
    return userAccountService.getCurrentUserAccount() != null
        && !userAccountService.getCurrentUserAccount().isAdmin();
  }

  private boolean isOperator() {
    return userAccountService.getCurrentUserAccount() != null
        && !userAccountService.getCurrentUserAccount().isOperator();
  }

  private void checkOperator() {
    if (isOperator()) {
      Object[] params = {userAccountService.getCurrentUserAccount()};
      throw new RuntimeException(
          ParameterizedStringFormatter.DEFAULT.format((String) "{} is not operator", params));
    }
  }

  private static GameInteractionServiceClient getGameInteractionServiceClient(String brokerUrl) {
    interactionServices.computeIfAbsent(brokerUrl, k -> new GameInteractionServiceClient(k));
    return interactionServices.get(brokerUrl);
  }

  @Override
  public boolean invokeMain(StartGameSessionJson json) {
    checkAdmin();
    getGameProcessService(json.getRootBrokerUrl())
        .invokeStaticMethod(json.getClassName(), "main", json.getAdditionalScript());
    return true;
  }

  @Override
  public String sendStartGameSession(StartGameSessionJson json) {
    log.debug(json);
    checkOperator();
    String additionalScript =
        (json.getAdditionalScript() == null ? "" : json.getAdditionalScript());
    return startAux(json, additionalScript);
  }

  private String startAux(final StartGameSessionJson json, final String additionalScript) {
    String brokerUrl = json.getRootBrokerUrl();
    ProcessServices.addBrokerUrl(brokerUrl);
    String script = gameScriptsTable.selectByPrimaryKey(json.getScriptId()).getScript();
    String bootstrapScript =
        (script + System.lineSeparator() + additionalScript)
            .replaceAll("\\r", "")
            .replaceAll("\\h", " ");
    String pid = sendBootstrapScript(json.getRootBrokerUrl(), bootstrapScript);
    GameSession s = new GameSession(pid, json.getRootBrokerUrl(), bootstrapScript);
    gameSessionsTable.insert(s);
    return pid;
  }

  @Override
  public String sendStartDemoGameSession(StartGameSessionJson json) {
    log.debug(json);
    String additionalScript =
        gameScriptsTable.selectByPrimaryKey(json.getScriptId()).getAdditionalScript();
    additionalScript = additionalScript == null ? "" : additionalScript;
    return startAux(json, additionalScript);
  }

  @Override
  public String resume(String processId) {
    checkOperator();
    try {
      log.info("Recieve request to resume {} ", processId);
      GameRecord record = gameRecordsTable.getLatestRecord(processId);
      if (record == null) {
        String msg = processId + " has no record.";
        log.error(msg);
        throw new RuntimeException(msg);
      }
      log.info("Record for resume is {}", record.getRecord());
      return resumeByRecord(processId, record);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String resumeFrom(String processId, int roundnum) {
    checkOperator();
    GameRecord record = gameRecordsTable.getRecordOf(processId, roundnum);
    return resumeByRecord(processId, record);
  }

  private String resumeByRecord(String processId, GameRecord record) {
    GameSession session = gameSessionsTable.selectByPrimaryKey(processId);
    if (session == null) {
      String msg = processId + " has no session.";
      log.error(msg);
      throw new RuntimeException(msg);
    }

    String script = session.getBootstrapScript() + System.lineSeparator();
    String resumeScript = record == null ? "" : record.getRecord();
    String brokerUrl =
        session.getRootBrokerUrl() != null ? session.getRootBrokerUrl() : session.getBrokerUrl();
    String conScript = script + resumeScript;
    log.info("call resume to {}", brokerUrl);
    return sendBootstrapScript(brokerUrl, conScript);
  }

  @Override
  public boolean sendAssignment(String processId, String playerName, String userId) {
    assignmentsTable.assign(processId, playerName, userId);
    getGameInteractionServiceClient(gameSessionsTable.selectByPrimaryKey(processId).getBrokerUrl())
        .sendAssignment(processId, playerName, userId);
    return true;
  }

  @Override
  public RequestToAssignOperator[] getNewAssignmentRequests(String processId) {
    List<RequestToAssignOperator> originals =
        new ArrayList<>(
            Arrays.asList(
                getGameInteractionServiceClient(
                        gameSessionsTable.selectByPrimaryKey(processId).getBrokerUrl())
                    .getNewAssignmentRequests(processId)));
    List<Assignment> assignments = assignmentsTable.readListByProcessId(processId);
    List<RequestToAssignOperator> result =
        originals.stream()
            .filter(
                o -> {
                  for (Assignment a : assignments) {
                    if (o.getOperatorId().equals(a.getUserId())) {
                      return false;
                    }
                  }
                  return true;
                })
            .collect(Collectors.toList());

    return result.toArray(new RequestToAssignOperator[0]);
  }

  @Override
  public Serializable requestToRegisterGameSession(GameSessionJson json) {
    log.info("Register game session={}", json);
    String brokerUrl = json.getBrokerUrl();
    if (!gameSessionsTable.exists(json.getProcessId())) {
      log.info("Register game session without processId");
      GameSession s =
          new GameSession(
              json.getProcessId(),
              json.getUserId(),
              json.getSessionName(),
              json.getDescription(),
              json.getRootBrokerUrl(),
              brokerUrl,
              json.getBootstrapScript());
      log.info("[INSERT] Game Session {}", s);
      gameSessionsTable.insert(s);
    } else {
      log.info("Modify game session reqiest is arrived.");
      GameSession s = gameSessionsTable.selectByPrimaryKey(json.getProcessId());
      s.setBrokerUrl(json.getBrokerUrl());
      s.setUserId(json.getUserId());
      s.setSessionName(json.getSessionName());
      s.setDescription(json.getDescription());
      log.info("[UPDATE] Game Session {}", s);
      gameSessionsTable.update(s);
    }
    ProcessServices.addBrokerUrl(brokerUrl);
    return null;
  }

  @Override
  public Serializable requestToAssign(String processId, String playerName, String operatorId) {
    if (userAccountService.exists(operatorId)) {
      assignmentsTable.assign(processId, playerName, operatorId);
      sendAssignment(processId, playerName, operatorId);
    }
    return null;
  }

  @Override
  public Serializable requestToInput(
      String processId,
      String playerName,
      long requestId,
      String label,
      int roundnum,
      InputToUI[] inputs) {
    // TODO 自動生成されたメソッド・スタブ
    return null;
  }

  @Override
  public Serializable requestToInputAndAutoResponse(
      String processId,
      String playerName,
      long requestId,
      String label,
      int roundnum,
      InputToUI[] inputs,
      int maxAutoResponseTime) {
    submitTask(
        () -> {
          log.debug("{},{}", label, inputs);
          // String l = label;
          // InputToUI[] tmp = inputs;
          getGameInteractionServiceClient(
                  gameSessionsTable.selectByPrimaryKey(processId).getBrokerUrl())
              .sendAutoInput(
                  processId,
                  playerName,
                  requestId,
                  roundnum,
                  InputToUI.toInputFromWebUIs(inputs),
                  maxAutoResponseTime);
        });
    return null;
  }

  @Override
  public Serializable showMessage(
      String processId, String playerId, long messageId, int roundnum, MessageToUI message) {
    log.debug(message);
    submitTask(
        () -> {
          messagesTable.insert(
              new Message(
                  processId,
                  roundnum,
                  playerId,
                  messageId,
                  message.getMessage(),
                  message.getAttrs()));
        });
    return null;
  }

  private void submitTask(Runnable task) {
    lazyExecutorService.submit(
        () -> {
          try {
            task.run();
          } catch (Exception e) {
            log.error(e, e);
            throw e;
          }
        });
  }

  @Override
  public boolean sendInput(
      String processId, String playerName, long requestId, int roundnum, InputFromWebUI[] inputs) {
    log.debug(inputs);
    submitTask(
        () -> {
          inputsTable.insert(
              new Input(
                  LocalDateTime.now(),
                  processId,
                  roundnum,
                  playerName,
                  requestId,
                  JacksonMapper.getDefaultMapper().toJson(inputs)));
        });
    return true;
  }

  @Override
  public Serializable notifyJoinInGame(String processId, String playerName) {
    log.info("{} is join in {}", playerName, processId);
    assignmentsTable.notifyJoinInGame(processId, playerName);
    progressesTable.insertJoinInGame(processId, playerName);
    return null;
  }

  @Override
  public Serializable notifyStartOfGame(String processId) {
    progressesTable.insertStartOfGame(processId);
    return null;
  }

  @Override
  public Serializable notifyStartOfRound(String processId, int roundnum) {
    progressesTable.insertStartOfRound(processId, roundnum);
    return null;
  }

  @Override
  public Serializable notifyEndOfRound(String processId, int roundnum) {
    progressesTable.insertEndOfRound(processId, roundnum);
    return null;
  }

  @Override
  public Serializable notifyGameRecord(String processId, int roundnum, String record) {
    gameRecordsTable.insert(new GameRecord(processId, roundnum, record));
    return null;
  }

  @Override
  public Serializable notifyEndOfGame(String processId) {
    progressesTable.insertEndOfGame(processId);
    return null;
  }

  @Override
  public int deleteScript(String scriptId) {
    checkAdmin();
    GameScript s = new GameScript();
    s.setId(scriptId);
    return gameScriptsTable.delete(s);
  }

  @Override
  public boolean deleteUser(String userId) {
    checkAdmin();
    return userAccountService.delete(userId);
  }

  @Override
  public boolean delete(String userId) {
    checkAdmin();
    return deleteUser(userId);
  }

  @Override
  public boolean updateScript(GameScriptJson json) {
    checkAdmin();
    GameScript s = gameScriptsTable.selectByPrimaryKey(json.getId());
    s.setCreatedAt(new Date());
    s.setName(json.getName());
    s.setDescription(json.getDescription());
    s.setScript(json.getScript());
    s.setAdditionalScript(json.getAdditionalScript());
    s.setClassName(json.getClassName());
    gameScriptsTable.update(s);
    return true;
  }

  @Override
  public boolean addScript(GameScriptJson json) {
    checkAdmin();
    gameScriptsTable.insert(
        new GameScript(
            json.getId(),
            "",
            json.getName(),
            json.getDescription(),
            json.getSourceUrl(),
            json.getClassName(),
            json.getAdditionalScript(),
            json.getScript()));
    return true;
  }

  @Override
  public String sendStartGameSessions(StartGameSessionsJson json) {
    checkAdmin();
    // TODO 自動生成されたメソッド・スタブ
    return null;
  }

  @Override
  public String requestToCreateGameProcesses(
      AwsServersSetting awsServersSetting, StartGameSessionsJson json) {
    // TODO 自動生成されたメソッド・スタブ
    return null;
  }

  @Override
  public boolean addBrokerUrls(String[] urls) {
    checkAdmin();
    Arrays.stream(urls).forEach(url -> ProcessServices.addBrokerUrl(url));
    return true;
  }

  @Override
  public boolean uploadUsersCsv(String base64EncodedData) {
    checkAdmin();
    log.debug("receuve csv={}", base64EncodedData);
    File usersCsvFile = saveUsersCsv(base64EncodedData);
    UserAccountsTable.readAsUserAccounts(usersCsvFile)
        .forEach(
            ua -> {
              userAccountService.merge(ua);
            });

    return true;
  }

  public File saveUsersCsv(String base64EncodedData) {
    File outputFile =
        new File(
            SystemFileUtils.getTempDirectory(), "users-" + System.currentTimeMillis() + ".csv");
    FileUtils.write(outputFile.toPath(), base64EncodedData, StandardOpenOption.CREATE);
    log.info("Output file is {}.", outputFile);
    return outputFile;
  }

  @Override
  public boolean sendLog(String errorLevel, String location, String msg, String options) {
    // TODO 自動生成されたメソッド・スタブ
    return false;
  }
}
