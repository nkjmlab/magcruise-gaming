package org.magcruise.gaming.executor.api.message;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.lang.SchemeEnvironment;

/**
 * UIからExecutorへ渡されるデータ．JSONで送られることもあるので，全体をPOJOにする． フィールドに持つクラスもPOJOにする必要があることに注意．
 *
 * @author nkjm
 *
 */
@SuppressWarnings("serial")
public class RequestToGameExecutorTransferObject implements RequestToGameExecutor {

	protected static Logger log = LogManager.getLogger();

	protected SConstructor<? extends RequestToGameExecutor> sConstructor;

	public RequestToGameExecutorTransferObject() {
	}

	public RequestToGameExecutorTransferObject(
			SConstructor<? extends RequestToGameExecutor> sConstructor) {
		this.sConstructor = sConstructor;
	}

	public SConstructor<? extends RequestToGameExecutor> getSConstructor() {
		return sConstructor;
	}

	public void setSConstructor(SConstructor<? extends RequestToGameExecutor> sConstructor) {
		this.sConstructor = sConstructor;
	}

	public RequestToGameExecutor makeInstance(String environmentName) {
		try {
			RequestToGameExecutor result = SchemeEnvironment.makeInstance(environmentName,
					getSConstructor());
			return result;
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public String getExpression() {
		return sConstructor.getExpression();
	}

	@Override
	public SConstructor<? extends RequestToGameExecutor> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), sConstructor);
	}

}
