package org.magcruise.gaming.model.game;

import java.io.Serializable;
import java.util.Set;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.Properties;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class RoundHistory implements SConstructive {

	/**
	 * ゲームシナリオからアクセスしやすいように特別にpublicにしている．ゲームシナリオ以外からはgetterでアクセスすること．
	 */

	protected Properties props = new Properties();
	private int roundnum;

	public RoundHistory(int roundnum, Properties props) {
		this.roundnum = roundnum;
		this.props.setAll(props);
	}

	public Serializable get(Symbol key) {
		return props.get(key);
	}

	public Set<Symbol> keySet() {
		return this.props.keySet();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public SConstructor<? extends RoundHistory> toConstructor(ToExpressionStyle style) {
		return new SConstructor<>(
				(style == ToExpressionStyle.MULTI_LINE ? System.lineSeparator() + "      " : "")
						+ SConstructor.toConstructor(style, getClass(), roundnum, props));
	}

	public int getRoundnum() {
		return roundnum;
	}
}
