package mgkim.online.com.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import mgkim.online.com.annotation.KBean;
import mgkim.online.com.env.KContext;
import mgkim.online.com.env.KContext.AttrKey;
import mgkim.online.com.exception.KException;
import mgkim.online.com.exception.KExceptionHandler;
import mgkim.online.com.exception.KMessage;
import mgkim.online.com.exception.KSysException;
import mgkim.online.com.mgr.ComUserSessionMgr;
import mgkim.online.com.session.KToken;
import mgkim.online.com.stereo.KFilter;
import mgkim.online.com.util.KObjectUtil;

@KBean(name = "사용자 session 생성 필터")
public class KFilterCreateUserSession extends KFilter {

	final String BEAN_NAME = KObjectUtil.name(KFilterCreateUserSession.class);

	@Autowired(required = true)
	private ComUserSessionMgr comUserSessionMgr;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			KToken token = KContext.getT(AttrKey.TOKEN);
			comUserSessionMgr.createUserSession(token);
		} catch(KException e) {
			KExceptionHandler.response(response, e);
			return;
		} catch(Exception e) {
			KExceptionHandler.response(response, new KSysException(KMessage.E7008, e, BEAN_NAME, "session 생성"));
			return;
		}
		chain.doFilter(request, response);
	}
}
