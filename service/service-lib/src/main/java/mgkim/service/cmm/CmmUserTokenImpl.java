package mgkim.service.cmm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.framework.cmm.online.CmmUserToken;
import mgkim.service.cmm.mapper.CmmUserTokenMapper;

@Service
public class CmmUserTokenImpl implements CmmUserToken {

	@Autowired(required = false)
	private CmmUserTokenMapper cmmUserTokenMapper;

	//@Override
	//public <T extends KToken> T parseToken(String json) throws Exception {
	//	// token 생성 (json-string > token-object)
	//	T token = null;
	//	{
	//		try {
	//			token = (T) KTokenParser.parse(CmmTokenApi.class, json);
	//			return token;
	//		} catch(Exception e) {
	//			throw new KSysException(KMessage.E6016, e);
	//		}
	//	}
	//}

	//@Override
	//public CmmOpenapiTokenVO selectOpenapiToken(String apikey) throws Exception {
	//	return cmmUserTokenMapper.selectOpenapiToken(apikey);
	//}
}
