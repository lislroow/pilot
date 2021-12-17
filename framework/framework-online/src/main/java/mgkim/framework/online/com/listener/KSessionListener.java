package mgkim.framework.online.com.listener;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import mgkim.framework.core.logging.KLogSys;

public class KSessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		KLogSys.trace("*** sessionCreated ***");
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		KLogSys.trace("*** sessionDestroyed ***");
	}

}
