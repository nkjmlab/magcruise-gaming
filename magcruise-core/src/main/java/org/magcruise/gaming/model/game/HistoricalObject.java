package org.magcruise.gaming.model.game;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.Properties;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.ui.HtmlUtils;
import gnu.lists.Pair;
import gnu.mapping.Symbol;

/**
 * Roundごとの履歴を保持したクラス． Kawaからシンプルに呼び出しをする(self:prevの様に呼び出したくて，self:history:prevの様に呼び出したくはない)ために，
 * 委譲ではなく継承を用いている．ゲーミングだけではなく大規模シミュレーションに用いる場合などは，クラス設計の見直しが必要．
 *
 * @author nkjm
 *
 */
@SuppressWarnings("serial")
public abstract class HistoricalObject extends GameObject {

	/**
	 * ゲームシナリオからアクセスしやすいように特別にpublicにしている．ゲームシナリオ以外からはgetterでアクセスすること．
	 */

	public History history = new History();

	public HistoricalObject(Properties props, History history) {
		super(props);
		this.history = history;
	}

	@Override
	public SConstructor<? extends HistoricalObject> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), props, history);
	}

	public History getHistory() {
		return history;
	}

	public RoundHistory getRoundHistory(int roundnum) {
		return history.getRoundHistory(roundnum);
	}

	public Serializable getValue(Symbol key, int round) {
		return history.getValue(key, round);
	}

	public Serializable getValueBefore(Symbol key, int round) {
		return history.getValueBefore(key, round);
	}

	public Serializable getLastValue(Symbol key) {
		return getValueBefore(key, 1);
	}

	public void revertToEndOfRound(int roundnum) {
		history.revertToEndOfRound(roundnum);
		this.props = new Properties(history.getRoundHistory(roundnum).props.deepCopy());
		syncAttributeFieldsToProperties();
	}

	public String tabulateHistory(Symbol... keys) {
		return history.tabulate(0, history.size(),
				getAttributeAndFieldNamePairs(keys).toArray(new Pair[0]));
	}

	public String tabulateRangeOfHistory(int from, int to, Symbol... keys) {
		return history.tabulate(from, to, getAttributeAndFieldNamePairs().toArray(new Pair[0]));
	}

	public int getHistorySize() {
		return history.size();
	}

	public String tabulateCurrentState(int roundnum) {
		return tabulateCurrentStateAux(roundnum, getCurrentHistoricalFields());

	}

	public String tabulateCurrentState(int roundnum, Symbol... fieldNames) {
		return tabulateCurrentStateAux(roundnum, getCurrentHistoricalFields(fieldNames));

	}

	private String tabulateCurrentStateAux(int roundnum, Map<String, Serializable> targets) {
		Map<String, Object> tmp = new LinkedHashMap<>();
		tmp.put("rd.", roundnum);
		tmp.putAll(targets);
		return HtmlUtils.tabulate(tmp);
	}

	public List<Pair> getAttributeAndFieldNamePairs(Symbol... keys) {
		List<Pair> pairs = new ArrayList<>();
		getHistoricalFields().forEach((HistoricalAnnotatedField af) -> {
			if (keys.length == 0 || Arrays.asList(keys).contains(af.getFieldNameAsSymbol())) {
				if (af.isVisible()) {
					pairs.add(new Pair(af.getAnnotationName(), af.getFieldNameAsSymbol()));
				}
			}
		});
		return pairs;
	}

	public void saveToHistory(int roundnum) {
		if (roundnum == 1) {
			verifyFeild();
		}

		getHistoricalFields().forEach((HistoricalAnnotatedField af) -> {
			props.put(af.getFieldNameAsSymbol(), af.getFieldValue(this));
		});
		history.putRoundHistory(roundnum, new RoundHistory(roundnum, props.deepCopy()));
	}

	private void verifyFeild() {
		getHistoricalFields().forEach((HistoricalAnnotatedField af) -> {
			if (af == null || af.getFieldValue(this) == null) {
				return;
			}
			if (Collection.class.isAssignableFrom(af.getFieldValue(this).getClass())
					|| Map.class.isAssignableFrom(af.getFieldValue(this).getClass())) {
				ParameterizedType pt = (ParameterizedType) af.getField().getGenericType();
				Arrays.asList(pt.getActualTypeArguments()).forEach(actualType -> {
					if (actualType.getTypeName().equals(Integer.class.getName())
							|| actualType.getTypeName().equals(Long.class.getName())
							|| actualType.getTypeName().equals(Float.class.getName())
							|| actualType.getTypeName().equals(Double.class.getName())) {
						log.warn(
								"At {}.{}, {} is not recommended for the generic type of "
										+ "the fileld annotated by {}. Using {} instead of it.",
								getClass().getName(), af.getField().getName(),
								actualType.getTypeName(),
								HistoricalField.class.getName(), Number.class.getName());
					}
				});
			}
		});
	}

	protected List<HistoricalAnnotatedField> getHistoricalFields() {
		return getAttributeFieldsByReflection(new ArrayList<>(), getClass());
	}

	protected List<HistoricalAnnotatedField> getHistoricalFields(Symbol... keys) {
		List<HistoricalAnnotatedField> afs = getHistoricalFields();
		List<HistoricalAnnotatedField> result = new ArrayList<>();
		for (HistoricalAnnotatedField af : afs) {
			if (Arrays.asList(keys).contains(af.getFieldNameAsSymbol())) {
				result.add(af);
			}
		}
		return result;
	}

	private List<HistoricalAnnotatedField> getAttributeFieldsByReflection(
			List<HistoricalAnnotatedField> attributeFields, Class<?> clazz) {
		for (Field f : clazz.getDeclaredFields()) {
			HistoricalField a = f.getAnnotation(HistoricalField.class);
			if (a == null) {
				continue;
			}
			attributeFields.add(new HistoricalAnnotatedField(f, a));
		}
		if (HistoricalObject.class.isAssignableFrom(clazz.getSuperclass())) {
			getAttributeFieldsByReflection(attributeFields, clazz.getSuperclass());
		}
		return attributeFields;
	}

	private Map<String, Serializable> getCurrentHistoricalFields() {
		return getCurrentHistoricalFieldsAux(new LinkedHashMap<>(), getClass(),
				getHistoricalFields());
	}

	private Map<String, Serializable> getCurrentHistoricalFields(Symbol... fieldNames) {
		return getCurrentHistoricalFieldsAux(new LinkedHashMap<>(), getClass(),
				getHistoricalFields(fieldNames));
	}

	private Map<String, Serializable> getCurrentHistoricalFieldsAux(
			Map<String, Serializable> keyAndVals, Class<?> target,
			List<HistoricalAnnotatedField> historicalFields) {

		for (HistoricalAnnotatedField f : historicalFields) {
			keyAndVals.put(f.getFieldNameAsSymbol().toString(), f.getFieldValue(this));
		}
		return keyAndVals;
	}

	protected class HistoricalAnnotatedField {
		Field field;
		HistoricalField annotation;

		public HistoricalAnnotatedField(Field field, HistoricalField attribute) {
			this.field = field;
			this.annotation = attribute;
		}

		public boolean isVisible() {
			return annotation.visible();
		}

		public String getAnnotationName() {
			return annotation.name().length() == 0 ? field.getName() : annotation.name();
		}

		public Symbol getFieldNameAsSymbol() {
			return Symbol.parse(field.getName());
		}

		public Field getField() {
			return field;
		}

		public Serializable getFieldValue(HistoricalObject obj) {
			try {
				field.setAccessible(true);
				return (Serializable) field.get(obj);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

	}
}
