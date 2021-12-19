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
import mgkim.framework.online.com.mgr.ComSessionStatusMgr;

@KBean(name = "session 검증 필터")
public class KFilterVerifySession extends KFilter {

	final String BEAN_NAME = KObjectUtil.name(KFilterVerifySession.class);

	@Autowired(required = true)
	private ComSessionStatusMgr comSessionStatusMgr;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			boolean debug = KContext.getT(AttrKey.DEBUG);
			if (debug) {
				chain.doFilter(request, response);
				return;
			}
			io.jsonwebtoken.Jwt token = KContext.getT(AttrKey.TOKEN);
			Map<String, Object> claims = (Map<String, Object>)token.getBody();
			boolean isLogin = comSessionStatusMgr.isLoginStatus(claims);
			if (isLogin == false) {
				throw new KSysException(KMessage.E6103);
			}
		} catch(KException e) {
			KExceptionHandler.response(response, e);
			return;
		} catch(Exception e) {
			KExceptionHandler.response(response, new KSysException(KMessage.E7008, e, BEAN_NAME, "세션상태체크"));
			return;
		}
		chain.doFilter(request, response);
	}
}