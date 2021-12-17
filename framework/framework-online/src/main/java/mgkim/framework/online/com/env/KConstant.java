package mgkim.framework.online.com.env;

import mgkim.framework.online.com.logging.KAnsi;

public class KConstant {

	public static final String VM_SPRING_PROFILES_ACTIVE = "spring.profiles.active";

	public static final String EMPTY = "";

	public static final String PATH_WEBINF_CLASSES;
	public static final String PATH_WEBINF_LIB;

	static {
		String RELATIVE_PATH = KConstant.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		PATH_WEBINF_CLASSES = RELATIVE_PATH.substring(0, RELATIVE_PATH.indexOf("WEB-INF/") + "WEB-INF/".length()) + "classes/";
		PATH_WEBINF_LIB = RELATIVE_PATH.substring(0, RELATIVE_PATH.indexOf("WEB-INF/") + "WEB-INF/".length()) + "lib/";
	}

	public static final String REFERER_SWAGGER = "/swagger-ui.html";        // io.springfox:2.10.5
	//public static final String REFERER_SWAGGER = "/swagger-ui/index.html";  // io.springfox:3.0.0

	public static final String JANSI = "jansi";

	public static final String SSID = "ssid";
	public static final String GUID = "guid";
	public static final String TXID = "txid";
	public static final String APIKEY = "apikey";
	public static final String USER_ID = "userId";
	public static final String USER_TYPE = "userType";
	public static final String REFERER = "referer";
	public static final String LANG_CD = "langCd";
	public static final String IP = "ip";
	public static final String URI = "uri";
	public static final String LOGGABLE = "loggable";
	public static final String CALLER = "caller";
	public static final String LOGSPACE = "logspace";

	public static final String RESULT_CODE = "code";
	public static final String RESULT_MESSAGE = "message";
	public static final String RESULT_TEXT = "text";
	public static final String RESULT_BCODE = "bcode";
	public static final String RESULT_BMESSAGE = "bmessage";

	public static final String TOKEN_JTI = "jti";
	public static final String TOKEN_ATI = "ati";
	public static final String TOKEN_VALIDITY = "validaty";
	public static final String TOKEN_EXPIRES = "exp";
	public static final String TOKEN_AES_KEY = "aesKey";
	public static final String TOKEN_ORG_CD = "orgCd";
	public static final String TOKEN_ORG_NM = "orgNm";
	//public static final String TOKEN_EXPIRES_IN = org.springframework.security.oauth2.common.OAuth2AccessToken.EXPIRES_IN;

	public static final String LT_REQUEST = KAnsi.boldMagenta("[ ##### REQUEST ##### ]");
	public static final String LT_RESPONSE = KAnsi.boldMagenta("[ $$$$$ RESPONSE $$$$$ ]");
	public static final String LT_SECURITY = KAnsi.magenta("[ *** SECURITY *** ]");
	public static final String LT_REQ_HEADER = KAnsi.magenta("[ *** REQ-HEADER *** ]");
	public static final String LT_REQ_BODY = KAnsi.magenta("[ *** REQ-BODY *** ]");
	public static final String LT_RES_INFO = KAnsi.magenta("[ *** RES-INFO *** ]");
	public static final String LT_RES_VERBOSS = KAnsi.magenta("[ *** RES-INFO-VERBOSS_ALL *** ]");
	public static final String LT_FILTER = KAnsi.magenta("[ *** FILTER *** ]");
	public static final String LT_CLASS = KAnsi.magenta("[ *** CLASS *** ]");
	public static final String LT_MVC_EXCEPTION = KAnsi.magenta("[ *** MVC-EXCEPTION *** ]");
	public static final String LT_SECURITY_EXCEPTION = KAnsi.magenta("[ *** SECURITY-EXCEPTION *** ]");
	public static final String LT_SECURITY_IGNORED = KAnsi.magenta("[ *** SECURITY-IGNORED *** ]");
	public static final String LT_SECURITY_FILTER = KAnsi.magenta("[ *** SECURITY-FILTER *** ]");
	public static final String LT_SQL = KAnsi.magenta("[ *** SQL *** ]");
	public static final String LT_SQL_COUNT1 = KAnsi.magenta("[ *** SQL-COUNT(t1) *** ]");
	public static final String LT_SQL_COUNT2 = KAnsi.magenta("[ *** SQL-COUNT(t2) *** ]");
	public static final String LT_SQL_COUNT3 = KAnsi.magenta("[ *** SQL-COUNT(t3) *** ]");
	public static final String LT_SQL_ERROR = KAnsi.boldMagenta("[ *** SQL-ERROR *** ]");
	public static final String LT_SQL_PAING = KAnsi.boldMagenta("[ *** SQL-PAGING *** ]");
	public static final String LT_SQL_PARAM = KAnsi.magenta("[ *** SQL-PARAM *** ]");
	public static final String LT_SQL_RESULT = KAnsi.magenta("[ *** SQL-RESULT *** ]");
	public static final String LT_SQL_RESULT_VERBOSS = KAnsi.magenta("[ *** SQL-RESULT-VERBOSS_ALL *** ]");
	public static final String LT_EXCEPTION = KAnsi.magenta("[ *** EXCEPTION *** ]");
	public static final String LT_PROFILE = KAnsi.magenta("[ *** PROFILE *** ]");
	public static final String LT_CONFIG = KAnsi.magenta("[ *** CONFIG *** ]");
	public static final String LT_PROPERTY = KAnsi.magenta("[ *** PROPERTY *** ]");

	public static final String HK_DEBUG = "debug";
	public static final String HK_AUTHORIZATION = "Authorization";
	public static final String HK_WL_PROXY_ClIENT_IP = "WL-Proxy-Client-IP";
	public static final String HK_X_FORWARDED_FOR = "X-Forwarded-For";
	public static final String HK_USER_AGENT = "User-Agent";
	public static final String HK_CONTENT_DISPOSITION = "Content-Disposition";

	public static final String MDC_EXEC_TYPE = "execType";
	public static final String MDC_DEBUG_MODE_YN = "DEBUG_MODE_YN";
	public static final String MDC_DEBUG_FILENAME = "DEBUG_FILENAME";

	public static final long MSEC = 1000L;

	public static final String FMT_YYYYMMDD = "yyyyMMdd";
	public static final String FMT_YYYY_MM_DD = "yyyy-MM-dd";
	public static final String FMT_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
	public static final String FMT_YYYYMMDDHHMMSSSSS = "yyyyMMddHHmmssSSS";
	public static final String FMT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
	public static final String FMT_YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss.SSS";
	public static final String FMT_HHMMSS = "HHmmss";
	public static final String FMT_HH_MM_SS = "HH:mm:ss";
	public static final String FMT_HH_MM_SS_SSS = "HH:mm:ss.SSS";

	public static final String SWG_SYSTEM_COMMON = "system-common";
	public static final String SWG_SYSTEM_MANAGEMENT = "system-management";
	public static final String SWG_SERVICE_ADMIN = "service-admin";
	public static final String SWG_SERVICE_COMMON = "service-common";
}
