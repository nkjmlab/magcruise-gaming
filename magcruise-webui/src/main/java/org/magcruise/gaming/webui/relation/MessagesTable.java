package org.magcruise.gaming.webui.relation;

import javax.sql.DataSource;
import org.magcruise.gaming.webui.row.Message;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.util.table_def.SimpleTableWithDefinition;
import org.nkjmlab.sorm4j.util.table_def.TableDefinition;

public class MessagesTable extends SimpleTableWithDefinition<Message> {

  public static final String TABLE_NAME = "MESSAGES";
  public static final String ID = "id";
  public static final String PROCESS_ID = "process_id";
  public static final String CREATED_AT = "created_at";
  public static final String PLAYER_NAME = "player_name";
  public static final String MESSAGE_ID = "message_id";
  public static final String ROUNDNUM = "roundnum";
  public static final String MESSAGE = "message";
  public static final String MESSAGE_ATTRS = "message_attrs";

  public MessagesTable(DataSource dataSource) {
    super(
        Sorm.create(dataSource),
        Message.class,
        TableDefinition.builder(TABLE_NAME)
            .addColumnDefinition(ID, BIGINT, AUTO_INCREMENT, PRIMARY_KEY)
            .addColumnDefinition(CREATED_AT, TIMESTAMP)
            .addColumnDefinition(PROCESS_ID, VARCHAR, NOT_NULL)
            .addColumnDefinition(ROUNDNUM, INT, NOT_NULL)
            .addColumnDefinition(PLAYER_NAME, VARCHAR, NOT_NULL)
            .addColumnDefinition(MESSAGE_ID, BIGINT, NOT_NULL)
            .addColumnDefinition(MESSAGE, VARCHAR, NOT_NULL)
            .addColumnDefinition(MESSAGE_ATTRS, VARCHAR)
            .addIndexDefinition(PROCESS_ID, PLAYER_NAME)
            .build());
  }
}
