package mgkim.core.com.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import mgkim.core.com.env.KConfig;
import mgkim.core.com.env.KConstant;
import mgkim.core.com.logging.KLog;
import mgkim.core.com.type.TApiType;

public class KHttpUtil {

	public static String getUri() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		return request.getRequestURI();
	}

	public static TApiType resolveApiType() {
		HttpServletRequest request = getRequest();
		TApiType apiType = null;
		if(KMatcherUtil.matchesByAnt(request, KConfig.FILTER_OPENAPI)) {
			apiType = TApiType.OPENAPI;
		} else if(KMatcherUtil.matchesByAnt(request, KConfig.FILTER_INTERAPI)) {
			apiType = TApiType.INTERAPI;
		} else if(KMatcherUtil.matchesByAnt(request, KConfig.FILTER_PUBLIC)) {
			apiType = TApiType.PUBLIC;
		} else if(KMatcherUtil.matchesByAnt(request, KConfig.FILTER_API3)) {
			apiType = TApiType.API3;
		} else if(KMatcherUtil.matchesByAnt(request, KConfig.FILTER_API2)) {
			apiType = TApiType.API2;
		} else if(KMatcherUtil.matchesByAnt(request, KConfig.FILTER_API)) {
			apiType = TApiType.API;
		} else {
			apiType = TApiType.UNKNOWN;
		}
		return apiType;
	}

	public static String getIp() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		// 클라이언트의 IP를 체크하는 순서는 weblogic 과 jboss-web 이 연동된 환경에서 wl_proxy 가 활성화 된 상태를 우선합니다.
		// http-header 에 WL-Proxy-Client-IP 가 있는지 확인하고, 없을 경우에 http-request 객체에서 확인합니다.
		String ip = request.getHeader(KConstant.HK_WL_PROXY_ClIENT_IP);
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader(KConstant.HK_X_FORWARDED_FOR);
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		// 네트워크상에서 IP의 정보가 request 객체에 포함되기까지 2개 이상의 식별된 IP가 포함될 경우
		// 맨 우측에 있는 끝부분의 IP를 가져오도록 합니다.
		int n = ip.lastIndexOf(", ");
		if(n > -1) {
			ip = ip.substring(n+", ".length(), ip.length());
			KLog.warn("cmm/http", String.format("ip.lastIndexOf() > ip.substring() = %s", ip));
		}
		return ip;
	}

	public static Map<String, String> getHeaders() {
		Map<String, String> result = new HashMap<String, String>();
		HttpServletRequest request = KHttpUtil.getRequest();
		Enumeration<String> hkeys = request.getHeaderNames();
		while(hkeys.hasMoreElements()) {
			String name = hkeys.nextElement();
			result.put(name, request.getHeader(name));
		}
		return result;
	}

	public static String getHeader(String hkey) {
		HttpServletRequest request = KHttpUtil.getRequest();
		Enumeration<String> hkeys = request.getHeaderNames();
		while(hkeys.hasMoreElements()) {
			String name = hkeys.nextElement();
			if(name.equalsIgnoreCase(hkey)) {
				return request.getHeader(name);
			}
		}
		return KConstant.EMPTY;
	}

	public static String getContentDisposition(String filename) {
		String value = null;
		try {
			value = "attachment; filename*=UTF-8'ko'" + URLEncoder.encode(filename, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return value;
	}

	public static HttpServletRequest getRequest() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		return request;
	}
	public static Object getAttribute(String key) {
		Object result = null;
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if(requestAttributes == null) {
			return result;
		}
		result = requestAttributes.getAttribute(key, RequestAttributes.SCOPE_REQUEST);
		return result;
	}
	public static void setAttribute(String key, Object val) {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if(requestAttributes == null) {
			return;
		}
		requestAttributes.setAttribute(key, val, RequestAttributes.SCOPE_REQUEST);
	}
	public static void removeAttribute(String key) {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if(requestAttributes == null) {
			return;
		}
		requestAttributes.removeAttribute(key, RequestAttributes.SCOPE_REQUEST);
	}
}
