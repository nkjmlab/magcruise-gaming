package org.magcruise.broker;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.h2.jdbcx.JdbcConnectionPool;
import org.magcruise.broker.jsonrpc.GameInteractionController;
import org.magcruise.broker.jsonrpc.GameProcessController;
import org.magcruise.broker.websocket.RequestToGameExecutorManagerEndpoint;
import org.magcruise.broker.websocket.RequestToUIManagerEndpoint;
import org.magcruise.gaming.executor.db.GameRecordsTable;
import org.magcruise.gaming.manager.process.ExternalGameProcessManager;
import org.nkjmlab.sorm4j.internal.util.Try;
import org.nkjmlab.sorm4j.util.h2.datasource.H2DataSourceFactory;
import org.nkjmlab.sorm4j.util.h2.server.H2TcpServerProcess;
import org.nkjmlab.sorm4j.util.h2.server.H2TcpServerProperties;
import org.nkjmlab.util.jackson.JacksonMapper;
import org.nkjmlab.util.jakarta.servlet.jsonrpc.JsonRpcServletService;
import org.nkjmlab.util.java.concurrent.BasicThreadFactory;
import org.nkjmlab.util.java.json.JsonMapper;
import org.nkjmlab.util.java.lang.ProcessUtils;
import org.nkjmlab.util.java.lang.PropertiesUtils;
import org.nkjmlab.util.java.lang.ResourceUtils;
import org.nkjmlab.util.java.math.Tuple;
import org.nkjmlab.util.java.net.UrlUtils;
import org.nkjmlab.util.jsonrpc.JsonRpcRequest;
import org.nkjmlab.util.jsonrpc.JsonRpcResponse;
import org.nkjmlab.util.thymeleaf.ThymeleafTemplateEnginBuilder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;

public class BrokerApplication {

  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();
  private static final int DEFAULT_GAME_PROCESS_TTL_MINITUES = 60 * 60 * 60;

  private static final Properties serverAuth = readServerAuth();

  private static Properties readServerAuth() {
    try {
      return PropertiesUtils.load("/conf/server-auth.properties");
    } catch (Exception e) {
      log.error(e, e);
      throw Try.rethrow(e);
    }
  }

  private static int port = 5678;

  private static final Set<String> localIpAddrs = Set.of("127.0.0.1", "[0:0:0:0:0:0:0:1]");

  private static final Set<String> safeIpAddrs = getSafeIpAddrs();

  private static final Set<String> getSafeIpAddrs() {
    List<String> addrs =
        Stream.of(serverAuth.getProperty("safeIpAddrs").split(",")).map(s -> s.trim()).toList();
    return Stream.concat(addrs.stream(), localIpAddrs.stream()).collect(Collectors.toSet());
  }

  private H2DataSourceFactory dataSourceFactory;

  private JdbcConnectionPool dataSource;
  protected static final ScheduledExecutorService scheduleTasksService =
      Executors.newSingleThreadScheduledExecutor(
          BasicThreadFactory.builder("scheduled-tasks", false).build());

  private static JsonMapper mapper = createDefaultMapper();

  private static JsonMapper createDefaultMapper() {
    ObjectMapper m = new ObjectMapper();
    m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    JacksonMapper mapper = JacksonMapper.create(m);
    return mapper;
  }

  static {
    new H2TcpServerProcess(H2TcpServerProperties.builder().build()).awaitStart();
  }

  private static final File APP_ROOT_DIR = ResourceUtils.getResourceAsFile("/");
  private static long THYMELEAF_EXPIRE_TIME_MILLI_SECOND = 1 * 1000;
  private static final String WEBROOT_DIR_NAME = "/";
  private Javalin app;

  private final String basicAuthUserId = serverAuth.getProperty("basicAuthUserId");
  private final String basicAuthPassword = serverAuth.getProperty("basicAuthPassword");

  private void prepareJavalin() {

    this.app =
        Javalin.create(
            config -> {
              config.staticFiles.add(WEBROOT_DIR_NAME, Location.CLASSPATH);
              config.http.generateEtags = true;
              config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
              config.fileRenderer(
                  new JavalinThymeleaf(
                      ThymeleafTemplateEnginBuilder.builder()
                          .setTtlMs(THYMELEAF_EXPIRE_TIME_MILLI_SECOND)
                          .build()));
            });
  }

  public static void main(String[] args) {
    ProcessUtils.stopProcessBindingPortIfExists(port);
    new BrokerApplication(port).init();
  }

  public BrokerApplication(int port) {
    log.info("appRootDir={}", WEBROOT_DIR_NAME);
    this.dataSourceFactory =
        JacksonMapper.getDefaultMapper()
            .toObject(
                ResourceUtils.getResourceAsFile("/conf/h2.conf"), H2DataSourceFactory.Builder.class)
            .build();

    dataSourceFactory.makeFileDatabaseIfNotExists();

    this.dataSource = dataSourceFactory.createServerModeDataSource();
    prepareJavalin();
  }

