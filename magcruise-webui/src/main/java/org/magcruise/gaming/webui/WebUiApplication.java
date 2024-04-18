package org.magcruise.gaming.webui;

import java.io.File;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.magcruise.gaming.executor.db.GameRecordsTable;
import org.magcruise.gaming.util.SystemEnvironmentUtils;
import org.magcruise.gaming.webui.jsonrpc.ProcessServices;
import org.magcruise.gaming.webui.jsonrpc.WebUiService;
import org.magcruise.gaming.webui.model.GameSessionWithAsignment;
import org.magcruise.gaming.webui.model.GameSessionWithAsignmentAndProgress;
import org.magcruise.gaming.webui.model.GameSessionWithAsignmentsAndProgress;
import org.magcruise.gaming.webui.relation.AssignmentsTable;
import org.magcruise.gaming.webui.relation.GameScriptsTable;
import org.magcruise.gaming.webui.relation.GameSessionsTable;
import org.magcruise.gaming.webui.relation.InputsTable;
import org.magcruise.gaming.webui.relation.MessagesTable;
import org.magcruise.gaming.webui.relation.ProgressesTable;
import org.magcruise.gaming.webui.row.Assignment;
import org.magcruise.gaming.webui.row.GameScript;
import org.magcruise.gaming.webui.row.GameSession;
import org.magcruise.gaming.webui.row.Progress;
import org.nkjmlab.sorm4j.internal.util.Try;
import org.nkjmlab.sorm4j.util.h2.datasource.H2DataSourceFactory;
import org.nkjmlab.sorm4j.util.h2.server.H2TcpServerProcess;
import org.nkjmlab.sorm4j.util.h2.server.H2TcpServerProperties;
import org.nkjmlab.util.jackson.JacksonMapper;
import org.nkjmlab.util.jakarta.servlet.UserSession;
import org.nkjmlab.util.jakarta.servlet.jsonrpc.JsonRpcServletService;
import org.nkjmlab.util.java.collections.IteratorUtils;
import org.nkjmlab.util.java.json.JsonMapper;
import org.nkjmlab.util.java.lang.ProcessUtils;
import org.nkjmlab.util.java.lang.PropertiesUtils;
import org.nkjmlab.util.java.lang.ResourceUtils;
import org.nkjmlab.util.java.net.UrlUtils;
import org.nkjmlab.util.java.web.ViewModel;
import org.nkjmlab.util.java.web.WebApplicationConfig;
import org.nkjmlab.util.jsonrpc.JsonRpcResponse;
import org.nkjmlab.util.thymeleaf.ThymeleafTemplateEnginBuilder;
import org.nkjmlab.webui.service.user.UserAccount;
import org.nkjmlab.webui.service.user.UserAccountService;
import org.nkjmlab.webui.service.user.UserAccountsTable;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import jakarta.servlet.http.HttpServletRequest;

public class WebUiApplication {

  public static final String X_FORWARDED_PROTO = "X-FORWARDED-PROTO";
  public static final String X_FORWARDED_HOST = "X-FORWARDED-HOST";

  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  private static final long THYMELEAF_EXPIRE_TIME_MILLI_SECOND = 12;

  private static final String[] noAuthPaths = {"index.html", "login.html", "signup.html"};

  private final GameScriptsTable scriptsTable;
  private final UserAccountsTable userAccountsTable;
  private final AssignmentsTable assignmentsTable;
  private final ProgressesTable progressesTable;
  private final GameSessionsTable gameSessionsTable;
  private final MessagesTable messagesTable;
  private final InputsTable inputsTable;
  private final GameRecordsTable gameRecordsTable;

  private final H2DataSourceFactory dataSourceFactory;
  private final DataSource dataSource;

  private static JsonMapper mapper;
  private final Javalin app;

  private static int port = 6789;

  private static final WebApplicationConfig WEB_APP_CONFIG =
      WebApplicationConfig.builder()
          .addWebJar(
              "jquery",
              "sweetalert2",
              "bootstrap",
              "datatables.net",
              "datatables.net-bs5",
              "fortawesome__fontawesome-free",
              "stacktrace-js",
              "jssha",
              "sprintf-js",
              "ua-parser-js")
          .build();

