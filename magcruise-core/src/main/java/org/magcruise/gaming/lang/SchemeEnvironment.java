package org.magcruise.gaming.lang;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.SExpression.ToExpressionStyle;
import org.magcruise.gaming.lang.exception.ApplyException;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.def.GameBuilder;
import org.magcruise.gaming.model.def.boot.BootstrapBuilder;
import org.magcruise.gaming.model.game.Game;
import org.magcruise.gaming.util.LiteralUtils;
import gnu.expr.Compilation;
import gnu.kawa.io.Path;
import gnu.mapping.Environment;
import gnu.mapping.Location;
import gnu.mapping.LocationEnumeration;
import gnu.mapping.Procedure;
import kawa.standard.Scheme;

public class SchemeEnvironment {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();
  private static Map<String, SchemeEnvironment> environments = new ConcurrentHashMap<>();
  private static Scheme scheme;
  private final Environment environment;

  static {
    scheme = new Scheme();
    Scheme.registerEnvironment();
    setOptions();
    Environment env = Environment.getCurrent();
    if (env.getName() == null || !env.getName().equals("main")) {
      log.info("Initial environment name is {} and rename to main.", env.getName());
      env.setName("main");
    }
    SchemeEnvironment sEnv = new SchemeEnvironment(env);
    environments.put("main", sEnv);
    log.info("load framework ...");
    sEnv.loadFramework();
  }

  private SchemeEnvironment(Environment environment) {
    this.environment = environment;
  }

  static SchemeEnvironment createNewEnvironmentAndSetCurrent(String environmentName) {
    if (environments.get(environmentName) != null) {
      log.warn("{} is already exists.", environmentName);
      return environments.get(environmentName);
    }

    Environment env = scheme.getNewEnvironment();
    env.setName(environmentName);
    environments.put(environmentName, new SchemeEnvironment(env));
    setCurrent(env);
    SchemeEnvironment sEnv = getInstanceAndSetCurrent(env.getName());
    sEnv.loadFramework();
    return sEnv;
  }

  public static void removeEnvironment(String envName) {
    if (envName.equals("main")) {
      log.warn("main env can not be removed.");
      return;
    }
    environments.remove(envName);
    getInstanceAndSetCurrent("main");
  }

  public static void setCurrent(String before) {
    if (before == null) {
      return;
    }
    setCurrent(getInstanceAndSetCurrent(before).getEnvironment());
  }

  private static String createNewEnvironmentAndSetCurrent() {
    Environment env = scheme.getNewEnvironment();
    if (env.getName() == null) {
      log.warn("env name is null");
    }
    environments.put(env.getName(), new SchemeEnvironment(env));
    setCurrent(env);
    SchemeEnvironment sEnv = getInstanceAndSetCurrent(env.getName());
    sEnv.loadFramework();
    return sEnv.getName();
  }

  private void loadFramework() {
    Map<String, String> fs = getFrameworks();
    for (String key : fs.keySet()) {
      log.trace("load... {}", key);
      SchemeEnvironment.eval(environment, String.join(System.lineSeparator(), fs.get(key)));
    }
  }

  /**
   *
   * @param environmentName
   * @return
   */
  static SchemeEnvironment getInstanceAndSetCurrent(String environmentName) {
    if (environmentName == null) {
      log.error("EnvironmentName should not be null.");
    }
    SchemeEnvironment env = environments.get(environmentName);
    if (env != null) {
      setCurrent(env.getEnvironment());
      return env;
    }
    return createNewEnvironmentAndSetCurrent(environmentName);
  }

  private static void setCurrent(Environment to) {
    if (to == null) {
      return;
    } else if (Environment.getCurrent() == null) {
      Environment.setCurrent(to);
      return;
    } else if (Environment.getCurrent().getName() == null) {
      // Environment.getCurrent()はnullではないのに，nameがnullの時がある．
      // マルチスレッドの影響？？
      Environment.setCurrent(to);
      return;
    } else if (to.getName().equals(Environment.getCurrent().getName())) {
      return;
    } else {
      Environment.setCurrent(to);
    }
  }

