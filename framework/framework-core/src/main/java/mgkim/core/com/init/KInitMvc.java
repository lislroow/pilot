package mgkim.core.com.init;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import mgkim.core.com.env.KConstant;
import mgkim.core.com.env.KProfile;
import mgkim.core.com.logging.KLogSys;
import mgkim.core.com.util.KStringUtil;
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
@EnableSwagger2WebMvc // io.springfox:2.10.5
@EnableWebMvc
public class KInitMvc implements WebMvcConfigurer {

	static {
		KLogSys.debug("current profile={}", KStringUtil.toJson2(KProfile.profiles));
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("redirect:"+KConstant.REFERER_SWAGGER);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// [status]
		registry.addResourceHandler("/uri.html").addResourceLocations("classpath:/META-INF/static/");
		registry.addResourceHandler("/status.html").addResourceLocations("classpath:/META-INF/static/");
		registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/META-INF/static/resources/");

		// [swagger]
		registry.addResourceHandler("/swagger-ui.html")
		.addResourceLocations("classpath:/META-INF/resources/");
		registry.addResourceHandler("/webjars/**")
		.addResourceLocations("classpath:/META-INF/resources/webjars/");
		//.resourceChain(false);
	}

	/*@Bean("internalResourceViewResolver")
	public InternalResourceViewResolver createInternalResourceViewResolver() {
		InternalResourceViewResolver bean = new InternalResourceViewResolver();
		bean.setViewClass(JstlView.class);
		bean.setPrefix("/WEB-INF/jsp/");
		bean.setSuffix(".jsp");
		return bean;
	}
	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable("mgkim-proto");
	}*/

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
				.useDefaultResponseMessages(false)
				.securityContexts(Arrays.asList(securityContext()))
				.securitySchemes(Arrays.asList(apiKey()))
				.apiInfo(apiInfo())
				.select()
				.apis(RequestHandlerSelectors.basePackage(KProfile.GROUP))
				.paths(PathSelectors.regex("/api/.*|/public/.*|/interapi/.*|/openapi/.*|/orgapi/.*")).build();
		return docket;
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("mgkim proto api")
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
