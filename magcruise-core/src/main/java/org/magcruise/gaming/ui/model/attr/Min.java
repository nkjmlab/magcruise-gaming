package org.magcruise.gaming.ui.model.attr;

import java.io.Serializable;
import org.magcruise.gaming.ui.model.input.Input;

/**
 * 境界値は有効として含む．inputValueがNullだったら検査しない．
 *
 * @author nkjm
 *
 */
public class Min extends Attribute {

	public Min(Number min) {
		super("min", min);
	}

	@Override
	public boolean isValid(Serializable inputValue) {
		// inputValueがNullだったら検査しない
		if (inputValue == null) {
			return true;
		}

		if (inputValue instanceof Number) {
			if (((Number) inputValue).doubleValue() >= ((Number) value).doubleValue()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String invalidMessage(Input input) {
		return super.invalidMessage(input) + "min value is " + getValue() + ". ";
	}

}
