package mgkim.framework.online.cmm;

import java.util.List;
import java.util.Map;

import mgkim.framework.core.annotation.KModule;
import mgkim.framework.core.session.KSession;

@KModule(name = "사용자 세션", required = true)
public interface CmmUserSession {

	public KSession selectUserSession(Map<String, Object> claims) throws Exception;

	public List<String> selectUserAuthority(Map<String, Object> claims) throws Exception;
}
