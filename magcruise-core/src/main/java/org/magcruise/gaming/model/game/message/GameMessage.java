package org.magcruise.gaming.model.game.message;

import org.magcruise.gaming.lang.Properties;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.ActorName;
import gnu.lists.Pair;

/**
 * The Message for interacting with other {@code Player} and {@code Context}.
 *
 * @author nkjm
 *
 */
@SuppressWarnings("serial")
public abstract class GameMessage extends SimpleMessage {

	public ActorName from;

	public ActorName to;

	/**
	 * Constructs a {@code} GameMessage}.
	 *
	 * @param from
	 * @param to the recipient of this message. the {@code to} is the name of a player or the name of
	 *        the context.
	 */
	public GameMessage(ActorName from, ActorName to) {
		this.from = from;
		this.to = to;
	}

	public GameMessage(ActorName from, ActorName to, Properties props) {
		super(props);
		this.from = from;
		this.to = to;
	}

	public GameMessage(ActorName from, ActorName to, Pair... pairs) {
		this(from, to, new Properties(pairs));
	}

	@Override
	public SConstructor<? extends GameMessage> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), getConstractorArgs());
	}

	public abstract Object[] getConstractorArgs();

	public ActorName getFrom() {
		return from;
	}

	public ActorName getTo() {
		return to;
	}

}
