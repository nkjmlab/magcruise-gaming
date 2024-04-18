package org.magcruise.gaming.executor;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import org.magcruise.gaming.executor.api.message.ReceiveInput;
import org.magcruise.gaming.lang.Parameters;
import org.magcruise.gaming.lang.exception.ApplyException;
import gnu.mapping.Procedure;

public class CallbackProcedureForInput extends CallbackForInput<Procedure> {

	public CallbackProcedureForInput(long requestId, Procedure callback, CountDownLatch latch) {
		super(requestId, callback, latch);
	}

	@Override
	public void callback(ReceiveInput rmsg) {
		Parameters parameters = rmsg.getParameters();
		try {
			callback.applyN(parameters.toArgsList().toArray(new Serializable[0]));
		} catch (Throwable e) {
			log.error(new ApplyException(callback, e, parameters.toArgsList().toArray()));
		} finally {
			latch.countDown();
		}
	}

}
