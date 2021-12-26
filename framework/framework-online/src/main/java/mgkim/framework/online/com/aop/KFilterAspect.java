package mgkim.framework.online.com.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.env.KConfig;
import mgkim.framework.core.env.KConstant;


@Profile(value = "debug")
@Aspect
@KBean
public class KFilterAspect {
	
	private static final Logger log = LoggerFactory.getLogger(KFilterAspect.class);

	@Around(value="execution(protected * mgkim.framework.online..filter.*.doFilterInternal(..))")
	public Object aroundFilter(ProceedingJoinPoint joinPoint) throws Throwable {
		if (!KConfig.DEBUG_FILTER) {
			return joinPoint.proceed();
		}
		String clazzStr = joinPoint.getSignature().getDeclaringType().getSimpleName();
		String filterName = null;
		@SuppressWarnings("unchecked")
		KBean annotation = (KBean) joinPoint.getSignature().getDeclaringType().getAnnotation(KBean.class);
		if (annotation != null) {
			filterName = annotation.name();
		}
		Object result = null;
		try {
			log.info(String.format("%s %s `%s`", KConstant.LT_FILTER, clazzStr, filterName));
			result = joinPoint.proceed();
		} finally {
		}
		return result;
	}

}
