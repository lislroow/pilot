package mgkim.core.com.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.method.HandlerMethod;

import mgkim.core.com.annotation.KBean;
import mgkim.core.com.env.KContext;
import mgkim.core.com.env.KContext.AttrKey;
import mgkim.core.com.exception.KException;
import mgkim.core.com.exception.KExceptionHandler;
import mgkim.core.com.exception.KMessage;
import mgkim.core.com.exception.KSysException;
import mgkim.core.com.mgr.ComUriListMgr;
import mgkim.core.com.stereo.KFilter;
import mgkim.core.com.type.TRequestType;
import mgkim.core.com.util.KObjectUtil;
import mgkim.core.com.util.KStringUtil;

@KBean(name = "api 접근 허용 체크 필터")
public class KFilterResolveUri extends KFilter {


	final String BEAN_NAME = KObjectUtil.name(KFilterResolveUri.class);

	@Autowired(required = true)
	private ComUriListMgr comUriListMgr;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		try {
			HandlerMethod method = comUriListMgr.getHandlerMethod(request);
			if(method == null) {
				throw new KSysException(KMessage.E7001);
			}

			// `REQUEST_TYPE` 결정
			{
				String contentType = KStringUtil.nvl(request.getContentType());
				if(contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
					KContext.set(AttrKey.REQUEST_TYPE, TRequestType.JSON);
				} else {
					KContext.set(AttrKey.REQUEST_TYPE, TRequestType.FILE);
				}
			}

		} catch(HttpMediaTypeNotSupportedException e) {
			String contentType = request.getHeader("Content-Type");
			KExceptionHandler.response(response, new KSysException(KMessage.E7002, e, contentType));
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
