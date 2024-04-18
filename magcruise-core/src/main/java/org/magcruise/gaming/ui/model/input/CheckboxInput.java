package org.magcruise.gaming.ui.model.input;

import java.util.ArrayList;
import java.util.List;
import org.magcruise.gaming.ui.model.attr.Attribute;

/**
 * 複数チェックが出来るので，valuesがListになることに注意．
 *
 * @author nkjm
 *
 */
public class CheckboxInput extends OptionInput {

	public CheckboxInput(String label, String name, List<String> values, List<String> optionLabels,
			List<String> options, Attribute... attrs) {
		super(InputType.CHECKBOX, label, name, new ArrayList<>(values),
				new ArrayList<>(optionLabels),
				new ArrayList<>(options), attrs);
	}

	@Override
	public InputToUI toInputToUI() {
		InputToUI i = super.toInputToUI();
		i.putAttr("optionLabels", new ArrayList<>(getOptionLabels()));
		i.putAttr("options", new ArrayList<>(getOptionValues()));
		return i;
	}

}
