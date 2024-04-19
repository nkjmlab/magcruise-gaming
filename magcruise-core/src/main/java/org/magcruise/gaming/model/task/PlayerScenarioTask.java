package org.magcruise.gaming.model.task;

import java.util.Arrays;
import java.util.List;
import org.magcruise.gaming.lang.SchemeEnvironment;
import org.magcruise.gaming.lang.exception.ApplyException;
import org.magcruise.gaming.model.def.scenario.player_scenario.DefBehavior;
import org.magcruise.gaming.model.def.scenario.player_scenario.DefPlayerScenario;
import org.magcruise.gaming.model.def.scenario.player_scenario.DefScene;
import org.magcruise.gaming.model.def.scenario.task.DefPlayerScenarioTask;
import org.magcruise.gaming.model.game.Context;
import org.magcruise.gaming.model.game.Player;
import org.magcruise.gaming.model.game.message.ScenarioEvent;
import gnu.mapping.Symbol;

public class PlayerScenarioTask extends EventDrivenTask<ScenarioEvent> {

	private Symbol currentSceneName;
	private DefPlayerScenario playerScenario;

	private static List<String> END_OF_SCENE_NAMES = Arrays.asList("exit", "exited", "finish",
			"finished", "end", "exit-scene", "exited-scene", "finish-scene", "finished-scene",
			"end-scene");

	public PlayerScenarioTask(SingleTaskLatch latch, Context context,
			DefPlayerScenarioTask scenarioTask, EventDrivenTasks eventDrivenTasks) {
		super(latch, context, scenarioTask, eventDrivenTasks);
		this.playerScenario = (DefPlayerScenario) SchemeEnvironment
				.applyProcedure(context.getEnvironmentName(),
						scenarioTask.getProcName().toString());
		this.currentSceneName = playerScenario.getDefScenes().get(0).getName();
	}

	private void interpretScenario(Context context, ScenarioEvent msg) throws ApplyException {
		log.debug("scenarioName is " + playerScenario.getScenarioName());
		log.debug("currentScene is " + currentSceneName);
		log.debug("Received msg is " + msg);
		latch.setPlayers(context.getPlayers());
		Player player = context.getPlayer(task.getPlayerName());

		for (DefScene scene : playerScenario.getDefScenes()) {
			if (!scene.getName().equals(currentSceneName)) {
				continue;
			}
			DefBehavior behavior = scene.interpretScene(context, player, msg);
			if (behavior != null) {
				currentSceneName = behavior.getNextScene();
				log.debug("Fired Behavior is {} and next scene is {}", behavior, currentSceneName);
			}
		}
	}

	@Override
	public Object call() throws Exception {
		log.debug("Start Player scenario: " + task);
		try {
			if (latch.isFinishied()) {
				return null;
			}

			exitIfFinished();
			ScenarioEvent event = events.poll();
			interpretScenario(context, event);
			exitIfFinished();
		} catch (Exception e) {
			log.error(
					System.lineSeparator() + "An exception occurred in: " + this
							+ System.lineSeparator());
			log.error(e, e);
			throw e;
		}
		return null;
	}

	private void exitIfFinished() {
		if (isFinished()) {
			getLatch().finish(task.getPlayerName());
		}
	}

	private boolean isFinished() {
		for (String e : END_OF_SCENE_NAMES) {
			if (currentSceneName.toString().equalsIgnoreCase(e)) {
				return true;
			}
		}
		return false;
	}

}
