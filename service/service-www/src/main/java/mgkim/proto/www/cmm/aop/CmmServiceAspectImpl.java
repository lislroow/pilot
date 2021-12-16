package mgkim.proto.www.cmm.aop;

import mgkim.core.cmm.aop.CmmServiceAspect;
import mgkim.core.com.annotation.KBean;
import mgkim.core.com.annotation.KAspect;
import mgkim.core.com.logging.KLogSys;
import mgkim.core.com.util.KDtoUtil;

@KBean
public class CmmServiceAspectImpl implements CmmServiceAspect {

	@Override
	@KAspect
	public void preProcess(Object[] args) throws Throwable {
		KLogSys.debug("업무서비스 `@Service` 메소드 전처리 (파라미터 전처리)");
		if(args == null) {
			return;
		}
		for(Object obj : args) {
			KDtoUtil.setSysValues(obj);
		}
	}

	@Override
	@KAspect
	public void postProcess(Object ret) throws Throwable {
		KLogSys.debug("업무서비스 `@Service` 메소드 후처리 (리턴 후처리)");
	}

}