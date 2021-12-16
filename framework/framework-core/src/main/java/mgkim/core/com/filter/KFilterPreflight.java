package mgkim.core.com.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.cors.CorsUtils;

import mgkim.core.com.annotation.KBean;
import mgkim.core.com.exception.KExceptionHandler;
import mgkim.core.com.exception.KMessage;
import mgkim.core.com.exception.KSysException;
import mgkim.core.com.stereo.KFilter;
import mgkim.core.com.util.KObjectUtil;

@KBean(name = "preflight 필터")
public class KFilterPreflight extends KFilter {

	final String BEAN_NAME = KObjectUtil.name(KFilterPreflight.class);

	final String ALLOW = "GET, POST, OPTIONS";
	final String ACCESS_CONTROL_ALLOW_ORIGIN = "*";
	final String ACCESS_CONTROL_ALLOW_HEADERS = "ssid, txid, Origin, X-Requested-With, Content-Type, Accept, Access-Control-Allow-Method, Access-Control-Allow-Headers, Authorization, Access-Control-Max-Age";
	final String ACCESS_CONTROL_ALLOW_METHODS = "GET, POST, OPTIONS";
	final String ACCESS_CONTROL_EXPOSE_HEADERS = "Content-Dispostion";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		try {
			response.setHeader("Allow", ALLOW);
			response.addHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
			response.setHeader("Access-Control-Allow-Headers", ACCESS_CONTROL_ALLOW_HEADERS);
			response.setHeader("Access-Control-Allow-Methods", ACCESS_CONTROL_ALLOW_METHODS);
			response.setHeader("Access-Control-Expose-Headers", ACCESS_CONTROL_EXPOSE_HEADERS);

			boolean isPreFlight = CorsUtils.isPreFlightRequest(request);
			if(isPreFlight) {
				return;
			}
		} catch(Exception e) {
			KExceptionHandler.response(response, new KSysException(KMessage.E7007, e, BEAN_NAME));
			return;
		}
		chain.doFilter(request, response);
	}
}
