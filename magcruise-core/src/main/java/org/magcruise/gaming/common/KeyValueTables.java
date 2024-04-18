package org.magcruise.gaming.common;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.ActorName;

@SuppressWarnings("serial")
public class KeyValueTables implements SConstructive {

	/** key is tableName **/
	protected final Map<String, KeyValueTable> tables = new LinkedHashMap<>();

	public KeyValueTables() {
	}

	public KeyValueTables(Map<String, KeyValueTable> tables) {
		this.tables.putAll(tables);
	}

	public void putTable(KeyValueTable table) {
		tables.put(table.getTableName(), table);
	}

	public Object get(String tableName, String key, int index) {
		if (!exists(tableName)) {
			return null;
		}
		return tables.get(tableName).get(key, index);
	}

	public boolean exists(String tableName) {
		return tables.containsKey(tableName);
	}

	public void putDefaultValue(String tableName, String key, Object defaultVal) {
		tables.putIfAbsent(tableName, new KeyValueTable(guessPlayerName(), tableName));
		tables.get(tableName).putDefaultValue(key, defaultVal);
	}

	public void put(String tableName, String key, List<?> vals) {
		tables.putIfAbsent(tableName, new KeyValueTable(guessPlayerName(), tableName));
		tables.get(tableName).putValueOfEachRound(key, vals);
	}

	public KeyValueTable getKeyValueTable(String tableName) {
		return tables.get(tableName);
	}

	private ActorName guessPlayerName() {
		for (KeyValueTable t : tables.values()) {
			return t.getPlayerName();
		}
		return ActorName.UNDEFINED;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public SConstructor<?> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(
				(style == ToExpressionStyle.MULTI_LINE ? System.lineSeparator() + "    " : "")
						+ SConstructor.toConstructor(style, getClass(), tables));
	}

}
