package mgkim.framework.core.env;

import java.util.Arrays;
import java.util.List;

public class KConstant {

	public static final String VM_SPRING_PROFILES_ACTIVE = "spring.profiles.active";
	public static final String VM_OS_NAME = "os.name";
	public static final String VM_APP_ID = "app.id";
	public static final String VM_APP_NAME = "app.name";
	
	public static final String EMPTY = "";
	public static final String LINE = System.getProperty("line.separator");
	
	public static final String REFERER_SWAGGER = "/swagger-ui.html";        // io.springfox:2.10.5
	//public static final String REFERER_SWAGGER = "/swagger-ui/index.html";  // io.springfox:3.0.0
	
	public static final String SYS = "sys";
	public static final String APP_ID = "appId";
	public static final String APP_NAME = "appName";
	public static final String SSID = "ssid";
	public static final String GUID = "guid";
	public static final String TXID = "txid";
	public static final String USER_ID = "userId";
	public static final String IP = "ip";
	public static final String URI = "uri";
	
	public static final String RESULT_CODE = "code";
	public static final String RESULT_MESSAGE = "message";
	public static final String RESULT_TEXT = "text";
	public static final String RESULT_BCODE = "bcode";
	public static final String RESULT_BMESSAGE = "bmessage";
	
	public static final String LT_EXCEPTION = "[ *** EXCEPTION *** ]";
	public static final String LT_PROPERTY = "[ *** PROPERTY *** ]";
	
	public static final String HK_DEBUG = "debug";
	public static final String HK_AUTHORIZATION = "Authorization";
	public static final String HK_USER_AGENT = "User-Agent";
	public static final String HK_CONTENT_DISPOSITION = "Content-Disposition";
	
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
	
	
	public static List<String> FILTER_HIDDENAPI = Arrays.asList(
			  "/"
			, "/status.html"
			, "/uri.html"
			, "/resources/**"
			, "/swagger-ui.html"       // io.springfox:2.10.5
			, "/swagger-resources**"
			, "/swagger-resources/**"
			, "/webjars/**"
			, "/v2/api-docs"           // -- io.springfox:2.10.5
			);
	
	public static final List<String> PUBLIC_URI = Arrays.asList(
			  "/public/**"
			, "/api/adm/runtime/**"
			, "/api/adm/initdata/**"
			);
	
	public static final List<String> API_URI = Arrays.asList(
			  "/api/**"
			);
}
