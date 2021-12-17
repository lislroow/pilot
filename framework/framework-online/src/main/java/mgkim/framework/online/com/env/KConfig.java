package mgkim.framework.online.com.env;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.type.TEncodingType;

public class KConfig {

	private static final Logger log = LoggerFactory.getLogger(KConfig.class);

	public static final String[] CONFIG_FILE = new String[] {
		"/config.properties",
		"/META-INF/core-online/core-config.properties"
	};

	//public static final File TMPDIR = new File(System.getProperty("java.io.tmpdir"));
	public static final File TMPDIR = new File("C:");

	public static boolean VERBOSS_ALL;
	public static boolean VERBOSS_SQL;
	public static boolean VERBOSS_REQ;
	public static boolean VERBOSS_CALLER;
	public static boolean DEBUG_FILTER;
	public static boolean DEBUG_COM;
	public static boolean VERBOSS_SCHEDULE;
	public static boolean SCHEDULE_ENABLE;
	public static List<String> CMM_URI;
	public static List<String> CMM_SQL;

	public static final List<String> SECURITY_FILTER_DENYAPI = Arrays.asList(
			  "/csrf"
			, "/favicon.ico"
			, "/"  // "/" > "swagger-ui.html" (redirect 설정)
			);

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

			//, "/springfox-swagger-ui**"  // -- io.springfox:3.0.0
			//, "/springfox.js.map"        // -- io.springfox:3.0.0
			);

	public static final List<String> FILTER_OPENAPI = Arrays.asList(
			  "/openapi/**"
			);

	public static final List<String> FILTER_ORGAPI = Arrays.asList(
			  "/orgapi/**"
			);

	public static final List<String> FILTER_INTERAPI = Arrays.asList(
			  "/interapi/**"
			);

	public static final List<String> FILTER_PUBLIC = Arrays.asList(
			  "/public/**"
			, "/api/adm/runtime/**"
			, "/api/adm/initdata/**"
			);
	public static final List<String> FILTER_API3 = Arrays.asList(
			  "/api/cmm/user/logout"
			);

	public static final List<String> FILTER_API2 = Arrays.asList(
			  "/api/cmm/file/**"
			);

	public static final List<String> FILTER_API = Arrays.asList(
			  "/api/**"
			);

	static {
		KConfig.refreshable();
		KConfig.init();
	}

	public static boolean refreshable() {
		boolean isChanged = false;
		boolean _verboss_all = KConfig.getConfigBoolean("debug.verboss.all."+KProfile.SYS.code());
		if(VERBOSS_ALL != _verboss_all) {
			log.warn(KMessage.get(KMessage.E5007, "debug.verboss.all."+KProfile.SYS.code(), VERBOSS_ALL, _verboss_all));
			VERBOSS_ALL = _verboss_all;
			isChanged = true;
		}
		boolean _verboss_sql = KConfig.getConfigBoolean("debug.verboss.sql."+KProfile.SYS.code());
		if(VERBOSS_SQL != _verboss_sql) {
			log.warn(KMessage.get(KMessage.E5007, "debug.verboss.sql."+KProfile.SYS.code(), VERBOSS_SQL, _verboss_sql));
			VERBOSS_SQL = _verboss_sql;
			isChanged = true;
		}
		boolean _verboss_req = KConfig.getConfigBoolean("debug.verboss.req."+KProfile.SYS.code());
		if(VERBOSS_REQ != _verboss_req) {
			log.warn(KMessage.get(KMessage.E5007, "debug.verboss.req."+KProfile.SYS.code(), VERBOSS_REQ, _verboss_req));
			VERBOSS_REQ = _verboss_req;
			isChanged = true;
		}
		boolean _verboss_caller = KConfig.getConfigBoolean("debug.verboss.caller."+KProfile.SYS.code());
		if(VERBOSS_CALLER != _verboss_caller) {
			log.warn(KMessage.get(KMessage.E5007, "debug.verboss.caller."+KProfile.SYS.code(), VERBOSS_CALLER, _verboss_caller));
			VERBOSS_CALLER = _verboss_caller;
			isChanged = true;
		}
		boolean _debug_filter = KConfig.getConfigBoolean("debug.request.filter."+KProfile.SYS.code());
		if(DEBUG_FILTER != _debug_filter) {
			log.warn(KMessage.get(KMessage.E5007, "debug.filter."+KProfile.SYS.code(), DEBUG_FILTER, _debug_filter));
			DEBUG_FILTER = _debug_filter;
			isChanged = true;
		}
		boolean _debug_com = KConfig.getConfigBoolean("debug.request.com."+KProfile.SYS.code());
		if (DEBUG_COM != _debug_com) {
			log.warn(KMessage.get(KMessage.E5007, "debug.request.com."+KProfile.SYS.code(), DEBUG_COM, _debug_com));
			DEBUG_COM = _debug_com;
			isChanged = true;
		}
		boolean _verboss_schedule = KConfig.getConfigBoolean("debug.verboss.schedule."+KProfile.SYS.code());
		if(VERBOSS_SCHEDULE != _verboss_schedule) {
			log.warn(KMessage.get(KMessage.E5007, "debug.verboss.schedule."+KProfile.SYS.code(), VERBOSS_SCHEDULE, _verboss_schedule));
			VERBOSS_SCHEDULE = _verboss_schedule;
			isChanged = true;
		}
		boolean _schedule_enable = KConfig.getConfigBoolean("schedule.enable."+KProfile.SYS.code());
		if(SCHEDULE_ENABLE != _schedule_enable) {
			log.warn(KMessage.get(KMessage.E5007, "schedule.enable."+KProfile.SYS.code(), SCHEDULE_ENABLE, _schedule_enable));
			SCHEDULE_ENABLE = _schedule_enable;
			isChanged = true;
		}
		return isChanged;
	}

	private static void init() {
		CMM_SQL = KConfig.getConfigListByNewLine("cmm.sql");
		CMM_URI = KConfig.getConfigListByNewLine("cmm.uri");
	}

	public static void reload() {
		log.warn("************ VERY IMPORTANT PROCESS [시작] ************");
		init();
		log.warn("************ VERY IMPORTANT PROCESS [종료] ************");
	}

	public static void reloadSys() {
		refreshable();
	}

	private static String trim(String str) {
		String result = null;
		int len = str.length();
		StringBuilder sb = new StringBuilder(str.length());
		for(int i=0; i<len; i++) {
			char c = str.charAt(i);
			if(!Character.isWhitespace(c)) {
				sb.append(c);
			}
		}
		result = sb.toString();
		return result;
	}

	private static Boolean getConfigBoolean(String key) {
		boolean result;
		String str = getConfigString(key);
		result = "true".equals(str) ? true : false;
		return result;
	}

	private static int getConfigInt(String key, int defVal) {
		int result = 0;
		String str = getConfigString(key);
		try {
			result = Integer.parseInt(str);
		} catch(Exception e) {
			result = defVal;
		}
		return result;
	}


	public static List<String> getConfigListByComma(String key) {
		List<String> result = null;

		String config = KConfig.getConfigString(key);
		String[] array = config.split(",");
		for(int i=0; i<array.length; i++) {
			array[i] = KConfig.trim(array[i]);
		}
		result = Arrays.asList(array);
		return result;
	}

	private static List<String> getConfigListByNewLine(String key) {
		List<String> result = null;

		String config = KConfig.getConfigString(key);
		String[] array = StringUtils.splitByWholeSeparator(config, null, 0);  // by newline
		for(int i=0; i<array.length; i++) {
			array[i] = KConfig.trim(array[i]);
		}
		result = Arrays.asList(array);
		return result;
	}

	private static String getConfigString(String key) {
		if(key.endsWith(".")) {
			key = key.substring(0, key.length()-1);
		}
		String result = "";
		InputStream is = null;
		BufferedInputStream bis = null;
		Properties props = null;
		for(String path : KConfig.CONFIG_FILE) {
			try {
				if(result != null && !KConstant.EMPTY.equals(result)) {
					return result;
				}
				props = new Properties();
				try {
					is = KConfig.class.getResourceAsStream("/"+path);
				} catch(Exception e) {
					e.printStackTrace();
					continue;
				}
				if(is == null) {
					continue;
				}
				bis = new BufferedInputStream(is);
				props.load(bis);
				result = props.getProperty(key);
				if(result == null) {
					result = "";
				} else {
					result.trim();
					String sysPropEncoding = System.getProperty("file.encoding");
					if(sysPropEncoding == null || KConstant.EMPTY.equals(sysPropEncoding)) {
						sysPropEncoding = TEncodingType.UTF8.code();
					}
					result = new String(result.getBytes(TEncodingType.ISO88591.code()), sysPropEncoding);
				}
			} catch(FileNotFoundException fne) {
				throw new RuntimeException("Property file not found", fne);
			} catch(IOException ioe) {
				throw new RuntimeException("Property file IO exception", ioe);
			} finally {
				if(is != null) {
					try {
						is.close();
					} catch(IOException e) {
					}
				}
				if(bis != null) {
					try {
						bis.close();
					} catch(IOException e) {
					}
				}
			}
		}
		return result;
	}

}
