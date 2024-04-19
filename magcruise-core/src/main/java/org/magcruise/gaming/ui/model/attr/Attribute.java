package org.magcruise.gaming.ui.model.attr;

import java.io.Serializable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.ui.model.input.Input;

/**
 * The {@code Attribute} class represents attribute of input. Such as {@code Max}, {@code Min},
 * {@code Required}.
 *
 * @author nkjm
 *
 */
public class Attribute {

	protected String name;
	protected Serializable value;

	public Attribute() {

	}

	public Attribute(String name, Serializable value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Serializable getValue() {
		return value;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	/**
	 * Input validator
	 *
	 * @param inputValue
	 * @return
	 */
	public boolean isValid(Serializable inputValue) {
		return true;
	}

	public String invalidMessage(Input input) {
		return input.getName() + "=" + input.getValue() + " is invalid. ";
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(Serializable value) {
		this.value = value;
	}

}