  public static String getCurrentEnvironmentName() {
    String current = Environment.getCurrent().getName();
    if (current == null) {
      log.error("Current env is null.");
      return null;
    }
    environments.putIfAbsent(current, new SchemeEnvironment(Environment.getCurrent()));
    return current;
  }

  public static String getCurrentGlobalName() {
    String global = Environment.getGlobal().getName();
    if (global == null) {
      log.error("Global env is null.");
      return null;
    }
    environments.putIfAbsent(global, new SchemeEnvironment(Environment.getGlobal()));
    return global;
  }

  private static SchemeEnvironment getGlobal() {
    return new SchemeEnvironment(Environment.getGlobal());
  }

  public Environment getEnvironment() {
    return environment;
  }

  private boolean isDefined(String procedureName) {
    return SchemeEnvironment.isDefined(getEnvironment(), procedureName);
  }

  public static boolean isDefined(String environmentName, String procedureName) {
    return getInstanceAndSetCurrent(environmentName).isDefined(procedureName);
  }

  private Object applyProcedure(String procedureName, Object... args) {
    return SchemeEnvironment.applyProcedure(getEnvironment(), procedureName, args);
  }

  public static Object applyProcedure(String environmentName, String procedureName,
      Object... args) {
    return getInstanceAndSetCurrent(environmentName).applyProcedure(procedureName, args);
  }

  public Object applyProcedureIfDefined(String procedureName, Object... args) {
    return SchemeEnvironment.applyProcedureIfDefined(getEnvironment(), procedureName, args);
  }

  public static Object applyProcedureIfDefined(String environmentName, String procedureName,
      Object... args) {
    return getInstanceAndSetCurrent(environmentName).applyProcedureIfDefined(procedureName, args);
  }

  public static void setOptions() {
    Compilation.defaultCallConvention = Compilation.CALL_WITH_TAILCALLS;
    Compilation.options.set("full-tailcalls", "yes");
    Compilation.options.set("warn-undefined-variable", "no");
    Compilation.options.set("warn-invoke-unknown-method", "no");
  }

  private static Map<String, String> frameworkSExprs;

  public static synchronized Map<String, String> getFrameworks() {
    if (frameworkSExprs != null) {
      return frameworkSExprs;
    }
    frameworkSExprs = new LinkedHashMap<>();
    Arrays.asList("global.scm", "lang.scm", "defgame.scm", "ui.scm", "html.scm").forEach(file -> {
      try {
        List<String> lines = IOUtils.readLines(
            SchemeEnvironment.class.getResourceAsStream("/scm/" + file), StandardCharsets.UTF_8);
        frameworkSExprs.put(file, String.join(System.lineSeparator(), lines));
      } catch (Exception e) {
        log.error(e, e);
      }
    });
    return frameworkSExprs;
  }

