package org.magcruise.gaming.model.def;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.common.KeyValueTable;
import org.magcruise.gaming.lang.Properties;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.def.actor.ContextBuilder;
import org.magcruise.gaming.model.def.actor.DefAssignmentRequest;
import org.magcruise.gaming.model.def.actor.DefAutoInput;
import org.magcruise.gaming.model.def.actor.DefContext;
import org.magcruise.gaming.model.def.actor.DefMaxAutoResponseTime;
import org.magcruise.gaming.model.def.actor.DefRevertContext;
import org.magcruise.gaming.model.def.actor.DefRoundValidationInput;
import org.magcruise.gaming.model.def.actor.DefStartRoundnum;
import org.magcruise.gaming.model.def.boot.BootstrapBuilder;
import org.magcruise.gaming.model.def.scenario.DefFinalRound;
import org.magcruise.gaming.model.def.scenario.DefObjectRegistration;
import org.magcruise.gaming.model.def.scenario.GameScenarioBuilder;
import org.magcruise.gaming.model.def.sys.DefUI;
import org.magcruise.gaming.model.def.sys.GameSystemPropertiesBuilder;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.model.game.Game;
import gnu.lists.LList;
import gnu.mapping.Symbol;

/**
 * Schemeから呼び出される可能性のあるものはデバッグしやすいようにtry-catchでラップ
 *
 * @author nkjm
 *
 */
@SuppressWarnings("serial")
public class GameBuilder implements SConstructive {
	private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger();

	private BootstrapBuilder bootstrapBuilder;
	private GameSystemPropertiesBuilder gameSystemPropertiesBuilder;
	private ContextBuilder contextBuilder;
	private GameScenarioBuilder gameScenarioBuilder;
	private ProcessId processId;

	public GameBuilder(ProcessId processId) {
		this(new BootstrapBuilder(), new GameSystemPropertiesBuilder(), new ContextBuilder(),
				new GameScenarioBuilder(), processId);
	}

	public GameBuilder() {
		this(ProcessId.getNewProcessId());
	}

	public GameBuilder(BootstrapBuilder bootstrapBuilder,
			GameSystemPropertiesBuilder gameSystemPropertiesBuilder, ContextBuilder contexBuilder,
			GameScenarioBuilder scenarioBuilder, ProcessId processId) {
		this.bootstrapBuilder = bootstrapBuilder;
		this.gameSystemPropertiesBuilder = gameSystemPropertiesBuilder;
		this.contextBuilder = contexBuilder;
		this.gameScenarioBuilder = scenarioBuilder;
		this.processId = processId;
	}

