package org.magcruise.gaming.ui.model.input;

import org.magcruise.gaming.ui.model.attr.Attribute;

public class TextInput extends Input {

	public TextInput(String label, String name, String value, Attribute... attrs) {
		super(InputType.TEXT, label, name, value, attrs);
	}

}
