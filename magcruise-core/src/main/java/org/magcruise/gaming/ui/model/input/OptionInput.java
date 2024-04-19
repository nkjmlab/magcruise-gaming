package org.magcruise.gaming.ui.model.input;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.magcruise.gaming.ui.model.attr.Attribute;

public class OptionInput extends Input {
	protected List<String> optionLabels;
	protected List<String> options;

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
	public OptionInput(InputType type, String label, String name, Serializable value,
			List<String> optionLabels, List<String> options, Attribute... attrs) {
		super(type, label, name, value, attrs);
		this.optionLabels = new ArrayList<>(optionLabels);
		this.options = new ArrayList<>(options);
	}

	public OptionInput(InputType type, String label, String name, Serializable value,
			String[] optionLabels, String[] options, Attribute... attrs) {
		this(type, label, name, value, Arrays.asList(optionLabels), Arrays.asList(options), attrs);
	}

	public List<String> getOptionLabels() {
		return optionLabels;
	}

	public List<String> getOptionValues() {
		return options;
	}

	@Override
	public InputToUI toInputToUI() {
		InputToUI i = super.toInputToUI();
		i.putAttr("optionLabels", new ArrayList<>(getOptionLabels()));
		i.putAttr("options", new ArrayList<>(getOptionValues()));
		return i;
	}

}
