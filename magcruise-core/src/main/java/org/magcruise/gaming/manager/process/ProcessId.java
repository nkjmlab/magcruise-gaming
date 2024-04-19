package org.magcruise.gaming.manager.process;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class ProcessId implements SConstructive, Comparable<ProcessId> {

	private String value;

	public ProcessId() {

	}

	public ProcessId(String processId) {
		this.value = processId;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public String getProcessId() {
		return value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public int compareTo(ProcessId processId) {
		return this.value.compareTo(processId.value);
	}

	@Override
	public SConstructor<?> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), value);
	}

	private static final Queue<Long> ids = new ConcurrentLinkedQueue<>();

	public static synchronized ProcessId getNewProcessId() {
		while (true) {
			long id = System.currentTimeMillis();
			if (!ids.contains(id)) {
				ids.offer(id);
				if (ids.size() > 1000) {
					ids.poll();
				}
				return new ProcessId(
						"proc-" + new SimpleDateFormat("yyyyMMdd'-'HHmmss'-'SSS")
								.format(new Date(id)));
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}

}
