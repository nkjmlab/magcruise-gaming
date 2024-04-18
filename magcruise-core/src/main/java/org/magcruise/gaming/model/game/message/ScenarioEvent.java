package org.magcruise.gaming.model.game.message;

import org.magcruise.gaming.lang.Properties;
import org.magcruise.gaming.model.game.ActorName;
import gnu.lists.Pair;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class ScenarioEvent extends GameEvent {

	protected Symbol name;

	public ScenarioEvent(ActorName from, ActorName to, Symbol name, Properties props) {
		super(from, to, props);
		this.name = name;
	}

	public ScenarioEvent(ActorName from, ActorName to, Symbol name, Pair... pairs) {
		this(from, to, name, new Properties(pairs));
	}

	public boolean isNamed(Symbol name) {
		return this.name.toString().equals(name.toString());
	}

	@Override
	public Object[] getConstractorArgs() {
		return new Object[] { from, to, name, props };
	}

}
