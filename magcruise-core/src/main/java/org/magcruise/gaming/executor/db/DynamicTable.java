package org.magcruise.gaming.executor.db;

import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.security.Timestamp;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.model.game.ActorObject;
import org.magcruise.gaming.model.game.HistoricalField;
import org.magcruise.gaming.model.game.HistoricalObject;
import org.magcruise.gaming.model.game.RoundHistory;
import org.nkjmlab.sorm4j.Sorm;
import gnu.lists.Pair;
import gnu.mapping.Symbol;
import gnu.math.DFloNum;
import gnu.math.IntNum;

public class DynamicTable {

  protected static Logger log = LogManager.getLogger();
  protected Sorm sorm;

  protected static final String ID = "ID";
  protected static final String PROCESS_ID = "PROCESS_ID";
  protected static final String CREATED_AT = "CREATED_AT";
  protected static final String PROPERTIES = "PROPERTIES";
  protected static final String ROUNDNUM = "ROUNDNUM";
  protected static final String PLAYER_NAME = "PLAYER_NAME";
  protected static final String OPERATOR_ID = "OPERATOR_ID";

  protected static final String ID_DEF = ID + " BIGINT AUTO_INCREMENT PRIMARY KEY";
  protected static final String PROCESS_ID_DEF = PROCESS_ID + " VARCHAR NOT NULL";

  protected static final String CREATED_AT_DEF = CREATED_AT + " TIMESTAMP";

  protected static final String ROUNDNUM_DEF = ROUNDNUM + " INT NOT NULL";
  protected static final String PROPERTIES_DEF = PROPERTIES + " VARCHAR";

  protected static final String PLAYER_NAME_DEF = PLAYER_NAME + " VARCHAR NOT NULL";
  protected static final String OPERATOR_ID_DEF = OPERATOR_ID + " VARCHAR NOT NULL";;

  private static final String[] keywords = {"CROSS", "CURRENT_DATE", "CURRENT_TIME",
      "CURRENT_TIMESTAMP", "DISTINCT", "EXCEPT", "EXISTS", "FALSE", "FETCH", "FOR", "FROM", "FULL",
      "GROUP", "HAVING", "INNER", "INTERSECT", "IS", "JOIN", "LIKE", "LIMIT", "MINUS", "NATURAL",
      "NOT", "NULL", "OFFSET", "ON", "ORDER", "PRIMARY", "ROWNUM", "SELECT", "SYSDATE", "SYSTIME",
      "SYSTIMESTAMP", "TODAY", "TRUE", "UNION", "UNIQUE", "WHERE"};

  public DynamicTable(DataSource dataSource) {
    this.sorm = Sorm.create(dataSource);
  }

  protected String getObjectTableName(Object obj) {
    return obj.getClass().getName().replace(".", "_");
  }

  protected void procAttrsForInsert(ActorObject target, int roundnum, List<String> attrNames,
      List<Object> vals) {
    RoundHistory r = target.getHistory().getRoundHistory(roundnum);
    for (Pair pair : target.getAttributeAndFieldNamePairs()) {
      Symbol key = (Symbol) pair.getCdr();
      attrNames.add(key.toString());
      Serializable val = r.get(key);
      if (IntNum.class.isAssignableFrom(val.getClass())) {
        vals.add(((IntNum) val).intValue());
      } else if (DFloNum.class.isAssignableFrom(val.getClass())) {
        vals.add(((DFloNum) val).doubleValue());
      } else if (Number.class.isAssignableFrom(val.getClass())) {
        vals.add(val);
      } else {
        vals.add(val.toString());
      }
    }

  }

  /**
   * @see Keywords / Reserved Words http://www.h2database.com/html/advanced.html
   * @param
   * @return
   */
  protected boolean isSQLReservedKeyword(String defOfColumn) {
    for (String keyword : keywords) {
      if (keyword.equalsIgnoreCase(defOfColumn.split(" ")[0])) {
        return true;
      }
    }
    return false;
  }

  protected List<String> procAttrsForSchema(List<String> attrs, Class<?> target) {

    for (Field f : target.getDeclaredFields()) {
      HistoricalField a = f.getAnnotation(HistoricalField.class);
      if (a == null) {
        continue;
      }
      String type = "VARCHAR";
      if (f.getType().equals(Long.TYPE)) {
        type = "LONG";
      } else if (f.getType().equals(Integer.TYPE)) {
        type = "INT";
      } else if (f.getType().equals(Double.TYPE)) {
        type = "DOUBLE";
      } else if (f.getType().equals(Float.TYPE)) {
        type = "FLOAT";
      } else if (f.getType().equals(Boolean.TYPE)) {
        type = "BOOLEAN";
      } else if (f.getType().getName().equalsIgnoreCase(BigDecimal.class.getTypeName())) {
        type = "DECIMAL";
      } else if (f.getType().getName().equalsIgnoreCase(Date.class.getTypeName())
          || f.getType().getName().equalsIgnoreCase(Timestamp.class.getTypeName())) {
        type = "TIMESTAMP";
      } else if (f.getType().getName().equalsIgnoreCase(Blob.class.getTypeName())
          || f.getType().getName().equalsIgnoreCase(InputStream.class.getTypeName())) {
        type = "BLOB";
      } else if (f.getType().isArray() && f.getType().getComponentType().equals(Byte.TYPE)) {
        type = "BINARY";
      } else if (f.getType().getName().equalsIgnoreCase(Clob.class.getTypeName())
          || f.getType().getName().equalsIgnoreCase(Reader.class.getTypeName())) {
        type = "CLOB";
      }
      if (isSQLReservedKeyword(f.getName())) {
        throw new RuntimeException(a + " is a reserved word for sql.");
      }
      attrs.add(f.getName() + " " + type);
    }

    if (HistoricalObject.class.isAssignableFrom(target.getSuperclass()))

    {
      procAttrsForSchema(attrs, target.getSuperclass());
    }

    return attrs;
  }

}
