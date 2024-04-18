package org.magcruise.gaming.ui.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.lang.Parameters;
import org.magcruise.gaming.ui.api.message.RequestToUI;
import org.magcruise.gaming.ui.model.attr.Attribute;
import org.magcruise.gaming.ui.model.input.Input;
import org.magcruise.gaming.ui.model.input.InputFromWebUI;
import org.magcruise.gaming.ui.model.input.InputToUI;

/**
 * The input {@Code Form} for human operators.
 *
 * @author nkjm
 *
 */
public class Form {

	private long id;
	private String label;
	private Map<String, Input> inputs = new LinkedHashMap<>();

	/**
	 * Constructs a {@code Form}. This {@code Form} has the label and inputs for a human operator.
	 *
	 * @param label the label of this {@code Form}
	 * @param inputs the inputs including this {@code Form}
	 */
	public Form(String label, Input... inputs) {
		this(RequestToUI.generateId(), label, inputs);
	}

	/**
	 *
	 * @param id the form id expected to unique in this system.
	 * @param label
	 * @param inputs
	 */
	public Form(long id, String label, Input... inputs) {
		this.id = id;
		this.label = label;
		for (Input input : inputs) {
			this.inputs.put(input.getName(), input);
		}
	}

	public Form(long id, String label, InputToUI... inputToWebUIs) {
		this.id = id;
		this.label = label;
		Arrays.asList(inputToWebUIs).forEach(iw -> {
			inputs.put(iw.getName(), iw.toInput());
		});

	}

	public String getLabel() {
		return this.label;
	}

	public Collection<Input> getInputs() {
		return this.inputs.values();
	}

	public void setInputVal(String inputName, Serializable val) {
		Input input = inputs.get(inputName);
		input.setValue(val);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getId() {
		return this.id;
	}

	public InputFromWebUI[] toInputFromWebUIs() {
		List<InputFromWebUI> result = new ArrayList<>();
		inputs.values().forEach(i -> {
			result.add(new InputFromWebUI(i.getName(), i.getType().toString(), i.getValue()));
		});
		return result.toArray(new InputFromWebUI[0]);
	}

	public Parameters toParameters() {
		Parameters params = new Parameters();
		for (Input input : inputs.values()) {
			params.set(input.getName(), input.getValue());
		}
		return params;
	}

	public boolean validate() {
		for (Input input : inputs.values()) {
			for (Attribute constraint : input.getAttributes()) {
				if (!constraint.isValid(input.getValue())) {
					return false;
				}
			}
		}
		return true;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String invalidMessage() {
		for (Input input : inputs.values()) {
			for (Attribute constraint : input.getAttributes()) {
				if (!constraint.isValid(input.getValue())) {
					return constraint.invalidMessage(input);
				}
			}
		}
		return "";
	}

	public InputToUI[] toInputToUIs() {

		List<InputToUI> result = new ArrayList<>();

		getInputs().forEach(input -> {
			result.add(input.toInputToUI());
		});

		return result.toArray(new InputToUI[0]);
	}

}
