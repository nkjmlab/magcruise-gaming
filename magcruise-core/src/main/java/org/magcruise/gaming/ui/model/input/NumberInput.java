package org.magcruise.gaming.ui.model.input;

import java.io.Serializable;
import org.magcruise.gaming.ui.model.attr.Attribute;
import gnu.kawa.lispexpr.LispReader;
import gnu.mapping.Symbol;

public class NumberInput extends Input {

	public NumberInput(String label, String name, Number val, Attribute... attrs) {
		super(InputType.NUMBER, label, name, val, attrs);
	}

	public NumberInput(String label, Symbol name, Number val, Attribute... attrs) {
		super(InputType.NUMBER, label, name, val, attrs);
	}

	@Override
	public void setValue(Serializable value) {
		if (value instanceof Number) {
			super.setValue(value);
		} else if (value instanceof CharSequence) {
			super.setValue((Serializable) LispReader.parseNumber((CharSequence) value, 10));
		} else {
			super.setValue(value);
		}
	}
}
