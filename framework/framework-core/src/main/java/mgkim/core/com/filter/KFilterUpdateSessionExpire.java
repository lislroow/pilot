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
import mgkim.core.com.exception.KExceptionHandler;
import mgkim.core.com.exception.KMessage;
import mgkim.core.com.exception.KSysException;
import mgkim.core.com.scheduler.ComSessionStatusMngScheduler;
import mgkim.core.com.stereo.KFilter;
import mgkim.core.com.util.KObjectUtil;

@KBean(name = "session 만료 갱신 필터")
public class KFilterUpdateSessionExpire extends KFilter {

	final String BEAN_NAME = KObjectUtil.name(KFilterUpdateSessionExpire.class);

	@Autowired(required = false)
	private ComSessionStatusMngScheduler comSessionStatusMngScheduler;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		try {
			boolean debug = KContext.getT(AttrKey.DEBUG);
			if(debug) {
				chain.doFilter(request, response);
				return;
			}
			comSessionStatusMngScheduler.addSession();
		} catch(Exception e) {
			KExceptionHandler.response(response, new KSysException(KMessage.E7008, e, BEAN_NAME, "갱신대상추가"));
			return;
		}
		chain.doFilter(request, response);
	}

}
