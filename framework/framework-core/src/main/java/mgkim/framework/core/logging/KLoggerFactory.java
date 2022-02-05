package mgkim.framework.core.logging;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.aop.framework.ProxyFactory;

import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.util.KStringUtil;

public class KLoggerFactory {

	static final Logger logger = org.slf4j.LoggerFactory.getLogger("klog");
	
	static final Pattern MDC_LABEL_PATTERN = Pattern.compile("^(.*)(\\{[0-9]+\\})$");
	
	public static <T> T getLogger(Class<T> klog, Class clazz) {
		return ProxyFactory.getProxy(klog,
				(MethodInterceptor) invocation -> {
					Object[] argv = invocation.getArguments();
					if (argv == null || argv.length < 2) {
						return null;
					}
					
					String mdc_label = KStringUtil.nvl(argv[0]);
					Matcher matcher = MDC_LABEL_PATTERN.matcher(mdc_label);
					if (matcher.find()) {
						String width = matcher.group(2);
						int w = Integer.parseInt(width.substring(1, width.length()-1));
						String label = matcher.replaceFirst("$1");
						mdc_label = KStringUtil.lpad(label, w, KConstant.SPACE);
					}
					String msg_format = KStringUtil.nvl(argv[1]);
					MDC.put("clazzname", clazz.getSimpleName());
					MDC.put("label", mdc_label);
					Object[] msgv = null;
					if (argv.length == 3) {
						msgv = Arrays.stream((Object[])argv[2]).collect(Collectors.toList()).toArray();
						logger.debug(msg_format, msgv);
					} else {
						logger.debug(msg_format);
					}
					MDC.remove("clazzname");
					MDC.remove("label");
					return null;
				}
			);
	}
}
