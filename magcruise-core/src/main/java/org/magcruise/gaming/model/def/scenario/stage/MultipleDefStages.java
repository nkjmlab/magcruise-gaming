package org.magcruise.gaming.model.def.scenario.stage;

import java.util.List;
import org.magcruise.gaming.model.def.common.MultipleElements;
import org.magcruise.gaming.model.def.scenario.round.RoundElement;
import org.magcruise.gaming.model.def.scenario.task.StageElement;

@SuppressWarnings("serial")
public class MultipleDefStages extends MultipleElements<DefStage>
		implements RoundElement, StageElement {

	public MultipleDefStages() {
	}

	public MultipleDefStages(List<DefStage> result) {
		super(result);
	}

	@Override
	public String getExpression(ToExpressionStyle style) {
		return RoundElement.super.getExpression(style);
	}

}
