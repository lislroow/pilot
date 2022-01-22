package mgkim.framework.online.com.init;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import mgkim.framework.core.annotation.KRequestMap;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.exception.KExceptionHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.ParameterType;
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
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
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
		registry.addResourceHandler("/swagger-ui/**").addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/");
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/");
	}
	
	@Autowired
	private KExceptionHandler exceptionHandler;
	
	@Override
	protected ExceptionHandlerExceptionResolver createExceptionHandlerExceptionResolver() {
		return exceptionHandler;
	}
	
	@Bean
	public MultipartFilter multipartFilter() {
		MultipartFilter filter = new MultipartFilter();
		filter.setMultipartResolverBeanName("multipartResolver");
		return filter;
	}

	@Bean
	public MultipartResolver multipartResolver() {
		return new StandardServletMultipartResolver() {
			@Override
			public boolean isMultipart(HttpServletRequest request) {
				String method = request.getMethod().toLowerCase();
				// 2022.01.23 기본은 post 이며, put 일 경우에도 multipart 로 인식할 수 있도록 함
				if (!Arrays.asList("put", "post").contains(method)) {
					return false;
				}
				String contentType = request.getContentType();
				return (contentType != null && contentType.toLowerCase().startsWith("multipart/"));
			}
		};
	}
	//@Bean("multipartResolver")
	//public CommonsMultipartResolver multipartResolver()  {
	//	CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
	//	multipartResolver.setDefaultEncoding("UTF-8");
	//	multipartResolver.setMaxUploadSize(104857600);
	//	multipartResolver.setMaxInMemorySize(104857600);
	//	return multipartResolver;
	//}
	
	
	/*@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxFileSize(DataSize.ofMegabytes(512));
		factory.setMaxRequestSize(DataSize.ofMegabytes(512));
		return factory.createMultipartConfig();
	}*/
	/**/
	/*
	public static final long MAX_UPLOAD_SIZE = 600000000;
	@Bean("multipartResolver")
	public CommonsMultipartResolver createCommonsMultipartResolver() {
		CommonsMultipartResolver bean = new CommonsMultipartResolver();
		//bean.setMaxUploadSize(Long.MAX_VALUE);
		bean.setMaxUploadSize(MAX_UPLOAD_SIZE);
		bean.setMaxInMemorySize(10240);
		return bean;
	}
	*/
	
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
		Docket docket = new Docket(DocumentationType.OAS_30)
				.globalRequestParameters(Arrays.asList(
						new RequestParameterBuilder()
							.name("debug")
							//.contentModel(new ModelSpecificationBuilder().)
							.in(ParameterType.HEADER)
							.required(false)
							.description("Y 일 경우 log-level TRACE")
							.build()
						))
				.ignoredParameterTypes(KRequestMap.class)
				.useDefaultResponseMessages(false)
				.securityContexts(Arrays.asList(SecurityContext.builder()
												.securityReferences(defaultAuth())
												.build()))
				.securitySchemes(Arrays.asList(new ApiKey(AUTHORIZATION_DESC, KConstant.HK_AUTHORIZATION, "header")))
				.apiInfo(new ApiInfoBuilder()
							.title("mgkim pilot api")
							.build())
				.select()
				.apis(RequestHandlerSelectors.basePackage(KProfile.BASE_PACKAGE))
				.paths(PathSelectors.regex("/api/.*|/public/.*|/v1/.*")).build();
		return docket;
	}
	
	List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Collections.singletonList(new SecurityReference(AUTHORIZATION_DESC, authorizationScopes));
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
}
