package mgkim.online.com.logging;

import ch.qos.logback.classic.Level;
import mgkim.online.com.env.KConstant;

public class KAnsi {

	public static final boolean JANSI;

	static {
		String val = System.getProperty(KConstant.JANSI);
		if (val != null && "true".equals(val)) {
			JANSI = true;
		} else {
			JANSI = false;
		}
	}

	public static final String RED = "\u001b[0;31m";
	public static final String GREEN = "\u001b[0;32m";
	public static final String YELLOW = "\u001b[0;33m";
	public static final String BLUE = "\u001b[0;34m";
	public static final String MAGENTA = "\u001b[0;35m";
	public static final String CYAN = "\u001b[0;36m";

	public static final String BOLD_RED = "\u001b[1;31m";
	public static final String BOLD_GREEN = "\u001b[1;32m";
	public static final String BOLD_YELLOW = "\u001b[1;33m";
	public static final String BOLD_BLUE = "\u001b[1;34m";
	public static final String BOLD_MAGENTA = "\u001b[1;35m";
	public static final String BOLD_CYAN = "\u001b[1;36m";

	public static final String END = "\u001b[m";


	public static String red(ch.qos.logback.classic.Level level) {
		switch(level.toInt()) {
		case Level.INFO_INT:
			return JANSI ? String.format("%s%s%s", BLUE, level.toString(), END) : level.toString();
		case Level.WARN_INT:
			return JANSI ? String.format("%s%s%s", RED, level.toString(), END) : level.toString();
		case Level.ERROR_INT:
			return JANSI ? String.format("%s%s%s", BOLD_RED, level.toString(), END) : level.toString();
		default:
			return level.toString();
		}
	}

	public static String red(String str) {
		return JANSI ? String.format("%s%s%s", RED, str, END) : str;
	}

	public static String green(String str) {
		return JANSI ? String.format("%s%s%s", GREEN, str, END) : str;
	}

	public static String yellow(String str) {
		return JANSI ? String.format("%s%s%s", YELLOW, str, END) : str;
	}

	public static String blue(String str) {
		return JANSI ? String.format("%s%s%s", BLUE, str, END) : str;
	}

	public static String magenta(String str) {
		return JANSI ? String.format("%s%s%s", MAGENTA, str, END) : str;
	}

	public static String cyan(String str) {
		return JANSI ? String.format("%s%s%s", CYAN, str, END) : str;
	}

	public static String boldRed(String str) {
		return JANSI ? String.format("%s%s%s", BOLD_RED, str, END) : str;
	}

	public static String boldGreen(String str) {
		return JANSI ? String.format("%s%s%s", BOLD_GREEN, str, END) : str;
	}

	public static String boldYellow(String str) {
		return JANSI ? String.format("%s%s%s", BOLD_YELLOW, str, END) : str;
	}

	public static String boldBlue(String str) {
		return JANSI ? String.format("%s%s%s", BOLD_BLUE, str, END) : str;
	}

	public static String boldMagenta(String str) {
		return JANSI ? String.format("%s%s%s", BOLD_MAGENTA, str, END) : str;
	}

	public static String boldCyan(String str) {
		return JANSI ? String.format("%s%s%s", BOLD_CYAN, str, END) : str;
	}
}
