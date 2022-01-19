package mgkim.service.bat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

import mgkim.framework.core.env.KProfile;
import mgkim.service.lib.env.CmmProfile;

@ComponentScan(basePackages = {KProfile.BASE_PACKAGE})
@SpringBootApplication
public class Main extends SpringBootServletInitializer {
	
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	static {
		CmmProfile.init();
	}

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Main.class);
	}
}
