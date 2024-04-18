package org.magcruise.gaming.model.def.scenario.stage;

import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.model.game.Context;
import gnu.mapping.Procedure;

@SuppressWarnings("serial")
public class DefCond extends SimpleStageElement {

	private Procedure cond;

	public DefCond(Procedure cond) {
		this.cond = cond;
	}

	public Procedure getCond() {
		return cond;
	}

	public boolean isFire(Context context) {
		try {
			boolean isFired = (boolean) cond.apply1(context);
			return isFired;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.out.println("ERROR");
		return false;
	}

	@Override
	public SConstructor<? extends DefCond> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), cond);
	}

}
