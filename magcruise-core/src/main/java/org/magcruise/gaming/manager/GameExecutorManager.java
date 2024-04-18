package org.magcruise.gaming.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.magcruise.gaming.executor.GameExecutor;
import org.magcruise.gaming.executor.GameMessenger;
import org.magcruise.gaming.executor.api.RequesterToGameExecutor;
import org.magcruise.gaming.executor.api.message.RequestToGameExecutor;
import org.magcruise.gaming.lang.Message;
import org.magcruise.gaming.lang.SExpression;
import org.magcruise.gaming.lang.SMethodCall;
import org.magcruise.gaming.lang.SimpleSExpression;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.def.GameBuilder;
import org.nkjmlab.util.java.lang.RuntimeUtils;

public class GameExecutorManager implements RequesterToGameExecutor {

	private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger();

	private static Map<ProcessId, GameExecutor> gameExecutors = new ConcurrentHashMap<>();

	private static GameExecutorManager instance;

	private GameExecutorManager() {

	}

	public static GameExecutorManager getInstance() {
		if (instance == null) {
			instance = new GameExecutorManager();
		}
		return instance;
	}

	public SMethodCall<GameExecutorManager> getInstanceSMethodCall() {
		return SMethodCall.toSMethodCall(GameExecutorManager.class, GameExecutorManager.class,
				"getInstance");
	}

	public GameMessenger getMessenger(ProcessId processId) {
		return gameExecutors.get(processId).getMessenger();

	}

	public GameExecutor getGameExecutor(ProcessId processId) {
		return gameExecutors.get(processId);

	}

	public SMethodCall<GameMessenger> getMessengerSMethodCall(ProcessId processId) {
		return SMethodCall.toSMethodCall(GameMessenger.class, getInstanceSMethodCall(),
				"getMessenger",
				processId);
	}

	public SMethodCall<GameExecutor> getGameExecutorSMethodCall(ProcessId processId) {
		return SMethodCall.toSMethodCall(GameExecutor.class, getInstanceSMethodCall(),
				"getGameExecutor", processId);
	}

	private SMethodCall<?> getKickStartExecutorSExpression(GameBuilder gameBuilder) {
		return SMethodCall.toSMethodCall(Void.class,
				getGameExecutorSMethodCall(gameBuilder.getProcessId()), "kickStartGame");
	}

	public void registerGameExecutor(ProcessId processId, GameExecutor gameManager) {
		gameExecutors.put(processId, gameManager);
	}

	public GameExecutor createAndRegisterGameExecutor(GameBuilder builder) {
		ProcessId processId = builder.getProcessId();

		GameExecutor gameExecutor = new GameExecutor(builder);
		gameExecutors.put(processId, gameExecutor);


		String pid = RuntimeUtils.getPid();
		log.info("PID on OS={}",pid);

		return gameExecutor;
	}

	public SExpression getCreateAndRegisterGameExecutorSExpression(GameBuilder gameBuilder) {

		SMethodCall<GameExecutor> createExecutorStr = SMethodCall.toSMethodCall(GameExecutor.class,
				getInstanceSMethodCall(), "createAndRegisterGameExecutor", gameBuilder);

		String kickStartExecutorStr = getKickStartExecutorSExpression(gameBuilder).toString();

		SExpression script = new SimpleSExpression(
				createExecutorStr + System.lineSeparator() + kickStartExecutorStr);
		log.debug(script);
		return script;
	}

	public SMethodCall<Void> getSendMessageSMethodCall(ProcessId processId, Message message) {
		return SMethodCall.toSMethodCall(Void.class,
				GameExecutorManager.getInstance().getMessengerSMethodCall(processId), "sendMessage",
				message.toConstructor());

	}

	@Override
	public void request(ProcessId processId, RequestToGameExecutor message) {
		getMessenger(processId).sendMessage(message);
	}

}
