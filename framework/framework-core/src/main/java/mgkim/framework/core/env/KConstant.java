package mgkim.framework.core.env;

import mgkim.framework.core.logging.KAnsi;

public class KConstant {

	public static final String VM_SPRING_PROFILES_ACTIVE = "spring.profiles.active";
	public static final String VM_APP_ID = "app.id";
	
	public static final String EMPTY = "";
	
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
	
	public static final String TOKEN_JTI = "ssid";
	public static final String TOKEN_ATI = "ati";
	public static final String TOKEN_VALIDITY = "validaty";
	public static final String TOKEN_EXPIRES = "exp";
	public static final String TOKEN_AES_KEY = "aesKey";
	public static final String TOKEN_ORG_CD = "orgCd";
	public static final String TOKEN_ORG_NM = "orgNm";
	
	public static final String LT_FILTER = KAnsi.magenta("[ *** FILTER *** ]");
	public static final String LT_CLASS = KAnsi.magenta("[ *** CLASS *** ]");
	public static final String LT_EXCEPTION = KAnsi.magenta("[ *** EXCEPTION *** ]");
	public static final String LT_PROFILE = KAnsi.magenta("[ *** PROFILE *** ]");
	public static final String LT_PROPERTY = KAnsi.magenta("[ *** PROPERTY *** ]");
	
	public static final String HK_DEBUG = "debug";
	public static final String HK_AUTHORIZATION = "Authorization";
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
