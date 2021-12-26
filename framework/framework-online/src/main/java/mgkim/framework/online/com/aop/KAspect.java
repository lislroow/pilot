package mgkim.framework.online.com.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.env.KConfig;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.type.TExecType;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.online.cmm.aop.CmmMapperAspect;
import mgkim.framework.online.cmm.aop.CmmServiceAspect;


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

	@Around(value="execution(public * mgkim..*Service*.*(..))"
			+ " && !@annotation(mgkim.framework.core.annotation.KAspect)")
	public Object aroundService(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();

		// 전처리
		{
			if (cmmServiceAspect != null) {
				cmmServiceAspect.preProcess(args);
			}
		}

		Object result = joinPoint.proceed();

		// 후처리
		{
			if (cmmServiceAspect != null) {
				cmmServiceAspect.postProcess(result);
			}
		}
		return result;
	}

	@Around(value="execution(public * mgkim..*Mapper.*(..))"
			+ " && !@annotation(mgkim.framework.core.annotation.KAspect)")
	public Object aroundMapper(ProceedingJoinPoint joinPoint) throws Throwable {
		Object[] args = joinPoint.getArgs();

		// 전처리
		{
			// `full-package 명` == `SQL namespace`
			// `method 명` == `SQL id`
			{
				KContext.resetSql();
				String sqlId = String.format("%s.%s",
						joinPoint.getSignature().getDeclaringTypeName()
						, joinPoint.getSignature().getName());
				KContext.set(AttrKey.SQL_ID, sqlId);
				if (KContext.getT(AttrKey.EXEC_TYPE) == TExecType.REQUEST) {
					KContext.set(AttrKey.SQL_ID, sqlId);

				}
			}

			if (cmmMapperAspect != null) {
				cmmMapperAspect.preProcess(args);
			}
		}

		Object result = joinPoint.proceed();

		// 후처리
		{
			if (cmmMapperAspect != null) {
				cmmMapperAspect.postProcess(result);
			}
		}
		return result;
	}


	@SuppressWarnings("unchecked")
	@Around(value="execution(public * mgkim..*.*(..))"
			+ " && !execution(public * mgkim.framework.online..*.*(..))"
			+ " && !@annotation(mgkim.framework.core.annotation.KAspect)")
	public Object aroundForLogging(ProceedingJoinPoint joinPoint) throws Throwable {
		boolean isVerboss = KConfig.VERBOSS_ALL;
		if (!isVerboss) {
			return joinPoint.proceed();
		}
		String pkg = joinPoint.getSignature().getDeclaringType().getPackage().getName();
		String shortStr = joinPoint.getSignature().toShortString();
		String text = null;

		if (joinPoint.getSignature().getDeclaringType().getAnnotation(RestController.class) != null) {
			text = String.format("%s %s.%s", KConstant.LT_CLASS, pkg, shortStr);
		} else if (joinPoint.getSignature().getDeclaringType().getAnnotation(Service.class) != null) {
			text = String.format("%s %s.%s", KConstant.LT_CLASS, pkg, shortStr);
		} else if (joinPoint.getSignature().getDeclaringType().getAnnotation(Component.class) != null) {
			text = String.format("%s %s.%s", KConstant.LT_CLASS, pkg, shortStr);
		} else if (joinPoint.getSignature().getDeclaringType().getAnnotation(org.apache.ibatis.annotations.Mapper.class) != null) {
			text = String.format("%s %s.%s", KConstant.LT_CLASS, pkg, shortStr);
		} else {
			if (joinPoint.getSignature().getDeclaringType().getAnnotation(Controller.class) != null) {
				text = String.format("%s %s.%s", KConstant.LT_CLASS, pkg, shortStr);
			} else {
				text = String.format("%s %s.%s", KConstant.LT_CLASS, pkg, shortStr);
			}
		}

		log.info(text);
		Object result = joinPoint.proceed();

		return result;
	}

}
