package org.magcruise.gaming.model.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.magcruise.gaming.lang.Parameters;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.lang.SimpleSExpression;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.def.GameBuilder;
import org.magcruise.gaming.model.def.actor.DefAssignmentRequest;
import org.magcruise.gaming.model.def.actor.RegisteredObjects;
import org.magcruise.gaming.model.def.sys.GameSystemProperties;
import org.magcruise.gaming.ui.model.Form;
import gnu.lists.LList;
import gnu.lists.Pair;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public abstract class Context extends ActorObject {

	/**
	 * ゲームシナリオからアクセスしやすいように特別にpublicにしている．ゲームシナリオ以外からはgetterでアクセスすること．
	 */
	public volatile int roundnum = 0;

	/**
	 * ゲームシナリオからアクセスしやすいように特別にpublicにしている．ゲームシナリオ以外からはgetterでアクセスすること．
	 */
	public Players<? extends Player> players;

	protected GameBuilder gameBuilder;

	protected ContextParameter contextParameter;

	private List<DefAssignmentRequest> defAssignmentRequests;

	/** The scenario arrived at the end. **/
	private volatile boolean finished = false;

	private RegisteredObjects registeredObjects;
	protected boolean roundValidation = false;

	private int maxAutoResponseTime = 0;

	private GameSystemProperties gameSystemProperties;

	private boolean unexpectedShutdown;

	public Context(ContextParameter contextParameter) {
		super(contextParameter.getProps(), contextParameter.getHistory(),
				contextParameter.getMsgbox(),
				contextParameter.getKeyValueTable());
		this.name = contextParameter.getName();
		this.players = contextParameter.getPlayers();
		this.registeredObjects = contextParameter.getRegisteredObjects();
		this.gameBuilder = contextParameter.getGameBuilder();
		this.roundnum = contextParameter.getRoundnum();
		this.contextParameter = contextParameter;
		this.defAssignmentRequests = contextParameter.getDefAssignRequests();
		this.roundValidation = contextParameter.getRoundValidation();
		this.maxAutoResponseTime = contextParameter.getMaxAutoResponseTime();
		this.gameSystemProperties = this.gameBuilder.getGameSystemPropertiesBuilder()
				.build(getEnvironmentName());
	}

	@Override
	public Object[] getConstractorArgs() {
		return new Object[] { getContextParameter() };
	}

	@Override
	public SConstructor<? extends Context> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(
				(style == ToExpressionStyle.MULTI_LINE ? System.lineSeparator() : "")
						+ SConstructor.toConstructor(style, getClass(), getContextParameter()));
	}

	private ContextParameter getContextParameter() {
		return new ContextParameter(name, roundnum, props, history, msgbox, roundValidation,
				maxAutoResponseTime, defAssignmentRequests, players, registeredObjects,
				keyValueTables,
				gameBuilder);
	}

	public Player getPlayer(ActorName name) {
		return players.get(name);
	}

	public <T extends Player> T getPlayer(Class<T> clazz) {
		return players.get(clazz);
	}

	public <T extends Player> T getPlayerAs(Class<T> clazz, ActorName name) {
		return clazz.cast(players.get(name));
	}

	public LList getPlayers(ActorName... names) {
		List<Player> result = new ArrayList<>();
		for (ActorName name : names) {
			result.add(players.get(name));
		}
		return Pair.makeList(result);
	}

	/**
	 * 原則としてログ出力用．外側から触らない．
	 *
	 * @return
	 */
	public GameBuilder getGameBuilder() {
		return gameBuilder;
	}

	public GameSystemProperties getGameSystemProperties() {
		return gameSystemProperties;
	}

	public String getEnvironmentName() {
		return gameBuilder.getProcessId().getValue();
	}

	public ProcessId getProcessId() {
		return gameBuilder.getProcessId();
	}

	public Players<? extends Player> getPlayers() {
		return players;
	}

	public <S extends Player> Players<S> getPlayers(Class<S> clazz) {
		return players.getPlayers(clazz);
	}

	public int getRoundnum() {
		return roundnum;
	}

	public int incrementRoundnum() {
		setRoundnum(getRoundnum() + 1);
		return roundnum;
	}

	public void finalizeRound(int roundnum) {
		super.saveToHistory(roundnum);
		players.forEach(p -> p.saveToHistory(roundnum));
	}

	public void roundValidation() {
		if (!roundValidation) {
			return;
		}
		roundValidation(this);
		players.forEach(p -> p.roundValidation(this));
	}

	@Override
	public void revertToEndOfRound(int roundnum) {
		super.revertToEndOfRound(roundnum);
		players.forEach(p -> p.revertToEndOfRound(roundnum));
	}

	public void setRoundnum(int roundnum) {
		this.roundnum = roundnum;
	}

	public String toSExpressionForRevert(ToExpressionStyle style) {
		String expr = String.format(
				"(define (def:revert-context game-builder ::GameBuilder)"
						+ " (game-builder:setStartFromEndOfRound %s %d))",
				toConstructor(style).getExpression(style), history.size() - 1);
		return new SimpleSExpression(expr).getExpression(style);
	}

	public String getAssignRequestOperatorId(ActorName name) {
		Optional<DefAssignmentRequest> req = defAssignmentRequests.stream()
				.filter(r -> r.getPlayerName().equals(name)).findFirst();

		return req.orElse(new DefAssignmentRequest(name, DefAssignmentRequest.ANONYMOUS_OPERATOR))
				.getOperatorId();

	}

	public void showMessageToAll(String msg, Object... params) {
		players.forEach(p -> p.showMessage(msg, params));
	}

	public void setFinished(boolean b) {
		this.finished = b;
	}

	public boolean isFinished() {
		if (unexpectedShutdown) {
			throw new RuntimeException("unexpectedShutdown");
		}
		return finished;
	}

	public String createMessage(String procedureName, Object... args) {
		try {
			return (String) SchemeEnvironment.applyProcedure(getEnvironmentName(), procedureName,
					args);
		} catch (Throwable e) {
			log.error(e, e);
			throw new RuntimeException(e);
		}
	}

	public Form createForm(String procedureName, Object... args) {
		try {
			return (Form) SchemeEnvironment.applyProcedure(getEnvironmentName(), procedureName,
					args);
		} catch (Throwable e) {
			log.error(e, e);
			throw new RuntimeException(e);
		}
	}

	public Object applyProcedure(String procedureName, Object... args) {
		return SchemeEnvironment.applyProcedure(getEnvironmentName(), procedureName, args);
	}

	public void setProcessId(ProcessId processId) {
		gameBuilder.setProcessId(processId);
	}

	public void setGameBuilder(GameBuilder gameBuilder2) {
		this.gameBuilder = gameBuilder2;
	}

	public SConstructive getRegisteredObject(Symbol name) {
		return this.registeredObjects.get(name);
	}

	public void showMessage(ActorName playerName, String msg, Object... params) {
		getPlayer(playerName).showMessage(msg, params);
	}

	public void showMessage(Collection<ActorName> playerNames, String msg, Object... params) {
		playerNames.forEach(playerName -> showMessage(playerName, msg, params));
	}

	public void syncRequestToInput(ActorName playerName, Form form, Consumer<Parameters> callback) {
		getPlayer(playerName).syncRequestToInput(form, callback);
	}

	public void sendTag(ActorName playerName, String contents) {
		getPlayer(playerName).sendHtmlElement(contents);
	}

	public int getMaxAutoResponseTime() {
		return maxAutoResponseTime;
	}

	public void setUnexpectedShutdown(boolean b) {
		this.unexpectedShutdown = b;

	}

}
