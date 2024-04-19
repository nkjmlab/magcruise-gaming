package org.magcruise.gaming.model.def.scenario.stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.magcruise.gaming.model.def.scenario.task.StageElement;
import org.magcruise.gaming.util.LiteralUtils;
import gnu.expr.Keyword;
import gnu.lists.LList;
import gnu.mapping.Symbol;

public class DefStageFactory {
	private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger();

	public static MultipleDefStages create(LList args) {
		log.debug(args);
		List<DefStage> result = new ArrayList<>();
		Map<Keyword, Object> argsMap = LiteralUtils.createArgsMap(args);

		Symbol type = (Symbol) argsMap.getOrDefault(Keyword.make("type"),
				Symbol.parse("sequential"));
		Symbol name = (Symbol) argsMap.getOrDefault(Keyword.make("name"),
				Symbol.parse(String.valueOf(System.currentTimeMillis())));
		@SuppressWarnings("unchecked")
		List<StageElement> rest = (List<StageElement>) argsMap.getOrDefault(Keyword.make("rest"),
				new ArrayList<>());
		switch (type.toString()) {
		case "sequential":
			result.add(new DefSequentialStage(name, rest));
			break;
		case "parallel":
			result.add(new DefParallelStage(name, rest));
			break;
		default:
			result.add(new DefSequentialStage(name, rest));
		}

		return new MultipleDefStages(result);
	}

}
