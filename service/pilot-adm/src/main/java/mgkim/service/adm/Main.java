package mgkim.service.adm;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import mgkim.service.lib.env.CmmProfile;

@ComponentScan(
	basePackages = {
		"mgkim.framework.core",
		"mgkim.framework.online",
		"mgkim.service.cmm.online",
		"mgkim.service.www",
	},
	includeFilters = {
		@ComponentScan.Filter(type = FilterType.ANNOTATION, value = org.springframework.stereotype.Service.class),
		@ComponentScan.Filter(type = FilterType.ANNOTATION, value = org.springframework.stereotype.Component.class),
		@ComponentScan.Filter(type = FilterType.ANNOTATION, value = mgkim.framework.core.annotation.KBean.class),
		@ComponentScan.Filter(type = FilterType.ANNOTATION, value = mgkim.framework.core.annotation.KTaskSchedule.class)
	}
)
@MapperScan(basePackages = "mgkim", annotationClass = org.apache.ibatis.annotations.Mapper.class)
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
