package mgkim.service.www.cmm.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KProfile;
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
	public void preProcess(Object[] args) throws Throwable {
		log.debug("업무서비스 `@Mapper` 메소드 전처리 (파라미터 전처리)");
		if (args == null) {
			return;
		}
		for (Object obj : args) {
			KDtoUtil.setSysValues(obj);
		}
	}

	@Override
	public void postProcess(Object ret) throws Throwable {
		log.debug("업무서비스 `@Mapper` 메소드 후처리 (리턴 후처리)");

		if (comPrivacyMgr == null) {
			return;
		}

		// 개인정보접근 로그 (10: SQL)
		{
			CmmPrivacyLogVO vo = new CmmPrivacyLogVO();
			vo.setSiteTpcd(KProfile.SITE_TPCD);
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