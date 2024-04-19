package org.magcruise.gaming.webui.relation;

import java.util.List;
import javax.sql.DataSource;
import org.magcruise.gaming.webui.row.GameSession;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.util.table_def.SimpleTableWithDefinition;
import org.nkjmlab.sorm4j.util.table_def.TableDefinition;

public class GameSessionsTable extends SimpleTableWithDefinition<GameSession> {

  public static final String TABLE_NAME = "GAME_SESSIONS";
  public static final String PROCESS_ID = "process_id";
  public static final String CREATED_AT = "created_at";
  public static final String USER_ID = "user_id";
  private static final String SESSION_NAME = "sessionName";
  private static final String DESCRIPTION = "description";
  private static final String BROKER_URL = "broker_url";
  private static final String ROOT_BROKER_URL = "root_broker_url";
  private static final String BOOTSTRAP_SCRIPT = "bootstrap_script";

  public GameSessionsTable(DataSource dataSource) {
    super(
        Sorm.create(dataSource),
        GameSession.class,
        TableDefinition.builder(TABLE_NAME)
            .addColumnDefinition(PROCESS_ID, VARCHAR, PRIMARY_KEY)
            .addColumnDefinition(CREATED_AT, TIMESTAMP)
            .addColumnDefinition(USER_ID, VARCHAR)
            .addColumnDefinition(SESSION_NAME, VARCHAR)
            .addColumnDefinition(DESCRIPTION, VARCHAR)
            .addColumnDefinition(ROOT_BROKER_URL, VARCHAR)
            .addColumnDefinition(BROKER_URL, VARCHAR)
            .addColumnDefinition(BOOTSTRAP_SCRIPT, VARCHAR)
            .addIndexDefinition(USER_ID)
            .build());
  }

  public List<GameSession> readListByUserId(String userId) {
    return selectListAllEqual(USER_ID, userId);
  }
}
