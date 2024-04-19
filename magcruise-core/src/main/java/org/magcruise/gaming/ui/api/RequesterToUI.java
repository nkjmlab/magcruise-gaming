package org.magcruise.gaming.ui.api;

import java.io.Serializable;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.manager.process.ProcessId;
import org.magcruise.gaming.ui.api.message.RequestToUI;

/**
 * ゲーミンシミュレーションからUIへのリクエストを送る．
 *
 * @author nkjm
 *
 */
public interface RequesterToUI extends SConstructive {

	public Serializable request(ProcessId processId, RequestToUI request);

	public void initialize();

	@Override
	public SConstructor<? extends RequesterToUI> toConstructor(ToExpressionStyle style);

	@Override
	public default SConstructor<? extends RequesterToUI> toConstructor() {
		return toConstructor(ToExpressionStyle.DEFAULT);
	}

	public void requestToRegisterGameSession(ProcessId processId);

}
