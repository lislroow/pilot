package mgkim.framework.online.com.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.stream.Stream;

import mgkim.framework.online.com.env.KConstant;
import mgkim.framework.online.com.env.KProfile;

public class KExceptionUtil {

	public static String getTrace(Throwable t) {
		// TODO boolean isFilterEnable = @condition
		return getTrace(t, true);
	}

	public static String getTrace(Throwable t, boolean isFilter) {
		String result = null;
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		Throwable throwable = null;
		if(t.getCause() != null) {
			throwable = t.getCause();
		} else {
			throwable = t;
		}
		if(isFilter) {
			Stream<StackTraceElement> stream = Arrays.stream(throwable.getStackTrace()).filter(se -> se.getClassName().startsWith(KProfile.GROUP+"."));
			StackTraceElement[] trace = stream.collect(java.util.stream.Collectors.toList()).toArray(new StackTraceElement[0]);
			throwable.setStackTrace(trace);
			throwable.printStackTrace(pw);
			//result = String.format("%s \n %s", "[ *** filtered *** ]", sw.toString());
			result = sw.toString();
		} else {
			throwable.printStackTrace(pw);
			result = sw.toString();
		}

		return result;
	}

	public static String getMessage(Exception e) {
		String result = null;
		result = KStringUtil.nvl(e.getMessage(), KConstant.EMPTY);
		result = result.replaceAll("(\"|\\n)", KConstant.EMPTY);
		return result;
	}

	public static String getCauseMessage(Exception e) {
		String result = null;
		if(e.getCause() != null) {
			result = KStringUtil.nvl(e.getCause().getMessage());
		} else {
			result = KStringUtil.nvl(e.getMessage());
		}
		result = result.replaceAll("(\"|\\n)", KConstant.EMPTY);
		return result;
	}

	@SuppressWarnings("unchecked")
	public static boolean isCausedBy(Exception ex, Class<? extends Exception>... causeExceptionClasses) {
		Throwable cause = ex.getCause();
		while(cause != null) {
			for(Class<? extends Exception> causeClass : causeExceptionClasses) {
				if(causeClass.isInstance(cause)) {
					return true;
				}
			}
			cause = cause.getCause();
		}
		return false;
	}
}
