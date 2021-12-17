package mgkim.framework.core.logging;

import static mgkim.framework.core.env.KConstant.CALLER;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import mgkim.framework.core.env.KConfig;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.util.KStringUtil;

public class KLogSys {

	private static final Logger log = LoggerFactory.getLogger("KLogSys");

	private static final Logger accesslog = LoggerFactory.getLogger("KLogSys-accesslog");

	private static void pre() {
		StackTraceElement s = Thread.currentThread().getStackTrace()[3];
		String caller = String.format("%s:%d", s.getClassName(), s.getLineNumber());
		MDC.put(CALLER, caller);
	}
	private static void post() {
		MDC.remove(CALLER);
	}

	public static boolean matchesExclude(String packageName) {
		List<String> urlPatterns = KConfig.CMM_SQL;
		boolean matched = false;
		packageName = KStringUtil.nvl(packageName);
		for(String pattern : urlPatterns) {
			matched = packageName.startsWith(pattern);
			if(matched) {
				break;
			}
		}
		return matched;
	}

	public static boolean matchesExclude(String packageName, List<String> excludeList) {
		boolean matched = false;
		packageName = KStringUtil.nvl(packageName);
		for(String pattern : excludeList) {
			matched = packageName.startsWith(pattern);
			if(matched) {
				break;
			}
		}
		return matched;
	}

	public static String getDebugFile(String debugFilename) {
		String result = "";
		//LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		//SiftingAppender appender = (SiftingAppender) context.getLogger("KLogSys-debuglog").getAppender("API2-debug");
		result = debugFilename+".log";
		return result;
	}

	public static void accesslog() {
		pre();
		accesslog.info(KConstant.EMPTY);
		post();
	}

	// trace
	public static void trace(String message) {
		pre();
		log.trace(message);
		post();
	}
	public static void trace(String format, Object ... arguments) {
		pre();
		log.trace(format, arguments);
		post();
	}
	// trace


	// debug
	public static void debug(String message) {
		pre();
		log.debug(message);
		post();
	}
	public static void debug(String format, Object ... arguments) {
		pre();
		log.debug(format, arguments);
		post();
	}
	// debug

	// info
	public static void info(String message) {
		pre();
		log.info(message);
		post();
	}
	public static void info(String format, Object ... arguments) {
		pre();
		log.info(format, arguments);
		post();
	}
	// info

	// warn
	public static void warn(String message) {
		pre();
		log.warn(message);
		post();
	}
	public static void warn(String format, Object ... arguments) {
		pre();
		log.warn(format, arguments);
		post();
	}
	// warn

	// error
	public static void error(String message) {
		pre();
		log.error(message);
		post();
	}
	public static void error(String message, Throwable e) {
		pre();
		log.error(message, e);
		post();
	}
	public static void error(String format, Object ... arguments) {
		pre();
		log.error(format, arguments);
		post();
	}
	// error
}
