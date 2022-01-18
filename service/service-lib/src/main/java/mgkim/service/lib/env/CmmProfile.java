package mgkim.service.lib.env;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KProfile;
import mgkim.service.lib.type.TAppType;

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
				String[] arr = java.util.Arrays.stream(TAppType.values()).map(item -> item.label()).toArray(String[]::new);
				Matcher appMatcher = Pattern.compile("[lds]*("+String.join("|", arr)+")[0-9]*").matcher(appId);
				if (appMatcher.find()) {
					TAppType app = TAppType.get(appMatcher.group(1));
					KProfile.APP_ID = appId;
					KProfile.APP_CD = app.code();
					KProfile.APP_NM = app.label();
					KProfile.APP_NAME = String.format("%s-%s", KProfile.DOMAIN, app.label());
					System.setProperty(KConstant.VM_APP_NAME, KProfile.APP_NAME);
				} else {
					System.exit(-1);
				}
			}
			
			switch (KProfile.SYS) {
			case LOC:
				break;
			case DEV:
			case STA:
			case PROD:
				String configXml = String.format("/app/pilot-%s/.logback-%s-%s.xml"
						, KProfile.SYS.label()
						, KProfile.APP_NM
						, KProfile.SYS.label());
				System.setProperty("logging.config", configXml);
				break;
			}
		}
	}
}
