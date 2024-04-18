package org.magcruise.gaming.executor.db;

import static org.magcruise.gaming.executor.db.GameRecordsTable.Column.CREATED_AT;
import static org.magcruise.gaming.executor.db.GameRecordsTable.Column.ID;
import static org.magcruise.gaming.executor.db.GameRecordsTable.Column.PROCESS_ID;
import static org.magcruise.gaming.executor.db.GameRecordsTable.Column.RECORD;
import static org.magcruise.gaming.executor.db.GameRecordsTable.Column.ROUNDNUM;
import static org.nkjmlab.sorm4j.util.sql.SelectSql.selectStarFrom;
import static org.nkjmlab.sorm4j.util.sql.SqlKeyword.AND;
import static org.nkjmlab.sorm4j.util.sql.SqlKeyword.AUTO_INCREMENT;
import static org.nkjmlab.sorm4j.util.sql.SqlKeyword.BIGINT;
import static org.nkjmlab.sorm4j.util.sql.SqlKeyword.INT;
import static org.nkjmlab.sorm4j.util.sql.SqlKeyword.PRIMARY_KEY;
import static org.nkjmlab.sorm4j.util.sql.SqlKeyword.TIMESTAMP;
import static org.nkjmlab.sorm4j.util.sql.SqlKeyword.VARCHAR;
import static org.nkjmlab.sorm4j.util.sql.SqlKeyword.WHERE;
import java.util.Comparator;
import java.util.List;
import javax.sql.DataSource;
import org.magcruise.gaming.lang.SExpression.ToExpressionStyle;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.game.Context;
import org.nkjmlab.sorm4j.Sorm;
import org.nkjmlab.sorm4j.util.table_def.TableDefinition;
import org.nkjmlab.sorm4j.util.table_def.TableWithDefinition;

public class GameRecordsTable implements TableWithDefinition<GameRecord> {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  public static final String TABLE_NAME = "GAME_RECORDS";

  public enum Column {
    ID, PROCESS_ID, CREATED_AT, ROUNDNUM, RECORD
  }

  private TableDefinition schema;

  private Sorm sorm;

  public GameRecordsTable(DataSource dataSource) {
    this.sorm = Sorm.create(dataSource);
    this.schema = TableDefinition.builder(TABLE_NAME)
        .addColumnDefinition(ID, BIGINT, AUTO_INCREMENT, PRIMARY_KEY)
        .addColumnDefinition(CREATED_AT, TIMESTAMP).addColumnDefinition(PROCESS_ID, VARCHAR)
        .addColumnDefinition(ROUNDNUM, INT).addColumnDefinition(RECORD, VARCHAR).build();
  }

  @Override
  public String getTableName() {
    return TABLE_NAME;
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
  public Class<GameRecord> getValueType() {
    return GameRecord.class;
  }



  public GameRecord readBy(ProcessId pid, int roundnum) {
    return getRecordOf(pid.toString(), roundnum);
  }


  public GameRecord getLatestRecord(String processId) {
    List<GameRecord> tmp =
        readList(selectStarFrom(TABLE_NAME) + WHERE + PROCESS_ID + "=?", processId);
    return tmp.stream()
        .sorted(Comparator.comparingInt((GameRecord s) -> s.getRoundnum()).reversed()).findFirst()
        .orElse(null);
  }


  public GameRecord getRecordOf(String processId, int roundnum) {
    return readOne(selectStarFrom(TABLE_NAME) + WHERE + PROCESS_ID + "=?" + AND + ROUNDNUM + "=?",
        processId, roundnum);
  }



  public void insertGameRecord(Context ctx) {
    log.debug("Current Context :::: " + System.lineSeparator() + "{}",
        ctx.toSExpressionForRevert(ToExpressionStyle.DEFAULT));
    try {
      sorm.insert(new GameRecord(ctx.getProcessId().getProcessId(), ctx.getRoundnum(),
          ctx.toConstructor(ToExpressionStyle.MULTI_LINE)
              .getExpression(ToExpressionStyle.MULTI_LINE)));
    } catch (Exception e) {
      log.info("Try to create new table for ", GameRecordsTable.TABLE_NAME);
      sorm.executeUpdate(
          H2SqlUtils.getRenameTableSql(TABLE_NAME, TABLE_NAME + "_" + System.currentTimeMillis()));
      schema.createTableIfNotExists(sorm).createIndexesIfNotExists(sorm);
      sorm.insert(new GameRecord(ctx.getProcessId().getProcessId(), ctx.getRoundnum(),
          ctx.toSExpressionForRevert(ToExpressionStyle.MULTI_LINE)));
    }

  }



}
