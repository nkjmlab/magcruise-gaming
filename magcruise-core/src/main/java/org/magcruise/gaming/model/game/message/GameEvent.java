package org.magcruise.gaming.model.game.message;

import org.magcruise.gaming.lang.Properties;
import org.magcruise.gaming.model.game.ActorName;

@SuppressWarnings("serial")
public class GameEvent extends GameMessage {

	public GameEvent(ActorName from, ActorName to) {
		super(from, to);
	}

	public GameEvent(ActorName from, ActorName to, Properties props) {
		super(from, to, props);
	}

	@Override
	public Object[] getConstractorArgs() {
		return new Object[] { from, to, props };
	}

}
