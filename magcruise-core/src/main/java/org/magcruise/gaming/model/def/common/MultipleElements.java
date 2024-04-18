package org.magcruise.gaming.model.def.common;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;

@SuppressWarnings("serial")
public class MultipleElements<T> implements SConstructive {
	private List<T> elements = new ArrayList<>();

	public MultipleElements() {
	}

	public MultipleElements(List<T> result) {
		this.elements.addAll(result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SConstructor<? extends T> toConstructor(ToExpressionStyle style) {
		return (SConstructor<? extends T>) SConstructor.toConstructor(style, getClass(), elements);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public List<T> values() {
		return elements;
	}

}
