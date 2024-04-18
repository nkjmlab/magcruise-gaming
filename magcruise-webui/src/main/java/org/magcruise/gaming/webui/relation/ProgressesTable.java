package org.magcruise.gaming.webui.relation;

import static org.nkjmlab.sorm4j.util.sql.SelectSql.*;
import java.util.List;
import javax.sql.DataSource;
import org.magcruise.gaming.webui.row.Progress;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.util.table_def.SimpleTableWithDefinition;
import org.nkjmlab.sorm4j.util.table_def.TableDefinition;

public class ProgressesTable extends SimpleTableWithDefinition<Progress> {

  public static final String TABLE_NAME = "PROGRESSES";
  public static final String ID = "id";
  public static final String PROCESS_ID = "process_id";
  public static final String CREATED_AT = "created_at";
  public static final String ROUNDNUM = "ROUNDNUM";
  public static final String STATUS = "status";

  public ProgressesTable(DataSource dataSource) {
    super(
        Sorm.create(dataSource),
        Progress.class,
        TableDefinition.builder(TABLE_NAME)
            .addColumnDefinition(ID, BIGINT, AUTO_INCREMENT, PRIMARY_KEY)
            .addColumnDefinition(PROCESS_ID, VARCHAR)
            .addColumnDefinition(CREATED_AT, TIMESTAMP)
            .addColumnDefinition(ROUNDNUM, INT)
            .addColumnDefinition(STATUS, VARCHAR)
            .addIndexDefinition(PROCESS_ID)
            .build());
  }

  public void insertStartOfGame(String processId) {
    insert(new Progress(processId, -1, "Start of game"));
  }

  public void insertStartOfRound(String processId, int roundnum) {
    insert(new Progress(processId, roundnum, "Start of round " + roundnum));
  }

  public void insertEndOfRound(String processId, int roundnum) {
    insert(new Progress(processId, roundnum, "End of round " + roundnum));
  }

  public void insertEndOfGame(String processId) {
    insert(new Progress(processId, 9999, "End of game"));
  }

  public void insertJoinInGame(String processId, String playerName) {
    insert(new Progress(processId, -1, playerName + " join in game"));
  }

  public Progress readLatest(String processId) {
    // "SELECT * FROM " + TABLE_NAME + " WHERE " + PROCESS_ID + "=?"
    // + " ORDER BY " + CREATED_AT + " DESC",

    String sql = selectStarFrom(TABLE_NAME) + where(PROCESS_ID + "=?") + orderByDesc(CREATED_AT);
    List<Progress> tmp = readList(sql, processId);
    if (tmp.size() == 0) {
      Progress p = new Progress(processId, -1, "Wait for entry");
      return p;
    }
    return tmp.get(0);
  }
}
