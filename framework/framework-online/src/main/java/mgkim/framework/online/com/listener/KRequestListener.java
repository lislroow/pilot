package mgkim.framework.online.com.listener;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletRequestEvent;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextListener;

import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.logging.KLogMarker;
import mgkim.framework.core.request.KReadableRequest;
import mgkim.framework.core.util.KStringUtil;

public class KRequestListener extends RequestContextListener {
	
	private static final Logger log = LoggerFactory.getLogger(KRequestListener.class);

	@Override
	public void requestInitialized(ServletRequestEvent requestEvent) {
		super.requestInitialized(requestEvent);
		HttpServletRequest request = ((HttpServletRequest) requestEvent.getServletRequest());
		String header = null;
		String body = null;
		try {
			long reqTime = System.currentTimeMillis();
			KContext.set(AttrKey.REQ_TIME, reqTime);

			KContext.initRequest(request);
			//log.accesslog();
			//ComDebugScheduler.check();
		} catch(KSysException ke) {
			try {
				if (header == null) {
					Map<String, String> headerMap = Collections.list(request.getHeaderNames()).stream()
							.collect(Collectors.toMap(name -> name, name -> request.getHeader(name)));
					header = KStringUtil.toJson(headerMap);
				}
				body = new KReadableRequest(request).getBodyString();
			} catch (IOException e) {
				log.error(KLogMarker.ERROR, "\nrequest-header = {}\nrequest-body = {}", header, body, e);
			}
			log.error(KLogMarker.ERROR, "{} {}\nrequest-header = {}\nrequest-body = {}", ke.getId(), ke.getText(), ke.getCause());
		} catch(Exception e) {
			try {
				if (header == null) {
					Map<String, String> headerMap = Collections.list(request.getHeaderNames()).stream()
							.collect(Collectors.toMap(name -> name, name -> request.getHeader(name)));
					header = KStringUtil.toJson(headerMap);
				}
				body = new KReadableRequest(request).getBodyString();
				log.error(KLogMarker.ERROR, "\nrequest-header = {}\nrequest-body = {}", header, body, e);
			} catch (IOException e1) {
				log.error(KLogMarker.ERROR, "\nrequest-header = {}\nrequest-body = {}", header, body, e);
			}
		}
	}

	@Override
	public void requestDestroyed(ServletRequestEvent requestEvent) {
		HttpServletRequest request = (HttpServletRequest) requestEvent.getServletRequest();
		try {
			SecurityContextHolder.clearContext();
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.invalidate();
			}
			if (request != null && request.getCookies() != null) {
				for (Cookie cookie : request.getCookies()) {
					cookie.setMaxAge(0);
				}
			}
		} finally {
			MDC.clear();
			KContext.reset();
		}
		super.requestDestroyed(requestEvent);
	}
}
