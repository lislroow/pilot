package mgkim.service.lib.env;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KProfile;
import mgkim.service.lib.type.TSiteType;

public class CmmProfile {
	
	public static void init(String defAppId) {
		// 사이트 구분 설정 (www, adm, bat)
		{
			String appId = System.getProperty(KConstant.VM_APP_ID);
			if (appId == null) {
				appId = defAppId;
				System.setProperty(KConstant.VM_APP_ID, appId);
			}
			String[] arr = java.util.Arrays.stream(TSiteType.values()).map(item -> item.label()).toArray(String[]::new);
			Matcher matcher = Pattern.compile("[ds]*("+String.join("|", arr)+")[0-9]{1,2}").matcher(appId);
			if (matcher.find()) {
				KProfile.APP_ID = appId;
				TSiteType site = TSiteType.get(matcher.group(1));
				KProfile.SITE_TPCD = site.code();
				KProfile.addProfile(site.label());
			} else {
				System.exit(-1);
			}
		}
	}
}
