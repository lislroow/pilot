package mgkim.framework.online.com.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.exception.KException;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.stereo.KFilter;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.online.com.mgr.ComUserTokenMgr;

@KBean(name = "token 검증 필터")
public class KFilterVerifyToken extends KFilter {

	final String BEAN_NAME = KObjectUtil.name(KFilterVerifyToken.class);

	@Autowired(required = true)
	private ComUserTokenMgr comUserTokenMgr;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		boolean debug = KContext.getT(AttrKey.DEBUG);
		String bearer = KContext.getT(AttrKey.BEARER);

		// token `guid` 확인
		{
			io.jsonwebtoken.Jwt token = KContext.getT(AttrKey.TOKEN);
			Map<String, Object> claims = (Map<String, Object>)token.getBody();
			String tguid = KStringUtil.nvl(claims.get("guid"));
			//String hguid = KStringUtil.nvl(KContext.getT(AttrKey.GUID));
			//if (!debug && !tguid.equals(hguid)) {
			//	KExceptionHandler.response(response, new KSysException(KMessage.E6019, tguid, hguid));
			//	return;
			//}
		}

		if (debug) {
			chain.doFilter(request, response);
			return;
		}

		// token 만료여부 확인
		{
			try {
				io.jsonwebtoken.Jwt token = KContext.getT(AttrKey.TOKEN);
				comUserTokenMgr.checkExpired(token.getHeader());
			} catch(KException e) {
				KExceptionHandler.response(response, e);
				return;
			} catch(Exception e) {
				KExceptionHandler.response(response, new KSysException(KMessage.E7008, e, BEAN_NAME, "token 만료여부 체크"));
				return;
			}
		}

		// token 변조여부 여부 확인
		try {
			comUserTokenMgr.parsetoken(bearer);
		} catch(KException e) {
			KExceptionHandler.response(response, e);
			return;
		} catch(Exception e) {
			KExceptionHandler.response(response, new KSysException(KMessage.E6013, e, "token"));
			return;
		}
		chain.doFilter(request, response);
	}
}