	@Override
	public SConstructor<? extends GameBuilder> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(
				(style == ToExpressionStyle.MULTI_LINE ? System.lineSeparator() : "")
						+ SConstructor.toConstructor(style, getClass(), bootstrapBuilder,
								gameSystemPropertiesBuilder, contextBuilder, gameScenarioBuilder,
								processId));
	}

	public GameBuilder addDefContext(DefContext defContext) {
		setDefContext(defContext);
		return this;
	}

	public GameBuilder setDefContext(DefContext defContext) {
		contextBuilder.addProperties(defContext);
		return this;
	}

	public GameBuilder addDefPlayers(Object... defPlayers) {
		contextBuilder.addDefPlayers(defPlayers);
		return this;
	}

	public GameBuilder addDefAssignmentRequests(DefAssignmentRequest... defAssignRequests) {
		contextBuilder.addDefAssignmentRequests(defAssignRequests);
		return this;
	}

	public GameBuilder addAssignmentRequests(List<Symbol> playerNames, List<Symbol> operatorIds) {
		for (int i = 0; i < playerNames.size(); i++) {
			contextBuilder.addDefAssignmentRequests(new DefAssignmentRequest(
					ActorName.of(playerNames.get(i)), operatorIds.get(i).toString()));
		}
		return this;
	}

	public LList getPlayerNames() {
		List<ActorName> names = new ArrayList<>();
		contextBuilder.getDefPlayers().forEach(p -> {
			names.add(p.getName());
		});
		return LList.makeList(names);
	}

	public GameBuilder addDefRounds(Object... defRounds) {
		gameScenarioBuilder.addRounds(defRounds);
		return this;

	}

	public Game build() {
		String environmentName = processId.getValue();

		bootstrapBuilder.setUpBootstrapBuilder(processId);
		SchemeEnvironment.beforeSetupGameBuilder(environmentName, this);
		SchemeEnvironment.revertContext(environmentName, this);

		SchemeEnvironment.setupGameBuilder(environmentName, this);
		SchemeEnvironment.afterSetupGameBuilder(environmentName, this);

		gameSystemPropertiesBuilder.build(processId.getValue());
		Context ctx;
		if (contextBuilder.getRevertContext() == null) {
			ctx = contextBuilder.build(this);
		} else {
			ctx = contextBuilder.getRevertContext();
			log.info("processId in reverted ctx is {}, processId in current gamebuilder is {}.",
					ctx.getProcessId(), processId);
			ctx.setGameBuilder(this);
		}
		log.debug("ProcessId of GameBuilder in ctx is {}", ctx.getGameBuilder().getProcessId());

		Game g = new Game(ctx, gameScenarioBuilder.build());
		SchemeEnvironment.afterBuildGame(environmentName, g);
		return g;

	}

	public GameBuilder setStartRoundnum(int roundnum) {
		contextBuilder.addProperties(new DefStartRoundnum(roundnum));
		return this;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	private Properties props = new Properties();

	public Serializable get(Symbol key) {
		Serializable val = this.props.get(key);
		return val;
	}

	public void set(Symbol key, Serializable val) {
		this.props.put(key, val);
	}

	public GameBuilder setContextBuilder(ContextBuilder contextBuilder) {
		this.contextBuilder = contextBuilder;
		return this;
	}

	public GameBuilder setStartFromEndOfRound(Context context, int finishedRoundnum) {
		contextBuilder.setStartFromEndOfRound(new DefRevertContext(context), finishedRoundnum);
		return this;
	}

	public GameBuilder setAutoInput(boolean autoInput) {
		contextBuilder.addProperties(new DefAutoInput(autoInput));
		return this;
	}

	public GameBuilder setRoundValidationInput(boolean roundValidation) {
		contextBuilder.addProperties(new DefRoundValidationInput(roundValidation));
		return this;
	}

	public GameBuilder setMaxAutoResponseTime(int maxAutoResponseTime) {
		contextBuilder.addProperties(new DefMaxAutoResponseTime(maxAutoResponseTime));
		return this;
	}

	public GameBuilder setFinalRound(int roundnum) {
		gameScenarioBuilder.addProperties(new DefFinalRound(roundnum));
		return this;
	}

	public GameSystemPropertiesBuilder getGameSystemPropertiesBuilder() {
		return gameSystemPropertiesBuilder;
	}

	public BootstrapBuilder getBootstrapBuilder() {
		return bootstrapBuilder;
	}

	public String getEnvironmentName() {
		return processId.toString();
	}

	public ProcessId getProcessId() {
		return processId;
	}

	public GameBuilder addDefUI(DefUI defUI) {
		gameSystemPropertiesBuilder.addProperties(defUI);
		return this;
	}

	public String getDefineDefCreateGameBuilder() {
		return "(define (def:create-game-builder) ::GameBuilder " + toConstructor().getExpression()
				+ ")";
	}

	public void setProcessId(ProcessId processId) {
		this.processId = processId;
	}

	public GameBuilder setDefKeyValueTables(KeyValueTable... keyValueTables) {
		contextBuilder.setDefKeyValueTables(keyValueTables);
		return this;
	}

	public GameBuilder addDefObjectRegistrations(DefObjectRegistration... defObjects) {
		contextBuilder.addProperties(defObjects);
		return this;
	}

	public ContextBuilder getContextBuilder() {
		return contextBuilder;
	}

	public GameScenarioBuilder getGameScenarioBuilder() {
		return gameScenarioBuilder;
	}

}
