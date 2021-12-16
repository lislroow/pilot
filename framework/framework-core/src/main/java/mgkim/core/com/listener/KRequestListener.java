package mgkim.core.com.listener;

import javax.servlet.ServletRequestEvent;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextListener;

import mgkim.core.com.env.KConfig;
import mgkim.core.com.env.KConstant;
import mgkim.core.com.env.KContext;
import mgkim.core.com.env.KContext.AttrKey;
import mgkim.core.com.exception.KSysException;
import mgkim.core.com.logging.KLogApm;
import mgkim.core.com.logging.KLogLayout;
import mgkim.core.com.logging.KLogSys;
import mgkim.core.com.scheduler.ComDebugScheduler;
import mgkim.core.com.util.KHttpUtil;
import mgkim.core.com.util.KStringUtil;

public class KRequestListener extends RequestContextListener {

	@Override
	public void requestInitialized(ServletRequestEvent requestEvent) {
		super.requestInitialized(requestEvent);
		HttpServletRequest request = ((HttpServletRequest) requestEvent.getServletRequest());
		try {

			long reqTime = System.currentTimeMillis();
			KContext.set(AttrKey.REQ_TIME, reqTime);

			KContext.initRequest(request);
			//KLogSys.accesslog();
			ComDebugScheduler.check();
			boolean loggable = KContext.getT(AttrKey.LOGGABLE);
			if(loggable) {
				KLogSys.warn("{} referer={}", KConstant.LT_REQUEST, KContext.getT(AttrKey.REFERER));
				boolean isVerboss = KConfig.VERBOSS_ALL || KConfig.VERBOSS_REQ;
				if(isVerboss) {
					String reqHeader = KStringUtil.toJson(KHttpUtil.getHeaders());
					KLogSys.info("{} {}{} {}{}`Header` = {}", KConstant.LT_REQ_HEADER, KLogLayout.LINE, KConstant.LT_REQ_HEADER, KContext.getT(AttrKey.URI), KLogLayout.LINE, reqHeader);
				}
			} else {
				KLogSys.debug("{} referer={}", KConstant.LT_REQUEST, KContext.getT(AttrKey.REFERER));
			}
		} catch(KSysException e) {
			KLogSys.error("", e);
		}
	}

	@Override
	public void requestDestroyed(ServletRequestEvent requestEvent) {
		HttpServletRequest request = (HttpServletRequest) requestEvent.getServletRequest();
		try {
			long reqTime = KContext.getT(AttrKey.REQ_TIME);
			double elapsedTime = (System.currentTimeMillis() - reqTime) / 1000.0;
			boolean loggable = KContext.getT(AttrKey.LOGGABLE);
			if(loggable) {
				KLogSys.warn("{} {} sec elapsed.", KConstant.LT_RESPONSE, String.format("%.3f", elapsedTime));
			} else {
				KLogSys.debug("{} {} sec elapsed.", KConstant.LT_RESPONSE, String.format("%.3f", elapsedTime));
			}
			SecurityContextHolder.clearContext();
			HttpSession session = request.getSession(false);
			if(session != null) {
				session.invalidate();
			}
			if(request != null && request.getCookies() != null) {
				for(Cookie cookie : request.getCookies()) {
					cookie.setMaxAge(0);
				}
			}
			KLogApm.api(KContext.getT(AttrKey.URI), elapsedTime);
		} finally {
			MDC.clear();
			KContext.reset();
		}
		super.requestDestroyed(requestEvent);
	}
}
