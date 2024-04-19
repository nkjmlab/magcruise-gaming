package org.magcruise.gaming.webui.relation;

import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.magcruise.gaming.webui.row.Assignment;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.util.table_def.SimpleTableWithDefinition;
import org.nkjmlab.sorm4j.util.table_def.TableDefinition;

public class AssignmentsTable extends SimpleTableWithDefinition<Assignment> {

  public static final String TABLE_NAME = "ASSIGNMENTS";
  public static final String ID = "id";
  public static final String PROCESS_ID = "process_id";
  public static final String CREATED_AT = "created_at";
  public static final String PLAYER_NAME = "player_name";
  public static final String USER_ID = "user_id";
  public static final String JOINED = "joined";

  public AssignmentsTable(DataSource dataSource) {
    super(
        Sorm.create(dataSource),
        Assignment.class,
        TableDefinition.builder(TABLE_NAME)
            .addColumnDefinition(ID, BIGINT, AUTO_INCREMENT, PRIMARY_KEY)
            .addColumnDefinition(CREATED_AT, TIMESTAMP)
            .addColumnDefinition(PROCESS_ID, VARCHAR)
            .addColumnDefinition(PLAYER_NAME, VARCHAR)
            .addColumnDefinition(USER_ID, VARCHAR)
            .addColumnDefinition(JOINED, BOOLEAN)
            .addIndexDefinition(USER_ID)
            .addIndexDefinition(PROCESS_ID)
            .addIndexDefinition(PROCESS_ID, USER_ID)
            .build());
  }

  public Assignment readByProcessIdAndPlayerName(String processId, String playerName) {
    return selectFirstAllEqual(PROCESS_ID, processId, PLAYER_NAME, playerName);
  }

  public Assignment readByProcessIdAndUserId(String processId, String userId) {
    return selectFirstAllEqual(PROCESS_ID, processId, USER_ID, userId);
  }

  public List<Assignment> readListByProcessId(String processId) {
    return selectListAllEqual(PROCESS_ID, processId);
  }

  public void assign(String processId, String playerName, String userId) {
    Assignment a = readByProcessIdAndUserId(processId, userId);
    if (a == null) {
      insert(new Assignment(processId, playerName, userId, false));
    } else {
      a.setProcessId(processId);
      a.setPlayerName(playerName);
      a.setUserId(userId);
      update(a);
    }
  }

  public List<Assignment> findByUserId(String userId) {
    return selectListAllEqual(USER_ID, userId);
  }

  public void notifyJoinInGame(String processId, String playerName) {
    Assignment a = readByProcessIdAndPlayerName(processId, playerName);
    if (a == null) {
      insert(new Assignment(processId, playerName, null, true));
      return;
    }

    if (a.getJoined()) {
      return;
    }
    a.setJoined(true);
    update(a);
  }

  public List<Assignment> findUnjoinedAssignment(String processId) {
    return selectListAllEqual(PROCESS_ID, processId).stream()
        .filter(a -> !a.getJoined())
        .collect(Collectors.toList());
  }

  public List<Assignment> findAssignments(String processId) {
    return selectListAllEqual(PROCESS_ID, processId);
  }
}
