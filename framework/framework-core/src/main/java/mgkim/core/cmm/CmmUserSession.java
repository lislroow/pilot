package mgkim.core.cmm;

import java.util.List;

import mgkim.core.com.annotation.KModule;
import mgkim.core.com.session.KSession;
import mgkim.core.com.session.KToken;

@KModule(name = "사용자 세션", required = true)
public interface CmmUserSession {

	public KSession selectUserSession(KToken token) throws Exception;

	public List<String> selectUserAuthority(KToken token) throws Exception;
}
