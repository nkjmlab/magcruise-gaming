package org.magcruise.gaming.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.util.LiteralUtils;
import gnu.expr.Keyword;
import gnu.lists.LList;
import gnu.lists.Pair;
import gnu.mapping.Symbol;

public class KeyValueTableFactory {

	public static KeyValueTable create(LList args) {
		Map<Keyword, Object> argsMap = LiteralUtils.createArgsMap(args);

		Symbol tableName = (Symbol) argsMap.get(Keyword.make("table-name"));
		Symbol actor = (Symbol) argsMap.get(Keyword.make("actor"));
		List<?> rest = (List<?>) argsMap.get(Keyword.make("rest"));

		KeyValueTable result = new KeyValueTable(ActorName.of(actor), tableName.toString());
		drainTo(result, LList.makeList(rest));
		return result;

	}

	private static void drainTo(KeyValueTable result, LList args) {
		if (args.isEmpty()) {
			return;
		}
		Pair pair = (Pair) args;
		LList arg = (LList) pair.getCar();

		Map<Keyword, Object> argsMap = LiteralUtils.createArgsMap(arg);
		Symbol name = (Symbol) argsMap.get(Keyword.make("name"));
		Object defaultVal = argsMap.get(Keyword.make("default"));
		LList round = (LList) argsMap.get(Keyword.make("round"));
		LList queue = (LList) argsMap.get(Keyword.make("queue"));
		LList roundQueues = (LList) argsMap.get(Keyword.make("round-queues"));
		if (round != null) {
			result.putValueOfEachRound(name.toString(), round);
		}
		if (roundQueues != null) {
			List<Queue<?>> qs = new CopyOnWriteArrayList<>();
			for (Object o : new ArrayList<>(roundQueues)) {
				qs.add(new ConcurrentLinkedQueue<>((List<?>) o));
			}
			result.putQueuesOfEachRound(name.toString(), qs);
		}

		if (queue != null) {
			result.putQueue(name.toString(), queue);
		}
		if (defaultVal != null) {
			result.putDefaultValue(name.toString(), defaultVal);
		}

		drainTo(result, (LList) pair.getCdr());

	}

}