  static {
    new H2TcpServerProcess(H2TcpServerProperties.builder().build()).awaitStart();
  }

  public static void main(String[] args) {
    ProcessUtils.stopProcessBindingPortIfExists(port);
    new WebUiApplication(port).init();
  }

  private static final File APP_ROOT_DIR = ResourceUtils.getResourceAsFile("/");
  private static final String WEBROOT_DIR_NAME = "/webroot";
  private static final File WEBROOT_DIR = new File(APP_ROOT_DIR, WEBROOT_DIR_NAME);

  public WebUiApplication(int port) {

    this.app =
        Javalin.create(
            config -> {
              config.staticFiles.add(WEBROOT_DIR_NAME, Location.CLASSPATH);
              config.staticFiles.enableWebjars();
              config.http.generateEtags = true;
              config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
              config.fileRenderer(
                  new JavalinThymeleaf(
                      ThymeleafTemplateEnginBuilder.builder()
                          .setTtlMs(THYMELEAF_EXPIRE_TIME_MILLI_SECOND)
                          .build()));
            });

    ObjectMapper m = new ObjectMapper();
    m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper = JacksonMapper.create(m);

    this.dataSourceFactory =
        JacksonMapper.getDefaultMapper()
            .toObject(
                ResourceUtils.getResourceAsFile("/conf/h2.conf"), H2DataSourceFactory.Builder.class)
            .build();

    dataSourceFactory.makeFileDatabaseIfNotExists();

    log.info("web ui jdbcURL={}", dataSourceFactory.getServerModeJdbcUrl());

    this.dataSource = dataSourceFactory.createServerModeDataSource();

    this.scriptsTable = new GameScriptsTable(dataSource);
    // scriptsTable.dropTableIfExists();
    scriptsTable.createTableIfNotExists().createIndexesIfNotExists();
    scriptsTable.readAndMerge(new File(APP_ROOT_DIR, "scripts"));

    this.userAccountsTable = new UserAccountsTable(dataSource);
    // table.dropTableIfExists();
    userAccountsTable.createTableIfNotExists().createIndexesIfNotExists();
    userAccountsTable.readAndMerge(new File(APP_ROOT_DIR, "conf/userlist.csv"));

    this.assignmentsTable = new AssignmentsTable(dataSource);
    // assignmentsTable.dropTableIfExists();
    assignmentsTable.createTableIfNotExists().createIndexesIfNotExists();

    this.progressesTable = new ProgressesTable(dataSource);
    // progressesTable.dropTableIfExists();
    progressesTable.createTableIfNotExists().createIndexesIfNotExists();

    this.gameSessionsTable = new GameSessionsTable(dataSource);
    // gameSessionsTable.dropTableIfExists();
    gameSessionsTable.createTableIfNotExists().createIndexesIfNotExists();

    this.messagesTable = new MessagesTable(dataSource);
    // messagesTable.dropTableIfExists();
    messagesTable.createTableIfNotExists().createIndexesIfNotExists();

    this.inputsTable = new InputsTable(dataSource);
    // inputsTable.dropTableIfExists();
    inputsTable.createTableIfNotExists().createIndexesIfNotExists();

    this.gameRecordsTable = new GameRecordsTable(dataSource);
    // messagesTable.dropTableIfExists();
    gameRecordsTable.createTableIfNotExists().createIndexesIfNotExists();
  }

