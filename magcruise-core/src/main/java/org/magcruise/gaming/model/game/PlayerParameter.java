package org.magcruise.gaming.model.game;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.common.KeyValueTables;
import org.magcruise.gaming.lang.Properties;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.Player.PlayerType;
import org.magcruise.gaming.model.game.message.GameMessage;

@SuppressWarnings("serial")
public class PlayerParameter implements SConstructive {

	private ActorName playerName;
	private PlayerType playerType;
	private String operatorId;
	private Properties props;
	private History history;
	private MessageBox<GameMessage> msgbox;
	private boolean autoInput;
	private KeyValueTables keyValueTable;

	public PlayerParameter() {
		this(ActorName.of("player0"), PlayerType.HUMAN, "", new Properties(), new History(),
				new MessageBox<GameMessage>(), false, new KeyValueTables());
	}

	public PlayerParameter(ActorName playerName, PlayerType playerType, String operatorId,
			Properties props, History history, MessageBox<GameMessage> msgbox, boolean autoInput,
			KeyValueTables defaultInputs) {
		this.playerName = playerName;
		this.playerType = playerType;
		this.operatorId = operatorId;
		this.props = props;
		this.history = history;
		this.msgbox = msgbox;
		this.autoInput = autoInput;
		this.keyValueTable = defaultInputs;
	}

	@Override
	public SConstructor<? extends PlayerParameter> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), playerName, playerType, operatorId,
				props,
				history, msgbox, autoInput, keyValueTable);
	}

	public ActorName getPlayerName() {
		return playerName;
	}

	public PlayerType getPlayerType() {
		return playerType;
	}

	public String getOperatorId() {
		return operatorId;
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

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public boolean getAutoInput() {
		return autoInput;
	}

	public KeyValueTables getKeyValueTable() {
		return this.keyValueTable;
	}

}
