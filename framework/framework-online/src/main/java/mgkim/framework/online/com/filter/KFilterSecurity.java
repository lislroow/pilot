package mgkim.framework.online.com.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.exception.KException;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.logging.KLogLayout;
import mgkim.framework.core.stereo.KFilter;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.online.com.listener.KRequestListener;

@KBean(name = "security 필터")
public class KFilterSecurity extends KFilter {
	
	private static final Logger log = LoggerFactory.getLogger(KRequestListener.class);

	final String BEAN_NAME = KObjectUtil.name(KFilterSecurity.class);

	final List<String> DEBUG_IP = Arrays.asList("172.28.");

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		boolean allow = false;
		boolean debug = KContext.getT(AttrKey.DEBUG);
		try {
			// `debug` 여부 확인
			{
				switch(KProfile.SYS) {
				case LOC:
					break;
				case DEV:
				case TEST:
				case PROD:
				default:
					if (debug) {
						String ip = KContext.getT(AttrKey.IP);
						for (String allowIp : DEBUG_IP) {
							if (ip.startsWith(allowIp)) {
								allow = true;
								break;
							}
						}
					}
					if (allow) {
						log.warn("{} {}{} {}", KConstant.LT_SECURITY, KLogLayout.LINE, KConstant.LT_SECURITY, KMessage.get(KMessage.E6024));
					} else {
						throw new KSysException(KMessage.E7009);
					}
				}

			}
		} catch(KException e) {
			KExceptionHandler.response(response, e);
			return;
		} catch(Exception e) {
			KExceptionHandler.response(response, new KSysException(KMessage.E7007, e, BEAN_NAME));
			return;
		}
		chain.doFilter(request, response);
	}
}