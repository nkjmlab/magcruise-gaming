package org.magcruise.gaming.ui.model.input;

import org.magcruise.gaming.ui.model.attr.Attribute;

public class TextAreaInput extends Input {

	public TextAreaInput(String label, String name, String value, Attribute... attrs) {
		super(InputType.TEXTAREA, label, name, value, attrs);
	}

}
