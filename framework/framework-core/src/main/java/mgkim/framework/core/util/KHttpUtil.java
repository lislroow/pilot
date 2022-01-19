package mgkim.framework.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.type.TApiType;

public class KHttpUtil {
	
	private static final List<String> IP_HEADERS = Arrays.asList(
			"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR");
	
	public static final String LOCAL_IPv4 = "127.0.0.1";
	public static final List<String> LOCAL_IP = Arrays.asList("0:0:0:0:0:0:0:1", LOCAL_IPv4);
	
	
	public static String getUri() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		return request.getRequestURI();
	}

	public static TApiType resolveApiType() {
		HttpServletRequest request = getRequest();
		TApiType apiType = null;
		if (KMatcherUtil.matchesByAnt(request, KConstant.PUBLIC_URI)) {
			apiType = TApiType.PUBLIC;
		} else if (KMatcherUtil.matchesByAnt(request, KConstant.API_URI)) {
			apiType = TApiType.API;
		} else {
			apiType = TApiType.UNKNOWN;
		}
		return apiType;
	}

	public static String getIp() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		String ip = IP_HEADERS.stream()
				.map(request::getHeader)
				.filter(Objects::nonNull)
				.filter(val -> !val.isEmpty() && !val.equalsIgnoreCase("unknown"))
				.findFirst()
				.orElseGet(request::getRemoteAddr);
		
		// -Djava.net.preferIPv4Stack=true 설정이 없을 경우 IPv6 로 기본 표시됩니다.
		// IPv6 일 경우 `0:0:0:0:0:0:0:1`
		// IPv4 일 경우 `127.0.0.1`
		if (LOCAL_IP.contains(ip)) {
			return LOCAL_IPv4;
		}
		return ip;
	}

	public static Map<String, String> getHeaders() {
		/*
		// 2022.01.20 stream 교체
		Map<String, String> result = new HashMap<String, String>();
		HttpServletRequest request = KHttpUtil.getRequest();
		Enumeration<String> hkeys = request.getHeaderNames();
		while (hkeys.hasMoreElements()) {
			String name = hkeys.nextElement();
			result.put(name, request.getHeader(name));
		}
		return result;
		*/
		HttpServletRequest request = KHttpUtil.getRequest();
		Map<String, String> headerMap = Collections.list(request.getHeaderNames()).stream()
				.collect(Collectors.toMap(name -> name, name -> request.getHeader(name)));
		return headerMap;
	}

	public static String getHeader(String hkey) {
		HttpServletRequest request = KHttpUtil.getRequest();
		Enumeration<String> hkeys = request.getHeaderNames();
		while (hkeys.hasMoreElements()) {
			String name = hkeys.nextElement();
			if (name.equalsIgnoreCase(hkey)) {
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
		if (requestAttributes == null) {
			return result;
		}
		result = requestAttributes.getAttribute(key, RequestAttributes.SCOPE_REQUEST);
		return result;
	}
	public static void setAttribute(String key, Object val) {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes == null) {
			return;
		}
		requestAttributes.setAttribute(key, val, RequestAttributes.SCOPE_REQUEST);
	}
	public static void removeAttribute(String key) {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if (requestAttributes == null) {
			return;
		}
		requestAttributes.removeAttribute(key, RequestAttributes.SCOPE_REQUEST);
	}
}
