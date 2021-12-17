package mgkim.core.com.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;

import mgkim.core.com.annotation.KBean;
import mgkim.core.com.env.KConfig;
import mgkim.core.com.env.KConstant;
import mgkim.core.com.logging.KLogSys;


@Profile(value = "debug")
@Aspect
@KBean
public class KFilterAspect {

	@Around(value="execution(protected * mgkim.core..filter.*.doFilterInternal(..))")
	public Object aroundFilter(ProceedingJoinPoint joinPoint) throws Throwable {
		if(!KConfig.DEBUG_FILTER) {
			return joinPoint.proceed();
		}
		String clazzStr = joinPoint.getSignature().getDeclaringType().getSimpleName();
		String filterName = null;
		@SuppressWarnings("unchecked")
		KBean annotation = (KBean) joinPoint.getSignature().getDeclaringType().getAnnotation(KBean.class);
		if(annotation != null) {
			filterName = annotation.name();
		}
		Object result = null;
		try {
			KLogSys.info(String.format("%s %s `%s`", KConstant.LT_FILTER, clazzStr, filterName));
			result = joinPoint.proceed();
		} finally {
		}
		return result;
	}

}
