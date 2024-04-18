package org.magcruise.gaming.model.game;

import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.common.KeyValueTables;
import org.magcruise.gaming.lang.Properties;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.def.GameBuilder;
import org.magcruise.gaming.model.def.actor.DefAssignmentRequest;
import org.magcruise.gaming.model.def.actor.RegisteredObjects;
import org.magcruise.gaming.model.game.message.GameMessage;

@SuppressWarnings("serial")
public class ContextParameter implements SConstructive {

	private ActorName name;
	private int roundnum = 0;
	private Properties props;
	private History history;
	private MessageBox<GameMessage> msgbox;
	private boolean roundValidation;
	private int maxAutoResponseTime = 0;

	private Players<? extends Player> players;
	private RegisteredObjects registeredObjects;
	private GameBuilder gameBuilder;
	private List<DefAssignmentRequest> defAssignRequests;
	private KeyValueTables keyValueTable;

	public ContextParameter(ActorName name, int roundnum, Properties props, History history,
			MessageBox<GameMessage> msgbox, boolean roundValidation, int maxAutoResponseTime,
			List<DefAssignmentRequest> defAssignRequests, Players<? extends Player> players,
			RegisteredObjects registeredObjects, KeyValueTables keyValueTable,
			GameBuilder gameBuilder) {
		this.name = name;
		this.roundnum = roundnum;
		this.props = props;
		this.history = history;
		this.msgbox = msgbox;
		this.roundValidation = roundValidation;
		this.maxAutoResponseTime = maxAutoResponseTime;
		this.defAssignRequests = defAssignRequests;
		this.players = players;
		this.registeredObjects = registeredObjects;
		this.keyValueTable = keyValueTable;
		this.gameBuilder = gameBuilder;
	}

	@Override
	public SConstructor<? extends ContextParameter> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), name, roundnum, props, history, msgbox,
				roundValidation, maxAutoResponseTime, defAssignRequests, players, registeredObjects,
				keyValueTable, gameBuilder);
	}

	public int getRoundnum() {
		return roundnum;
	}

	public Players<? extends Player> getPlayers() {
		return players;
	}

	public Properties getProps() {
		return props;
	}

	public History getHistory() {
		return history;
	}

	public MessageBox<GameMessage> getMsgbox() {
		return msgbox;
	}

	public GameBuilder getGameBuilder() {
		return gameBuilder;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public List<DefAssignmentRequest> getDefAssignRequests() {
		return defAssignRequests;
	}

	public ActorName getName() {
		return name;
	}

	public KeyValueTables getKeyValueTable() {
		return keyValueTable;
	}

	public RegisteredObjects getRegisteredObjects() {
		return registeredObjects;
	}

	public boolean getRoundValidation() {
		return roundValidation;
	}

	public int getMaxAutoResponseTime() {
		return maxAutoResponseTime;
	}

}
