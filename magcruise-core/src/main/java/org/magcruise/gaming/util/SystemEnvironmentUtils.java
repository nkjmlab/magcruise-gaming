package org.magcruise.gaming.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.magcruise.gaming.executor.db.GameRecordsTable;
import org.magcruise.gaming.manager.process.ProcessId;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.internal.util.Try;
import org.nkjmlab.sorm4j.util.h2.datasource.H2DataSourceFactory;
import org.nkjmlab.util.java.io.SystemFileUtils;

public class SystemEnvironmentUtils {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  // private static final List<String> DEFAULT_JAVA_OPTIONS = Arrays.asList("-Dfile.encoding=UTF8",
  // "-XX:+UseSerialGC", "-Xms96m", "-Xmx96m");
  private static final List<String> DEFAULT_JAVA_OPTIONS =
      Arrays.asList("-Dfile.encoding=UTF8", "-XX:+UseSerialGC", "-XX:MaxRAM=96m");

  private static final File TMP_ROOT_DIR =
      new File(System.getProperty("java.io.tmpdir"), "magcruise");

  private static final String STDOUT_FILENAME = "magc.stdout";
  private static final String STDERR_FILENAME = "magc.stderr";

  private static final String DEFAULT_LOCAL_BROKER_URL = "http://localhost:5678/app";
  private static final String DEFAULT_PUBLIC_BROKER_URL = "https://broker.magcruise.org/app";

  private static final String DEFAULT_LOCAL_WEB_UI = "http://localhost:6789/app/json/WebUiService";
  private static final String DEFAULT_PUBLIC_WEB_UI =
      "https://webui.magcruise.org/app/json/WebUiService";

  public static Path getTempDir(ProcessId processId) {
    return new File(TMP_ROOT_DIR, processId.toString()).toPath();
  }

  public static Path getTempFile(ProcessId processId, String fileName) {
    return new File(getTempDir(processId).toFile(), fileName).toPath();
  }

  public static Path createTempDirIfNotExists(ProcessId processId) {
    Path tmpDir = getTempDir(processId);
    if (tmpDir.toFile().exists()) {
      return tmpDir;
    }
    return createTempDirAux(tmpDir.toFile(), processId.toString());
  }

  private static Path createTempDirAux(File tmpDir, String subDir) {
    if (!tmpDir.mkdirs()) {
      throw new RuntimeException("Cannot create " + tmpDir);
    }
    return tmpDir.toPath();
  }

  public static Path createTempFile(ProcessId processId, String fileName) {
    Path dir = createTempDirIfNotExists(processId);
    File result = new File(dir.toFile(), fileName);
    if (!result.exists()) {
      try {
        result.createNewFile();
      } catch (IOException e) {
        log.error(e, e);
        throw new RuntimeException(e);
      }
    }
    return result.toPath();
  }

  /**
   * @param filePrefix the prefix of the file.
   * @param fileSuffix the suffix of the file.
   * @param processId the process id associated with the file
   * @param gameDefineScript the contents of the file.
   * @return
   */
  public static Path toTmpScriptFile(
      String filePrefix, String fileSuffix, ProcessId processId, String gameDefineScript) {
    try {
      String[] script = {gameDefineScript};

      File tmpFile =
          createTempFile(processId, filePrefix + "-" + System.currentTimeMillis() + fileSuffix)
              .toFile();
      return Files.write(tmpFile.toPath(), Arrays.asList(script), StandardOpenOption.WRITE);
    } catch (IOException e) {
      log.error(e, e);
      throw new RuntimeException(e);
    }
  }

  public static boolean existsTempFile(ProcessId processId, String fileName) {
    return new File(createTempDirIfNotExists(processId).toFile(), fileName).exists();
  }

  public static Path getDefaultWorkingDirectoryPath() {
    return new File(System.getProperty("user.dir")).toPath();
  }

  private static final H2DataSourceFactory dataSourceFactory =
      H2DataSourceFactory.builder(
              new File(SystemFileUtils.getUserHomeDirectory(), "h2db/2.2/magcruise-core/"),
              "game-logger",
              "sa",
              "magcruise-core")
          .build();

  static {
    dataSourceFactory.makeFileDatabaseIfNotExists();
  }

  private static final DataSource defaultDataSource =
      dataSourceFactory.createServerModeDataSource();

  public static DataSource getDefaultDataSource() {
    return defaultDataSource;
  }

  public static Path getDefaultDbFilePath() {
    return Path.of(dataSourceFactory.getDatabasePath());
  }

  public static String getLatestRecord(ProcessId processId) {
    String record =
        Sorm.create(defaultDataSource)
            .readFirst(
                String.class,
                "SELECT record FROM "
                    + GameRecordsTable.TABLE_NAME
                    + " WHERE PROCESS_ID=? ORDER BY roundnum DESC LIMIT 1",
                processId.toString());
    return record;
  }

  public static List<String> getDefaultJavaOptions() {
    return DEFAULT_JAVA_OPTIONS;
  }

  public static Path createTempStdOutFile(ProcessId processId) {
    return SystemEnvironmentUtils.createTempFile(processId, STDOUT_FILENAME);
  }

  public static Path createTempStdErrFile(ProcessId processId) {
    return SystemEnvironmentUtils.createTempFile(processId, STDERR_FILENAME);
  }

  private static Path getTempStdOutFile(ProcessId processId) {
    return SystemEnvironmentUtils.getTempFile(processId, STDOUT_FILENAME);
  }

  private static Path getTempStdErrFile(ProcessId processId) {
    return SystemEnvironmentUtils.getTempFile(processId, STDERR_FILENAME);
  }

  public static String getStdOutContent(ProcessId processId) {
    return Try.getOrElseThrow(
        () -> String.join(System.lineSeparator(), Files.readAllLines(getTempStdOutFile(processId))),
        Try::rethrow);
  }

  public static String getStdErrContent(ProcessId processId) {
    return Try.getOrElseThrow(
        () -> String.join(System.lineSeparator(), Files.readAllLines(getTempStdErrFile(processId))),
        Try::rethrow);
  }

  public static String getDefaultPublicBrokerUrl() {
    return DEFAULT_PUBLIC_BROKER_URL;
  }

  public static String getDefaultLocalBrokerUrl() {
    return DEFAULT_LOCAL_BROKER_URL;
  }

  public static String getDefaultPublicWebUI() {
    return DEFAULT_PUBLIC_WEB_UI;
  }

  public static String getDefaultLocalWebUI() {
    return DEFAULT_LOCAL_WEB_UI;
  }
}