  public void init() {
    JsonRpcServletService jsonRpcService = new JsonRpcServletService(mapper);

    app.exception(
        Exception.class,
        (e, ctx) -> {
          log.error(e, e);
        });

    app.ws(
        "/websocket/checkcon",
        ws -> {
          ws.onConnect(
              ctx -> {
                log.debug("{}", ctx.session.getUpgradeRequest().getRequestURI());
              });
        });

    app.get(
        "/app",
        ctx -> {
          ctx.redirect("/app/index.html");
        });

    app.post(
        "/app/json/WebUiService",
        ctx -> {
          // log.debug("orig requestUrl={}", ctx.url());
          // log.debug("{}", ctx.headerMap());

          try {
            UserAccountService ua = new UserAccountService(userAccountsTable, ctx.req());
            JsonRpcResponse jres =
                jsonRpcService
                    .callHttpJsonRpc(
                        new WebUiService(
                            ua,
                            scriptsTable,
                            assignmentsTable,
                            gameSessionsTable,
                            messagesTable,
                            inputsTable,
                            progressesTable,
                            gameRecordsTable),
                        ctx.req(),
                        ctx.res())
                    .getJsonRpcResponse();

            String ret = mapper.toJson(jres);
            // ctx.result(ret).contentType("application/json")
            ctx.result(ret);
          } catch (Exception e) {
            log.error(e, e);
            Try.rethrow(e);
          }
        });

    app.get(
        "/app/<pageName>",
        ctx -> {
          try {
            HttpServletRequest req = ctx.req();
            String pathInfo =
                ctx.pathParam("pageName") == null ? "login.html" : ctx.pathParam("pageName");

            if (pathInfo == null
                || !containsNoAuthPathElements(noAuthPaths, pathInfo)
                    && !UserSession.wrap(req.getSession()).isLogined()) {
              log.debug(
                  "{},{},{},{}",
                  req.getSession().getId(),
                  IteratorUtils.toList(req.getSession().getAttributeNames().asIterator()),
                  req.getAttribute("userId"),
                  req.getAttribute("USER_ID"));
              ctx.redirect("login.html");
              return;
            }

            Map<String, String[]> params = req.getParameterMap();
            ViewModel model = getViewModel(ctx, req, pathInfo, params);
            ctx.render(model.getFilePath(), model);
          } catch (Throwable e) {
            log.error(e, e);
            ctx.redirect("/app/index.html");
          }
        });
    app.start(port);
    log.info("start");
  }

  private ViewModel getViewModel(
      Context ctx, HttpServletRequest req, String pathInfo, Map<String, String[]> params) {
    switch (pathInfo) {
      case "monitor.html":
        return createMonitorPage(req, pathInfo, params);
      case "game-scripts-demo.html":
        return createGameScriptsDemoPage(req, pathInfo, params);
      case "game-scripts.html":
        return createGameScriptsPage(ctx, req, pathInfo, params);
      case "active-sessions.html":
      case "active-sessions-demo.html":
        return createActiveSessionsPage(req, pathInfo, params);
      case "all-sessions.html":
        return createAllSessionsPage(req, pathInfo, params);
      case "open-sessions.html":
        return createOpenSessionsPage(req, pathInfo, params);
      case "local-sessions.html":
        return createLocalSessionsPage(req, pathInfo, params);
      case "participation-records.html":
        return createParticipationRecordsPage(req, pathInfo, params);
      case "session-record.html":
        return createSessionRecordPage(req, pathInfo, params);
      case "participation-record.html":
        return createParticipationRecordLogPage(req, pathInfo, params);
      case "play.html":
        {
          return createPlayPage(ctx, pathInfo, params);
        }
      case "users.html":
        {
          ViewModel.Builder model = newViewModelBuilder(req, pathInfo);
          model.put("users", userAccountsTable.selectAll());
          return model.build();
        }
      case "user-settings.html":
        {
          String userId = params.get("userId")[0];
          ViewModel.Builder model = newViewModelBuilder(req, pathInfo);
          model.put("user", userAccountsTable.selectByPrimaryKey(userId));
          return model.build();
        }
      case "":
      case "login.html":
      case "index.html":
        return newViewModelBuilder(req, "login.html").build();
      case "signup.html":
        return newViewModelBuilder(req, "signup.html").build();
      default:
        if (getCurrentUserSession(req).isLogined()) {
          ViewModel.Builder model = newViewModelBuilder(req, pathInfo);
          return model.build();
        }
        return newViewModelBuilder(req, pathInfo).build();
    }
  }

  private String getCurrentUserId(HttpServletRequest request) {
    return getCurrentUserSession(request).getUserId().orElse("");
  }

