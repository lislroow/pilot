package mgkim.framework.online.com.handler;

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
import org.springframework.web.bind.annotation.PathVariable;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.annotation.KPageIdx;
import mgkim.framework.core.annotation.KPageUnit;
import mgkim.framework.core.annotation.KRequestMap;
import mgkim.framework.core.annotation.KRowUnit;
import mgkim.framework.core.dto.KInPageVO;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.util.KHttpUtil;
import mgkim.framework.core.util.KStringUtil;

@Aspect
@KBean
public class KRequestMapHandler {
	
	private static final Logger log = LoggerFactory.getLogger(KRequestMapHandler.class);
	
	@Before(value="execution(public * mgkim..*Controller*.*(..))")
	public void beforeController(JoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();
		
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		
		KInPageVO inPageVO = null;
		// 2022.01.22 http-get 일 경우 querystring 을 map 으로 변환  
		for (int paramIdx=0; paramIdx<method.getParameters().length; paramIdx++) {
			Parameter param = method.getParameters()[paramIdx];
			if (param.getAnnotation(KPageIdx.class) != null) {
				if (inPageVO == null) {
					inPageVO = KContext.getT(AttrKey.IN_PAGE);
					if (inPageVO == null) {
						inPageVO = new KInPageVO();
						KContext.set(AttrKey.IN_PAGE, inPageVO);
					}
				}
				inPageVO.setPageindex(Integer.parseInt(KStringUtil.nvl(args[paramIdx], "1")));
			} else if (param.getAnnotation(KRowUnit.class) != null) {
				if (inPageVO == null) {
					inPageVO = KContext.getT(AttrKey.IN_PAGE);
					if (inPageVO == null) {
						inPageVO = new KInPageVO();
						KContext.set(AttrKey.IN_PAGE, inPageVO);
					}
				}
				inPageVO.setRowunit(Integer.parseInt(KStringUtil.nvl(args[paramIdx], "3")));
			} else if (param.getAnnotation(KPageUnit.class) != null) {
				if (inPageVO == null) {
					inPageVO = KContext.getT(AttrKey.IN_PAGE);
					if (inPageVO == null) {
						inPageVO = new KInPageVO();
						KContext.set(AttrKey.IN_PAGE, inPageVO);
					}
				}
				inPageVO.setPageunit(Integer.parseInt(KStringUtil.nvl(args[paramIdx], "10")));
			} else if (param.getAnnotation(PathVariable.class) != null) {
				((Map)args[0]).put(param.getAnnotation(PathVariable.class).name(), args[paramIdx]);
			} else if (param.getAnnotation(KRequestMap.class) != null) {
				String querystrings = KStringUtil.nvl(KHttpUtil.getRequest().getQueryString());
				Map<String, Object> m = Arrays.stream(querystrings.split("&"))
						.collect(HashMap<String, Object>::new,
								(map, qs) -> {
									if (qs.split("=").length == 2 ) {
										map.put(qs.split("=")[0], qs.split("=")[1]);
									}
								},
								HashMap<String, Object>::putAll);
				((Map)args[0]).putAll(m);
			}
		}
		
	}
}
