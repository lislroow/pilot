package mgkim.framework.online.com.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.exception.KException;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.online.com.env.KContext;
import mgkim.framework.online.com.env.KContext.AttrKey;
import mgkim.framework.online.com.mgr.ComSessionStatusMgr;
import mgkim.framework.online.com.session.KToken;
import mgkim.framework.online.com.stereo.KFilter;
import mgkim.framework.online.com.util.KObjectUtil;

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
			if(debug) {
				chain.doFilter(request, response);
				return;
			}
			KToken token = KContext.getT(AttrKey.TOKEN);
			boolean isLogin = comSessionStatusMgr.isLoginStatus(token);
			if(isLogin == false) {
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