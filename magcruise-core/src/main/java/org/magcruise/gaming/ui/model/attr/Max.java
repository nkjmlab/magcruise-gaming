package org.magcruise.gaming.ui.model.attr;

import java.io.Serializable;
import org.magcruise.gaming.ui.model.input.Input;

/**
 * 境界値は有効値に含む．inputValueがNullだったら検査しない．
 *
 * @author nkjm
 *
 */
public class Max extends Attribute {

	public Max(Number max) {
		super("max", max);
	}

	@Override
	public boolean isValid(Serializable inputValue) {
		// inputValueがNullだったら検査しない
		if (inputValue == null) {
			return true;
		}
		if (inputValue instanceof Number) {
			if (((Number) inputValue).doubleValue() <= ((Number) value).doubleValue()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String invalidMessage(Input input) {
		return super.invalidMessage(input) + "max value is " + getValue() + ". ";
	}

}
