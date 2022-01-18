package mgkim.framework.online.com.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.method.HandlerMethod;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.exception.KException;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.logging.KLogMarker;
import mgkim.framework.core.stereo.KFilter;
import mgkim.framework.core.type.TRequestType;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.online.com.mgr.ComUriListMgr;

@KBean(name = "api 접근 허용 체크 필터")
public class KFilterResolveUri extends KFilter {
	
	private static final Logger log = LoggerFactory.getLogger(KFilterResolveUri.class);

	final String BEAN_NAME = KObjectUtil.name(KFilterResolveUri.class);

	@Autowired(required = true)
	private ComUriListMgr comUriListMgr;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		try {
			HandlerMethod method = comUriListMgr.getHandlerMethod(request);
			if (method == null) {
				throw new KSysException(KMessage.E7001);
			}

			// `REQUEST_TYPE` 결정
			{
				String contentType = KStringUtil.nvl(request.getContentType());
				if (contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
					KContext.set(AttrKey.REQUEST_TYPE, TRequestType.JSON);
				} else {
					KContext.set(AttrKey.REQUEST_TYPE, TRequestType.FILE);
				}
			}

		} catch(HttpMediaTypeNotSupportedException e) {
			String contentType = request.getHeader("Content-Type");
			KException ke = new KSysException(KMessage.E7002, e, contentType);
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
		} catch(KException ke) {
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), ke.getCause());
			KExceptionHandler.response(response, ke);
			return;
		} catch(Exception e) {
			KException ke = new KSysException(KMessage.E7007, e, BEAN_NAME);
			log.error(KLogMarker.ERROR, "{} {}", ke.getId(), ke.getText(), e);
			KExceptionHandler.response(response, ke);
			return;
		}
		chain.doFilter(request, response);
	}

}
