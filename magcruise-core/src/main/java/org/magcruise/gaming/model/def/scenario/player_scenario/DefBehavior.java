package org.magcruise.gaming.model.def.scenario.player_scenario;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.lang.exception.ApplyException;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.model.game.Player;
import org.magcruise.gaming.model.game.message.ScenarioEvent;
import org.nkjmlab.util.java.lang.reflect.ReflectionMethodInvoker;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class DefBehavior implements SConstructive {
	protected static Logger log = LogManager.getLogger();

	protected Symbol cue;
	protected Symbol action;
	protected Symbol nextSceneName;

	public DefBehavior(Symbol cue, Symbol action, Symbol nextSceneName) {
		this.cue = cue;
		this.action = action;
		this.nextSceneName = nextSceneName;
	}

	@Override
	public SConstructor<? extends DefBehavior> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), cue, action, nextSceneName);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public boolean interpretBehavior(Context context, Player self, ScenarioEvent msg)
			throws ApplyException {
		boolean isFired;
		try {
			if (SchemeEnvironment.isDefined(context.getEnvironmentName(), cue.toString())) {
				isFired = (boolean) SchemeEnvironment.applyProcedure(context.getEnvironmentName(),
						cue.toString(), self, context, msg);
			} else {
				isFired = (boolean) ReflectionMethodInvoker.invoke(self, cue.toString(), context,
						msg);
			}

		} catch (Throwable e) {
			log.error(
					System.lineSeparator() + "An exception occurred in: " + this
							+ System.lineSeparator());
			log.error(e, e);
			throw new RuntimeException(e);
		}

		if (isFired) {
			procAction(self, context, msg);
		}
		return isFired;
	}

	protected void procAction(Player self, Context context, ScenarioEvent msg) {
		try {

			if (SchemeEnvironment.isDefined(context.getEnvironmentName(), action.toString())) {
				SchemeEnvironment.applyProcedure(context.getEnvironmentName(), action.toString(),
						self,
						context, msg);
			} else {
				ReflectionMethodInvoker.invoke(self, action.toString(), context, msg);
			}
		} catch (Throwable e) {
			log.error(
					System.lineSeparator() + "An exception occurred in: " + this
							+ System.lineSeparator());
			log.error(e, e);
			throw new RuntimeException(e);
		}

	}

	public Symbol getNextScene() {
		return nextSceneName;
	}

}
