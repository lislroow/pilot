package mgkim.service.adm.cmm.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mgkim.framework.core.annotation.KAspect;
import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.util.KDtoUtil;
import mgkim.framework.online.cmm.aop.CmmServiceAspect;

@KBean
public class CmmServiceAspectImpl implements CmmServiceAspect {
	
	private static final Logger log = LoggerFactory.getLogger(CmmServiceAspectImpl.class);

	@Override
	@KAspect
	public void preProcess(Object[] args) throws Throwable {
		log.debug("업무서비스 `@Service` 메소드 전처리 (파라미터 전처리)");
		if (args == null) {
			return;
		}
		for (Object obj : args) {
			KDtoUtil.setSysValues(obj);
		}
	}

	@Override
	@KAspect
	public void postProcess(Object ret) throws Throwable {
		log.debug("업무서비스 `@Service` 메소드 후처리 (리턴 후처리)");
	}

}