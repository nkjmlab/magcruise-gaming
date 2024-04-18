package org.magcruise.gaming.model.def.scenario;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.model.def.scenario.round.DefRound;
import org.magcruise.gaming.model.def.scenario.stage.DefStage;
import org.magcruise.gaming.model.game.Context;

/**
 * シナリオの静的なデータを持つ．
 *
 * @author nkjm
 *
 */
public class GameScenario {

	private List<DefRound> defRounds = new ArrayList<>();
	private int finalRound;

	public GameScenario(List<DefRound> defRounds, int finalRound) {
		this.defRounds.addAll(defRounds);
		if (finalRound == -1) {
			this.finalRound = defRounds.size() - 1;
		} else {
			this.finalRound = finalRound;
		}

	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public List<DefStage> getStages(int roundNum) {
		DefRound round = defRounds.get(roundNum);
		return round.getAllStages();
	}

	public boolean hasRound(int roundNum) {
		return roundNum < defRounds.size();
	}

	public boolean hasStageElement(Context ctx, int roundNum, int stageNum, int elementNum) {
		DefRound round = defRounds.get(roundNum);
		return round.hasNextStageElement(ctx, stageNum, elementNum);
	}

	public boolean hasStage(int roundNum, int stageNum) {
		DefRound round = defRounds.get(roundNum);
		return round.hasStage(stageNum);
	}

	public DefRound getRound(int roundNum) {
		return defRounds.get(roundNum);
	}

	public int getFinalRound() {
		return finalRound;
	}

}
