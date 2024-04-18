package org.magcruise.gaming.model.def.actor;

import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.Player;
import org.magcruise.gaming.model.game.Player.PlayerType;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class DefPlayer extends DefActorElements<Player> {

	private PlayerType playerType;

	public DefPlayer(Symbol name, Symbol playerType, Class<? extends Player> clazz,
			Object... args) {
		this(ActorName.of(name), PlayerType.of(playerType), clazz, args);
	}

	public DefPlayer(ActorName name, PlayerType playerType, Class<? extends Player> clazz,
			Object... args) {
		super(name, clazz, args);
		this.playerType = playerType;
	}

	public PlayerType getPlayerType() {
		return playerType;
	}

	@Override
	public Class<? extends Player> getInstanceClass() {
		return instanceClass;
	}

	@Override
	public SConstructor<? extends DefPlayer> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), name, playerType, instanceClass, args);
	}

}
