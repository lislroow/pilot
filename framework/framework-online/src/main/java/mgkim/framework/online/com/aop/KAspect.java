package mgkim.framework.online.com.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.cmm.online.CmmMapperAspect;
import mgkim.framework.cmm.online.CmmServiceAspect;
import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.logging.KLogMarker;
import mgkim.framework.core.type.TExecType;
import mgkim.framework.core.util.KObjectUtil;


@Aspect
@KBean
public class KAspect implements InitializingBean {
	
	private static final Logger log = LoggerFactory.getLogger(KAspect.class);
	
	@Autowired(required = false)
	private CmmServiceAspect cmmServiceAspect;
	
	@Autowired(required = false)
	private CmmMapperAspect cmmMapperAspect;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (cmmServiceAspect == null) {
			log.warn(KMessage.get(KMessage.E5002, KObjectUtil.name(CmmServiceAspect.class)));
		}
		if (cmmMapperAspect == null) {
			log.warn(KMessage.get(KMessage.E5002, KObjectUtil.name(CmmMapperAspect.class)));
		}
	}
	
	@Around(value="execution(public * "+KProfile.BASE_PACKAGE+"..*Service*.*(..))"
			+ " && !@annotation(mgkim.framework.core.annotation.KNonAspect)")
	public Object aroundService(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();
		Object result = null;
		if (KContext.getT(AttrKey.EXEC_TYPE) == TExecType.REQUEST) {
			String clazzName = joinPoint.getSignature().getDeclaringTypeName();
			String methodName = joinPoint.getSignature().getName();
			
			String pkg = joinPoint.getSignature().getDeclaringType().getPackage().getName();
			String shortStr = joinPoint.getSignature().toShortString();
			log.debug(KLogMarker.aop, "{}.{}", pkg, shortStr);
			
			// 전처리
			if (cmmServiceAspect != null) {
				cmmServiceAspect.preProcess(clazzName, methodName, args);
			}
			result = joinPoint.proceed();
			// 후처리
			if (cmmServiceAspect != null) {
				cmmServiceAspect.postProcess(clazzName, methodName, result);
			}
		} else {
			result = joinPoint.proceed();
		}
		return result;
	}

	@Around(value="execution(public * "+KProfile.BASE_PACKAGE+"..*Mapper.*(..))"
			+ " && !@annotation(mgkim.framework.core.annotation.KNonAspect)")
	public Object aroundMapper(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();
		Object result = null;
		if (KContext.getT(AttrKey.EXEC_TYPE) == TExecType.REQUEST) {
			String clazzName = joinPoint.getSignature().getDeclaringTypeName();
			String methodName = joinPoint.getSignature().getName();
			
			String pkg = joinPoint.getSignature().getDeclaringType().getPackage().getName();
			String shortStr = joinPoint.getSignature().toShortString();
			log.debug(KLogMarker.aop, "{}.{}", pkg, shortStr);
			
			// 전처리
			// `full-package 명` == `SQL namespace`
			// `method 명` == `SQL id`
			{
				//KContext.resetSql();
				//String sqlId = String.format("%s.%s",
				//		joinPoint.getSignature().getDeclaringTypeName()
				//		, joinPoint.getSignature().getName());
				//KContext.set(AttrKey.SQL_ID, sqlId);
				//if (KContext.getT(AttrKey.EXEC_TYPE) == TExecType.REQUEST) {
				//	KContext.set(AttrKey.SQL_ID, sqlId);
				//}
				
				if (cmmMapperAspect != null) {
					cmmMapperAspect.preProcess(clazzName, methodName, args);
				}
			}
			
			result = joinPoint.proceed();
			
			// 후처리
			if (cmmMapperAspect != null) {
				cmmMapperAspect.postProcess(clazzName, methodName, result);
			}
		} else {
			result = joinPoint.proceed();
		}
		return result;
	}
	
	//@Around(value="execution(public * mgkim..*Controller.*(..))"
	//		+ " && execution(public * mgkim..*Controller.*(..))"
	//		+ " && !@annotation(mgkim.framework.core.annotation.KNonAspect)")
	//public Object aroundForLogging(ProceedingJoinPoint joinPoint) throws Throwable {
	//	if (KContext.getT(AttrKey.EXEC_TYPE) == TExecType.REQUEST) {
	//		String pkg = joinPoint.getSignature().getDeclaringType().getPackage().getName();
	//		String shortStr = joinPoint.getSignature().toShortString();
	//		log.debug(KLogMarker.aop, "{}.{}", pkg, shortStr);
	//	}
	//	Object result = joinPoint.proceed();
	//	return result;
	//}

}
