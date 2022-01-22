package mgkim.framework.online.com.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.annotation.KRequestMap;
import mgkim.framework.core.util.KHttpUtil;
import mgkim.framework.core.util.KStringUtil;

@Aspect
@KBean
public class KRequestMapHandler {
	
	private static final Logger log = LoggerFactory.getLogger(KRequestMapHandler.class);
	
	@Before(value="execution(public * mgkim..*Controller*.*(..))")
	public void beforeController(JoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		
		// 2022.01.22 http-get 일 경우 querystring 을 map 으로 변환  
		for (int paramIdx=0; paramIdx<method.getParameters().length; paramIdx++) {
			Parameter param = method.getParameters()[paramIdx];
			Annotation ann = param.getAnnotation(KRequestMap.class);
			if (ann != null) {
				Object[] args = joinPoint.getArgs();
				String querystrings = KStringUtil.nvl(KHttpUtil.getRequest().getQueryString());
				Map<String, String> m = Arrays.stream(querystrings.split("&"))
						.collect(HashMap<String, String>::new,
								(map, qs) -> map.put(qs.split("=")[0], qs.split("=")[1]),
								HashMap<String, String>::putAll);
				((Map)args[paramIdx]).putAll(m);
			}
		}
		
	}
}