  private UserSession getCurrentUserSession(HttpServletRequest request) {
    return UserSession.wrap(request.getSession());
  }

  private boolean containsNoAuthPathElements(String[] noAuthPaths, String pathInfo) {
    for (String noAuthPath : noAuthPaths) {
      if (pathInfo.contains(noAuthPath)) {
        return true;
      }
    }
    return false;
  }

  private ViewModel createMonitorPage(
      HttpServletRequest request, String pathInfo, Map<String, String[]> params) {
    ViewModel.Builder model = newViewModelBuilder(request, pathInfo);
    String processId = params.get("processId")[0];
    model.put("brokerUrl", gameSessionsTable.selectByPrimaryKey(processId).getBrokerUrl());
    model.put("processId", processId);
    model.put(
        "usersList",
        userAccountsTable.selectAll().stream()
            .map(ua -> ua.getUserId())
            .collect(Collectors.toList()));

    GameSession session =
        findActiveSessionsByProcessId(processId, gameSessionsTable)
            .orElseGet(
                () -> {
                  log.info(processId + " is not found as active session on broker");
                  return gameSessionsTable.selectByPrimaryKey(processId);
                });
    if (session == null) {
      throw new IllegalArgumentException(processId + " is not found");
    }

    model.put("sessionName", session.getSessionName());

    return model.build();
  }

  private ViewModel createGameScriptsPage(
      Context ctx, HttpServletRequest request, String pathInfo, Map<String, String[]> params) {
    ViewModel.Builder model = newViewModelBuilder(request, pathInfo);
    model.put("gameScripts", scriptsTable.selectAll());
    model.put(
        "origGameScriptsMap",
        scriptsTable.selectAll().stream()
            .collect(Collectors.toMap(script -> script.getId(), script -> script)));
    model.put(
        "gameScriptsMap",
        scriptsTable.selectAll().stream()
            .collect(
                Collectors.toMap(
                    script -> script.getId(),
                    script -> {
                      script.setAdditionalScript(
                          replacePlaceholder(script.getAdditionalScript(), request, ctx));
                      script.setScript(replacePlaceholder(script.getScript(), request, ctx));
                      return script;
                    })));

    model.put("rootBrokerUrl", getCurrentBrokerHost(ctx) + "/app");
    return model.build();
  }

  private String getForwardedHost(Context ctx) {
    return getProtocol(ctx) + "://" + getHost(ctx);
  }

  private String getHost(Context ctx) {
    return ctx.header(X_FORWARDED_HOST) != null ? ctx.header(X_FORWARDED_HOST) : (ctx.host());
  }

  private String getProtocol(Context ctx) {
    return ctx.header(X_FORWARDED_PROTO) != null && ctx.header(X_FORWARDED_PROTO).equals("https")
        ? "https"
        : "http";
  }

  private String replacePlaceholder(String text, HttpServletRequest request, Context ctx) {
    UserAccount ua = getCurrentUserAccount(request);
    return text.replace("{currentUserId}", ua.getUserId())
        .replace("{currentWebUiUrl}", getForwardedHost(ctx))
        .replace(
            "{currentBrokerUrl}",
            getForwardedHost(ctx).replace("webui", "broker").replace(":6789", ":5678"));
  }

  private String getCurrentBrokerHost(Context ctx) {
    String host = getHost(ctx);
    // host include port like "localhost:5678"
    if (host.contains("localhost")) {
      URL reqUrl = UrlUtils.of(ctx.req().getRequestURL().toString());
      return reqUrl.getProtocol() + "://" + reqUrl.getHost() + ":5678";
    } else {
      return "https://" + host.replace("webui", "broker");
    }
  }

  private ViewModel createGameScriptsDemoPage(
      HttpServletRequest request, String pathInfo, Map<String, String[]> params) {
    List<GameScript> scripts =
        scriptsTable.selectAll().stream()
            .filter(s -> s.getUserId().equals("demo"))
            .collect(Collectors.toList());
    ViewModel.Builder model = newViewModelBuilder(request, pathInfo);
    model.put("gameScripts", scripts);
    model.put(
        "gameScriptsMap",
        scripts.stream().collect(Collectors.toMap(script -> script.getId(), script -> script)));
    model.put("rootBrokerUrl", getDefaultRootBrokerUrl());
    return model.build();
  }

