package org.magcruise.gaming.webui.relation;

import javax.sql.DataSource;
import org.magcruise.gaming.webui.row.Input;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.util.table_def.SimpleTableWithDefinition;
import org.nkjmlab.sorm4j.util.table_def.TableDefinition;

public class InputsTable extends SimpleTableWithDefinition<Input> {

  public static final String TABLE_NAME = "INPUTS";
  public static final String ID = "id";
  public static final String PROCESS_ID = "process_id";
  public static final String CREATED_AT = "created_at";
  public static final String PLAYER_NAME = "player_name";
  public static final String REQUEST_ID = "request_id";
  public static final String ROUNDNUM = "roundnum";
  public static final String INPUTS = "inputs";

  public InputsTable(DataSource dataSource) {
    super(
        Sorm.create(dataSource),
        Input.class,
        TableDefinition.builder(TABLE_NAME)
            .addColumnDefinition(ID, BIGINT, AUTO_INCREMENT, PRIMARY_KEY)
            .addColumnDefinition(CREATED_AT, TIMESTAMP)
            .addColumnDefinition(PROCESS_ID, VARCHAR, NOT_NULL)
            .addColumnDefinition(ROUNDNUM, INT, NOT_NULL)
            .addColumnDefinition(PLAYER_NAME, VARCHAR, NOT_NULL)
            .addColumnDefinition(REQUEST_ID, BIGINT, NOT_NULL)
            .addColumnDefinition(INPUTS, VARCHAR, NOT_NULL)
            .addIndexDefinition(PROCESS_ID, PLAYER_NAME)
            .build());
  }
}
