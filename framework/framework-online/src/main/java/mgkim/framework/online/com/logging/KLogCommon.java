package mgkim.framework.online.com.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class KLogCommon {

	private static final Logger malformed = LoggerFactory.getLogger("KLogCommon-malformed");

	private static final Logger table = LoggerFactory.getLogger("KLogCommon-table");

	public static void malformed(String message) {
		malformed.info(message);
	}

	public static void malformed(String format, Object ... arguments) {
		if(arguments != null && arguments.length > 0 && arguments[0] instanceof Throwable) {
			malformed.error(format, arguments);
		} else {
			malformed.info(format, arguments);
		}
	}

	public static void table(String filePath, String message) {
		MDC.put("filePath", filePath);
		table.info(message);
		MDC.remove("filePath");
	}

}
