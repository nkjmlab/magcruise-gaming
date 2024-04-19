package org.magcruise.gaming.manager.resource;

import java.net.URI;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.util.SystemEnvironmentUtils;
import org.nkjmlab.util.java.net.BasicHttpClient;

public class GameResourceDownloader {
  protected static Logger log = LogManager.getLogger();

  public static Path downloadFile(ProcessId processId, URI uri) {
    return BasicHttpClient.newBasicHttpClient()
        .download(uri, SystemEnvironmentUtils.createTempDirIfNotExists(processId).toFile())
        .toPath();
  }

  protected static Path download(URI uri, Path outPath) {
    return BasicHttpClient.newBasicHttpClient().download(uri, outPath.toFile()).toPath();
  }

}
