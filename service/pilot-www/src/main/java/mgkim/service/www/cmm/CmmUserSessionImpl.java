package mgkim.service.www.cmm;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.framework.core.exception.KException;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.session.KSession;
import mgkim.framework.online.cmm.CmmUserSession;
import mgkim.service.www.cmm.mapper.CmmUserSessionMapper;

@Service
public class CmmUserSessionImpl implements CmmUserSession {

	@Autowired
	private CmmUserSessionMapper cmmUserSessionMapper;

	@Override
	public KSession selectUserSession(Map<String, Object> claims) throws Exception {
		KSession session = null;
		try {
			session = cmmUserSessionMapper.selectUserSession(claims);
		} catch(Exception e) {
			KException ex = KExceptionHandler.resolve(e);
			throw ex;
		}

		if (session == null) {
			throw new KSysException(KMessage.E6101); // TODO 세션이 만료되었습니다.
		}

		// token 객체의 전체 필드를 session 객체로 복사
		{
			//Field[] list = KObjectUtil.getFieldList(token);
			//for (int i=0; i<list.length; i++) {
			//	KObjectUtil.setValue(session, list[0].getName(), list);
			//}
		}
		return session;
	}

	public List<String> selectUserAuthority(Map<String, Object> claims) throws Exception {
		List<String> result = cmmUserSessionMapper.selectUserAuthority(claims);
		return result;
	}
}
