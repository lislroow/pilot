package mgkim.framework.core.env;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.logging.KLogMDC;
import mgkim.framework.core.logging.KLogMarker;
import mgkim.framework.core.type.KType.ApiType;
import mgkim.framework.core.type.KType.AuthType;
import mgkim.framework.core.type.KType.ExecType;
import mgkim.framework.core.type.KType.ReqType;
import mgkim.framework.core.type.KType.UuidType;
import mgkim.framework.core.util.KHttpUtil;
import mgkim.framework.core.util.KStringUtil;

public class KContext {
	
	private static final Logger log = LoggerFactory.getLogger(KContext.class);
	
	private static final ThreadLocal<Map<AttrKey, Object>> attr = new ThreadLocal<Map<AttrKey, Object>>();
	
	public enum AttrKey {
		  REQ_TIME
		, DEBUG
		, URI, IP, REFERER
		, REQUEST_TYPE, RESPONSE_TYPE, API_TYPE, AUTH_TYPE, EXEC_TYPE
		, GUID, SSID, TXID
		, AUTHORIZATION, BEARER, APIKEY, TOKEN, USER_ID
		, SQL_ID, SQL_FILE, SQL_TEXT, IN_PAGE, OUT_PAGE
		, DOWN_FILE
		, RESULT_CODE, RESULT_MESSAGE, RESULT_TEXT
		, RESULT_BCODE, RESULT_BMESSAGE
		;
	}

	public static void initSystem() {
		Map<AttrKey, Object> map = attr.get();
		if (map == null) {
			KContext.reset();
			map = attr.get();
		}

		ExecType execType = ExecType.SYSTEM;
		KContext.set(AttrKey.EXEC_TYPE, execType);
		KLogMDC.put(AttrKey.EXEC_TYPE, execType.name());
	}

	public static void initSchedule() {
		Map<AttrKey, Object> map = attr.get();
		if (map == null) {
			KContext.reset();
			map = attr.get();
		}
		
		ExecType execType = ExecType.SCHEDULE;
		KContext.set(AttrKey.EXEC_TYPE, execType);
		KLogMDC.put(AttrKey.EXEC_TYPE, execType.name());
		
		String txid = KStringUtil.createUuid(true, UuidType.TXID);
		KContext.set(AttrKey.TXID, txid);
		KLogMDC.put(AttrKey.TXID, txid);
	}

