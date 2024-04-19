package org.magcruise.gaming.manager.process;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.executor.GameExecutor;
import org.magcruise.gaming.executor.api.message.RequestToGameExecutorTransferObject;
import org.magcruise.gaming.manager.GameExecutorManager;
import org.magcruise.gaming.model.def.GameBuilder;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.model.game.Player;
import org.magcruise.gaming.model.game.Players;
import org.magcruise.gaming.util.SystemEnvironmentUtils;

public class InternalGameProcess implements GameProcess {

	protected static Logger log = LogManager.getLogger();

	protected GameExecutor executor;

	protected GameBuilder gameBuilder;

	public InternalGameProcess(GameBuilder gameBuilder) {
		this.gameBuilder = gameBuilder;
	}

	@Override
	public Object call() throws Exception {
		try {
			start();
		} catch (Exception e) {
			executor.context.setFinished(true);
			executor.shutdown();
		}
		return null;
	}

	@Override
	public void start() {
		executor = GameExecutorManager.getInstance().createAndRegisterGameExecutor(gameBuilder);
		executor.kickStartGame();
	}

	public Player getPlayer(ActorName playerName) {
		return executor.getContext().getPlayer(playerName);
	}

	public Context getContext() {
		return executor.getContext();
	}

	public void request(RequestToGameExecutorTransferObject request) {
		executor.getMessenger().sendMessage(request);
	}

	public GameExecutor getExecutor() {
		return executor;

	}

	private boolean isFinished() {
		return executor.getContext().isFinished();
	}

	public Players<? extends Player> getPlayers() {
		return getContext().getPlayers();
	}

	@Override
	public int waitFor() {
		while (!isFinished()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.warn(e, e);
				return 1;
			}
		}
		return 0;
	}

	@Override
	public ProcessId getProcessId() {
		return executor.getProcessId();
	}

	@Override
	public String getLatestRecord(ProcessId processId) {
		return SystemEnvironmentUtils.getLatestRecord(processId);
	}

}
