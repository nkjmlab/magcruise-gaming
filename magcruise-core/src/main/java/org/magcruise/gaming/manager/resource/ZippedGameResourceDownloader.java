package org.magcruise.gaming.manager.resource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.util.SystemEnvironmentUtils;
import org.nkjmlab.util.java.zip.ZipUtils;

public class ZippedGameResourceDownloader extends GameResourceDownloader {
  public static void main(String[] args) throws Exception {
    URI uri = new URI("https://www.dropbox.com/s/xq3kx1oxrwo7apx/MAGCruiseScenario.jar?dl=1");
    addClassesFromZipFile(new ProcessId(String.valueOf(System.currentTimeMillis())), uri);

  }

  private static void addClassesFromZipFile(ProcessId processId, URI uri) {
    try {
      Path outPath = SystemEnvironmentUtils.createTempDirIfNotExists(processId);
      ZipUtils.unzip(download(uri, outPath).toFile(), outPath.toFile());
      URLClassLoader classLoader =
          (URLClassLoader) ZippedGameResourceDownloader.class.getClassLoader();
      addClassPath(classLoader, outPath.toAbsolutePath().toString());
    } catch (ReflectiveOperationException | IOException e) {
      throw new RuntimeException(e);
    }

  }

  private static void addClassPath(URLClassLoader classLoader, String path)
      throws ReflectiveOperationException, MalformedURLException {
    Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
    method.setAccessible(true);
    method.invoke(classLoader, new File(path).toURI().toURL());
  }

  public static Path downloadZipFileAndUnzip(ProcessId processId, URI uri) {
    log.debug("Start of download game classes dir zip from {} ." + uri);
    Path outPath = SystemEnvironmentUtils.createTempDirIfNotExists(processId);
    ZipUtils.unzip(download(uri, outPath).toFile(), outPath.toFile());
    return outPath;

  }

}
