package org.magcruise.gaming.ui.model.input;

import static org.nkjmlab.sorm4j.util.sql.SqlKeyword.AUTO_INCREMENT;
import static org.nkjmlab.sorm4j.util.sql.SqlKeyword.BIGINT;
import static org.nkjmlab.sorm4j.util.sql.SqlKeyword.INT;
import static org.nkjmlab.sorm4j.util.sql.SqlKeyword.PRIMARY_KEY;
import static org.nkjmlab.sorm4j.util.sql.SqlKeyword.TIMESTAMP;
import static org.nkjmlab.sorm4j.util.sql.SqlKeyword.VARCHAR;
import javax.sql.DataSource;
import org.magcruise.gaming.executor.db.H2SqlUtils;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.util.table_def.TableDefinition;
import org.nkjmlab.sorm4j.util.table_def.TableWithDefinition;

public class ReceivedInputsTable implements TableWithDefinition<ReceivedInput> {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  public static final String TABLE_NAME = "RECEIVED_INPUTS";
  public static final String ID = "id";
  public static final String CREATED_AT = "created_at";
  public static final String PROCESS_ID = "process_id";
  public static final String REQUEST_ID = "request_id";
  public static final String ROUNDNUM = "roundnum";
  public static final String PLAYER_NAME = "player_name";
  public static final String PARAMETERS = "parameters";

  private TableDefinition schema;

  private Sorm sorm;

  public ReceivedInputsTable(DataSource ds) {
    this.sorm = Sorm.create(ds);

    this.schema = TableDefinition.builder(TABLE_NAME)
        .addColumnDefinition(ID, BIGINT, AUTO_INCREMENT, PRIMARY_KEY)
        .addColumnDefinition(CREATED_AT, TIMESTAMP).addColumnDefinition(REQUEST_ID, BIGINT)
        .addColumnDefinition(PROCESS_ID, VARCHAR).addColumnDefinition(ROUNDNUM, INT)
        .addColumnDefinition(PLAYER_NAME, VARCHAR).addColumnDefinition(PARAMETERS, VARCHAR).build();
  }

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }

  public int insertReceivedInput(ReceivedInput input) {
    try {
      return sorm.insert(input);
    } catch (Exception e) {
      log.info("Try to create new table for ", ReceivedInputsTable.TABLE_NAME);
      sorm.executeUpdate(
          H2SqlUtils.getRenameTableSql(TABLE_NAME, TABLE_NAME + "_" + System.currentTimeMillis()));
      schema.createTableIfNotExists(sorm).createIndexesIfNotExists(sorm);
      return sorm.insert(input);
    }
  }


  @Override
  public TableDefinition getTableDefinition() {
    return schema;
  }

  @Override
  public Sorm getOrm() {
    return sorm;
  }

  @Override
  public Class<ReceivedInput> getValueType() {
    return ReceivedInput.class;
  }

}
