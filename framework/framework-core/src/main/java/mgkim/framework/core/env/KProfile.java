package mgkim.framework.core.env;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mgkim.framework.core.type.KType.OsType;
import mgkim.framework.core.type.TSysType;

public class KProfile {

	private static final Logger log = LoggerFactory.getLogger(KProfile.class);


	public static final String BASE_PACKAGE = "mgkim";
	public static final String DOMAIN = "pilot";
	public static final String HOSTNAME;
	public static String APP_ID;
	public static String APP_CD;
	public static String APP_NM;
	public static String APP_NAME;
	public static TSysType SYS;
	public static OsType OS;
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
		}
		String osName = System.getProperty(KConstant.VM_OS_NAME);
		KProfile.OS = OsType.get(osName);
		
		String appId = System.getProperty(KConstant.VM_APP_ID);
		if (appId == null) {
			KProfile.SYS = TSysType.LOC;
		} else {
			String appId_c1 = appId.substring(0, 1);
			KProfile.SYS = TSysType.get(appId_c1);
		}
		KProfile.addProfile(KProfile.SYS.label());
	}
	
	private static void addProfile(String addVal) {
		String val = System.getProperty(KConstant.VM_SPRING_PROFILES_ACTIVE);
		if (val == null || "".equals(val)) {
			System.setProperty(KConstant.VM_SPRING_PROFILES_ACTIVE, addVal);
		} else {
			System.setProperty(KConstant.VM_SPRING_PROFILES_ACTIVE, val + "," + addVal);
		}
	}

	public static boolean isLocal() {
		return SYS == TSysType.LOC;
	}

	public static String getHostname() {
		return HOSTNAME;
	}
}
