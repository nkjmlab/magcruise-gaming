package org.magcruise.gaming.model.task;

import org.magcruise.gaming.model.game.ActorName;

public interface TaskLatch {

	void finish(ActorName name);

	void countDown(ActorName name);

	void finishStage(ActorName name);

}
