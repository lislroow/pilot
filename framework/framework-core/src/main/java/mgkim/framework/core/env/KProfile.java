package mgkim.framework.core.env;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import mgkim.framework.core.type.TSysType;

public class KProfile {

	private static final Logger log = LoggerFactory.getLogger(KProfile.class);


	public static final String GROUP = "mgkim";
	public static final String DOMAIN = "pilot";
	public static final String HOSTNAME;
	public static String APP_ID;
	public static String APP_NAME;
	public static String SITE_TPCD;
	public static TSysType SYS;
	public static List<String> profiles = new ArrayList<String>();
	public static String PROFILES_STR;

	static {
		// KProfile.HOSTNAME 설정
		{
			String hostname = null;
			try {
				hostname = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} finally {
				if (hostname == null || "".equals(hostname)) {
					hostname = "localhost";
				}
			}
			HOSTNAME = hostname;
			log.warn("{} KProfile.HOSTNAME={}", KConstant.LT_PROFILE, HOSTNAME);
		}

		String val = null;
		if (System.getProperty(KConstant.VM_SPRING_PROFILES_ACTIVE) == null) {
			val = "";
		} else {
			val = System.getProperty(KConstant.VM_SPRING_PROFILES_ACTIVE);
		}
		String[] arr = val.split(",");
		
		// 시스템구분 설정 (loc, dev, test, prod)
		{
			TSysType[] sysType = TSysType.values();
			for (int i=0; i<sysType.length; i++) {
				for (int j=0; j<arr.length; j++) {
					if (arr[j].equalsIgnoreCase(sysType[i].name())) {
						KProfile.SYS = sysType[i];
						KProfile.profiles.add(KProfile.SYS.toString());
						break;
					}
				}
			}
			if (KProfile.SYS == null) {
				KProfile.SYS = TSysType.LOC;
				KProfile.profiles.add(KProfile.SYS.toString());
				addProfile(KProfile.SYS.code());
			}
			log.warn("{} KProfile.SYS={}", KConstant.LT_PROFILE, KProfile.SYS.toString());
		}
	}
	
	public static void addProfile(String addVal) {
		String val = System.getProperty(KConstant.VM_SPRING_PROFILES_ACTIVE);
		if (val == null || "".equals(val)) {
			System.setProperty(KConstant.VM_SPRING_PROFILES_ACTIVE, addVal);
		} else {
			System.setProperty(KConstant.VM_SPRING_PROFILES_ACTIVE, val + "," + addVal);
		}
		
		// PROFILES_STR (시스템로깅에서 사용할 문자열 생성)
		MDC.put("profiles", System.getProperty(KConstant.VM_SPRING_PROFILES_ACTIVE));
	}

	public static boolean isLocal() {
		return SYS == TSysType.LOC;
	}

	public static String getHostname() {
		return HOSTNAME;
	}
}
