package mgkim.framework.core.util;

import java.util.Arrays;
import java.util.List;

public class KThreadUtil {

	public static String stack() {
		StackTraceElement[] s = Thread.currentThread().getStackTrace();
		List<StackTraceElement> list = Arrays.asList(s);
		return list.toString();
	}
}
