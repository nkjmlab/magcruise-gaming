package org.magcruise.gaming.model.game;

@SuppressWarnings("serial")
public class TaskInterruptedException extends RuntimeException {

	public TaskInterruptedException(InterruptedException e) {
		super(e);
	}

}
