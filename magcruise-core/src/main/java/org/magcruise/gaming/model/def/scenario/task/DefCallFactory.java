package org.magcruise.gaming.model.def.scenario.task;

import java.util.Map;
import org.magcruise.gaming.util.LiteralUtils;
import gnu.expr.Keyword;
import gnu.lists.LList;
import gnu.mapping.Symbol;

public class DefCallFactory {
	protected static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger();

	public static DefCall create(LList args) {
		Map<Keyword, Object> argsMap = LiteralUtils.createArgsMap(args);

		Symbol object = (Symbol) argsMap.get(Keyword.make("object"));

		Symbol method = (Symbol) argsMap.get(Keyword.make("method"));

		if (object == null || method == null) {
			throw new RuntimeException("object and method are required.");
		}

		return new DefCall(object, method);
	}

}
