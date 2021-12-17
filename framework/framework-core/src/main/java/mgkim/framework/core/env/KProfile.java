package mgkim.framework.core.env;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import mgkim.framework.core.type.TSiteType;
import mgkim.framework.core.type.TSysType;

public class KProfile {

	private static final Logger log = LoggerFactory.getLogger(KProfile.class);


	public static final String GROUP = "mgkim";
	public static final String DOMAIN = "proto";
	public static final String HOSTNAME;
	public static TSiteType SITE;
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
				if(hostname == null || "".equals(hostname)) {
					hostname = "localhost";
				}
			}
			HOSTNAME = hostname;
			log.warn("{} KProfile.HOSTNAME={}", KConstant.LT_PROFILE, HOSTNAME);
		}

		String val = null;
		if(System.getProperty(KConstant.VM_SPRING_PROFILES_ACTIVE) == null) {
			val = "";
		} else {
			val = System.getProperty(KConstant.VM_SPRING_PROFILES_ACTIVE);
		}
		String[] arr = val.split(",");


		// 사이트 구분 설정 (www, adm, bat)
		{
			TSiteType[] type = TSiteType.values();
			for(int i=0; i<type.length; i++) {
				for(int j=0; j<arr.length; j++) {
					if(arr[j].contains(type[i].toString())) { // 업무구분은 like 로 비교합니다.
						KProfile.SITE = type[i];
						KProfile.profiles.add(arr[j]);
						break;
					}
				}
			}
			if(KProfile.SITE == null) {
				KProfile.SITE = TSiteType.WWW;
				KProfile.profiles.add(KProfile.SITE.label());
			}
			log.warn("{} KProfile.SITE={}", KConstant.LT_PROFILE, KProfile.SITE.label());

			String str = System.getProperty(KConstant.VM_SPRING_PROFILES_ACTIVE);
			if(str == null || "".equals(str)) {
				System.setProperty(KConstant.VM_SPRING_PROFILES_ACTIVE, KProfile.SITE.label());
			} else {
				System.setProperty(KConstant.VM_SPRING_PROFILES_ACTIVE, str + "," + KProfile.SITE.label());
			}
		}


		// 시스템구분 설정 (loc, dev, test, prod)
		{
			TSysType[] sysType = TSysType.values();
			for(int i=0; i<sysType.length; i++) {
				for(int j=0; j<arr.length; j++) {
					if(arr[j].equals(sysType[i].toString())) { // 시스템구분은 equal 로 비교합니다.
						KProfile.SYS = sysType[i];
						KProfile.profiles.add(KProfile.SYS.toString());
						break;
					}
				}
			}
			if(KProfile.SYS == null) {
				KProfile.SYS = TSysType.LOC;
				KProfile.profiles.add(KProfile.SYS.toString());
			}
			log.warn("{} KProfile.SYS={}", KConstant.LT_PROFILE, KProfile.SYS.toString());

			//
			String str = System.getProperty(KConstant.VM_SPRING_PROFILES_ACTIVE);
			if(str == null || "".equals(str)) {
				System.setProperty(KConstant.VM_SPRING_PROFILES_ACTIVE, KProfile.SYS.toString());
			} else {
				System.setProperty(KConstant.VM_SPRING_PROFILES_ACTIVE, str + "," + KProfile.SYS.toString());
			}
		}

		// PROFILES_STR (시스템로깅에서 사용할 문자열 생성)
		{
			if(profiles.size() > 0) {
				PROFILES_STR = profiles.toString().substring(1, profiles.toString().length() -1);
			} else {
				PROFILES_STR = "";
			}
		}

		MDC.put("profiles", PROFILES_STR);
	}

	public static boolean isLocal() {
		return SYS == TSysType.LOC;
	}

	public static String getHostname() {
		return HOSTNAME;
	}

	public static String getWasId() {
		return KProfile.SITE.label();
	}
}
