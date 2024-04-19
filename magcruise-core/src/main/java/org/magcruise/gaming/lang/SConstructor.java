package org.magcruise.gaming.lang;

import java.util.ArrayList;
import java.util.List;
import org.magcruise.gaming.util.LiteralUtils;

@SuppressWarnings("serial")
public class SConstructor<T> implements SExpression {

	protected String sExpression;

	public SConstructor() {

	}

	public SConstructor(String sExpression) {
		this.sExpression = sExpression;
	}

	@Override
	public String toString() {
		return getExpression();
	}

	public T makeInstance(String environmentName) {
		return SchemeEnvironment.makeInstance(environmentName, this);
	}

	@Override
	public String getExpression(ToExpressionStyle style) {
		if (style == ToExpressionStyle.SINGLE) {
			return sExpression.replaceAll(System.lineSeparator(), "");
		}
		return sExpression;
	}

	/**
	 * JSONで送るので必須．
	 */
	public String getSExpression() {
		return this.sExpression.replaceAll(System.lineSeparator(), "");
	}

	/**
	 * JSONで送るので必須．
	 */
	public void setSExpression(String sExpression) {
		this.sExpression = sExpression;
	}

	/**
	 * 指定したクラスのSConstructorを作成する．
	 *
	 * @param target
	 * @param constractorArgs List，Map，プリミティブクラス，SConstructiveなものなどtoLiteralでうまく扱えるものだけ
	 * @return
	 */
	public static <T> SConstructor<T> toConstructor(ToExpressionStyle style, Class<T> target,
			Object... constractorArgs) {
		List<String> strs = new ArrayList<>();

		for (Object val : constractorArgs) {
			strs.add(LiteralUtils.toLiteral(style, val));
		}

		return new SConstructor<T>("(" + target.getName() + (constractorArgs.length == 0 ? "" : " ")
				+ String.join(" ", strs) + ")");
	}

	public static <T> SConstructor<?> toConstructorWithSetter(ToExpressionStyle style,
			Class<T> target, String[] setterNames, Object... setterArgs) {
		String entries = "";
		for (int i = 0; i < setterNames.length; i++) {
			entries += "(o:" + setterNames[i] + " " + LiteralUtils.toLiteral(style, setterArgs[i])
					+ ")";
		}
		String result = String.format("(let ((o ::%s %s)) %s o)", target.getName(),
				toConstructor(style, target), entries);

		return new SConstructor<T>(result);
	}

}