  public void init() {

    new GameRecordsTable(dataSource).createTableIfNotExists();
    setupCleanUpProcess(DEFAULT_GAME_PROCESS_TTL_MINITUES);

    app.ws(
        "/app/websocket/requestToUI",
        ws -> {
          RequestToUIManagerEndpoint endpoint = new RequestToUIManagerEndpoint();
          ws.onConnect(ctx -> endpoint.onConnect(ctx.session));
          ws.onClose(ctx -> endpoint.onClose(ctx.session, ctx.status(), ctx.reason()));
          ws.onError(ctx -> endpoint.onError(ctx.session, ctx.error()));
        });

    app.ws(
        "/app/websocket/requestToGameExecutor",
        ws -> {
          RequestToGameExecutorManagerEndpoint endpoint =
              new RequestToGameExecutorManagerEndpoint();
          ws.onConnect(ctx -> endpoint.onConnect(ctx.session));
          ws.onClose(ctx -> endpoint.onClose(ctx.session, ctx.status(), ctx.reason()));
          ws.onError(ctx -> endpoint.onError(ctx.session, ctx.error()));
        });

    app.get("/app/json/GameInteractionService", ctx -> ctx.render("index.html"));
    app.get("/app/json/GameProcessService", ctx -> ctx.render("index.html"));

    app.before(
        "/app/json/GameProcessService",
        ctx -> {
          ctx.res().setStatus(200);
        });

    JsonRpcServletService jsonRpcService = new JsonRpcServletService(mapper);

    app.post(
        "/app/json/GameProcessService",
        ctx -> {
          // log.debug("receive message to GameProcessService.");
          JsonRpcRequest jreq = jsonRpcService.toJsonRpcRequest(ctx.req());
          log.info(
              "Call [{}] of GameProcessService, ctx={{}}", jreq.getMethod(), contextToString(ctx));

          if (!beforeCheck(ctx)) {
            return;
          }

          JsonRpcResponse jres =
              jsonRpcService
                  .callHttpJsonRpc(
                      new GameProcessController(APP_ROOT_DIR, getBrokerUrl(ctx)), jreq, ctx.res())
                  .getJsonRpcResponse();
          String ret = mapper.toJson(jres);
          // ctx.result(ret).contentType("application/json");
          ctx.result(ret);
        });

    GameInteractionController controller = new GameInteractionController();
    app.post(
        "/app/json/GameInteractionService",
        ctx -> {
          JsonRpcRequest jreq = jsonRpcService.toJsonRpcRequest(ctx.req());
          log.debug("Call [{}] of GameInteractionService", jreq.getMethod());
          JsonRpcResponse jres =
              jsonRpcService.callHttpJsonRpc(controller, jreq, ctx.res()).getJsonRpcResponse();
          String ret = mapper.toJson(jres);
          ctx.result(ret);
        });
    app.start(port);
  }

  public static final String X_FORWARDED_PROTO = "X-FORWARDED-PROTO";
  public static final String X_FORWARDED_HOST = "X-FORWARDED-HOST";

  private String getBrokerUrl(Context ctx) {
    return getCurrentBrokerHost(ctx) + "/app";
  }

  private String getHost(Context ctx) {
    return ctx.header(X_FORWARDED_HOST) != null ? ctx.header(X_FORWARDED_HOST) : (ctx.host());
  }

  private String getCurrentBrokerHost(Context ctx) {
    String host = getHost(ctx);
    // host include port like "localhost:5678"
    if (host.contains("localhost")) {
      URL reqUrl = UrlUtils.of(ctx.req().getRequestURL().toString());
      return reqUrl.getProtocol() + "://" + reqUrl.getHost() + ":" + reqUrl.getPort();
    } else {
      return "https://" + host.replace("webui", "broker");
    }
  }

  private String contextToString(Context ctx) {
    return List.of(
            Tuple.of("headerMap", ctx.headerMap()),
            Tuple.of("contextPath", ctx.contextPath()),
            Tuple.of("fullUrl", ctx.fullUrl()),
            Tuple.of("url", ctx.url()),
            Tuple.of("host", ctx.host()),
            Tuple.of("ip", ctx.ip()),
            Tuple.of("method", ctx.method()),
            Tuple.of("port", ctx.port()),
            Tuple.of("protocol", ctx.protocol()),
            Tuple.of("queryString", ctx.queryString()),
            Tuple.of("schema", ctx.scheme()))
        .toString()
        .toString();
  }

  private boolean beforeCheck(Context ctx) {
    if (ctx.header("X-Forwarded-For") == null && localIpAddrs.contains(ctx.ip())) {
      log.debug("access from local host = {}", ctx.ip());
      return true;
    } else if (ctx.header("X-Forwarded-For") != null
        && safeIpAddrs.contains(ctx.header("X-Forwarded-For"))) {
      log.debug("access from safe host = {}", ctx.ip());
      return true;
    } else {
      log.warn(
          "not safe ip. ctx.ip={}, X-Forwarded-For={}. so will be check auth.",
          ctx.ip(),
          ctx.header("X-Forwarded-For"));
    }

    String reqAuth = "" + ctx.header("Authorization");
    String basicAuth =
        Base64.getEncoder()
            .encodeToString(
                (basicAuthUserId + ":" + basicAuthPassword).getBytes(StandardCharsets.UTF_8));
    if (!reqAuth.replaceAll("Basic\\s", "").equals(basicAuth)) {
      log.warn(
          "Basic Authorization is failed! excpected={},real={}",
          basicAuth,
          reqAuth.replaceAll("Basic\\s", ""));
      ctx.header("WWW-Authenticate", "Basic realm=\"Secret Zone\"\r\n");
      ctx.status(401);
      return false;
    } else {
      log.info("Basic auth is ok");
      return true;
    }
  }

  private void setupCleanUpProcess(int ttlMinutes) {
    log.info("set TTL(MINUTES) {} and schedule to checkIdleProcess.", ttlMinutes);
    scheduleTasksService.scheduleWithFixedDelay(
        () -> {
          log.info("check idle process....");
          ExternalGameProcessManager.getInstance().cleanUpIdleProcess(ttlMinutes);
          log.info(
              "check num of active process is {}",
              ExternalGameProcessManager.getInstance().listActiveProcesses().size());
        },
        1,
        1,
        TimeUnit.MINUTES);
  }
}