  private static String getDefaultRootBrokerUrl() {
    return SystemEnvironmentUtils.getDefaultPublicBrokerUrl();
  }

  private ViewModel createActiveSessionsPage(
      HttpServletRequest request, String pathInfo, Map<String, String[]> params) {
    List<GameSession> sessions =
        getCurrentUserAccount(request).isAdmin()
            ? ProcessServices.getActiveGameProcesses(gameSessionsTable)
            : findActiveSessionsByOwnerUserId(getCurrentUserId(request), gameSessionsTable);

    List<GameSessionWithAsignmentsAndProgress> sessionItems =
        sessions.stream()
            .map(
                s -> {
                  try {
                    List<Assignment> assignments =
                        assignmentsTable.readListByProcessId(s.getProcessId());
                    Progress progress = progressesTable.readLatest(s.getProcessId());
                    return new GameSessionWithAsignmentsAndProgress(s, assignments, progress);
                  } catch (Exception e) {
                    log.warn(e, e);
                    return null;
                  }
                })
            .filter(e -> Objects.nonNull(e))
            .distinct()
            .collect(Collectors.toList());

    ViewModel.Builder model = newViewModelBuilder(request, pathInfo);
    model.put("brokerUrls", ProcessServices.getWatchingBrokerUrls());
    model.put("gameSessions", sessionItems);
    return model.build();
  }

  private ViewModel createAllSessionsPage(
      HttpServletRequest request, String pathInfo, Map<String, String[]> params) {
    List<GameSession> sessions =
        gameSessionsTable.selectListAllEqual(GameSessionsTable.USER_ID, getCurrentUserId(request));
    List<GameSessionWithAsignmentsAndProgress> sessionItems =
        sessions.stream()
            .sorted(Comparator.comparing(s -> s.getProcessId(), Comparator.reverseOrder()))
            .limit(500)
            .map(
                s -> {
                  try {
                    List<Assignment> assignments =
                        assignmentsTable.readListByProcessId(s.getProcessId());
                    Progress progress = progressesTable.readLatest(s.getProcessId());
                    modifyProgoress(progress);
                    return new GameSessionWithAsignmentsAndProgress(s, assignments, progress);
                  } catch (Exception e) {
                    log.warn(e, e);
                    return null;
                  }
                })
            .filter(e -> Objects.nonNull(e))
            .collect(Collectors.toList());

    ViewModel.Builder model = newViewModelBuilder(request, pathInfo);
    model.put("gameSessions", sessionItems);
    return model.build();
  }

  private void modifyProgoress(Progress progress) {
    if (progress.getStatus().contains("join") || progress.getStatus().contains("entry")) {
      List<String> unjoined =
          assignmentsTable.findUnjoinedAssignment(progress.getProcessId()).stream()
              .map(a -> a.getPlayerName() + " (" + a.getUserId() + ")")
              .collect(Collectors.toList());
      if (unjoined.size() != 0) {
        progress.setStatus("Waiting for entry of " + String.join(" and ", unjoined) + ".");
      }
    }
  }

  private ViewModel createOpenSessionsPage(
      HttpServletRequest request, String pathInfo, Map<String, String[]> params) {
    List<GameSession> sessions =
        findActiveSessionsByUserId(getCurrentUserId(request), assignmentsTable, gameSessionsTable);
    return createSessionsPage(request, pathInfo, params, sessions);
  }

  private ViewModel createLocalSessionsPage(
      HttpServletRequest request, String pathInfo, Map<String, String[]> params) {
    List<GameSession> sessions =
        findAssingedSessionsByUserId(
            getCurrentUserId(request), assignmentsTable, gameSessionsTable);
    return createSessionsPage(request, pathInfo, params, sessions);
  }

