package org.magcruise.gaming.model.def.scenario.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.message.GameEvent;
import org.magcruise.gaming.util.LiteralUtils;
import gnu.expr.Keyword;
import gnu.lists.LList;
import gnu.mapping.Symbol;

public class DefTaskFactory {
	private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger();

	public static MultipleDefTasks create(LList args) {
		log.debug(args);
		List<DefTask> result = new ArrayList<>();
		Map<Keyword, Object> argsMap = LiteralUtils.createArgsMap(args);

		Object actor = argsMap.getOrDefault(Keyword.make("actor"), Symbol.parse("Context"));
		Symbol action = (Symbol) argsMap.get(Keyword.make("action"));
		Symbol type = (Symbol) argsMap.getOrDefault(Keyword.make("type"), Symbol.parse("default"));
		if (action == null) {
			throw new RuntimeException("action is required.");
		}

		if (actor instanceof List) {
			((List<?>) actor).forEach(a -> {
				result.add(createTask(argsMap, type, ActorName.of((Symbol) a), action));
			});

		} else if (actor instanceof Symbol) {
			result.add(createTask(argsMap, type, ActorName.of((Symbol) actor), action));
		}

		return new MultipleDefTasks(result);
	}

	private static DefTask createTask(Map<Keyword, Object> argsMap, Symbol type, ActorName actor,
			Symbol method) {
		if (type.toString().equals("event-driven")) {
			@SuppressWarnings("unchecked")
			Class<? extends GameEvent> clazz = (Class<? extends GameEvent>) argsMap
					.getOrDefault(Keyword.make("event"), GameEvent.class);
			return new DefEventDrivenTask(actor, method, clazz);
		} else {
			return new DefPlayerTask(actor, method);
		}
	}

}
