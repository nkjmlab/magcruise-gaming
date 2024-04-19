package org.magcruise.gaming.executor;

import java.sql.Connection;
import javax.sql.DataSource;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.executor.db.DynamicContextTable;
import org.magcruise.gaming.executor.db.DynamicPlayerTable;
import org.magcruise.gaming.executor.db.GameRecordsTable;
import org.magcruise.gaming.lang.Parameters;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.ui.model.input.ReceivedInput;
import org.magcruise.gaming.ui.model.input.ReceivedInputsTable;
import gnu.mapping.Symbol;

public class GameRecorder {
  private static final org.apache.logging.log4j.Logger log =
      org.apache.logging.log4j.LogManager.getLogger();

  private final DataSource dataSource;

  private final boolean useDb;

  private GameRecordsTable gameRecordsTable;
  private ReceivedInputsTable receivedInputsTable;
  private DynamicContextTable contextTable;
  private DynamicPlayerTable playerTable;

  public GameRecorder(DataSource dataSource, boolean useDb) {
    this.dataSource = dataSource;
    this.useDb = checkUseDb(useDb);
    if (this.useDb) {
      log.info("Could connect to db", dataSource);
      receivedInputsTable = new ReceivedInputsTable(dataSource);
      receivedInputsTable.createTableIfNotExists().createIndexesIfNotExists();
      gameRecordsTable = new GameRecordsTable(dataSource);
      gameRecordsTable.createTableIfNotExists().createIndexesIfNotExists();
      contextTable = new DynamicContextTable(dataSource);
      playerTable = new DynamicPlayerTable(dataSource);
    } else {
      log.warn("Could not connect to db={}", dataSource);
    }
  }

  private boolean checkUseDb(boolean useDb) {
    if (useDb == false) {
      return false;
    }
    if (checkConnection()) {
      return true;
    }
    try {
      long time = 3000;
      log.info("Wait to reconnect in {}... msec", time);
      Thread.sleep(time);
    } catch (InterruptedException e) {
      log.warn(e, e);
    }
    if (checkConnection()) {
      return true;
    }
    // throw new RuntimeException("GAME LOG IS NOT RECOREDED IN LOCAL DB. Can't connect to H2
    // server=" + jdbcUrl);
    log.error("GAME LOG IS NOT RECOREDED IN LOCAL DB. Can't connect to H2 server");
    return false;
  }

  public boolean checkConnection() {
    try (Connection con = dataSource.getConnection()) {
      return true;
    } catch (Exception e) {
      log.warn(e, e);
      log.warn("Cannot connect to the database.");
      return false;
    }
  }

  void insertInput(long requestId, ProcessId processId, int roundnum, Symbol playerName,
      Parameters params) {
    if (!useDb) {
      return;
    }
    receivedInputsTable
        .insertReceivedInput(new ReceivedInput(requestId, processId, roundnum, playerName, params));
  }

  void finalizeRound(Context context) {
    if (!useDb) {
      return;
    }
    gameRecordsTable.insertGameRecord(context);
    contextTable.insertContext(context);
    context.getPlayers().forEach(player -> playerTable.insertPlayer(context, player));
    log.info("Write the records of round {} to db is finished.", context.getRoundnum());
  }

  public void shutdown() {
    // client.dispose();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
