package mgkim.framework.core.env;

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

import mgkim.framework.core.type.TEncodingType;

public class KConfig {
	
	private static final Logger log = LoggerFactory.getLogger(KConfig.class);
	
	public static final String[] CONFIG_FILE = new String[] {
		"/config.properties",
	};
	
	//public static final File TMPDIR = new File(System.getProperty("java.io.tmpdir"));
	public static final File TMPDIR = new File("C:");
	
	private static String trim(String str) {
		String result = null;
		int len = str.length();
		StringBuilder sb = new StringBuilder(str.length());
		for (int i=0; i<len; i++) {
			char c = str.charAt(i);
			if (!Character.isWhitespace(c)) {
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
		for (int i=0; i<array.length; i++) {
			array[i] = KConfig.trim(array[i]);
		}
		result = Arrays.asList(array);
		return result;
	}
	
	private static List<String> getConfigListByNewLine(String key) {
		List<String> result = null;
		String config = KConfig.getConfigString(key);
		String[] array = StringUtils.splitByWholeSeparator(config, null, 0);  // by newline
		for (int i=0; i<array.length; i++) {
			array[i] = KConfig.trim(array[i]);
		}
		result = Arrays.asList(array);
		return result;
	}
	
	private static String getConfigString(String key) {
		if (key.endsWith(".")) {
			key = key.substring(0, key.length()-1);
		}
		String result = "";
		InputStream is = null;
		BufferedInputStream bis = null;
		Properties props = null;
		for (String path : KConfig.CONFIG_FILE) {
			try {
				if (result != null && !KConstant.EMPTY.equals(result)) {
					return result;
				}
				props = new Properties();
				try {
					is = KConfig.class.getResourceAsStream("/"+path);
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
					String sysPropEncoding = System.getProperty("file.encoding");
					if (sysPropEncoding == null || KConstant.EMPTY.equals(sysPropEncoding)) {
						sysPropEncoding = TEncodingType.UTF8.code();
					}
					result = new String(result.getBytes(TEncodingType.ISO88591.code()), sysPropEncoding);
				}
			} catch(FileNotFoundException fne) {
				throw new RuntimeException("Property file not found", fne);
			} catch(IOException ioe) {
				throw new RuntimeException("Property file IO exception", ioe);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch(IOException e) {
					}
				}
				if (bis != null) {
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
