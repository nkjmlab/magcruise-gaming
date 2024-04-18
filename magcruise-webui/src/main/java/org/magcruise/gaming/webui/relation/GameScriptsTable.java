package org.magcruise.gaming.webui.relation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.sql.DataSource;
import org.magcruise.gaming.webui.json.GameScriptDefinitionJson;
import org.magcruise.gaming.webui.row.GameScript;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.util.table_def.SimpleTableWithDefinition;
import org.nkjmlab.sorm4j.util.table_def.TableDefinition;
import org.nkjmlab.util.jackson.JacksonMapper;

public class GameScriptsTable extends SimpleTableWithDefinition<GameScript> {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  public static final String TABLE_NAME = "GAME_SCRIPTS";
  public static final String ID = "id";
  public static final String CREATED_AT = "created_at";
  private static final String USER_ID = "user_id";
  private static final String NAME = "name";
  private static final String DESCRIPTION = "description";
  private static final String SOURCE_URL = "source_url";
  private static final String CLASS_NAME = "class_name";
  private static final String ADDITIONAL_SCRIPT = "additional_script";
  private static final String SCRIPT = "script";

  public GameScriptsTable(DataSource dataSource) {
    super(
        Sorm.create(dataSource),
        GameScript.class,
        TableDefinition.builder(TABLE_NAME)
            .addColumnDefinition(ID, VARCHAR, PRIMARY_KEY)
            .addColumnDefinition(CREATED_AT, TIMESTAMP)
            .addColumnDefinition(NAME, VARCHAR)
            .addColumnDefinition(USER_ID, VARCHAR)
            .addColumnDefinition(DESCRIPTION, VARCHAR)
            .addColumnDefinition(SOURCE_URL, VARCHAR)
            .addColumnDefinition(CLASS_NAME, VARCHAR)
            .addColumnDefinition(ADDITIONAL_SCRIPT, VARCHAR)
            .addColumnDefinition(SCRIPT, VARCHAR)
            .build());
  }

  public void readAndMerge(File dir) {
    Arrays.stream(dir.listFiles())
        .filter(f -> f.isDirectory())
        .forEach(
            dirPath -> {
              Arrays.stream(dirPath.listFiles())
                  .filter(f -> f.getName().equals("game.json"))
                  .forEach(f -> readAndMergeFromFile(f));
            });
  }

  private void readAndMergeFromFile(File file) {
    GameScriptDefinitionJson j =
        JacksonMapper.getDefaultMapper().toObject(file, GameScriptDefinitionJson.class);
    try {
      // logger.debug("Load game.json {}", j);
      String script =
          j.getScriptFile() == null
              ? ""
              : String.join(
                  System.lineSeparator(),
                  Files.readAllLines(
                      Paths.get(file.getParent() + File.separator + j.getScriptFile())));

      String additionalScript =
          j.getAdditionalScriptFile() == null
              ? ""
              : String.join(
                  System.lineSeparator(),
                  Files.readAllLines(
                      Paths.get(file.getParent() + File.separator + j.getAdditionalScriptFile())));

      GameScript s =
          new GameScript(
              j.getId(),
              j.getUserId(),
              j.getName(),
              j.getDescription(),
              j.getSourceUrl(),
              j.getClassName(),
              additionalScript,
              script);
      if (j.getId().length() == 0) {
        throw new RuntimeException("id should not be blank =>" + file.getAbsolutePath());
      }
      merge(s);
    } catch (IOException e) {
      log.error(e, e);
    }
  }
}
