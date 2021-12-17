package mgkim.proto.www.cmm;

import java.lang.reflect.Field;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.online.cmm.CmmUserSession;
import mgkim.online.com.exception.KException;
import mgkim.online.com.exception.KExceptionHandler;
import mgkim.online.com.exception.KMessage;
import mgkim.online.com.exception.KSysException;
import mgkim.online.com.session.KSession;
import mgkim.online.com.session.KToken;
import mgkim.online.com.util.KObjectUtil;
import mgkim.proto.www.cmm.mapper.CmmUserSessionMapper;

@Service
public class CmmUserSessionImpl implements CmmUserSession {

	@Autowired
	private CmmUserSessionMapper cmmUserSessionMapper;

	@Override
	public KSession selectUserSession(KToken token) throws Exception {
		KSession session = null;
		try {
			session = cmmUserSessionMapper.selectUserSession(token);
		} catch(Exception e) {
			KException ex = KExceptionHandler.resolve(e);
			throw ex;
		}

		if(session == null) {
			throw new KSysException(KMessage.E6108);
		}

		// token 객체의 전체 필드를 session 객체로 복사
		{
			Field[] list = KObjectUtil.getFieldList(token);
			for(int i=0; i<list.length; i++) {
				KObjectUtil.setValue(session, list[0].getName(), list);
			}
		}
		return session;
	}

	public List<String> selectUserAuthority(KToken token) throws Exception {
		List<String> result = cmmUserSessionMapper.selectUserAuthority(token);
		return result;
	}
}
