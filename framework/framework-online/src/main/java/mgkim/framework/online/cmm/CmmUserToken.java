package mgkim.framework.online.cmm;

import mgkim.framework.core.annotation.KModule;

@KModule(name = "사용자 토큰", required = true)
public interface CmmUserToken {

	//public <T extends KToken> KToken parseToken(String token) throws Exception;

	/**
	 * openapi 토큰 조회: select
	 */
	//public CmmOpenapiTokenVO selectOpenapiToken(String apikey) throws Exception;

}
