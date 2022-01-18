package mgkim.framework.online.com.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.cors.CorsUtils;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.exception.KException;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.logging.KLogMarker;
import mgkim.framework.core.stereo.KFilter;
import mgkim.framework.core.util.KObjectUtil;

@KBean(name = "public 필터")
public class KFilterPublic extends KFilter {
	
	private static final Logger log = LoggerFactory.getLogger(KFilterApi.class);
	
	final String BEAN_NAME = KObjectUtil.name(KFilterPublic.class);
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		// 2) preflight
		try {
			response.setHeader("Allow", "GET, POST, OPTIONS");
			response.addHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Headers", "ssid, txid, Origin, X-Requested-With, Content-Type, Accept, Access-Control-Allow-Method, Access-Control-Allow-Headers, Authorization, Access-Control-Max-Age");
			response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
			response.setHeader("Access-Control-Expose-Headers", "Content-Dispostion");
			boolean isPreFlight = CorsUtils.isPreFlightRequest(request);
			if (isPreFlight) {
				return;
			}
		} catch (Exception e) {
			KException ke = new KSysException(KMessage.E7007, e, "public");
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
			return;
		}
		chain.doFilter(request, response);
	}
}
