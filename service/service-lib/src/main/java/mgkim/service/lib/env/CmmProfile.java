package mgkim.service.lib.env;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KProfile;
import mgkim.service.lib.type.TSiteType;

public class CmmProfile {
	
	public static void init(String defAppName, String defAppId) {
		// 사이트 구분 설정 (www, adm, bat)
		{
			// app.id 설정
			{
				String appId = System.getProperty(KConstant.VM_APP_ID);
				if (appId == null) {
					appId = defAppId;
					System.setProperty(KConstant.VM_APP_ID, appId);
				}
				String[] arr = java.util.Arrays.stream(TSiteType.values()).map(item -> item.label()).toArray(String[]::new);
				Matcher matcher = Pattern.compile("[lds]*("+String.join("|", arr)+")[0-9]*").matcher(appId);
				if (matcher.find()) {
					KProfile.APP_ID = appId;
					TSiteType site = TSiteType.get(matcher.group(1));
					KProfile.SITE_TPCD = site.code();
				} else {
					System.exit(-1);
				}
			}
			
			// app.name 설정
			String appName = System.getProperty(KConstant.VM_APP_NAME);
			if (appName == null) {
				appName = defAppName;
				System.setProperty(KConstant.VM_APP_NAME, appName);
			}
			
			KProfile.APP_NAME = appName;
			switch (KProfile.SYS) {
			case DEV:
			case STA:
			case PROD:
				String a3 = appName.substring(appName.indexOf("-")+1, appName.length());
				System.setProperty("logging.config", "/app/pilot-"+KProfile.SYS.code()+"/.logback-"+a3+"-"+KProfile.SYS.code()+".xml");
				break;
			}
		}
	}
}
