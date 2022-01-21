package mgkim.service.cmm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mgkim.framework.cmm.online.CmmMapperAspect;
import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.annotation.KNonAspect;
import mgkim.framework.core.logging.KLogMarker;
import mgkim.framework.core.util.KDtoUtil;

@KBean
public class CmmMapperAspectImpl implements CmmMapperAspect {
	
	private static final Logger log = LoggerFactory.getLogger(CmmMapperAspectImpl.class);

	@KNonAspect
	@Override
	public void preProcess(String clazzName, String methodName, Object[] args) throws Throwable {
		if (args == null) {
			return;
		}
		
		log.trace(KLogMarker.aop, "sys-field 설정 {}.{}()", clazzName, methodName);
		for (Object obj : args) {
			KDtoUtil.setSysValues(obj);
		}
	}
	
	@KNonAspect
	@Override
	public void postProcess(String clazzName, String methodName, Object ret) throws Throwable {
	}

}