package mgkim.online.com.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

public class KLogApm {

	private static final Logger api = LoggerFactory.getLogger("KLogApm-api");

	private static final Logger apiDetails = LoggerFactory.getLogger("KLogApm-api-details");

	public static void api(String uri, double elapsedTime) {
		api.trace("[api][{}]", elapsedTime);
	}

	public static void filter(StopWatch stopWatch) {
		apiDetails.trace("[api-filter][{}][{}]", stopWatch.getId(), stopWatch.getTotalTimeSeconds());
	}

	public static void sql(StopWatch stopWatch) {
		apiDetails.trace("[api-sql][{}][{}]", stopWatch.getId(), stopWatch.getTotalTimeSeconds());
	}
}
