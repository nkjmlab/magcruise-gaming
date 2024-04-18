package org.magcruise.gaming.ui.model.attr;

import java.io.Serializable;
import org.magcruise.gaming.ui.model.input.Input;

public class SimpleSubmit extends Attribute {
	public SimpleSubmit() {
		super("simple_submit", true);
	}

	@Override
	public boolean isValid(Serializable inputValue) {
		return true;
	}

	@Override
	public String invalidMessage(Input input) {
		return super.invalidMessage(input);
	}

}
