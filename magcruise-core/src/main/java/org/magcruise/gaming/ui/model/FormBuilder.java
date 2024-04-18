package org.magcruise.gaming.ui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.magcruise.gaming.ui.api.message.RequestToUI;
import org.magcruise.gaming.ui.model.input.Input;

public class FormBuilder {

	private String label;
	private List<Input> inputs = new ArrayList<>();

	public FormBuilder() {
		this("");
	}

	public FormBuilder(String label) {
		this.label = label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public FormBuilder addInput(Input input) {
		this.inputs.add(input);
		return this;
	}

	public FormBuilder addInputs(Input... inputs) {
		Arrays.asList(inputs).forEach(input -> this.inputs.add(input));
		return this;
	}

	public Form build() {
		return new Form(RequestToUI.generateId(), label, inputs.toArray(new Input[0]));
	}

	public String getLabel() {
		return label;
	}

	public void addLabel(String additionalScentence) {
		label += additionalScentence;
	}

}
