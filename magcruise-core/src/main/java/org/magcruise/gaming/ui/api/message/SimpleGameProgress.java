package org.magcruise.gaming.ui.api.message;

@SuppressWarnings("serial")
public class SimpleGameProgress extends RequestToUI implements GameProgress {

	private int roundnum;
	@SuppressWarnings("unused")
	private String message;

	public SimpleGameProgress(int roundnum) {
		super();
		this.roundnum = roundnum;
	}

	@Override
	public int getRoundnum() {
		return roundnum;
	}

	@Override
	public String getMessage() {
		return toString();
	}

	@Override
	public void setMessage(String msg) {
		this.message = msg;
	}
}
