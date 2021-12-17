package mgkim.online.com.init;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.sql.DataSource;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import mgkim.online.com.env.KContext;
import mgkim.online.com.env.KProfile;
import mgkim.online.com.exception.KMessage;
import mgkim.online.com.exception.KSysException;
import mgkim.online.com.logging.KLogSys;
import mgkim.online.com.mybatis.ComSqlSessionFactory;
import mgkim.online.com.util.KStringUtil;

@Configuration
@EnableAsync
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan(basePackages = {KProfile.GROUP},
	useDefaultFilters = false,
	includeFilters = {
		@ComponentScan.Filter(type = FilterType.ANNOTATION, value = org.springframework.stereotype.Service.class),
		@ComponentScan.Filter(type = FilterType.ANNOTATION, value = org.springframework.stereotype.Component.class),
		@ComponentScan.Filter(type = FilterType.ANNOTATION, value = mgkim.online.com.annotation.KBean.class),
		@ComponentScan.Filter(type = FilterType.ANNOTATION, value = mgkim.online.com.annotation.KTaskSchedule.class)
	},
	excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ANNOTATION, value = EnableWebMvc.class)
	}
)
public class KInit implements ServletContextInitializer, BeanFactoryPostProcessor {

	private static final Logger log = LoggerFactory.getLogger(KInit.class);

	static {
		// 웹 어플리케이션 초기화 시 가장 먼저 호출되는 코드 블럭
		{
			// 초기화를 진행하는 thread 의 KContext 초기화
			KContext.initSystem();
			KLogSys.debug("current profile={}", KStringUtil.toJson2(KProfile.profiles));
		}

	}

	// ServletContextInitializer
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		log.info("ServletContextInitializer onStartup(ServletContext servletContext) 실행");
		servletContext.addListener(new mgkim.online.com.listener.KContextListener());
		servletContext.addListener(new mgkim.online.com.listener.KSessionListener());
		servletContext.addListener(new mgkim.online.com.listener.KRequestListener());

		FilterRegistration.Dynamic characterEncoding = servletContext.addFilter("encodingFilter", new org.springframework.web.filter.CharacterEncodingFilter());
		characterEncoding.setInitParameter("encoding", "UTF-8");
		characterEncoding.setInitParameter("forceEncoding", "true");
		characterEncoding.addMappingForUrlPatterns(null, false, "*");

		MultipartFilter springMultipartFilter = new MultipartFilter();
		springMultipartFilter.setMultipartResolverBeanName("multipartResolver");
		FilterRegistration.Dynamic multipartFilter = servletContext.addFilter("springMultipartFilter", springMultipartFilter);
		multipartFilter.addMappingForUrlPatterns(null, false, "*");

