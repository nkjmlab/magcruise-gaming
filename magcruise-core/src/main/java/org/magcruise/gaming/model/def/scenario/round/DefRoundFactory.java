package org.magcruise.gaming.model.def.scenario.round;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.magcruise.gaming.model.def.scenario.stage.DefStage;
import org.magcruise.gaming.util.LiteralUtils;
import gnu.expr.Keyword;
import gnu.lists.LList;
import gnu.mapping.Symbol;

public class DefRoundFactory {
	private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger();

	public static MultipleDefRounds create(LList args) {
		log.debug(args);
		List<DefRound> result = new ArrayList<>();
		Map<Keyword, Object> argsMap = LiteralUtils.createArgsMap(args);

		int repeat = ((Number) argsMap.getOrDefault(Keyword.make("repeat"), 1)).intValue();
		Symbol name = (Symbol) argsMap.getOrDefault(Keyword.make("name"),
				Symbol.parse(String.valueOf(System.currentTimeMillis())));
		@SuppressWarnings("unchecked")
		List<DefStage> rest = (List<DefStage>) argsMap.getOrDefault(Keyword.make("rest"),
				new ArrayList<>());

		for (int i = 0; i < repeat; i++) {
			result.add(new DefRound(name.toString(), rest));
		}

		return new MultipleDefRounds(result);
	}

}
