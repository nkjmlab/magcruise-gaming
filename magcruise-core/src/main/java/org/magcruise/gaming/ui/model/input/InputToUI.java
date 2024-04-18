/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 MAGCruise
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.magcruise.gaming.ui.model.input;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.magcruise.gaming.ui.model.attr.Attribute;

/**
 *
 * @author Yuu NAKAJIMA
 *
 */
public class InputToUI {

	private InputType type;
	private String label;
	private String name;
	private Serializable value;
	/**
	 * HTMLタグのアトリビュートのこと．JSONに変換して飛ばすので，setter-getterの名前が重要
	 */
	private Map<String, Serializable> attrs = new HashMap<>();
	private List<Attribute> attributes = new ArrayList<>();

	public InputToUI() {

	}

	public InputToUI(InputType type, String label, String name, Serializable value,
			Attribute... attributes) {
		this.type = type;
		this.label = label;
		this.name = name;
		this.value = value;
		this.attributes.addAll(Arrays.asList(attributes));
		this.attributes.forEach(a -> {
			putAttr(a.getName(), a.getValue());
		});
	}

	@SuppressWarnings("unchecked")
	public Input toInput() {
		switch (type) {
		case NUMBER:
			return new NumberInput(label, name, (Number) value,
					attributes.toArray(new Attribute[0]));
		case TEXT:
		case TEXTAREA:
			return new TextAreaInput(label, name, value.toString(),
					attributes.toArray(new Attribute[0]));
		case RADIO:
			return new RadioInput(label, name, value.toString(),
					(List<String>) getAttr("optionLabels"),
					(List<String>) getAttr("options"), attributes.toArray(new Attribute[0]));
		case CHECKBOX:
			return new CheckboxInput(label, name, (List<String>) value,
					(List<String>) getAttr("optionLabels"), (List<String>) getAttr("options"),
					attributes.toArray(new Attribute[0]));
		default:
			throw new IllegalArgumentException(type + " is not supported.");
		}
	}

	public InputType getType() {
		return type;
	}

	public void setType(InputType type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Serializable getValue() {
		return value;
	}

	public void setValue(Serializable value) {
		this.value = value;
	}

	public Map<String, Serializable> getAttrs() {
		return attrs;
	}

	public void setAttrs(Map<String, Serializable> htmlAttributes) {
		this.attrs.putAll(htmlAttributes);
	}

	public void putAttr(String attrName, Serializable value) {
		this.attrs.put(attrName, value);
	}

	public Serializable getAttr(String attrName) {
		return this.attrs.get(attrName);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public static InputFromWebUI[] toInputFromWebUIs(InputToUI[] inputs) {
		List<InputFromWebUI> result = new ArrayList<>();

		Arrays.asList(inputs).forEach(input -> {
			result.add(input.toInput().toInputFromWebUI());
		});

		return result.toArray(new InputFromWebUI[0]);
	}

}
