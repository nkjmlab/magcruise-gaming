package org.magcruise.gaming.lang;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.util.LiteralUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import gnu.lists.Pair;
import gnu.mapping.Symbol;

/**
 * もとはSchemeの連想リスト (association list : a-list： ドット対を要素とするリスト)と対応
 *
 * @author nkjm
 *
 */
@SuppressWarnings("serial")
public class Properties implements SConstructive {

	private Map<Symbol, Serializable> props = new ConcurrentHashMap<>();

	public Properties() {

	}

	public Properties(Pair... keyValPairs) {
		this();
		setAll(keyValPairs);
	}

	public Properties(Map<Symbol, Serializable> props) {
		this.props.putAll(props);
	}

	public Properties(Properties props) {
		this.props.putAll(props.props);
	}

	@Override
	public SConstructor<? extends Properties> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), props);
	}

	/**
	 * 入れ子にすると破綻する．
	 *
	 * @return
	 */
	public String toSimpleCsv() {
		List<String> tmp = new ArrayList<>();
		props.entrySet().forEach(e -> {
			tmp.add("\"" + e.getKey().toString().toUpperCase() + "\",\""
					+ e.getValue().toString().replace("\"", "\"\"") + "\"");
		});
		return String.join(",", tmp);
	}

	public Serializable get(Symbol key) {
		Serializable val = this.props.get(key);
		return val;
	}

	/**
	 *
	 * @param key Cannot be {@code null}.
	 * @param val If {@code val} is {@code null}, {@code val} is converted to "#!null".
	 */
	public void put(Symbol key, Serializable val) {
		if (val == null) {
			this.props.put(key, "#!null");
		} else {
			this.props.put(key, val);
		}
	}

	@JsonIgnore
	public void setAll(Properties props) {
		for (Symbol key : props.props.keySet()) {
			this.props.put(key, props.props.get(key));
		}

	}

	/**
	 * @param keyValPairs (cons key val) (cons key val)
	 */
	@JsonIgnore
	public void setAll(Pair... keyValPairs) {
		for (Pair p : keyValPairs) {
			put((Symbol) p.getCar(), (Serializable) p.getCdr());
		}
	}

	public Set<Symbol> keySet() {
		return this.props.keySet();
	}

	public Set<Entry<Symbol, Serializable>> entrySet() {
		return this.props.entrySet();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public Properties deepCopy() {
		Map<Symbol, Serializable> copies = new LinkedHashMap<>();

		for (Symbol key : props.keySet()) {
			Serializable val = props.get(key);
			Serializable copy = LiteralUtils.deepCopy(val);
			copies.put(key, copy);
		}

		return new Properties(copies);
	}

	@Override
	public String getExpression(ToExpressionStyle style) {
		return (style == ToExpressionStyle.MULTI_LINE ? System.lineSeparator() + "    " : "")
				+ SConstructive.super.getExpression(style);
	}

}
