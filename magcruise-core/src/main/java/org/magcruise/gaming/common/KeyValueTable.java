package org.magcruise.gaming.common;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.ActorName;

@SuppressWarnings("serial")
public class KeyValueTable implements SConstructive {

	protected ActorName playerName;
	protected String tableName;

	protected final Map<String, List<Queue<?>>> roundQueuesMap = new ConcurrentHashMap<>();

	/** key is name of field or that of input. **/
	protected final Map<String, List<?>> roundsMap = new ConcurrentHashMap<>();

	protected final Map<String, Queue<?>> queuesMap = new ConcurrentHashMap<>();

	protected final Map<String, Object> defaultsMap = new ConcurrentHashMap<>();

	public KeyValueTable(ActorName playerName, String tableName) {
		this.tableName = tableName;
		this.playerName = playerName;
	}

	public KeyValueTable(ActorName playerName, String tableName,
			Map<String, List<Queue<?>>> roundQueuesMap, Map<String, List<Object>> roundsMap,
			Map<String, Queue<?>> queuesMap, Map<String, Object> defaultsMap) {
		this(playerName, tableName);
		this.roundQueuesMap.putAll(roundQueuesMap);
		this.roundsMap.putAll(roundsMap);
		this.queuesMap.putAll(queuesMap);
		this.defaultsMap.putAll(defaultsMap);
	}

	public Set<String> keySet() {
		Set<String> result = new HashSet<>(roundsMap.keySet());
		result.addAll(defaultsMap.keySet());
		return result;
	}

	public Object get(String key, int index) {

		if (roundQueuesMap.get(key) != null && index < roundQueuesMap.get(key).size()) {
			Object r = roundQueuesMap.get(key).get(index).poll();
			if (r != null) {
				return r;
			}
		}

		if (roundsMap.get(key) != null && index < roundsMap.get(key).size()) {
			Object r = roundsMap.get(key).get(index);
			if (r != null) {
				return r;
			}
		}

		if (queuesMap.get(key) != null && queuesMap.get(key).size() > 0) {
			Object r = queuesMap.get(key).poll();
			if (r != null) {
				return r;
			}
		}

		Object defVal = defaultsMap.get(key);
		return defVal;
	}

	public void putValueOfEachRound(String key, List<?> vals) {
		roundsMap.put(key, new CopyOnWriteArrayList<>(vals));
	}

	public void putQueuesOfEachRound(String key, List<Queue<?>> vals) {
		roundQueuesMap.put(key, new CopyOnWriteArrayList<Queue<?>>(vals));
	}

	public void putQueue(String key, List<?> vals) {
		queuesMap.put(key, new ConcurrentLinkedQueue<>(vals));
	}

	public void putDefaultValue(String key, Object defaultVal) {
		defaultsMap.put(key, defaultVal);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public SConstructor<?> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(
				(style == ToExpressionStyle.MULTI_LINE ? System.lineSeparator() + "      " : "")
						+ SConstructor.toConstructor(style, getClass(), playerName, tableName,
								roundQueuesMap,
								roundsMap, queuesMap, defaultsMap));
	}

	public ActorName getPlayerName() {
		return playerName;
	}

	public String getTableName() {
		return tableName;
	}

}
