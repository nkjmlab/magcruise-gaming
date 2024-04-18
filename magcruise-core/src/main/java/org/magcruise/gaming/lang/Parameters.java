package org.magcruise.gaming.lang;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.ui.model.input.InputFromWebUI;
import gnu.lists.LList;
import gnu.lists.Pair;
import gnu.mapping.Symbol;

@SuppressWarnings("serial")
public class Parameters implements SConstructive {

	private Map<String, Serializable> props = new LinkedHashMap<>();

	public Parameters() {
	}

	public Parameters(Map<String, Serializable> props) {
		this.props = new LinkedHashMap<>(props);
	}

	public Parameters(Object... objs) {
		for (int i = 0; i < objs.length; i += 2) {
			props.put(objs[i].toString(), (Serializable) objs[i + 1]);
		}
	}

	@Override
	public SConstructor<? extends Parameters> toConstructor(ToExpressionStyle style) {
		return SConstructor.toConstructor(style, getClass(), props);
	}

	public Serializable get(String key) {
		return props.get(key);
	}

	public void set(String key, Serializable val) {
		props.put(key, val);
	}

	public Serializable get(Symbol key) {
		return get(key.toString());
	}

	public String getString(String key) {
		Serializable value = get(key);
		if (value == null) {
			throw new IllegalArgumentException(key + " is not found.");
		}
		return value.toString();
	}

	public Serializable getInt(Symbol key) {
		return getInt(key.toString());
	}

	public int getInt(String key) {
		return toInt(get(key));
	}

	private int toInt(Serializable value) {
		if (value == null) {
			throw new NullPointerException("value is null.");
		} else if (value instanceof Number) {
			return ((Number) value).intValue();
		} else if (value instanceof String) {
			return Integer.valueOf((String) value);
		}
		return (int) value;
	}

	public void set(Symbol key, Serializable val) {
		set(key.toString(), val);
	}

	public Collection<Serializable> values() {
		return props.values();
	}

	public LList asLList() {
		return Pair.makeList(new ArrayList<>(props.values()));
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public List<Serializable> toArgsList() {
		List<Serializable> args = new ArrayList<>();
		for (Serializable val : props.values()) {
			args.add(val);
		}
		return args;
	}

	public Map<String, Serializable> toArgsMap() {
		Map<String, Serializable> args = new HashMap<>();
		for (Entry<String, Serializable> entry : props.entrySet()) {
			args.put(entry.getKey(), entry.getValue());
		}
		return args;
	}

	public static Parameters parse(InputFromWebUI[] inputs) {
		Parameters params = new Parameters();

		Arrays.asList(inputs).forEach(input -> {
			params.set(input.getName(), input.getValue());
		});
		return params;
	}

	public Serializable getArg(int index) {
		return toArgsList().get(index);
	}

	public int getArgAsInt(int index) {
		return toInt(getArg(index));
	}

	public String getArgAsString(int index) {
		return getArg(index).toString();
	}

	public <T> T getArgAs(Class<T> clazz, int index) {
		return clazz.cast(getArg(index));
	}

	public List<String> getArgAsStringList(int index) {
		List<?> t = getArgAs(List.class, index);
		return t.stream().map(o -> {
			return t.toString();
		}).collect(Collectors.toList());

	}

}
