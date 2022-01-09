package mgkim.service.lib.env;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KProfile;
import mgkim.service.lib.type.TSiteType;

public class CmmProfile {
	
	public static void init(String defAppName) {
		// 사이트 구분 설정 (www, adm, bat)
		{
			String appName = System.getProperty(KConstant.VM_APP_NAME);
			if (appName == null) {
				appName = defAppName;
				System.setProperty(KConstant.VM_APP_NAME, appName);
			}
			String[] arr = java.util.Arrays.stream(TSiteType.values()).map(item -> item.label()).toArray(String[]::new);
			Matcher matcher = Pattern.compile("("+String.join("|", arr)+")[0-9]{1,2}").matcher(appName);
			if (matcher.find()) {
				TSiteType site = TSiteType.get(matcher.group(1));
				KProfile.SITE_TPCD = site.code();
				KProfile.addProfile(site.label());
			} else {
				System.exit(-1);
			}
		}
	}
}
