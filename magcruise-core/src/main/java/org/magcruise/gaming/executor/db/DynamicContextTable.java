package org.magcruise.gaming.executor.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.magcruise.gaming.model.game.Context;
import org.nkjmlab.sorm4j.result.RowMap;

public class DynamicContextTable extends DynamicTable {

  public DynamicContextTable(DataSource dataSource) {
    super(dataSource);
  }

  public void insertContext(Context context) {
    try {
      log.debug("Start of write context of round {} to db ... ", context.getRoundnum());
      createTableAndIndexesIfNotExists(context);
      insertContextData(context, context.getRoundnum());
    } catch (Exception e) {
      log.error(e.getMessage());
      try {
        log.info("Try to create new table for {}", context.getClass().getSimpleName());
        sorm.executeUpdate(H2SqlUtils.getRenameTableSql(getObjectTableName(context),
            getObjectTableName(context) + "_" + System.currentTimeMillis()));
        createTableAndIndexesIfNotExists(context);
        insertContextData(context, context.getRoundnum());
      } catch (Exception e2) {
        log.error(e2, e2);
      }
    }

    log.debug("End of write context of round {} to db... ", context.getRoundnum());
    log.debug("Start of write players of round {} to db... ", context.getRoundnum());
  }

  public void createTableAndIndexesIfNotExists(Context context) {
    sorm.executeUpdate("create table if not exists " + convertContextToSchema(context));
    sorm.executeUpdate(H2SqlUtils.getCreateIndexOnSql(getObjectTableName(context), PROCESS_ID));

  }

  private String convertContextToSchema(Context context) {
    List<String> columns =
        new ArrayList<>(Arrays.asList(ID_DEF, PROCESS_ID_DEF, CREATED_AT_DEF, ROUNDNUM_DEF));
    columns.addAll(procAttrsForSchema(new ArrayList<>(), context.getClass()));
    columns.add(PROPERTIES_DEF);
    String schema = getObjectTableName(context) + "(" + String.join(",", columns) + ")";
    log.info("Table schema = {}", schema);
    return schema;
  }

  private void insertContextData(Context context, int roundnum) {

    List<String> attrNames = new ArrayList<>();
    List<Object> vals = new ArrayList<>();
    attrNames.add(PROCESS_ID);
    vals.add(context.getProcessId().toString());
    attrNames.add(ROUNDNUM);
    vals.add(String.valueOf(roundnum));

    procAttrsForInsert(context, roundnum, attrNames, vals);

    attrNames.add(PROPERTIES);
    vals.add(context.getProperties().toSimpleCsv());

    Map<String, Object> map = new HashMap<>();

    for (int i = 0; i < attrNames.size(); i++) {
      map.put(attrNames.get(i), vals.get(i));
    }


    sorm.insertMapInto(getObjectTableName(context), RowMap.create(map));
  }

}
