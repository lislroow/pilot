package mgkim.framework.online.com.logging;

import static mgkim.framework.online.com.env.KConstant.CALLER;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.Level;
import mgkim.framework.core.type.TExecType;
import mgkim.framework.online.com.env.KConfig;
import mgkim.framework.online.com.env.KContext;
import mgkim.framework.online.com.env.KContext.AttrKey;

public class KLogSql {

	private static final Logger log = LoggerFactory.getLogger("KLogSql");

	private static void pre() {
		StackTraceElement s = Thread.currentThread().getStackTrace()[3];
		String caller = String.format("%s:%d", s.getClassName(), s.getLineNumber());
		MDC.put(CALLER, caller);
	}
	private static void post() {
		MDC.remove(CALLER);
	}

	public static int getLevel() {
		Level level = ((ch.qos.logback.classic.LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory()).getLogger("KLogSql").getLevel();
		if(level == null) {
			KLogSys.warn("logger `KLogSql`의 `level`을 얻어오는데, 실패했습니다. 기본값 `warn`으로 반환합니다.");
			return Level.WARN_INT;
		}
		return level.toInt();
	}

	public static boolean isComSql(String sqlId) {
		List<String> comSqlList = KConfig.CMM_SQL;
		boolean matched = false;
		for(String comSql : comSqlList) {
			matched = sqlId.startsWith(comSql);
			if(matched) {
				break;
			}
		}
		return matched;
	}

	public static boolean isLoggableSql(String sqlId) {
		TExecType execType = KContext.getT(AttrKey.EXEC_TYPE);
		boolean loggable = KContext.getT(AttrKey.LOGGABLE);
		switch(execType) {
		case REQUEST:
		case SYSTEM:
			boolean isComSql = isComSql(sqlId);
			if(!isComSql || KConfig.DEBUG_COM) {
				return true;
			}
			break;
		case SCHEDULE:
		default:
			return loggable;
		}
		return false;
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
