package mgkim.core.com.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import mgkim.core.com.annotation.KBean;
import mgkim.core.com.env.KContext;
import mgkim.core.com.env.KContext.AttrKey;
import mgkim.core.com.exception.KException;
import mgkim.core.com.exception.KExceptionHandler;
import mgkim.core.com.exception.KMessage;
import mgkim.core.com.exception.KSysException;
import mgkim.core.com.mgr.ComUserTokenMgr;
import mgkim.core.com.session.KToken;
import mgkim.core.com.stereo.KFilter;
import mgkim.core.com.util.KObjectUtil;
import mgkim.core.com.util.KStringUtil;

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
			KToken token = KContext.getT(AttrKey.TOKEN);
			String tguid = KStringUtil.nvl(token.getGuid());
			String hguid = KStringUtil.nvl(KContext.getT(AttrKey.GUID));
			if(!debug && !tguid.equals(hguid)) {
				KExceptionHandler.response(response, new KSysException(KMessage.E6019, tguid, hguid));
				return;
			}
		}

		if(debug) {
			chain.doFilter(request, response);
			return;
		}

		// token 만료여부 확인
		{
			try {
				KToken token = KContext.getT(AttrKey.TOKEN);
				comUserTokenMgr.verifyExpiration(token);
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
			comUserTokenMgr.verifySignature(bearer);
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
