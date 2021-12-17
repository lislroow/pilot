package mgkim.proto.www.cmm.aop;

import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.online.cmm.aop.CmmMapperAspect;
import mgkim.framework.online.cmm.vo.privacy.CmmPrivacyLogVO;
import mgkim.framework.online.com.env.KContext;
import mgkim.framework.online.com.env.KProfile;
import mgkim.framework.online.com.env.KContext.AttrKey;
import mgkim.framework.online.com.logging.KLogSys;
import mgkim.framework.online.com.mgr.ComPrivacyMgr;
import mgkim.framework.online.com.type.TPrivacyType;
import mgkim.framework.online.com.util.KDtoUtil;

@KBean
public class CmmMapperAspectImpl implements CmmMapperAspect {

	@Autowired
	private ComPrivacyMgr comPrivacyMgr;

	@Override
	public void preProcess(Object[] args) throws Throwable {
		KLogSys.debug("업무서비스 `@Mapper` 메소드 전처리 (파라미터 전처리)");
		if(args == null) {
			return;
		}
		for(Object obj : args) {
			KDtoUtil.setSysValues(obj);
		}
	}

	@Override
	public void postProcess(Object ret) throws Throwable {
		KLogSys.debug("업무서비스 `@Mapper` 메소드 후처리 (리턴 후처리)");

		if(comPrivacyMgr == null) {
			return;
		}

		// 개인정보접근 로그 (10: SQL)
		{
			CmmPrivacyLogVO vo = new CmmPrivacyLogVO();
			vo.setSiteTpcd(KProfile.SITE.code());
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