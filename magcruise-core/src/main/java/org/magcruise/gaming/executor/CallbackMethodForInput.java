package org.magcruise.gaming.executor;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import org.magcruise.gaming.executor.api.message.ReceiveInput;
import org.magcruise.gaming.lang.Parameters;

public class CallbackMethodForInput extends CallbackForInput<Consumer<Parameters>> {

	public CallbackMethodForInput(long requestId, Consumer<Parameters> callback,
			CountDownLatch latch) {
		super(requestId, callback, latch);
	}

	@Override
	public void callback(ReceiveInput rmsg) {
		try {
			callback.accept(rmsg.getParameters());
		} catch (Throwable e) {
			log.error(e, e);
		} finally {
			latch.countDown();
		}
	}

}
