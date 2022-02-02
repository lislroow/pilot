package mgkim.framework.online.com.init;

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
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import mgkim.framework.core.env.KContext;

@EnableAsync
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Configuration
public class KInit implements ServletContextInitializer, BeanFactoryPostProcessor {

	private static final Logger log = LoggerFactory.getLogger(KInit.class);
	
	static {
		// 초기화를 진행하는 thread 의 KContext 초기화
		KContext.initSystem();
	}
	
	// ServletContextInitializer
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		log.info("ServletContextInitializer onStartup(ServletContext servletContext) 실행");
		servletContext.addListener(new mgkim.framework.online.com.listener.KContextListener());
		servletContext.addListener(new mgkim.framework.online.com.listener.KSessionListener());
		servletContext.addListener(new mgkim.framework.online.com.listener.KRequestListener());
		
		FilterRegistration.Dynamic characterEncoding = servletContext.addFilter("encodingFilter", new org.springframework.web.filter.CharacterEncodingFilter());
		characterEncoding.setInitParameter("encoding", "UTF-8");
		characterEncoding.setInitParameter("forceEncoding", "true");
		characterEncoding.addMappingForUrlPatterns(null, false, "*");
		
		servletContext.getFilterRegistration("springSecurityFilterChain")
			.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
	}
	// -- ServletContextInitializer
	
	
	
	public static final String TRANSACTION_POINTCUT = ""
			+ "     execution(* mgkim..*Service*.*(..))"
			+ " && !execution(* mgkim..*NonTx.*(..))"
			+ " && !execution(* mgkim..*.*NonTx(..))";

	
	@Bean
	public TomcatServletWebServerFactory tomcatFactory() {
		log.info("tomcat server 생성");
		return new TomcatServletWebServerFactory() {
			@Value("${spring.datasource.jndi-name:'jdbc/space-app'}")
			String jndiName;
			
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
				resource.setName(jndiName);
				resource.setType("javax.sql.DataSource");
				resource.setAuth("Container");
				resource.setProperty("maxTotal", "30"); // maxIdle: Maximum number of idle database connections to retain in pool. Set to -1 for no limit.  See also the DBCP 2 documentation on this and the minEvictableIdleTimeMillis configuration parameter.
				resource.setProperty("maxWaitMillis", "1"); // Maximum time to wait for a database connection to become available in ms, in this example 10 seconds. An Exception is thrown if this timeout is exceeded.  Set to -1 to wait indefinitely.
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








	// BeanFactoryPostProcessor
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory bf) throws BeansException {
		DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) bf;
		Arrays.stream(beanFactory.getBeanNamesForType(mgkim.framework.core.stereo.KFilter.class)).forEach(name -> {
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
