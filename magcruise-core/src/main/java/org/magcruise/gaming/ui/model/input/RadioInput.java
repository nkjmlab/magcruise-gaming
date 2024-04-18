package org.magcruise.gaming.ui.model.input;

import java.util.List;
import org.magcruise.gaming.ui.model.attr.Attribute;

public class RadioInput extends OptionInput {

	/**
	 * Construct an input element including radio buttons.
	 *
	 * @param label label of form
	 * @param name name of input. the name is used for getting value from result {@code Parameter}.
	 * @param value value of input. the value is mainly used for default value of this.
	 * @param optionLabels labels of radio buttons.
	 * @param options value of radio buttons.
	 * @param attrs attribute of form
	 */
	public RadioInput(String label, String name, String value, List<String> optionLabels,
			List<String> options, Attribute... attrs) {
		super(InputType.RADIO, label, name, value, optionLabels, options, attrs);
	}

	public RadioInput(String label, String name, String value, String[] optionLabels,
			String[] options, Attribute... attrs) {
		super(InputType.RADIO, label, name, value, optionLabels, options, attrs);
	}

}
