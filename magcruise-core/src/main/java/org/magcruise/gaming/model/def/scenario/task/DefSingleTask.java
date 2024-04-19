package org.magcruise.gaming.model.def.scenario.task;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.model.game.ActorName;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public abstract class DefSingleTask implements DefTask {
	protected static Logger log = LogManager.getLogger();

	protected ActorName playerName;
	protected Symbol procName;

	public DefSingleTask(ActorName player, Symbol procName) {
		this.playerName = player;
		this.procName = procName;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public ActorName getPlayerName() {
		return this.playerName;
	}

	public Symbol getProcName() {
		return procName;
	}

}
