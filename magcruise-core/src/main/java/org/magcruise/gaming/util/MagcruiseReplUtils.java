package org.magcruise.gaming.util;

import org.magcruise.gaming.lang.SchemeEnvironment;
import gnu.expr.Language;
import gnu.kawa.io.CheckConsole;
import gnu.mapping.Environment;
import kawa.GuiConsole;
import kawa.repl;
import kawa.standard.Scheme;

public class MagcruiseReplUtils {
	private static String loadStr = "";

	static {
		Scheme.registerEnvironment();
		setupLoadStr();
	}

	private static void setupLoadStr() {
		SchemeEnvironment.getFrameworkPaths().forEach((scm) -> {
			loadStr += "(load (path \"" + scm + "\"))";
		});
	}

	private static String[] opts = { "--full-tailcalls", "--warn-undefined-variable=no",
			"--warn-invoke-unknown-method=no", "-e", loadStr, "--" };

	/**
	 * 呼び出しスレッドと同じスレッドで実行されることに注意．
	 */
	public static void startReplWithGui() {
		CheckConsole.setHaveConsole(true);
		repl.processArgs(opts, 0, opts.length);
		@SuppressWarnings("unused")
		GuiConsole guiConsole = new GuiConsole(Language.getDefaultLanguage(),
				Environment.getCurrent(), true);
	}

	public static void startRepl() {
		System.out.println("input expr ...");
		repl.main(opts);
	}
}
