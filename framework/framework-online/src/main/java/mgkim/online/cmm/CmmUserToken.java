package mgkim.online.cmm;

import mgkim.online.cmm.vo.token.CmmOpenapiTokenVO;
import mgkim.online.com.annotation.KModule;
import mgkim.online.com.session.KToken;

@KModule(name = "사용자 토큰", required = true)
public interface CmmUserToken {

	public <T extends KToken> KToken parseToken(String token) throws Exception;

	/**
	 * openapi 토큰 조회: select
	 */
	public CmmOpenapiTokenVO selectOpenapiToken(String apikey) throws Exception;

}
