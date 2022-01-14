package mgkim.framework.core.logging;

import org.slf4j.MDC;

import mgkim.framework.core.env.KContext.AttrKey;

public class KLogMDC {
	
	public static final void put(AttrKey key, String val) {
		MDC.put(key.name().toLowerCase(), String.format("[%s]", val));
	}
	
	public static final String get(String key) {
		String val = MDC.get(key);
		if (val == null) {
			return "";
		}
		
		val = val.length() >= 2 ? 
				val.substring(1, val.length()-1) : 
				val;
		return val;
	}
}
