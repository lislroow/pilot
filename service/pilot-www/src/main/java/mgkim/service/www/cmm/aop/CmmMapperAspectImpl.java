package mgkim.service.www.cmm.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mgkim.framework.core.annotation.KNonAspect;
import mgkim.framework.cmm.online.CmmMapperAspect;
import mgkim.framework.cmm.online.vo.CmmPrivacyLogVO;
import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.logging.KLogMarker;
import mgkim.framework.core.type.TPrivacyType;
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
		// 개인정보접근 로그 (10: SQL)
		{
			log.trace(KLogMarker.aop, "개인정보접근 로그 {}.{}()", clazzName, methodName);
			CmmPrivacyLogVO vo = new CmmPrivacyLogVO();
			vo.setAppCd(KProfile.APP_CD);
			vo.setMngtgId(KContext.getT(AttrKey.SQL_ID));
			vo.setMngtgTpcd(TPrivacyType.SQL.code());
			vo.setIp(KContext.getT(AttrKey.IP));
			vo.setUserId(KContext.getT(AttrKey.USER_ID));
			vo.setSsid(KContext.getT(AttrKey.SSID));
			vo.setTxid(KContext.getT(AttrKey.TXID));
		}
	}

}