package org.magcruise.gaming.model.game.message;

import org.magcruise.gaming.lang.Message;
import org.magcruise.gaming.lang.Properties;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.GameObject;
import gnu.lists.Pair;

@SuppressWarnings("serial")
public class SimpleMessage extends GameObject implements Message {

	public SimpleMessage() {
		this(new Properties());
	}

	public SimpleMessage(Properties props) {
		super(props);
	}

	public SimpleMessage(Pair... pairs) {
		this(new Properties(pairs));
	}

	@Override
	public SConstructor<? extends SimpleMessage> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), props);
	}

}
