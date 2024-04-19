package org.magcruise.gaming.model.def.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.magcruise.gaming.model.game.Player;
import org.magcruise.gaming.util.LiteralUtils;
import gnu.expr.Keyword;
import gnu.lists.LList;
import gnu.mapping.Symbol;

public class DefPlayerFactory {
	protected static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger();

	public static List<DefPlayer> create(LList args) {
		List<DefPlayer> result = new ArrayList<>();
		Map<Keyword, Object> argsMap = LiteralUtils.createArgsMap(args);

		Object name = argsMap.get(Keyword.make("name"));
		Symbol type = (Symbol) argsMap.get(Keyword.make("type"));
		@SuppressWarnings("unchecked")
		Class<? extends Player> clazz = (Class<? extends Player>) argsMap
				.get(Keyword.make("class"));
		List<?> rest = (List<?>) argsMap.getOrDefault(Keyword.make("rest"), new ArrayList<>());

		if (name == null || type == null || clazz == null) {
			throw new RuntimeException("name, type and class are required.");
		}

		if (name instanceof List) {
			@SuppressWarnings("unchecked")
			List<Symbol> names = (List<Symbol>) name;
			names.forEach(n -> {
				result.add(new DefPlayer(n, type, clazz, rest.toArray(new Object[0])));
			});

		} else if (name instanceof Symbol) {
			result.add(new DefPlayer((Symbol) name, type, clazz, rest.toArray(new Object[0])));
		}

		return result;
	}

}
