package mgkim.framework.online.com.init;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import mgkim.framework.core.annotation.KRequestMap;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.exception.KExceptionHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.DocExpansion;
import springfox.documentation.swagger.web.ModelRendering;
import springfox.documentation.swagger.web.OperationsSorter;
import springfox.documentation.swagger.web.TagsSorter;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
@EnableSwagger2WebMvc
public class KInitMvc extends WebMvcConfigurationSupport {
	
	private static final Logger log = LoggerFactory.getLogger(KInitMvc.class);
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		//registry.addViewController("/").setViewName("redirect:"+KConstant.REFERER_SWAGGER);
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// [status]
		registry.addResourceHandler("/runtime.html").addResourceLocations("classpath:/META-INF/static/runtime/");
		registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/META-INF/static/runtime/resources/");
		
		// [swagger]
		registry.addResourceHandler("/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
	}
	
	@Autowired
	private KExceptionHandler exceptionHandler;
	
	@Override
	protected ExceptionHandlerExceptionResolver createExceptionHandlerExceptionResolver() {
		return exceptionHandler;
	}


	
	
	/*
	@Bean("internalResourceViewResolver")
	public InternalResourceViewResolver createInternalResourceViewResolver() {
		InternalResourceViewResolver bean = new InternalResourceViewResolver();
		bean.setViewClass(JstlView.class);
		bean.setPrefix("/WEB-INF/jsp/");
		bean.setSuffix(".jsp");
		return bean;
	}
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable("");
	}
	*/

	final String AUTHORIZATION_DESC = "Bearer + accessToken";

	@Bean
	public Docket docket() {
		//Docket docket = new Docket(DocumentationType.SPRING_WEB)
		//Docket docket = new Docket(DocumentationType.OAS_30)
		Docket docket = new Docket(DocumentationType.SWAGGER_2)
				//.globalRequestParameters(Arrays.asList(new RequestParameterBuilder()
				//		.name("debug")
				//		.in(ParameterType.HEADER)
				//		.required(false)
				//		//.example(new ExampleBuilder().value("Y").build())
				//		.build()
				//	))
				.globalOperationParameters(Arrays.asList(
						new ParameterBuilder()
						.name("debug")
						.defaultValue("Y")
						.modelRef(new ModelRef("string"))
						.parameterType("header")
						.build()
						))
				.ignoredParameterTypes(KRequestMap.class)
				.useDefaultResponseMessages(false)
				.securityContexts(Arrays.asList(securityContext()))
				.securitySchemes(Arrays.asList(apiKey()))
				.apiInfo(apiInfo())
				.select()
				.apis(RequestHandlerSelectors.basePackage(KProfile.BASE_PACKAGE))
				.paths(PathSelectors.regex("/api/.*|/public/.*")).build();
		return docket;
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("mgkim pilot api")
				.build();
	}

	// io.springfox:3.0.0
	private SecurityContext securityContext() {
		return SecurityContext.builder()
				.securityReferences(defaultAuth())
				.build();
	}

	List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Collections.singletonList(new SecurityReference(AUTHORIZATION_DESC, authorizationScopes));
	}

	ApiKey apiKey() {
		return new ApiKey(AUTHORIZATION_DESC, KConstant.HK_AUTHORIZATION, "header");
	}

	@Bean
	UiConfiguration uiConfig() {
		return UiConfigurationBuilder.builder()
				.deepLinking(true)
				.displayOperationId(false)
				.defaultModelsExpandDepth(1)
				.defaultModelExpandDepth(1)
				.defaultModelRendering(ModelRendering.EXAMPLE)
				.displayRequestDuration(false)
				.docExpansion(DocExpansion.NONE)
				.filter(false)
				.maxDisplayedTags(null)
				.operationsSorter(OperationsSorter.ALPHA)
				.showExtensions(false)
				.showCommonExtensions(false)
				.tagsSorter(TagsSorter.ALPHA)
				.supportedSubmitMethods(UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS)
				.validatorUrl(null)
				.build();
	}
	// -- io.springfox:3.0.0

}
