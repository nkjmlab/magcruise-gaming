package org.magcruise.gaming.lang.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import gnu.mapping.Procedure;

@SuppressWarnings("serial")
public class ApplyException extends RuntimeException {

	private Procedure proc;
	private String sourceLocation;
	private Throwable originalException;
	private String[] args;

	public ApplyException(Procedure proc, Throwable e, Object... args) {
		super(e.getMessage(), e);
		this.proc = proc;
		this.sourceLocation = proc != null ? proc.getSourceLocation() : e.getMessage();
		this.originalException = e;
		List<String> tmp = new ArrayList<>();
		Arrays.asList(args).forEach(arg -> {
			tmp.add(arg.toString());
		});
		this.args = tmp.toArray(new String[0]);

	}

	@Override
	public String toString() {

		return getClass().getName() + ": "
				+ "An exception occurred when following procedure is called: (" + getProcName()
				+ (args.length != 0 ? " " : "") + String.join(" ", args) + ")"
				+ System.lineSeparator()
				+ getSourceLocationMsg() + getOriginalExceptionMsg();
	}

	private String getProcName() {
		if (proc != null) {
			return proc.getName();
		}
		return "(undefined-procedure)";
	}

	private String getOriginalExceptionMsg() {
		String msg = originalException.toString() + System.lineSeparator();
		for (StackTraceElement s : originalException.getStackTrace()) {
			msg += "        " + s + System.lineSeparator();
		}
		return msg;
	}

	private String getSourceLocationMsg() {
		if (sourceLocation != null && sourceLocation.length() != 0) {
			return "an exception is occurred in a procedure defined on " + sourceLocation
					+ System.lineSeparator();
		}
		return "";
	}

}
