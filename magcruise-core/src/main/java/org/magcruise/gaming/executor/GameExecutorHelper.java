package org.magcruise.gaming.executor;

import java.util.List;
import org.magcruise.gaming.lang.SConstructive;

public interface GameExecutorHelper extends SConstructive {

	void start();

	List<Runnable> shutdownForceAfterAwating();

}
