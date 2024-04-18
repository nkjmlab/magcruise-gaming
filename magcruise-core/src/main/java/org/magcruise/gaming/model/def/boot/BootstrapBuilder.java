package org.magcruise.gaming.model.def.boot;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.manager.session.ResourceLoader;
import org.magcruise.gaming.model.def.sys.DefLog4jConfig;
import org.magcruise.gaming.util.SystemEnvironmentUtils;
import org.nkjmlab.sorm4j.internal.util.Try;
import org.nkjmlab.util.log4j.Log4jConfigurator;
import gnu.kawa.io.Path;

@SuppressWarnings("serial")
public class BootstrapBuilder implements SConstructive {

  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  private List<DefBootstrapProperty> defProperties = new ArrayList<>();

  public BootstrapBuilder() {}

  public BootstrapBuilder(BootstrapBuilder bootstrapBuilder) {
    this(bootstrapBuilder.defProperties);
  }

  public BootstrapBuilder(List<DefBootstrapProperty> defProperties) {
    this.defProperties.addAll(defProperties);
  }

  @Override
  public SConstructor<? extends BootstrapBuilder> toConstructor(ToExpressionStyle style) {
    return new SConstructor<>(
        (style == ToExpressionStyle.MULTI_LINE ? System.lineSeparator() + "  " : "")
            + SConstructor.toConstructor(style, getClass(), defProperties));
  }

  public BootstrapBuilder addDefBootstrap(DefBootstrapProperty... defProperties) {
    return addProperties(defProperties);
  }

  public BootstrapBuilder addProperties(DefBootstrapProperty... defProperties) {
    this.defProperties.addAll(Arrays.asList(defProperties));
    return this;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public BootstrapBuilder setBootstrapScript(URI uri) {
    addProperties(new DefBootstrapPath(uri));
    return this;
  }

  public Path getBootstrapScriptPath() {
    for (DefBootstrapProperty p : defProperties) {
      if (p instanceof DefBootstrapPath) {
        return ((DefBootstrapPath) p).getPath();
      }
    }
    return null;
  }

  public void setUpBootstrapBuilder(ProcessId processId) {
    try {
      setupFromDefBootstrapScript(processId);

      SchemeEnvironment.loadBootstrapScript(processId, getBootstrapScriptPath());
      SchemeEnvironment.setUpBootstrapBuilderIfDefined(processId, this);
      List<Path> gameScripts = build(processId).getGameScripts();
      for (gnu.kawa.io.Path path : gameScripts) {
        log.info("In {}, load {} ...", processId, path);
        SchemeEnvironment.load(processId.getValue(), path.toUri());
      }
    } catch (Throwable e) {
      log.error(e, e);
      throw new RuntimeException(e);
    }
  }

  private void setupFromDefBootstrapScript(ProcessId processId) {
    for (DefBootstrapProperty p : new ArrayList<>(defProperties)) {
      if (p instanceof DefBootstrapScript) {
        java.nio.file.Path path = SystemEnvironmentUtils.toTmpScriptFile("bootstrap-script",
            System.nanoTime() + ".scm", processId, ((DefBootstrapScript) p).getScript());
        addProperties(new DefBootstrapPath(path.toUri()));
      }
    }
  }

  private BootstrapProperties build(ProcessId processId) {

    List<Path> gameScripts = new ArrayList<>();
    for (DefBootstrapProperty p : defProperties) {
      if (p instanceof DefResourceLoader) {
        DefResourceLoader defLoader = ((DefResourceLoader) p);
        String resourceLoaderName = defLoader.getClassName();
        log.debug("ResourceLoaderName is {}.", resourceLoaderName);
        try {
          ResourceLoader loader = (ResourceLoader) Class.forName(resourceLoaderName)
              .getDeclaredConstructor().newInstance();
          defLoader.getResources()
              .forEach(def -> gameScripts.add(loader.getResource(def.getPathString())));
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
          log.warn(e, e);
        } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException
            | SecurityException e) {
          throw Try.rethrow(e);
        }
      } else if (p instanceof DefBootstrapPath) {
        gameScripts.add(((DefBootstrapPath) p).getPath());
      }
    }
    for (DefBootstrapProperty p : defProperties) {
      if (p instanceof DefGameDefinition) {
        gameScripts.add(((DefGameDefinition) p).getPath());
      } else if (p instanceof DefGameScript) {
        java.nio.file.Path path = SystemEnvironmentUtils.toTmpScriptFile("game-script",
            System.nanoTime() + ".scm", processId, ((DefGameScript) p).getScript());
        gameScripts.add(Path.coerceToPathOrNull(path.toUri()));
      } else if (p instanceof DefLog4jConfig) {
        DefLog4jConfig defLog4jConfig = (DefLog4jConfig) p;
        Log4jConfigurator.overrideByBundledXmlConfiguration(defLog4jConfig.getLevel(),
            defLog4jConfig.getLocation());
      }
    }
    log.debug("Bootstrap properteis are {}", this);
    return new BootstrapProperties(getClasspath(), gameScripts, isRemoteDebugMode());
  }

  public boolean isRemoteDebugMode() {
    for (DefBootstrapProperty p : defProperties) {
      if (p instanceof DefRemoteDebug) {
        return ((DefRemoteDebug) p).getFlag();
      }
    }
    return false;
  }

  public List<DefClasspath> getClasspath() {
    List<DefClasspath> classpaths = new ArrayList<>();
    for (DefBootstrapProperty p : defProperties) {
      if (p instanceof DefClasspath) {
        classpaths.add((DefClasspath) p);
      }
    }
    return classpaths;
  }

}