	public static void initRequest(HttpServletRequest request) throws KSysException {
		Map<AttrKey, Object> map = attr.get();
		if (map == null) {
			KContext.reset();
			map = attr.get();
		}
		
		ExecType execType = ExecType.REQUEST;
		KContext.set(AttrKey.EXEC_TYPE, execType);
		KLogMDC.put(AttrKey.EXEC_TYPE, execType.name());
		
		String uri = request.getRequestURI();
		KContext.set(AttrKey.URI, uri);
		
		boolean debug = KStringUtil.toBoolean(KHttpUtil.getHeader(KConstant.HK_DEBUG));
		KContext.set(AttrKey.DEBUG, debug);
		KLogMDC.put(AttrKey.DEBUG, Boolean.toString(debug));
		
		String ip = KHttpUtil.getIp();
		KContext.set(AttrKey.IP, ip);
		
		String referer = KStringUtil.nvl(request.getHeader(HttpHeaders.REFERER));
		KContext.set(AttrKey.REFERER, referer);
		
		String contentType = KStringUtil.nvl(request.getContentType());
		if (contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
			KContext.set(AttrKey.REQUEST_TYPE, ReqType.JSON);
		} else if (contentType.equals("multipart/form-data")) {
			KContext.set(AttrKey.REQUEST_TYPE, ReqType.FILE);
		} else {
			KContext.set(AttrKey.REQUEST_TYPE, ReqType.QUERY);
		}
		
		String authorization = KStringUtil.nvl(request.getHeader(KConstant.HK_AUTHORIZATION));
		KContext.set(AttrKey.AUTHORIZATION, authorization);
		
		AuthType authType = null;
		if (KStringUtil.isEmpty(authorization)) {
			authType = AuthType.NOAUTH;
		} else {
			if (authorization.toUpperCase().startsWith(AuthType.BEARER.name())) {
				authType = AuthType.BEARER;
			} else if (authorization.toUpperCase().startsWith(AuthType.APIKEY.name())) {
				authType = AuthType.APIKEY;
			} else {
				authType = AuthType.NOAUTH;
			}
		}
		KContext.set(AttrKey.AUTH_TYPE, authType);
		
		String bearer = null;
		String apikey = null;
		String guid = null;
		String txid = null;
		switch(authType) {
		case BEARER:
			// accessToken
			bearer = authorization.substring(AuthType.BEARER.name().length()+1);  // +1은 "Bearer " 공백문자
			// apikey
			apikey = "";
		case NOAUTH:
			// guid
			guid = request.getHeader(KConstant.GUID);
			if (guid == null) {
				guid = KStringUtil.createUuid(true, UuidType.GUID);
			}
			// txid
			txid = request.getHeader(KConstant.TXID);
			if (txid == null) {
				txid = KStringUtil.createUuid(true, UuidType.TXID);
			}
			break;
		case APIKEY:
			// accessToken
			bearer = "";
			// apikey
			apikey = authorization.substring(AuthType.APIKEY.name().length()+1);  // +1은 "Apikey " 공백문자
			// guid `api key`를 guid 로 사용함
			guid = apikey;
			// txid
			txid = request.getHeader(KConstant.TXID);
			if (txid == null) {
				txid = KStringUtil.createUuid(true);
				if (!referer.endsWith(KConstant.REFERER_SWAGGER)) {
				}
			}
			break;
		}
		KContext.set(AttrKey.BEARER, bearer);
		KContext.set(AttrKey.APIKEY, apikey);
		KContext.set(AttrKey.GUID, guid);
		KContext.set(AttrKey.TXID, txid);
		
		// apiType 확인
		ApiType apiType = KHttpUtil.resolveApiType();
		KContext.set(AttrKey.API_TYPE, apiType);
		
		KLogMDC.put(AttrKey.URI, uri);
		KLogMDC.put(AttrKey.IP, ip);
		KLogMDC.put(AttrKey.GUID, guid);
		KLogMDC.put(AttrKey.TXID, txid);
		KLogMDC.put(AttrKey.REFERER, referer);
	}
	
	public static void initToken(io.jsonwebtoken.Jwt token) throws KSysException {
		Map<AttrKey, Object> map = attr.get();
		if (map == null) {
			KContext.reset();
			map = attr.get();
		}
		Map claims = ((Map)token.getBody());
		String ssid = KStringUtil.nvl(claims.get(KConstant.SSID), "");
		String userId = KStringUtil.nvl(claims.get(KConstant.USER_ID), "");
		boolean debug = KContext.getT(AttrKey.DEBUG);
		if (debug) {
			log.debug(KLogMarker.security, "{}", KMessage.get(KMessage.E6023, KContext.get(AttrKey.SSID), ssid));
			KContext.set(AttrKey.SSID, ssid);
			KLogMDC.put(AttrKey.SSID, ssid);
		}
		
		KContext.set(AttrKey.TOKEN, token);
		KContext.set(AttrKey.SSID, ssid);
		KLogMDC.put(AttrKey.SSID, ssid);
		KContext.set(AttrKey.USER_ID, userId);
		KLogMDC.put(AttrKey.USER_ID, userId);
	}


	public static void resetSql() {
		KContext.set(AttrKey.SQL_ID, "");
		KContext.set(AttrKey.SQL_TEXT, "");
		KContext.set(AttrKey.SQL_FILE, "");
	}




	public static void reset() {
		attr.remove();
		attr.set(new HashMap<AttrKey, Object>());
	}

	public static void set(AttrKey key, Object val) {
		Map<AttrKey, Object> map = attr.get();
		if (map == null) {
			KContext.reset();
			map = attr.get();
		}
		map.put(key, val);
	}

	public static <T> T getT(AttrKey key) {
		@SuppressWarnings("unchecked")
		T result = (T) KContext.get(key);
		return result;
	}

	public static <T> T getT(AttrKey key, T def) {
		@SuppressWarnings("unchecked")
		T result = (T) KContext.get(key);
		if (result == null) {
			return def;
		}
		return result;
	}

	private static Object get(AttrKey key) {
		Map<AttrKey, Object> map = attr.get();
		if (map == null) {
			KContext.reset();
			map = attr.get();
		}
		Object val = map.get(key);
		return val;
	}
}