		servletContext.getFilterRegistration("springSecurityFilterChain")
			.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
	}
	// -- ServletContextInitializer


	public static final String JNDI = "jdbc/space-app";

	public static final String TRANSACTION_POINTCUT = ""
			+ "     execution(* mgkim..service.*.*(..))"
			+ " && !execution(* mgkim..service.*NonTx.*(..))"
			+ " && !execution(* mgkim..service.*.*NonTx(..))";

	public static final String MAPPER_CLASS = "**.mapper";
	public static final String CONFIG_FILE_PATH = "classpath:mybatis/mybatis-config.xml";
	public static final String SQL_FILE = "classpath*:mapper/**/*SQL.xml";
	//public static final List<String> JAR = Arrays.asList("core*.jar");
	public static final long MAX_UPLOAD_SIZE = 600000000;

	@Bean
	public TomcatServletWebServerFactory tomcatFactory() {
		log.info("tomcat server 생성");
		return new TomcatServletWebServerFactory() {
			@Override
			protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
				tomcat.enableNaming();
				//tomcat.setHostname("localhost");
				return super.getTomcatWebServer(tomcat);
			}
			@Override
			protected void postProcessContext(Context context) {
				super.postProcessContext(context);
				ContextResource resource = new ContextResource();
				resource.setName(JNDI); //resource.setName("java:comp/env/"+JNDI); [not working]
				resource.setType("javax.sql.DataSource");
				resource.setAuth("Container");
				resource.setProperty("factory", "org.apache.commons.dbcp2.BasicDataSourceFactory");
				resource.setProperty("driverClassName", "oracle.jdbc.driver.OracleDriver");
				resource.setProperty("url", "jdbc:oracle:thin:@develop:1521/SPADBP");
				resource.setProperty("username", "SPADBA");
				resource.setProperty("password", "1");
				context.getNamingResources().addResource(resource);
			}
		};
	}


	@Bean("txManager")
	public DataSourceTransactionManager createDataSourceTransactionManager(@Autowired DataSource dataSource) {
		DataSourceTransactionManager bean = new DataSourceTransactionManager();
		bean.setDataSource(dataSource);
		bean.setGlobalRollbackOnParticipationFailure(false);
		bean.setDefaultTimeout(-1);
		return bean;
	}

	@Bean("txAdvice")
	public TransactionInterceptor createTransactionInterceptor(DataSourceTransactionManager dataSourceTransactionManager) {
		TransactionInterceptor bean = new TransactionInterceptor();

		// 주요 설정
		String READONLY_PATTERN = "select*";
		String WRITE_PATTERN = "*";
		int TX_READONLY_TIMEOUT;
		int TX_WRITE_TIMEOUT;
		{
			TX_READONLY_TIMEOUT = 15;
			TX_WRITE_TIMEOUT = 15;
		}

		DefaultTransactionAttribute readOnly = new DefaultTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRED);
		readOnly.setReadOnly(true);
		readOnly.setTimeout(TX_READONLY_TIMEOUT);

		List<RollbackRuleAttribute> rollbackRules = new ArrayList<RollbackRuleAttribute>();
		rollbackRules.add(new RollbackRuleAttribute(Exception.class));
		RuleBasedTransactionAttribute write = new RuleBasedTransactionAttribute(TransactionDefinition.PROPAGATION_REQUIRED, rollbackRules);
		write.setTimeout(TX_WRITE_TIMEOUT);

		Properties txAttributes = new Properties();
		txAttributes.setProperty(READONLY_PATTERN, readOnly.toString());
		txAttributes.setProperty(WRITE_PATTERN, write.toString());
		bean.setTransactionAttributes(txAttributes);
		bean.setTransactionManager(dataSourceTransactionManager);
		return bean;
	}

	@Bean("requiredTx")
	public DefaultPointcutAdvisor createAdvisor(TransactionInterceptor transactionInterceptor) {
		AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
		pointcut.setExpression(TRANSACTION_POINTCUT);
		DefaultPointcutAdvisor bean = new DefaultPointcutAdvisor(pointcut, transactionInterceptor);
		return bean;
	}








	@Bean("multipartResolver")
	public CommonsMultipartResolver createCommonsMultipartResolver() {
		CommonsMultipartResolver bean = new CommonsMultipartResolver();
		//bean.setMaxUploadSize(Long.MAX_VALUE);
		bean.setMaxUploadSize(MAX_UPLOAD_SIZE);
		bean.setMaxInMemorySize(10240);
		return bean;
	}






	// mybatis
	@Bean("lobHandler")
	public DefaultLobHandler defaultLobHandler() {
		DefaultLobHandler bean = new DefaultLobHandler();
		return bean;
	}

	//@Bean("dataSource")
	//public JndiObjectFactoryBean jndiObjectFactoryBean() {
	//	JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
	//	boolean isWeblogic = bean.getClass().getClassLoader().toString().contains("weblogic");
	//	KLogSys.debug("jndi-name 설정을 위해 weblogic 여부를 확인합니다. ClassLoader명={}", bean.getClass().getClassLoader().toString());
	//	if(!isWeblogic) {
	//		bean.setJndiName("java:comp/env/"+JNDI);
	//	} else {
	//		bean.setJndiName(JNDI);
	//	}
	//	return bean;
	//}

	@Bean("sqlSessionTemplate")
	public SqlSessionTemplate sqlSession(SqlSessionFactory factory) throws Exception {
		SqlSessionTemplate bean = new SqlSessionTemplate(factory);
		return bean;
	}

	@Bean("sqlSession")
	public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource
			, @Qualifier("mybatisConfigLocation") Resource mybatisConfigLocation
			, @Qualifier("mapperLocations") Resource[] mapperLocations) throws Exception {
		ComSqlSessionFactory bean = new ComSqlSessionFactory();
		bean.setDataSource(dataSource);
		bean.setConfigLocation(mybatisConfigLocation);
		bean.setMapperLocations(mapperLocations);
		bean.setTypeAliasesPackage(KProfile.GROUP);
		return bean;
	}

	@Bean("mapperScannerConfigurer")
	public MapperScannerConfigurer mapperScannerConfigurer() {
		MapperScannerConfigurer bean = new MapperScannerConfigurer();
		bean.setBasePackage(MAPPER_CLASS);
		return bean;
	}

	@Bean("mybatisConfigLocation")
	public Resource getMybatisConfigLocation() throws Exception {
		Resource resource = new PathMatchingResourcePatternResolver().getResource(CONFIG_FILE_PATH);
		if(!resource.exists()) {
			KSysException ex = new KSysException(KMessage.E5101, resource);
			throw ex;
		}
		return resource;
	}

	@Bean("mapperLocations")
	public Resource[] getMapperLocations() throws Exception {
		//Resource[] result = null;
		//File rootFile = new File(KConstant.PATH_WEBINF_LIB);
		Resource[] resourceInClasspath = new PathMatchingResourcePatternResolver().getResources(SQL_FILE);
		//Resource[] resourceInJar = KResourceUtil.getResourceInJar(rootFile, JAR, SQL_FILE);
		//result = new Resource[resourceInClasspath.length+resourceInJar.length];
		//System.arraycopy(resourceInClasspath, 0, result, 0, resourceInClasspath.length);
		//System.arraycopy(resourceInJar, 0, result, resourceInClasspath.length, resourceInJar.length);
		//KLogSys.info("{} mapper files loaded", result.length);
		//return result;
		return resourceInClasspath;
	}
	// -- mybatis


	// BeanFactoryPostProcessor
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory bf) throws BeansException {
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) bf;
		Arrays.stream(beanFactory.getBeanNamesForType(mgkim.online.com.stereo.KFilter.class)).forEach(name -> {
			BeanDefinition definition = BeanDefinitionBuilder
					.genericBeanDefinition(FilterRegistrationBean.class)
					.setScope(BeanDefinition.SCOPE_SINGLETON)
					.addConstructorArgReference(name)
					.addConstructorArgValue(new ServletRegistrationBean[] {})
					.addPropertyValue("enabled", false)
					.getBeanDefinition();
			beanFactory.registerBeanDefinition(name + "FilterRegistrationBean", definition);
		});
	}
	// -- BeanFactoryPostProcessor
}
