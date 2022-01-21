package mgkim.service.lib.env;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.type.KType.OsType;
import mgkim.framework.core.type.KType.TSysType;
import mgkim.service.lib.type.TAppType;

public class CmmProfile {
	
	public static void init() {
		// profile 설정 {app_id, app_cd, app_nm, app_name}
		{
			String appId = System.getProperty(KConstant.VM_APP_ID);
			if (appId == null) {
				StackTraceElement s = Thread.currentThread().getStackTrace()[2];
				appId = "l"+s.getClassName().split("\\.")[2];
				System.setProperty(KConstant.VM_APP_ID, appId);
			}
			String[] sysArr = java.util.Arrays.stream(TSysType.values()).map(item -> item.label().substring(0, 1)).toArray(String[]::new);
			String[] appArr = java.util.Arrays.stream(TAppType.values()).map(item -> item.label()).toArray(String[]::new);
			String appPtrn = String.format("(%s)(%s)[0-9]*"
					, String.join("|", sysArr)
					, String.join("|", appArr));
			Matcher appMatcher = Pattern.compile(appPtrn).matcher(appId);
			if (appMatcher.find()) {
				String grp1 = appMatcher.group(1);
				String grp2 = appMatcher.group(2);
				TAppType app = TAppType.get(grp2);
				KProfile.APP_ID = appId;
				KProfile.APP_CD = app.code();
				KProfile.APP_NM = app.label();
				KProfile.APP_NAME = String.format("%s-%s", KProfile.DOMAIN, app.label());
				System.setProperty(KConstant.VM_APP_NAME, KProfile.APP_NAME);
			} else {
				System.exit(-1);
			}
		}
		
		// logback 설정
		{
			switch (KProfile.SYS) {
			case LOC:
				break;
			case DEV:
			case STA:
			case PROD:
				if (KProfile.OS == OsType.LINUX) {
					String configXml = String.format("/app/pilot-%s/.logback-%s-%s.xml"
							, KProfile.SYS.label()
							, KProfile.APP_NM
							, KProfile.SYS.label());
					System.setProperty("logging.config", configXml);
				} else if (KProfile.OS == OsType.WIN) {
					String configXml = String.format("src/main/resources/.logback-%s-%s.xml"
							, KProfile.APP_NM
							, KProfile.SYS.label());
					System.setProperty("logging.config", configXml);
				}
				break;
			}
		}
	}
}