  public static List<URI> getFrameworkPaths() {
    List<URI> uris = new ArrayList<>();
    Arrays.asList(new String[] {"global.scm", "lang.scm", "defgame.scm", "ui.scm", "html.scm"})
        .forEach(f -> {
          try {
            URI scm = SchemeEnvironment.class.getResource("/scm/" + f).toURI();
            uris.add(scm);
          } catch (Exception e) {
            log.error(e, e);
          }
        });
    return uris;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @SuppressWarnings("unused")
  private List<Location<?>> getLocations() {
    List<Location<?>> result = new ArrayList<>();
    LocationEnumeration locations = getEnvironment().enumerateLocations();

    while (locations.hasNext()) {
      result.add(locations.next());
    }
    return result;

  }

  @SuppressWarnings("unused")
  private List<Location<?>> getAllLocations() {
    List<Location<?>> result = new ArrayList<>();
    LocationEnumeration locations = getEnvironment().enumerateAllLocations();

    while (locations.hasNext()) {
      result.add(locations.next());
    }
    return result;

  }

  private <T> T makeInstance(Class<T> clazz, Object... args) {
    return SchemeEnvironment.makeInstance(getEnvironment(), clazz, args);
  }

  public static <T> T makeInstance(String environmentName, Class<T> clazz, Object... args) {
    return getInstanceAndSetCurrent(environmentName).makeInstance(clazz, args);
  }

  private <T> T makeInstance(SConstructor<T> sConstructor) {
    return SchemeEnvironment.makeInstance(getEnvironment(), sConstructor);
  }

  public static <T> T makeInstance(String environmentName, SConstructor<T> sConstructor) {
    return getInstanceAndSetCurrent(environmentName).makeInstance(sConstructor);
  }

  private void load(URI path) {
    SchemeEnvironment.load(getEnvironment(), path);
  }

  public static void load(String environmentName, URI uri) {
    getInstanceAndSetCurrent(environmentName).load(uri);
  }

  @SuppressWarnings("unchecked")
  private <T> T eval(SMethodCall<T> sMethodCall) {
    return (T) SchemeEnvironment.eval(getEnvironment(), sMethodCall);
  }

  public String getName() {
    return environment.getName();
  }

  private Object eval(String sexper) {
    return SchemeEnvironment.eval(getEnvironment(), sexper);
  }

  public static Object eval(String environmentName, String sexper) {
    return getInstanceAndSetCurrent(environmentName).eval(sexper);
  }

  public static <T> T eval(String environmentName, SMethodCall<T> sMethodCall) {
    return getInstanceAndSetCurrent(environmentName).eval(sMethodCall);
  }

  public static Object evalOnGlobalEnvironment(String sexper) {
    return getGlobal().eval(sexper);
  }

  private void afterBuildGame(Game game) {
    log.debug("proc def:after-build-game");
    SchemeEnvironment.applyProcedureIfDefined(getEnvironment(), "def:after-build-game", game);

  }

  public static void afterSetupGameBuilder(String environmentName, GameBuilder gameBuilder) {
    log.debug("Start of def:after-setup-game-builder...");
    getInstanceAndSetCurrent(environmentName)
        .applyProcedureIfDefined("def:after-setup-game-builder", gameBuilder);
    log.debug("End of def:after-setup-game-builder.");
  }

  public static void revertContext(String environmentName, GameBuilder gameBuilder) {
    if (getInstanceAndSetCurrent(environmentName).isDefined("def:revert-context")) {
      log.info("Call def:revert-context...");
    }
    getInstanceAndSetCurrent(environmentName).applyProcedureIfDefined("def:revert-context",
        gameBuilder);
  }

  public static void beforeSetupGameBuilder(String environmentName, GameBuilder gameBuilder) {
    log.info("Start of before setup build game...");

    getInstanceAndSetCurrent(environmentName)
        .applyProcedureIfDefined("def:before-setup-game-builder", gameBuilder);

    getInstanceAndSetCurrent(environmentName).applyProcedureIfDefined(
        "def:setup-game-system-properties-builder", gameBuilder.getGameSystemPropertiesBuilder());

    getInstanceAndSetCurrent(environmentName).applyProcedureIfDefined("def:setup-context-builder",
        gameBuilder.getContextBuilder());

    getInstanceAndSetCurrent(environmentName).applyProcedureIfDefined(
        "def:setup-game-scenario-builder", gameBuilder.getGameScenarioBuilder());

    log.debug("End of before setup build game.");
  }

  public static void setupGameBuilder(String environmentName, GameBuilder gameBuilder) {
    log.debug("Start of build game...");
    getInstanceAndSetCurrent(environmentName).applyProcedureIfDefined("def:setup-game-builder",
        gameBuilder);
    log.debug("End of build game.");
  }

  public static void load(Environment env, URI path) {
    log.debug("{} is load in {}", path, env);
    applyProcedure(env, "load", path, env);
  }

  public static Object eval(Environment env, String sexpr) {
    try {
      return Scheme.eval(sexpr, env);
    } catch (Throwable e) {
      log.error("Fail to eval expression : {}", env.getName(), sexpr);
      log.error(e, e);
      throw e;
    }
  }

  public static Object eval(Environment env, SExpression sexpr) {
    return SchemeEnvironment.eval(env, sexpr.getExpression());
  }

  @SuppressWarnings("unchecked")
  public static <T> T makeInstance(Environment env, SConstructor<? extends T> constructor) {
    T result = (T) SchemeEnvironment.eval(env, constructor);
    if (result == null) {
      if (constructor == null) {
        throw new RuntimeException("sConstructor is null");
      }
      throw new RuntimeException("sCnstructor is not valid." + constructor);
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  public static <T> T makeInstance(Environment env, Class<T> clazz, Object... args) {
    List<Object> objs = new ArrayList<>();
    objs.add(clazz);
    Arrays.asList(args).forEach(o -> {
      objs.add(o);
    });
    return (T) applyProcedure(env, "make", objs.toArray());
  }

  public static Object applyProcedure(Procedure procedure, Object... args) {
    try {

      switch (args.length) {
        case 0:
          return procedure.apply0();
        case 1:
          return procedure.apply1(args[0]);
        case 2:
          return procedure.apply2(args[0], args[1]);
        case 3:
          return procedure.apply3(args[0], args[1], args[2]);
        case 4:
          return procedure.apply4(args[0], args[1], args[2], args[3]);
        default:
          return procedure.applyN(args);
      }

    } catch (Throwable e) {
      throw new ApplyException(procedure, e, args);
    }
  }

  private static Object applyProcedure(Environment env, String procedureName, Object... args) {
    Procedure proc = null;
    try {
      proc = (Procedure) Scheme.eval(procedureName, env);
    } catch (Throwable e) {
      log.error("Error is ocurred in {}", env.getName());
      log.error(e, e);
      throw e;
    }
    log.debug("call {} with {} in {}.", proc.getName(), Arrays.asList(args), env.getName());
    try {
      return SchemeEnvironment.applyProcedure(proc, args);
    } catch (Throwable e) {
      List<String> argsStr = new ArrayList<>();

      Arrays.asList(args).forEach(o -> {
        argsStr.add(LiteralUtils.toLiteral(ToExpressionStyle.DEFAULT, o));
      });
      log.error("In {}, An exception occurred when following procedure is called: (" + " {} {} )",
          env.getName(), proc.getName(), String.join(" ", argsStr));
      throw e;
    }
  }

  private static Object applyProcedureIfDefined(Environment env, String procedureName,
      Object... args) {
    if (!isDefined(env, procedureName)) {
      return null;
    }
    return SchemeEnvironment.applyProcedure(env, procedureName, args);

  }

  public static boolean isDefined(Environment env, String procedureName) {
    LocationEnumeration locations = env.enumerateLocations();

    while (locations.hasNext()) {
      if (locations.next().getKeySymbol().toString().equals(procedureName)) {
        return true;
      }
    }
    return false;
  }

  public static void afterBuildGame(String environmentName, Game game) {
    getInstanceAndSetCurrent(environmentName).afterBuildGame(game);
  }

  public static <T> T applyOnTemporalEnvironment(Function<String, T> proc) {
    String before = SchemeEnvironment.getCurrentEnvironmentName();
    String temporalEnvName = SchemeEnvironment.createNewEnvironmentAndSetCurrent();
    T result = proc.apply(temporalEnvName);
    SchemeEnvironment.setCurrent(before);
    SchemeEnvironment.removeEnvironment(temporalEnvName);
    return result;
  }

  public static void acceptOnTemporalEnvironment(Consumer<String> proc) {
    String before = SchemeEnvironment.getCurrentEnvironmentName();
    String temporalEnvName = SchemeEnvironment.createNewEnvironmentAndSetCurrent();
    proc.accept(temporalEnvName);
    SchemeEnvironment.setCurrent(before);
    SchemeEnvironment.removeEnvironment(temporalEnvName);
  }

  public static void setUpBootstrapBuilderIfDefined(ProcessId processId,
      BootstrapBuilder bootstrapBuilder) {
    SchemeEnvironment.applyProcedureIfDefined(processId.getValue(), "def:setup-bootstrap-builder",
        bootstrapBuilder);

  }

  public static void loadBootstrapScript(ProcessId processId, Path bootstrapScript) {
    if (bootstrapScript == null) {
      return;
    }
    SchemeEnvironment.load(processId.getValue(), bootstrapScript.toUri());

  }

}
