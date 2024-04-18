package org.magcruise.gaming.lang;

import org.magcruise.gaming.util.LiteralUtils;

@SuppressWarnings("serial")
public class SimpleSExpression implements SExpression {

	protected String sExpression;

	/**
	 *
	 * @param sExpression Scheme.evalで評価可能な文字列を入れなければならない．
	 */
	public SimpleSExpression(String sExpression) {
		this.sExpression = sExpression;
	}

	@Override
	public String toString() {
		return getExpression();
	}

	@Override
	public String getExpression(ToExpressionStyle style) {
		switch (style) {
		case DEFAULT:
			return sExpression;
		case MULTI_LINE:
			return sExpression;
		case SINGLE:
			return sExpression.replaceAll(System.lineSeparator(), "");
		default:
			return sExpression;
		}
	}

	/**
	 * Scheme.evalで評価可能なSExpressionを得る．
	 *
	 * @param sexpr
	 * @return
	 */
	public static SExpression parse(ToExpressionStyle style, String sexpr) {
		String str = LiteralUtils.toLiteral(style, sexpr);
		String s = str.substring(str.indexOf("\"") + 1, str.lastIndexOf("\""));
		return new SimpleSExpression(s);
	}

}
