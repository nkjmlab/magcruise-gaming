package org.magcruise.gaming.model.game;

import java.io.Serializable;
import java.lang.reflect.Field;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.Properties;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.lang.SchemeEnvironment;
import gnu.lists.Pair;
import gnu.mapping.Symbol;
import gnu.math.DFloNum;
import gnu.math.IntNum;

@SuppressWarnings("serial")
public abstract class GameObject implements SConstructive {

	protected static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager
			.getLogger();

	/**
	 * ゲームシナリオからアクセスしやすいように特別にpublicにしている．ゲームシナリオ以外からはgetterでアクセスすること．
	 */

	public Properties props;

	public GameObject() {
		this(new Properties());
	}

	public GameObject(Properties props) {
		this.props = props;
	}

	@Override
	public SConstructor<? extends GameObject> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), props);
	}

	public Serializable get(Symbol key) {
		Serializable val = this.props.get(key);
		return val;
	}

	public void set(Symbol key, Serializable val) {
		this.props.put(key, val);
	}

	public void setAll(Pair... keyValPairs) {
		this.props.setAll(keyValPairs);
	}

	public void increment(Symbol key) {
		increment(key, 1);
	}

	public void increment(Symbol key, Number val) {
		Serializable curVal = this.props.get(key);
		if (curVal == null) {
			curVal = 0;
		}
		Serializable result = (Serializable) SchemeEnvironment
				.evalOnGlobalEnvironment("(+ " + curVal + " " + val + ")");
		this.props.put(key, result);
	}

	public Properties getProperties() {
		return props;
	}

	public void setProperties(Properties props) {
		this.props = props;
	}

	public void syncAttributeFieldsToProperties() {
		syncFieldsToPropertiesByReflection(getClass(), this.props);
	}

	protected void syncFieldsToPropertiesByReflection(Class<?> clazz, Properties props) {
		for (Field f : clazz.getDeclaredFields()) {
			HistoricalField a = f.getAnnotation(HistoricalField.class);
			if (a == null) {
				continue;
			}
			Object val = props.get(Symbol.parse(f.getName()));

			if (val == null || val.equals("#!null")) {
				val = null;
			} else if (f.getType().isAssignableFrom(val.getClass())) {
				// OK
			} else if (val instanceof IntNum) {
				val = ((IntNum) val).intValue();
			} else if (val instanceof DFloNum) {
				val = ((DFloNum) val).doubleValue();
			}
			try {
				f.setAccessible(true);
				f.set(this, val);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		if (GameObject.class.isAssignableFrom(clazz.getSuperclass())) {
			syncFieldsToPropertiesByReflection(clazz.getSuperclass(), props);
		}
	}

	public String toSimpleCsv() {
		return props.toSimpleCsv();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
