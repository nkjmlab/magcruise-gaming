package org.magcruise.gaming.model.def.actor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.common.KeyValueTable;
import org.magcruise.gaming.common.KeyValueTables;
import org.magcruise.gaming.lang.Properties;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.def.GameBuilder;
import org.magcruise.gaming.model.def.scenario.DefObjectRegistration;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.model.game.ContextParameter;
import org.magcruise.gaming.model.game.History;
import org.magcruise.gaming.model.game.MessageBox;
import org.magcruise.gaming.model.game.Player;
import org.magcruise.gaming.model.game.PlayerParameter;
import org.magcruise.gaming.model.game.Players;
import org.magcruise.gaming.model.game.SimpleContext;
import org.magcruise.gaming.model.game.message.GameMessage;
import org.magcruise.gaming.util.LiteralUtils;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class ContextBuilder implements SConstructive {
	private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger();

	private List<DefContextProperty> properties = new ArrayList<>();
	private Map<ActorName, KeyValueTables> keyValueTablesMap;
	private DefRevertContext defRevertContext;

	public ContextBuilder() {
		this(new ArrayList<>(), new LinkedHashMap<>());
	}

	public ContextBuilder(List<DefContextProperty> properties,
			Map<ActorName, KeyValueTables> defKeyValueTables) {
		this.properties = properties;
		this.keyValueTablesMap = defKeyValueTables;
	}

	@Override
	public SConstructor<? extends ContextBuilder> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), properties, keyValueTablesMap);
	}

	public Context build(GameBuilder gameBuilder) {
		if (defRevertContext != null) {
			Context ctx = defRevertContext.getContext();
			log.info("processId in reverted ctx is {}, processId in current gamebuilder is {}.",
					ctx.getProcessId(), gameBuilder.getProcessId());
			ctx.setGameBuilder(gameBuilder);
			return ctx;
		}

		log.debug("Start of create context...");
		Context context = SchemeEnvironment.applyOnTemporalEnvironment((environmentName) -> {
			Object[] defaultArgs = { getDefContext().getName(), 0, new Properties(), new History(),
					new MessageBox<GameMessage>(), getRoundValidation(), getMaxAutoResponseTime(),
					getDefAssignRequests(), createPlayers(gameBuilder.getProcessId()),
					createRegisteredObjects(gameBuilder.getProcessId(), getDefRegisteredObjects()),
					keyValueTablesMap.getOrDefault(getDefContext().getName(), new KeyValueTables()),
					gameBuilder };
			ContextParameter contextParameter = SchemeEnvironment.makeInstance(environmentName,
					ContextParameter.class, defaultArgs);

			Object[] contextArgs = { contextParameter };
			if (getDefContext().getArgs().length != 0) {
				contextArgs = ArrayUtils.addAll(contextArgs, getDefContext().getArgs());
			}

			return SchemeEnvironment.makeInstance(environmentName,
					getDefContext().getInstanceClass(),
					contextArgs);
		});
		log.debug("End of create context...");
		if (context == null) {
			throw new RuntimeException(" context has not created yet.");
		}
		context.setRoundnum(getStartRoundnum());
		return context;
	}

	private List<DefObjectRegistration> getDefRegisteredObjects() {
		return getDefContextProperties(DefObjectRegistration.class);
	}

	private DefContext getDefContext() {
		return getDefContextProperty(DefContext.class)
				.orElse(new DefContext(ActorName.DEFAULT_CONTEXT_NAME, SimpleContext.class));
	}

	private int getMaxAutoResponseTime() {
		return getDefContextProperty(DefMaxAutoResponseTime.class)
				.orElse(new DefMaxAutoResponseTime(0))
				.intValue();
	}

	private boolean getRoundValidation() {
		return getDefContextProperty(DefRoundValidationInput.class)
				.orElse(new DefRoundValidationInput(false)).getFlag();
	}

	private RegisteredObjects createRegisteredObjects(ProcessId processId,
			List<DefObjectRegistration> defRegisteredObjects) {
		Map<Symbol, SConstructive> tmp = new HashMap<>();
		defRegisteredObjects.forEach(def -> {
			SConstructive obj = SchemeEnvironment.makeInstance(processId.getValue(),
					def.getInstanceClass(), def.getArgs());
			tmp.put(def.getName(), obj);
		});
		return new RegisteredObjects(tmp);
	}

	private Players<? extends Player> createPlayers(ProcessId processId) {
		log.debug("Start of create players...");
		List<Player> result = new ArrayList<>();
		getDefPlayers().forEach((DefPlayer defPlayer) -> {
			Object[] playerParameterArgs = { defPlayer.getName(), defPlayer.getPlayerType(),
					DefAssignmentRequest.ANONYMOUS_OPERATOR,
					new Properties(), new History(), new MessageBox<GameMessage>(), getAutoInput(),
					keyValueTablesMap.getOrDefault(defPlayer.getName(), new KeyValueTables()) };

			PlayerParameter playerParameter = SchemeEnvironment.makeInstance(
					processId.getValue(),
					PlayerParameter.class, playerParameterArgs);

			Object[] playerArgs = { playerParameter };
			if (defPlayer.getArgs().length != 0) {
				playerArgs = LiteralUtils.addToHead(playerParameter, defPlayer.getArgs());
			}
			result.add(SchemeEnvironment.makeInstance(processId.getValue(),
					defPlayer.getInstanceClass(), playerArgs));
		});
		log.debug("End of create players...");
		Players<? extends Player> players = new Players<>(result);
		return players;
	}

	private boolean getAutoInput() {
		return getDefContextProperty(DefAutoInput.class).orElse(new DefAutoInput(false)).getFlag();
	}

	public ContextBuilder addDefPlayers(Object... defPlayers) {
		Arrays.asList(defPlayers).forEach(_defPlayers -> {
			if (_defPlayers instanceof DefPlayer) {
				addProperties((DefPlayer) _defPlayers);
			} else if (_defPlayers instanceof List) {
				((List<?>) _defPlayers).forEach(defPlayer -> {
					addProperties((DefPlayer) defPlayer);
				});
			}
		});
		return this;
	}

	public ContextBuilder addDefAssignmentRequests(DefAssignmentRequest... defAssignRequests) {
		addProperties(defAssignRequests);
		return this;
	}

	private List<DefAssignmentRequest> getDefAssignRequests() {
		return getDefContextProperties(DefAssignmentRequest.class);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public List<DefPlayer> getDefPlayers() {
		return getDefContextProperties(DefPlayer.class);
	}

	public Context getRevertContext() {
		if (defRevertContext == null) {
			return null;
		} else {
			return defRevertContext.getContext();
		}

	}

	public void setStartFromEndOfRound(DefRevertContext defRevertContext, int finishedRoundnum) {
		log.info("Registered context will be used. "
				+ "Definitions of Context and Players in def:setup-game-builder will be ignored.");
		log.info("Registered context will be used. {}", defRevertContext);
		defRevertContext.revertToEndOfRound(finishedRoundnum);
		defRevertContext.setRoundnum(finishedRoundnum + 1);
		this.defRevertContext = defRevertContext;
		log.debug("Context was reverted. {}", defRevertContext);
	}

	public void setDefKeyValueTables(KeyValueTable... keyValueTables) {
		for (KeyValueTable table : keyValueTables) {
			keyValueTablesMap.putIfAbsent(table.getPlayerName(), new KeyValueTables());
			keyValueTablesMap.get(table.getPlayerName()).putTable(table);
		}
	}

	public void addProperties(DefContextProperty... props) {
		Arrays.asList(props).forEach(p -> {
			if (p instanceof DefAutoInput || p instanceof DefRoundValidationInput
					|| p instanceof DefMaxAutoResponseTime || p instanceof DefStartRoundnum
					|| p instanceof DefContext) {
				setProperty(p);
			} else {
				addProperty(p);
			}
		});

	}

	private void addProperty(DefContextProperty p) {
		properties.add(p);
	}

	private int getStartRoundnum() {
		return getDefContextProperty(DefStartRoundnum.class).orElse(new DefStartRoundnum(0))
				.intValue();
	}

	@SuppressWarnings("unchecked")
	private <T extends DefContextProperty> List<T> getDefContextProperties(Class<T> clazz) {
		return (List<T>) properties.stream()
				.filter(p -> p.getClass().getName().equals(clazz.getName()))
				.collect(Collectors.toList());

	}

	@SuppressWarnings("unchecked")
	private <T extends DefContextProperty> Optional<T> getDefContextProperty(Class<T> clazz) {
		return (Optional<T>) properties.stream()
				.filter(p -> p.getClass().getName().equals(clazz.getName())).findFirst();

	}

	private void setProperty(DefContextProperty element) {
		getDefContextProperty(element.getClass()).ifPresent(e -> properties.remove(e));
		properties.add(element);
	}

}
