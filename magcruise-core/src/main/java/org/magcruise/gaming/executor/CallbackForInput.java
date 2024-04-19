package org.magcruise.gaming.executor;

import java.util.concurrent.CountDownLatch;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.executor.api.message.ReceiveInput;

public abstract class CallbackForInput<T> {
	protected static Logger log = LogManager.getLogger();

	public long requestId;
	public T callback;
	public CountDownLatch latch;

	public CallbackForInput(long requestId, T callback, CountDownLatch latch) {
		this.requestId = requestId;
		this.callback = callback;
		this.latch = latch;
	}

	public abstract void callback(ReceiveInput rmsg);

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
