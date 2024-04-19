package org.magcruise.gaming.model.def.actor;

import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.Context;

@SuppressWarnings("serial")
public class DefRevertContext implements DefContextProperty {

	private Context context;

	public DefRevertContext(Context context) {
		this.context = context;
	}

	@Override
	public SConstructor<?> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), context);
	}

	public void revertToEndOfRound(int roundnum) {
		context.revertToEndOfRound(roundnum);
	}

	public void setRoundnum(int roundnum) {
		context.setRoundnum(roundnum);
	}

	public Context getContext() {
		return context;
	}

}
