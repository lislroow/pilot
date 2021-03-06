package mgkim.framework.core.property;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.type.KType.TEncodingType;
import mgkim.framework.core.util.KStringUtil;

public class KProperty {

	private static final Logger log = LoggerFactory.getLogger(KProperty.class);
	
	// TODO [2022.01.10] spring-boot 에서 classpath 경로 확인 
	public static final String[] CONFIG_FILE = new String[] {
		//  KConstant.PATH_WEBINF_CLASSES + "config.properties"
		//, "jar:file:" + KConstant.PATH_WEBINF_LIB + "META-INF/core-online/core-config.properties" + "!/"
	};

	public static String getString(String key) {
		String result = "";
		InputStream is = null;
		BufferedInputStream bis = null;
		Properties props = null;
		for (String path : CONFIG_FILE) {
			try {
				if (result != null && !KConstant.EMPTY.equals(result)) {
					return result;
				}
				props = new Properties();
				try {
					is = KProperty.class.getResourceAsStream(path);
				} catch(Exception e) {
					e.printStackTrace();
					continue;
				}
				if (is == null) {
					continue;
				}
				bis = new BufferedInputStream(is);
				props.load(bis);
				result = props.getProperty(key);
				if (result == null) {
					result = "";
				} else {
					result.trim();
					String encoding = System.getProperty("file.encoding");
					if (encoding == null || KConstant.EMPTY.equals(encoding)) {
						encoding = TEncodingType.UTF8.code();
					}
					result = new String(result.getBytes("iso-8859-1"), encoding);
				}
			} catch(FileNotFoundException fne) {
				throw new RuntimeException("Property file not found", fne);
			} catch(IOException ioe) {
				throw new RuntimeException("Property file IO exception", ioe);
			} finally {
				if (bis != null) {
					try {
						bis.close();
					} catch(IOException e) {
					}
				}
				if (is != null) {
					try {
						is.close();
					} catch(IOException e) {
					}
				}
			}
		}
		return result;
	}

	public static String[] getArray(String key) {
		String[] result = null;
		String str = getString(key);
		result = str.split(",");
		return result;
	}

	public static List<String> getList(String key) {
		List<String> result = null;
		String str = getString(key);
		String[] tokens = KStringUtil.splitByWholeSeparator(str);
		for (int i=0; i<tokens.length; i++) {
			tokens[i] = StringUtils.trimAllWhitespace(tokens[i]);
		}
		result = Arrays.asList(tokens);
		return result;
	}

	public static Boolean getBoolean(String key) {
		boolean result;
		String str = getString(key);
		result = KStringUtil.toBoolean(str) == null ? false : KStringUtil.toBoolean(str);
		return result;
	}

	public static int getInt(String key, int def) {
		int result;
		String str = getString(key);
		result = KStringUtil.nvl(str, def);
		return result;
	}

	public static Map<String, Object> getMap(String key) {
		Map<String, Object> result = null;
		InputStream is = null;
		BufferedInputStream bis = null;
		Properties props = null;
		for (String path : CONFIG_FILE) {
			try {
				props = new Properties();
				is = KProperty.class.getResourceAsStream(path);
				bis = new BufferedInputStream(is);
				props.load(bis);
				String value = props.getProperty(key);
				if (value == null) {
					result = Collections.emptyMap();
					return result;
				}
				result = KStringUtil.parseJson(value);
				return result;
			} catch(FileNotFoundException fne) {
				log.debug("Property file not found.", fne);
				throw new RuntimeException("Property file not found", fne);
			} catch(IOException ioe) {
				log.debug("Property file IO exception", ioe);
				throw new RuntimeException("Property file IO exception", ioe);
			} finally {
				if (bis != null) {
					try {
						bis.close();
					} catch(IOException e) {
					}
				}
				if (is != null) {
					try {
						is.close();
					} catch(IOException e) {
					}
				}
			}
		}
		result = Collections.emptyMap();
		return result;
	}
}