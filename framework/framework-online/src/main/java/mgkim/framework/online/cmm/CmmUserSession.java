package mgkim.framework.online.cmm;

import java.util.List;

import mgkim.framework.core.annotation.KModule;
import mgkim.framework.core.session.KSession;
import mgkim.framework.core.session.KToken;

@KModule(name = "사용자 세션", required = true)
public interface CmmUserSession {

	public KSession selectUserSession(KToken token) throws Exception;

	public List<String> selectUserAuthority(KToken token) throws Exception;
}
