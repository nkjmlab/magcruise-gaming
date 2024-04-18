package org.magcruise.gaming.util;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.magcruise.gaming.lang.SConstructive;
import org.magcruise.gaming.lang.SConstructor;
import org.magcruise.gaming.lang.SExpression.ToExpressionStyle;
import org.magcruise.gaming.lang.SMethodCall;
import gnu.expr.Keyword;
import gnu.lists.LList;
import gnu.lists.Pair;
import gnu.mapping.Symbol;

public class LiteralUtils {
	protected static Logger log = LogManager.getLogger();

	public static Object[] addToHead(Object headElement, Object[] array) {
		Object[] headElementArray = new Object[1];
		headElementArray[0] = headElement;
		return ArrayUtils.addAll(headElementArray, array);
	}

	public static Serializable deepCopy(Object obj) {
		Serializable copy;
		if (obj instanceof LList) {
			copy = deepCopyLList((LList) obj);
		} else {
			copy = SerializationUtils.clone((Serializable) obj);
		}
		return copy;
	}

	private static Serializable deepCopyLList(LList obj) {
		Object rest = obj;
		List<Serializable> l = new ArrayList<>();
		while (true) {
			if (rest == LList.Empty)
				break;
			if (rest instanceof Pair) {
				Pair pair = (Pair) rest;
				l.add(deepCopy(pair.getCar()));
				rest = pair.getCdr();
			} else {
				l.add(deepCopy(rest));
				break;
			}
		}
		return (Serializable) l;
	}

	public static String toLiteral(ToExpressionStyle style, Object val) {
		if (val == null) {
			return " #!null";
		}
		String expr;
		if (val instanceof LList) {
			expr = llistToLiteral(style, (LList) val);
		} else if (val.getClass().isArray()) {
			List<Object> list = new ArrayList<>();
			for (int i = 0; i < Array.getLength(val); i++) {
				Object a = Array.get(val, i);
				list.add(a);
			}
			expr = llistToLiteral(style, LList.makeList(list));
			expr = expr.replaceFirst("\\(list",
					"(" + val.getClass().getComponentType().getName() + "[]");
		} else if (val instanceof List) {
			expr = String.format("(%s %s)",
					(val.getClass().getName().equals("java.util.Arrays$ArrayList")
							? "java.util.ArrayList"
							: val.getClass().getName()),
					llistToLiteral(style, LList.makeList((List<?>) val)));
		} else if (val instanceof Set) {
			expr = String.format("(%s %s)", val.getClass().getName(),
					llistToLiteral(style, LList.makeList(new ArrayList<>((Set<?>) val))));
		} else if (val instanceof Map<?, ?>) {
			Map<?, ?> tmp = (Map<?, ?>) val;
			String entries = "";
			for (Object key : tmp.keySet()) {
				entries += "(m:put " + toLiteral(style, key) + " " + toLiteral(style, tmp.get(key))
						+ ")";
			}
			expr = String.format("(let ((m ::%s (%s))) %s m)", Map.class.getName(),
					val.getClass().getName(), entries);
		} else if (val instanceof org.apache.commons.lang3.tuple.Pair) {
			org.apache.commons.lang3.tuple.Pair<?, ?> p = (org.apache.commons.lang3.tuple.Pair<?, ?>) val;
			expr = String.format("(%s:of %s %s", p.getClass(), toLiteral(style, p.getLeft()),
					toLiteral(style, p.getRight()));
		} else if (val instanceof SConstructor<?>) {
			expr = ((SConstructor<?>) val).getExpression(style);
		} else if (val instanceof SConstructive) {
			expr = ((SConstructive) val).toConstructor(style).getExpression(style);
		} else if (val instanceof Date) {
			expr = String.format("(%s %s)", val.getClass().getName(), ((Date) val).getTime());
		} else if (val instanceof Enum) {
			expr = " (" + val.getClass().getName() + " \"" + ((Enum<?>) val).name() + "\")";
		} else if (val instanceof URL || val instanceof URI) {
			expr = SConstructor.toConstructor(style, val.getClass(), val.toString())
					.getExpression(style);
		} else if (val instanceof java.nio.file.Path) {
			expr = SMethodCall.toSMethodCall(val.getClass(), Paths.class, "get",
					toEscapeBackSlashAndDoubleQuotation(val.toString())).getExpression(style);
		} else if (val instanceof gnu.kawa.io.Path) {
			expr = "(path \"" + toEscapeBackSlashAndDoubleQuotation(val.toString()) + "\")";
		} else if (val instanceof Class<?>) {
			expr = " " + ((Class<?>) val).getCanonicalName();
		} else if (val instanceof File) {
			expr = SConstructor
					.toConstructor(style, val.getClass(),
							toEscapeBackSlashAndDoubleQuotation(val.toString()))
					.getExpression();
		} else if (val instanceof Number) {
			expr = " " + val.toString();
		} else if (val instanceof Symbol) {
			expr = " \'" + val;
		} else if (val instanceof Boolean) {
			if ((Boolean) val) {
				expr = " #t";
			} else {
				expr = " #f";
			}
        } else if (val instanceof String) {
          String str = (String) val;
          str = toEscapeBackSlashAndDoubleQuotation(str);
          expr = (" \"" + str + "\"");
        } else if (val instanceof gnu.lists.IString) {
          gnu.lists.IString istr = (gnu.lists.IString) val;
          String str = toEscapeBackSlashAndDoubleQuotation(istr.toString());
          expr = (" \"" + str + "\"");
		} else {
			expr = val.toString();
		}
		return expr;
	}

	private static String toEscapeBackSlashAndDoubleQuotation(String str) {
		return str.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private static String llistToLiteral(ToExpressionStyle style, LList obj) {
		if (obj == null) {
			return "#!null";
		}
		Object rest = obj;
		StringBuffer sbuf = new StringBuffer(100);
		sbuf.append("(list ");
		for (int i = 0;;) {
			if (rest == LList.Empty)
				break;
			if (i > 0)
				sbuf.append(' ');
			if (rest instanceof Pair) {
				Pair pair = (Pair) rest;
				sbuf.append(toLiteral(style, pair.getCar()));
				rest = pair.getCdr();
			} else {
				sbuf.append(". ");
				sbuf.append(toLiteral(style, rest));
				break;
			}
			i++;
		}
		sbuf.append(')');
		return sbuf.toString();
	}

	public static List<String> toLiteralsList(ToExpressionStyle style, Object... args) {
		return toLiteralsList(style, Arrays.asList(args));
	}

	public static List<String> toLiteralsList(ToExpressionStyle style, List<Object> args) {
		List<String> result = new ArrayList<>();
		args.forEach(arg -> {
			result.add(toLiteral(style, arg));
		});
		return result;
	}

	public static Map<Keyword, Object> createArgsMap(LList args) {
		Map<Keyword, Object> argsMap = new HashMap<>();
		proc(args, argsMap);
		return argsMap;
	}

	private static void proc(LList args, Map<Keyword, Object> argsMap) {
		if (!(args instanceof Pair)) {
			return;
		}
		Pair p = (Pair) args;
		Object car = p.getCar();
		LList cdr = ((LList) p.getCdr());
		if (car instanceof Keyword) {
			argsMap.put((Keyword) car, ((Pair) cdr).getCar());
		} else {
			argsMap.put(Keyword.make("rest"), new ArrayList<Object>(Arrays.asList(args.toArray())));
			return;
		}

		proc((LList) ((Pair) cdr).getCdr(), argsMap);
	}

}
