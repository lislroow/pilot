package mgkim.core.com.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.multipart.MultipartException;

import mgkim.core.com.annotation.KBean;
import mgkim.core.com.exception.KException;
import mgkim.core.com.exception.KExceptionHandler;
import mgkim.core.com.exception.KMessage;
import mgkim.core.com.exception.KSysException;
import mgkim.core.com.stereo.KFilter;
import mgkim.core.com.util.KObjectUtil;

@KBean(name = "spring-authorize 처리 결과 필터")
public class KFilterSpringAuthorizeHandle extends KFilter {

	final String BEAN_NAME = KObjectUtil.name(KFilterSpringAuthorizeHandle.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		try {
			chain.doFilter(request, response);
		} catch(AuthenticationCredentialsNotFoundException | AccessDeniedException e) {
			KExceptionHandler.response(response, new KSysException(KMessage.E6011, e));
			return;
		} catch(MultipartException e) {
			KExceptionHandler.response(response, new KSysException(KMessage.E7005, e));
			return;
		} catch(Exception e) {
			KException ex = KExceptionHandler.resolve(e);
			KExceptionHandler.response(response, ex);
			return;
		}
	}
}
