package org.magcruise.gaming.model.def.scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.util.LiteralUtils;
import gnu.expr.Keyword;
import gnu.lists.LList;
import gnu.mapping.Symbol;

public class DefObjectRegistrationFactory {
	protected static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger();

	public static DefObjectRegistration create(LList args) {
		Map<Keyword, Object> argsMap = LiteralUtils.createArgsMap(args);

		Symbol name = (Symbol) argsMap.get(Keyword.make("name"));
		@SuppressWarnings("unchecked")
		Class<? extends Context> clazz = (Class<? extends Context>) argsMap
				.get(Keyword.make("class"));
		List<?> rest = (List<?>) argsMap.getOrDefault(Keyword.make("rest"), new ArrayList<>());

		if (name == null || clazz == null) {
			throw new RuntimeException("name and class are required.");
		}

		return new DefObjectRegistration(name, clazz, rest.toArray(new Object[0]));
	}

}
