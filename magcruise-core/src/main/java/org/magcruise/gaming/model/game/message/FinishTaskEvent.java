package org.magcruise.gaming.model.game.message;

import org.magcruise.gaming.model.game.ActorName;

@SuppressWarnings("serial")
public class FinishTaskEvent extends GameEvent {

	public FinishTaskEvent(ActorName to) {
		super(ActorName.DEFAULT_CONTEXT_NAME, to);
	}

	public FinishTaskEvent(ActorName from, ActorName to) {
		super(from, to);
	}

}