  private ViewModel createSessionsPage(
      HttpServletRequest request,
      String pathInfo,
      Map<String, String[]> params,
      List<GameSession> sessions) {
    ViewModel.Builder model = newViewModelBuilder(request, pathInfo);
    model.put(
        "gameSessionsWithAssignmentsAndProgresses",
        createGameSessionsWithAssignmentsAndProgresses(request, sessions));
    return model.build();
  }

  private List<GameSessionWithAsignmentAndProgress> createGameSessionsWithAssignmentsAndProgresses(
      HttpServletRequest request, List<GameSession> sessions) {
    List<GameSessionWithAsignmentAndProgress> sessionItems =
        sessions.stream()
            .filter(
                s -> {
                  return Objects.nonNull(s.getProcessId());
                })
            .map(
                s -> {
                  try {
                    Assignment assignment =
                        assignmentsTable.readByProcessIdAndUserId(
                            s.getProcessId(), getCurrentUserId(request));
                    log.debug(
                        "Search Assignment={},{}", s.getProcessId(), getCurrentUserId(request));
                    Progress progress = progressesTable.readLatest(s.getProcessId());
                    modifyProgoress(progress);
                    return new GameSessionWithAsignmentAndProgress(s, assignment, progress);
                  } catch (Exception e) {
                    log.warn(e, e);
                    return null;
                  }
                })
            .filter(e -> Objects.nonNull(e))
            .collect(Collectors.toList());
    return sessionItems;
  }

  private ViewModel createParticipationRecordsPage(
      HttpServletRequest request, String pathInfo, Map<String, String[]> params) {
    List<GameSession> sessions =
        assignmentsTable.findByUserId(getCurrentUserId(request)).stream()
            .map(a -> gameSessionsTable.selectByPrimaryKey(a.getProcessId()))
            .filter(s -> Objects.nonNull(s))
            .collect(Collectors.toList());
    ViewModel.Builder model = newViewModelBuilder(request, pathInfo);
    List<GameSessionWithAsignmentAndProgress> items =
        createGameSessionsWithAssignmentsAndProgresses(request, sessions);
    model.put(
        "gameSessionsWithAssignmentsAndProgresses",
        items.stream()
            .filter(i -> i.getProgress().getRoundnum() > -1)
            .collect(Collectors.toList()));
    return model.build();
  }

  private ViewModel createSessionRecordPage(
      HttpServletRequest request, String pathInfo, Map<String, String[]> params) {
    String processId = params.get("processId")[0];
    List<GameSessionWithAsignment> sessionItems =
        assignmentsTable.selectListAllEqual(AssignmentsTable.PROCESS_ID, processId).stream()
            .map(
                a ->
                    new GameSessionWithAsignment(
                        gameSessionsTable.selectByPrimaryKey(a.getProcessId()), a))
            .collect(Collectors.toList());

    ViewModel.Builder model = newViewModelBuilder(request, pathInfo);
    model.put("gameSessionsWithAssignments", sessionItems);
    model.put(
        "gameRecords",
        gameRecordsTable.selectListAllEqual(GameRecordsTable.Column.PROCESS_ID, processId));
    return model.build();
  }

  private ViewModel createParticipationRecordLogPage(
      HttpServletRequest request, String pathInfo, Map<String, String[]> params) {
    ViewModel.Builder model = newViewModelBuilder(request, pathInfo);
    String pid = "processId";
    String pname = "playerName";
    String processId = params.get(pid)[0];
    String playerName = params.get(pname)[0];
    model.put(
        "messages",
        messagesTable.selectListAllEqual(
            MessagesTable.PROCESS_ID, processId, MessagesTable.PLAYER_NAME, playerName));

    model.put(
        "inputs",
        inputsTable.selectListAllEqual(
            InputsTable.PROCESS_ID, processId, InputsTable.PLAYER_NAME, playerName));

    return model.build();
  }

