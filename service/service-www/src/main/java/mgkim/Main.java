package mgkim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.util.KStringUtil;

@SpringBootApplication
public class Main extends SpringBootServletInitializer {
	
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	static {
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
