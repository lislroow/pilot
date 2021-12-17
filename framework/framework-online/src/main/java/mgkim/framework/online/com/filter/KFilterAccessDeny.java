package mgkim.framework.online.com.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mgkim.framework.online.com.annotation.KBean;
import mgkim.framework.online.com.exception.KExceptionHandler;
import mgkim.framework.online.com.exception.KMessage;
import mgkim.framework.online.com.exception.KSysException;
import mgkim.framework.online.com.stereo.KFilter;
import mgkim.framework.online.com.util.KObjectUtil;

@KBean(name = "api 접근 거부 필터")
public class KFilterAccessDeny extends KFilter {

	final String BEAN_NAME = KObjectUtil.name(KFilterAccessDeny.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		try {
		//} catch(KException e) {
		//	KExceptionHandler.response(response, e);
		//	return;
			System.out.println();
		} catch(Exception e) {
			KExceptionHandler.response(response, new KSysException(KMessage.E7007, e, BEAN_NAME));
			return;
		}
	}

}
