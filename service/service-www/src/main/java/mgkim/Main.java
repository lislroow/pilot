package mgkim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.type.TSiteType;
import mgkim.framework.core.util.KStringUtil;

@SpringBootApplication
public class Main extends SpringBootServletInitializer {
	
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	static {
		String val = null;
		if (System.getProperty(KConstant.VM_SPRING_PROFILES_ACTIVE) == null) {
			val = "";
		} else {
			val = System.getProperty(KConstant.VM_SPRING_PROFILES_ACTIVE);
		}
		String[] arr = val.split(",");
		// 사이트 구분 설정 (www, adm, bat)
		{
			TSiteType[] type = TSiteType.values();
			for (int i=0; i<type.length; i++) {
				for (int j=0; j<arr.length; j++) {
					if (arr[j].equalsIgnoreCase(type[i].name())) {
						KProfile.SITE = type[i];
						KProfile.profiles.add(arr[j]);
						break;
					}
				}
			}
			if (KProfile.SITE == null) {
				KProfile.SITE = TSiteType.WWW;
				KProfile.profiles.add(KProfile.SITE.label());
			}
			log.warn("{} KProfile.SITE={}", KConstant.LT_PROFILE, KProfile.SITE.label());

			String str = System.getProperty(KConstant.VM_SPRING_PROFILES_ACTIVE);
			if (str == null || "".equals(str)) {
				System.setProperty(KConstant.VM_SPRING_PROFILES_ACTIVE, KProfile.SITE.label());
			} else {
				System.setProperty(KConstant.VM_SPRING_PROFILES_ACTIVE, str + "," + KProfile.SITE.label());
			}
		}
		
		// 웹 어플리케이션 초기화 시 가장 먼저 호출되는 코드 블럭
		{
			// 초기화를 진행하는 thread 의 KContext 초기화
			KContext.initSystem();
			log.debug("current profile={}", KStringUtil.toJson2(KProfile.profiles));
		}

	}

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Main.class);
	}
}
