package org.magcruise.gaming.scm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.nkjmlab.util.java.lang.MethodInvokerInfoUtils;

public class GameLogger {

	private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger();

	private static GameLogger gameLogger = new GameLogger();

	public static GameLogger getLogger() {
		return gameLogger;
	}

	public void debug(Object... objs) {
		log.debug(objectsToString(objs));
	}

	private String objectsToString(Object... objs) {
		List<String> strs = new ArrayList<>();
		Arrays.asList(objs).forEach(o -> {
			if (o == null) {
				strs.add("null");
			} else {
				strs.add(o.toString());
			}
		});
		return String.join(", ", strs);
	}

	public void info(Object... objs) {
		log.info(objectsToString(objs));
	}

	public void error(Object... objs) {
		log.error(objectsToString(objs));
	}

	public void error(Throwable e, Object... objs) {
		log.error(objectsToString(objs), e);
	}

	public void bp(Object... objs) {
		this.breakpoint(objs);
	}

	public void breakpoint(Object... objs) {
		String ln = MethodInvokerInfoUtils.getInvokerFileNameAndLineNumber(7, new Throwable().getStackTrace());
		log.error(ln + ":" + objectsToString(objs));
	}

	public void raw(Object... obj) {
		log.error("logger:raw obj.toString-:{}", obj);
	}
}
