package mgkim.framework.online.com.listener;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KSessionListener implements HttpSessionListener {
	
	private static final Logger log = LoggerFactory.getLogger(KSessionListener.class);

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		log.trace("*** sessionCreated ***");
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		log.trace("*** sessionDestroyed ***");
	}

}
