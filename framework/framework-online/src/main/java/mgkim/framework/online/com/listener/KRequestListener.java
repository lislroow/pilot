package mgkim.framework.online.com.listener;

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
import mgkim.framework.online.com.scheduler.ComDebugScheduler;

public class KRequestListener extends RequestContextListener {
	
	private static final Logger log = LoggerFactory.getLogger(KRequestListener.class);

	@Override
	public void requestInitialized(ServletRequestEvent requestEvent) {
		super.requestInitialized(requestEvent);
		HttpServletRequest request = ((HttpServletRequest) requestEvent.getServletRequest());
		try {

			long reqTime = System.currentTimeMillis();
			KContext.set(AttrKey.REQ_TIME, reqTime);

			KContext.initRequest(request);
			//log.accesslog();
			ComDebugScheduler.check();
			boolean loggable = KContext.getT(AttrKey.LOGGABLE);
			String referer = KContext.getT(AttrKey.REFERER);
			log.debug(KLogMarker.REQUEST, "referer={}", referer);
			/*if (false) {
				String reqHeader = KStringUtil.toJson(KHttpUtil.getHeaders());
				log.info("[ *** req-header *** ]\nheader = {}", reqHeader);
			}*/
		} catch(KSysException e) {
			log.error("", e);
		}
	}

	@Override
	public void requestDestroyed(ServletRequestEvent requestEvent) {
		HttpServletRequest request = (HttpServletRequest) requestEvent.getServletRequest();
		try {
			long reqTime = KContext.getT(AttrKey.REQ_TIME);
			String elapsed = String.format("%.3f", (System.currentTimeMillis() - reqTime) / 1000.0);
			log.debug(KLogMarker.RESPONSE, "(elapsed={})", elapsed);
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
