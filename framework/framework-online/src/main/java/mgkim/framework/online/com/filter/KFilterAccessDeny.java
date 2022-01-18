package mgkim.framework.online.com.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.exception.KException;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.logging.KLogMarker;
import mgkim.framework.core.stereo.KFilter;
import mgkim.framework.core.util.KObjectUtil;

@KBean(name = "api 접근 거부 필터")
public class KFilterAccessDeny extends KFilter {
	
	private static final Logger log = LoggerFactory.getLogger(KFilterAccessDeny.class);
	final String BEAN_NAME = KObjectUtil.name(KFilterAccessDeny.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		try {
		//} catch(KException e) {
		//	KExceptionHandler.response(response, e);
		//	return;
		} catch(Exception e) {
			KException ke = new KSysException(KMessage.E7007, e, BEAN_NAME);
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
			return;
		}
	}

}
