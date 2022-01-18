package mgkim.service.adm.cmm.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.logging.KLogMarker;
import mgkim.framework.core.type.TPrivacyType;
import mgkim.framework.core.util.KDtoUtil;
import mgkim.framework.online.cmm.aop.CmmMapperAspect;
import mgkim.framework.online.cmm.vo.privacy.CmmPrivacyLogVO;
import mgkim.framework.online.com.mgr.ComPrivacyMgr;

@KBean
public class CmmMapperAspectImpl implements CmmMapperAspect {
	
	private static final Logger log = LoggerFactory.getLogger(CmmMapperAspectImpl.class);

	@Autowired
	private ComPrivacyMgr comPrivacyMgr;

	@Override
	public void preProcess(String clazzName, String methodName, Object[] args) throws Throwable {
		if (args == null) {
			return;
		}
		log.trace(KLogMarker.aop_stereo, "sys-field 설정 {}.{}()", clazzName, methodName);
		for (Object obj : args) {
			KDtoUtil.setSysValues(obj);
		}
	}

	@Override
	public void postProcess(String clazzName, String methodName, Object ret) throws Throwable {
		if (comPrivacyMgr == null) {
			return;
		}

		// 개인정보접근 로그 (10: SQL)
		{
			log.trace(KLogMarker.aop_stereo, "개인정보접근 로그 {}.{}()", clazzName, methodName);
			CmmPrivacyLogVO vo = new CmmPrivacyLogVO();
			vo.setAppCd(KProfile.APP_CD);
			vo.setMngtgId(KContext.getT(AttrKey.SQL_ID));
			vo.setMngtgTpcd(TPrivacyType.SQL.code());
			vo.setIp(KContext.getT(AttrKey.IP));
			vo.setUserId(KContext.getT(AttrKey.USER_ID));
			vo.setSsid(KContext.getT(AttrKey.SSID));
			vo.setTxid(KContext.getT(AttrKey.TXID));
			comPrivacyMgr.insertLog(vo);
		}
	}

}