  private ViewModel createPlayPage(Context ctx, String pathInfo, Map<String, String[]> params) {
    HttpServletRequest request = ctx.req();
    String pid = "processId";
    String pname = "playerName";
    String processId = params.get(pid)[0];
    String playerName = params.get(pname)[0];

    if (!getCurrentUserAccount(request).isAdmin()
        && !gameSessionsTable
            .selectByPrimaryKey(processId)
            .getUserId()
            .equals(getCurrentUserId(request))
        && !assignmentsTable
            .readByProcessIdAndPlayerName(processId, playerName)
            .getUserId()
            .equals(getCurrentUserId(request))) {
      ctx.redirect("login.html");
      return newViewModelBuilder(request, pathInfo).build();
    }

    ViewModel.Builder model = newViewModelBuilder(request, pathInfo);
    model.put(pid, processId);
    model.put(pname, playerName);

    GameSession session =
        findActiveSessionsByProcessId(processId, gameSessionsTable)
            .orElseGet(
                () -> {
                  log.debug(
                      "{} is not found as active session on broker. It may be a process on a local pc.",
                      processId);
                  return gameSessionsTable.selectByPrimaryKey(processId);
                });
    if (session == null) {
      throw new IllegalArgumentException(processId + " is not found anywhere");
    }

    model.put("interactionServiceUrl", session.toGameInteractionServiceUrl());
    model.put("websocketUrl", session.toWebsocketUrl(playerName));
    model.put("sessionName", session.getSessionName());

    return model.build();
  }

  private UserAccount getCurrentUserAccount(HttpServletRequest request) {
    return userAccountsTable.selectByPrimaryKey(getCurrentUserId(request));
  }

  private static final Properties serverAuth = readServerAuth();

  private static Properties readServerAuth() {
    try {
      return PropertiesUtils.load("/conf/server-auth.properties");
    } catch (Exception e) {
      log.error(e, e);
      throw Try.rethrow(e);
    }
  }

  private final String basicAuthUserId = serverAuth.getProperty("basicAuthUserId");
  private final String basicAuthPassword = serverAuth.getProperty("basicAuthPassword");

  private ViewModel.Builder newViewModelBuilder(HttpServletRequest request, String filePath) {
    ViewModel.Builder modelBuilder =
        ViewModel.builder()
            .setFilePath(filePath)
            .setFileModifiedDate(WEBROOT_DIR.toPath(), 10, "js", "css")
            .put("webjars", WEB_APP_CONFIG.getWebJars());
    modelBuilder.put("currentUser", getCurrentUserAccount(request));
    modelBuilder.put("basicAuthUserId", basicAuthUserId);
    modelBuilder.put("basicAuthPassword", basicAuthPassword);
    return modelBuilder;
  }

  private static List<GameSession> findActiveSessionsByOwnerUserId(
      String userId, GameSessionsTable gameSessionsTable) {
    List<GameSession> result =
        ProcessServices.getActiveGameProcesses(gameSessionsTable).stream()
            .filter(
                session -> {
                  boolean b =
                      session.getUserId() == null ? false : session.getUserId().equals(userId);
                  return b;
                })
            .collect(Collectors.toList());
    return result;
  }

  private static Optional<GameSession> findActiveSessionsByProcessId(
      String processId, GameSessionsTable gameSessionsTable) {
    return ProcessServices.getActiveGameProcesses(gameSessionsTable).stream()
        .filter(session -> session.getProcessId().equals(processId))
        .findFirst();
  }

  private static List<GameSession> findActiveSessionsByUserId(
      String userId, AssignmentsTable assignmentsTable, GameSessionsTable gameSessionsTable) {
    List<GameSession> assignedSessions =
        findAssingedSessionsByUserId(userId, assignmentsTable, gameSessionsTable);
    Set<String> pids = ProcessServices.getActiveGameProcessId(gameSessionsTable);
    List<GameSession> result =
        assignedSessions.stream()
            .filter(s -> pids.contains(s.getProcessId()))
            .collect(Collectors.toList());
    return result;
  }

  private static List<GameSession> findAssingedSessionsByUserId(
      String userId, AssignmentsTable assignmentsTable, GameSessionsTable gameSessionsTable) {
    List<GameSession> l1 =
        assignmentsTable.findByUserId(userId).stream()
            .map(a -> gameSessionsTable.selectByPrimaryKey(a.getProcessId()))
            .filter(a -> Objects.nonNull(a))
            .collect(Collectors.toList());
    return l1;
  }
}
