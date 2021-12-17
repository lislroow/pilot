package mgkim.online.com.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mgkim.online.com.annotation.KBean;
import mgkim.online.com.exception.KExceptionHandler;
import mgkim.online.com.exception.KMessage;
import mgkim.online.com.exception.KSysException;
import mgkim.online.com.stereo.KFilter;
import mgkim.online.com.util.KObjectUtil;

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
