package mgkim.online.com.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;

public class KLogSign {

	private static final Logger sign = LoggerFactory.getLogger("KLogSign");

	private static String getSignFile() {
		String result = "";
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		SiftingAppender appender = (SiftingAppender) context.getLogger("KLogSign").getAppender("API2-KLogSign");
		RollingFileAppender<?> fileAppender = (RollingFileAppender<?>) (appender.getAppenderTracker().allComponents().toArray()[0]);
		result = fileAppender.getFile();
		return result;
	}

	public static String sign(String filePath, String message) {
		MDC.put("filePath", filePath);
		sign.info(message);
		String file = getSignFile();
		MDC.remove("filePath");
		return file;
	}

}
