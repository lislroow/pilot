package mgkim.service.adm.cmm.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mgkim.framework.core.annotation.KAspect;
import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.logging.KLogMarker;
import mgkim.framework.core.util.KDtoUtil;
import mgkim.framework.online.cmm.aop.CmmServiceAspect;

@KBean
public class CmmServiceAspectImpl implements CmmServiceAspect {
	
	private static final Logger log = LoggerFactory.getLogger(CmmServiceAspectImpl.class);

	@Override
	@KAspect
	public void preProcess(Object[] args) throws Throwable {
		if (args == null) {
			return;
		}
		log.debug(KLogMarker.aop_stereo, "sys-field 설정");
		for (Object obj : args) {
			KDtoUtil.setSysValues(obj);
		}
	}

	@Override
	@KAspect
	public void postProcess(Object ret) throws Throwable {
	}

}