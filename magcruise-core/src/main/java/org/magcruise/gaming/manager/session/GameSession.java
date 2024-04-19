package org.magcruise.gaming.manager.session;

import java.util.ArrayList;
import java.util.List;
import org.magcruise.gaming.common.KawaRepl;
import org.magcruise.gaming.manager.process.InternalGameProcess;
import org.magcruise.gaming.manager.process.InternalGameProcessBuilder;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.model.def.GameBuilder;
import org.magcruise.gaming.model.def.actor.DefPlayer;
import org.magcruise.gaming.model.def.scenario.round.DefRound;
import org.magcruise.gaming.model.def.scenario.stage.DefParallelStage;
import org.magcruise.gaming.model.def.scenario.task.DefEventDrivenTask;
import org.magcruise.gaming.model.def.scenario.task.StageElement;
import org.magcruise.gaming.model.def.sys.DefRequestToGameExecutorPublisherService;
import org.magcruise.gaming.model.def.sys.DefSwingGui;
import org.magcruise.gaming.model.def.sys.DefUIService;
import org.magcruise.gaming.model.game.ActorName;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.model.game.Player;
import org.magcruise.gaming.model.game.Player.PlayerType;
import org.magcruise.gaming.model.game.message.FinishTaskEvent;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class GameSession extends GameSessionCore<InternalGameProcess> {

	public GameSession() {
		this(ResourceLoader.class);
	}

	public GameSession(Class<? extends ResourceLoader> clazz) {
		super(clazz);
	}

	@Override
	public GameBuilder build() {
		if (brokerUrl != null) {
			resourceLoader.addDefGameSystemProperties(
					new DefUIService(brokerUrl.toString()),
					new DefRequestToGameExecutorPublisherService(brokerUrl.toString(),
							useWebsocket));
		}
		return super.build();
	}

	public void finishTaskFor(ActorName playerName) {
		getContext().sendEvent(new FinishTaskEvent(playerName));
	}

	public Context getContext() {
		return getProcess().getContext();
	}

	public <T extends Player> T getPlayerAs(Class<T> clazz, ActorName name) {
		return getContext().getPlayerAs(clazz, name);
	}

	public void setTaskFor(ActorName playerName, Class<? extends Player> clazz, Object... args) {
		setTasksFor(0, playerName, clazz, args);
	}

	public void setTasksFor(int roundnum, ActorName playerName, Class<? extends Player> clazz,
			Object... args) {
		List<StageElement> tasks = new ArrayList<>();
		tasks.add(
				new DefEventDrivenTask(playerName, Symbol.parse("finish"), FinishTaskEvent.class));
		addDefPlayer(new DefPlayer(playerName, PlayerType.HUMAN, clazz, args));
		for (int i = 0; i < roundnum + 1; i++) {
			addDefRound(new DefRound(new DefParallelStage(new ArrayList<>(tasks))));
		}
	}

	/**
	 * 通常の開発時はこのメソッドを呼び出して，ゲームを起動する．エラーメッセージがコンソールに表示される．ただし，外部のクラスファイル読み込みは無効．
	 * 呼び出したプロセスの上でKawaインタプリタを動かし，そのKawaに対してメソッドコールをすることで，ゲームを実行する．
	 * 外部のクラスファイル読み込みは無効となる．エラーログはコンソールに出力される．
	 *
	 * @return
	 */
	@Override
	public ProcessId start() {
		checkExecuted();
		buildIfNotBuilt();
		process = new InternalGameProcessBuilder(getGameBuilder()).build();
		process.start();
		executed = true;
		return getProcessId();
	}

	public void startGameAndRepl() {
		ProcessId processId = start();
		new KawaRepl(processId.toString()).start();
	}

	public void useSwingGui() {
		checkBuilt();
		addDefUI(new DefSwingGui());
	}

}
