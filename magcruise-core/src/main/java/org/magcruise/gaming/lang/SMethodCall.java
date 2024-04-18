package org.magcruise.gaming.lang;

import org.magcruise.gaming.util.LiteralUtils;

@SuppressWarnings("serial")
public class SMethodCall<T> implements SExpression {

	protected String sExpression;

	/**
	 * SConstructorで作成したオブジェクトに対するメソッド呼び出し．
	 *
	 * @param target
	 * @param methodName
	 * @param args
	 */
	public SMethodCall(SConstructor<?> target, String methodName, Object... args) {
		this.sExpression = createMethod(target.getExpression(), methodName, args);

	}

	/**
	 * Staticメソッドの呼び出し
	 *
	 * @param target the target of static method call.
	 * @param methodName
	 * @param args
	 */
	public SMethodCall(Class<?> target, String methodName, Object... args) {
		this.sExpression = createMethod(target.getName(), methodName, args);
	}

	/**
	 *
	 * @param target targetの評価結果に対してメソッドが呼ばれる．
	 * @param methodName
	 * @param args
	 */
	public SMethodCall(SMethodCall<?> target, String methodName, Object... args) {
		this.sExpression = createMethod(target.getExpression(), methodName, args);
	}

	private static String createMethod(String target, String methodName, Object... args) {
		return "(" + target + ":" + methodName + (args.length == 0 ? "" : " ")
				+ String.join(" ", LiteralUtils.toLiteralsList(ToExpressionStyle.DEFAULT, args))
				+ ")";
	}

	public T call(String environmentName) {
		return SchemeEnvironment.eval(environmentName, this);
	}

	@Override
	public String toString() {
		return getExpression();
	}

	@Override
	public String getExpression(ToExpressionStyle style) {
		return sExpression;
	}

	/**
	 * SConstructorで作成したオブジェクトへのメソッド呼び出し．
	 *
	 * @param returnValType
	 * @param target
	 * @param methodName
	 * @param args
	 * @return
	 */
	public static <T> SMethodCall<T> toSMethodCall(Class<T> returnValType, SConstructor<?> target,
			String methodName, Object... args) {
		return new SMethodCall<T>(target, methodName, args);
	}

	/**
	 * Static メソッドの呼び出し
	 *
	 * @param returnValType
	 * @param target
	 * @param methodName
	 * @param args
	 * @return
	 */
	public static <T> SMethodCall<T> toSMethodCall(Class<T> returnValType, Class<?> target,
			String methodName, Object... args) {
		return new SMethodCall<T>(target, methodName, args);
	}

	/**
	 * SMethodCallの入れ子の呼び出し
	 *
	 * @param returnValType
	 * @param target
	 * @param methodName
	 * @param args
	 * @return
	 */
	public static <T> SMethodCall<T> toSMethodCall(Class<T> returnValType, SMethodCall<?> target,
			String methodName, Object... args) {
		return new SMethodCall<T>(target, methodName, args);
	}

}
