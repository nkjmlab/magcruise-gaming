package org.magcruise.gaming.ui.model.input;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.ui.model.attr.Attribute;
import gnu.mapping.Symbol;

/**
 * Input elements for human operators. A {@code Input} object is translated to a {@code InputToUI}
 * object which is represented input element on Web browser.
 *
 * @author nkjm
 *
 */
public abstract class Input {

	protected String label;
	protected String name;
	protected InputType type;
	protected Serializable value;
	protected List<Attribute> attributes;

	/**
	 * Construct an {@code Form}
	 *
	 * @param type type of form
	 * @param label label of form
	 * @param name
	 * @param value
	 * @param attrs
	 */
	public Input(InputType type, String label, String name, Serializable value,
			Attribute... attrs) {
		this.type = type;
		this.label = label;
		this.name = name;
		this.value = value;
		this.attributes = new ArrayList<>(Arrays.asList(attrs));
	}

	public Input(InputType type, String label, Symbol name, Serializable value,
			Attribute... attrs) {
		this(type, label, name.toString(), value, attrs);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public String getLabel() {
		return label;
	}

	public String getName() {
		return name;
	}

	public InputType getType() {
		return type;
	}

	public Serializable getValue() {
		return value;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setValue(Serializable value) {
		this.value = value;
	}

	public void addAttribute(Attribute constraint) {
		this.attributes.add(constraint);
	}

	public void removeAttribute(Attribute constraint) {
		attributes.remove(constraint);
	}

	public void removeAttribute(Class<? extends Attribute> clazz) {
		new ArrayList<Attribute>(attributes).forEach(constraint -> {
			if (constraint.getClass().equals(clazz)) {
				attributes.remove(constraint);
			}
		});
	}

	public InputToUI toInputToUI() {
		InputToUI i = new InputToUI(type, label, name, value,
				getAttributes().toArray(new Attribute[0]));
		return i;
	}

	public InputFromWebUI toInputFromWebUI() {
		return new InputFromWebUI(name, type.name(), value);
	}

}
