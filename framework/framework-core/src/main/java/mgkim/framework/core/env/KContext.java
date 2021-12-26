package mgkim.framework.core.env;

import static mgkim.framework.core.env.KConstant.GUID;
import static mgkim.framework.core.env.KConstant.IP;
import static mgkim.framework.core.env.KConstant.MDC_DEBUG_MODE_YN;
import static mgkim.framework.core.env.KConstant.REFERER;
import static mgkim.framework.core.env.KConstant.TXID;
import static mgkim.framework.core.env.KConstant.URI;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;

import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.logging.KLogLayout;
import mgkim.framework.core.type.TApiType;
import mgkim.framework.core.type.TAuthType;
import mgkim.framework.core.type.TExecType;
import mgkim.framework.core.type.TUuidType;
import mgkim.framework.core.util.KHttpUtil;
import mgkim.framework.core.util.KMatcherUtil;
import mgkim.framework.core.util.KStringUtil;

public class KContext {
	
	private static final Logger log = LoggerFactory.getLogger(KContext.class);
	
	private static final ThreadLocal<Map<AttrKey, Object>> attr = new ThreadLocal<Map<AttrKey, Object>>();
	
	public enum AttrKey {
		  REQ_TIME
		, LOGGABLE, DEBUG
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

	public static boolean isDebugMode() {
		return "Y".equals(MDC.get(MDC_DEBUG_MODE_YN));
	}

	public static void initSystem() {
		Map<AttrKey, Object> map = attr.get();
		if (map == null) {
			KContext.reset();
			map = attr.get();
		}

		TExecType execType = TExecType.SYSTEM;
		KContext.set(AttrKey.EXEC_TYPE, execType);

		KContext.set(AttrKey.LOGGABLE, true);
	}

	public static void initSchedule() {
		Map<AttrKey, Object> map = attr.get();
		if (map == null) {
			KContext.reset();
			map = attr.get();
		}

		TExecType execType = TExecType.SCHEDULE;
		KContext.set(AttrKey.EXEC_TYPE, execType);

		KContext.set(AttrKey.LOGGABLE, KConfig.VERBOSS_SCHEDULE);

		String txid = KStringUtil.createUuid(true, TUuidType.TXID);
		KContext.set(AttrKey.TXID, txid);

		MDC.put(TXID, txid);
	}

	public static void initRequest(HttpServletRequest request) throws KSysException {
		Map<AttrKey, Object> map = attr.get();
		if (map == null) {
			KContext.reset();
			map = attr.get();
		}

		TExecType execType = TExecType.REQUEST;
		KContext.set(AttrKey.EXEC_TYPE, execType);

		String uri = request.getRequestURI();
		KContext.set(AttrKey.URI, uri);

		boolean loggable = ! (KMatcherUtil.matchesByAnt(request, KConfig.CMM_URI)
				|| KMatcherUtil.matchesByAnt(request, KConfig.FILTER_HIDDENAPI));
		KContext.set(AttrKey.LOGGABLE, loggable);

		boolean debug = KStringUtil.toBoolean(KHttpUtil.getHeader(KConstant.HK_DEBUG));
		KContext.set(AttrKey.DEBUG, debug);

		String ip = KHttpUtil.getIp();
		KContext.set(AttrKey.IP, ip);

		String referer = KStringUtil.nvl(request.getHeader(HttpHeaders.REFERER));
		KContext.set(AttrKey.REFERER, referer);

		String authorization = KStringUtil.nvl(request.getHeader(KConstant.HK_AUTHORIZATION));
		KContext.set(AttrKey.AUTHORIZATION, authorization);

		TAuthType authType = null;
		if (KStringUtil.isEmpty(authorization)) {
			authType = TAuthType.NOAUTH;
		} else {
			if (authorization.toUpperCase().startsWith(TAuthType.BEARER.name())) {
				authType = TAuthType.BEARER;
			} else if (authorization.toUpperCase().startsWith(TAuthType.APIKEY.name())) {
				authType = TAuthType.APIKEY;
			} else {
				authType = TAuthType.NOAUTH;
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
			bearer = authorization.substring(TAuthType.BEARER.name().length()+1);  // +1은 "Bearer " 공백문자
			// apikey
			apikey = "";
		case NOAUTH:
			// guid
			guid = request.getHeader(KConstant.GUID);
			if (guid == null) {
				guid = KStringUtil.createUuid(true, TUuidType.GUID);
			}
			// txid
			txid = request.getHeader(KConstant.TXID);
			if (txid == null) {
				txid = KStringUtil.createUuid(true, TUuidType.TXID);
			}
			break;
		case APIKEY:
			// accessToken
			bearer = "";
			// apikey
			apikey = authorization.substring(TAuthType.APIKEY.name().length()+1);  // +1은 "Apikey " 공백문자
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
		TApiType apiType = KHttpUtil.resolveApiType();
		KContext.set(AttrKey.API_TYPE, apiType);


		MDC.put(URI, uri);
		MDC.put(IP, ip);
		MDC.put(GUID, guid);
		MDC.put(TXID, txid);
		MDC.put(REFERER, referer);
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
			log.info("{} {}{} {}", KConstant.LT_SECURITY, KLogLayout.LINE, KConstant.LT_SECURITY, KMessage.get(KMessage.E6023, KContext.get(AttrKey.SSID), ssid));
			KContext.set(AttrKey.SSID, ssid);
			MDC.put(KConstant.SSID, ssid);
		}
		
		KContext.set(AttrKey.TOKEN, token);
		KContext.set(AttrKey.SSID, ssid);
		MDC.put(KConstant.SSID, ssid);
		KContext.set(AttrKey.USER_ID, userId);
		MDC.put(KConstant.USER_ID, userId);
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
