package mgkim.proto.www.cmm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.util.KTokenParser;
import mgkim.framework.online.cmm.CmmUserToken;
import mgkim.framework.online.cmm.vo.token.CmmOpenapiTokenVO;
import mgkim.framework.online.com.session.KToken;
import mgkim.proto.www.cmm.mapper.CmmUserTokenMapper;
import mgkim.proto.www.com.token.CmmTokenApi;

@Service
public class CmmUserTokenImpl implements CmmUserToken {

	@Autowired
	private CmmUserTokenMapper cmmUserTokenMapper;

	@Override
	public <T extends KToken> T parseToken(String json) throws Exception {
		// token 생성 (json-string > token-object)
		T token = null;
		{
			try {
				token = (T) KTokenParser.parse(CmmTokenApi.class, json);
				return token;
			} catch(Exception e) {
				throw new KSysException(KMessage.E6016, e);
			}
		}
	}

	@Override
	public CmmOpenapiTokenVO selectOpenapiToken(String apikey) throws Exception {
		return cmmUserTokenMapper.selectOpenapiToken(apikey);
	}
}
