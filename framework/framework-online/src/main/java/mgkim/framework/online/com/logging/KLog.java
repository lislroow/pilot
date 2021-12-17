package mgkim.framework.online.com.logging;

import static mgkim.framework.online.com.env.KConstant.CALLER;
import static mgkim.framework.online.com.env.KConstant.LOGSPACE;

import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.util.Assert;

public final class KLog {

	private static final Logger log = LoggerFactory.getLogger("KLog");

	private static void pre(String logspace) {
		Assert.notNull(logspace, "logspace is not null");
		MDC.put(LOGSPACE, logspace);

		StackTraceElement s = Thread.currentThread().getStackTrace()[3];
		String caller = String.format("%s:%d", s.getClassName(), s.getLineNumber());
		MDC.put(CALLER, caller);
	}
	private static void post() {
		MDC.remove(LOGSPACE);

		MDC.remove(CALLER);
	}

	// trace
	public static void trace(String logspace, String message) {
		pre(logspace);
		log.trace(message);
		post();
	}
	public static void trace(String logspace, String format, Object ... arguments) {
		pre(logspace);
		log.trace(format, Stream.of(arguments).flatMap(Stream::of).toArray());
		post();
	}
	// trace


	// debug
	public static void debug(String logspace, String message) {
		pre(logspace);
		log.debug(message);
		post();
	}
	public static void debug(String logspace, String format, Object ... arguments) {
		pre(logspace);
		log.debug(format, Stream.of(arguments).flatMap(Stream::of).toArray());
		post();
	}
	// debug

	// info
	public static void info(String logspace, String message) {
		pre(logspace);
		log.info(message);
		post();
	}
	public static void info(String logspace, String format, Object ... arguments) {
		pre(logspace);
		log.info(format, Stream.of(arguments).flatMap(Stream::of).toArray());
		post();
	}
	// info

	// warn
	public static void warn(String logspace, String message) {
		pre(logspace);
		log.warn(message);
		post();
	}
	public static void warn(String logspace, String format, Object ... arguments) {
		pre(logspace);
		log.warn(format, Stream.of(arguments).flatMap(Stream::of).toArray());
		post();
	}
	// warn

	// error
	public static void error(String logspace, String message) {
		pre(logspace);
		log.error(message);
		post();
	}
	public static void error(String logspace, String message, Throwable e) {
		pre(logspace);
		log.warn(message, e);
		post();
	}
	public static void error(String logspace, String format, Object ... arguments) {
		pre(logspace);
		log.error(format, Stream.of(arguments).flatMap(Stream::of).toArray());
		post();
	}
	// error
}
