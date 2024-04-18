package org.magcruise.gaming.executor.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.model.game.Player;
import org.nkjmlab.sorm4j.result.RowMap;

public class DynamicPlayerTable extends DynamicTable {

  public DynamicPlayerTable(DataSource dataSource) {
    super(dataSource);
  }

  public void insertPlayer(Context context, Player player) {
    try {
      createTableAndIndexesIfNotExists(player);
      insertPlayerData(context, player, context.getRoundnum());
    } catch (Exception e) {
      log.error(e.getMessage());
      try {
        log.info("Try to create new table for {}", player.getClass().getSimpleName());
        sorm.executeUpdate(H2SqlUtils.getRenameTableSql(getObjectTableName(player),
            getObjectTableName(player) + "_" + System.currentTimeMillis()));
        createTableAndIndexesIfNotExists(player);
        insertPlayerData(context, player, context.getRoundnum());
      } catch (Exception e2) {
        log.error(e2, e2);
      }
    }
  }

  public void createTableAndIndexesIfNotExists(Player player) {
    sorm.executeUpdate("create table if not exists " + convertPlayerToSchema(player));
    sorm.executeUpdate(H2SqlUtils.getCreateIndexOnSql(getObjectTableName(player), PROCESS_ID));
  }

  private String convertPlayerToSchema(Player player) {
    List<String> columns = new ArrayList<>(Arrays.asList(ID_DEF, PROCESS_ID_DEF, CREATED_AT_DEF,
        ROUNDNUM_DEF, PLAYER_NAME_DEF, OPERATOR_ID_DEF));
    columns.addAll(procAttrsForSchema(new ArrayList<>(), player.getClass()));
    columns.add(PROPERTIES_DEF);
    String schema = getObjectTableName(player) + "(" + String.join(",", columns) + ")";
    log.info("Table schema = {}", schema);
    return schema;
  }

  private void insertPlayerData(Context ctx, Player player, int roundnum) {
    List<String> attrNames = new ArrayList<>();
    List<Object> vals = new ArrayList<>();
    attrNames.add(PROCESS_ID);
    vals.add(ctx.getProcessId().toString());
    attrNames.add(ROUNDNUM);
    vals.add(roundnum);
    attrNames.add(PLAYER_NAME);
    vals.add(player.getName().toString());
    attrNames.add(OPERATOR_ID);
    vals.add(player.getOperatorId());
    procAttrsForInsert(player, roundnum, attrNames, vals);

    attrNames.add(PROPERTIES);
    vals.add("'" + player.getProperties().toSimpleCsv() + "'");

    Map<String, Object> map = new HashMap<>();

    for (int i = 0; i < attrNames.size(); i++) {
      map.put(attrNames.get(i), vals.get(i));
    }


    sorm.insertMapInto(getObjectTableName(player), RowMap.create(map));
  }

}
