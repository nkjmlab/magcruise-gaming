package org.magcruise.gaming.ui.model.attr;

import java.io.Serializable;
import org.magcruise.gaming.ui.model.input.Input;

public class Required extends Attribute {
	public Required() {
		super("required", true);
	}

	@Override
	public boolean isValid(Serializable inputValue) {
		if (inputValue != null) {
			return true;
		}
		return false;
	}

	@Override
	public String invalidMessage(Input input) {
		return super.invalidMessage(input) + "This input should not be null. ";
	}

}
