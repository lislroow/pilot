package mgkim.framework.online.com.init;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.support.lob.DefaultLobHandler;

import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.online.com.mybatis.KMapWrapperFactory;
import mgkim.framework.online.com.mybatis.KSqlSessionFactory;

@Configuration
public class KInitMybatis {

	public static final String CONFIG_FILE_PATH = "classpath:mybatis/mybatis-config.xml";
	public static final String SQL_FILE = "classpath*:mapper/**/*SQL.xml";
	
	// mybatis
	@Bean
	public ConfigurationCustomizer configurationCustomizer() {
		return configuration -> configuration.setObjectWrapperFactory(new KMapWrapperFactory());
	}
	
	@Bean("lobHandler")
	public DefaultLobHandler defaultLobHandler() {
		DefaultLobHandler bean = new DefaultLobHandler();
		return bean;
	}
	
	//@Bean("dataSource")
	//public JndiObjectFactoryBean jndiObjectFactoryBean() {
	//	JndiObjectFactoryBean bean = new JndiObjectFactoryBean();
	//	boolean isWeblogic = bean.getClass().getClassLoader().toString().contains("weblogic");
	//	log.debug("jndiName-name 설정을 위해 weblogic 여부를 확인합니다. ClassLoader명={}", bean.getClass().getClassLoader().toString());
	//	if (!isWeblogic) {
	//		bean.setJndiName("java:comp/env/"+jndiName);
	//	} else {
	//		bean.setJndiName(jndiName);
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
		KSqlSessionFactory bean = new KSqlSessionFactory();
		bean.setDataSource(dataSource);
		bean.setConfigLocation(mybatisConfigLocation);
		bean.setMapperLocations(mapperLocations);
		bean.setTypeAliasesPackage(KProfile.BASE_PACKAGE);
		return bean;
	}
	
	@Bean("mybatisConfigLocation")
	public Resource getMybatisConfigLocation() throws Exception {
		Resource resource = new PathMatchingResourcePatternResolver().getResource(CONFIG_FILE_PATH);
		if (!resource.exists()) {
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
		//log.info("{} mapper files loaded", result.length);
		//return result;
		return resourceInClasspath;
	}
	// -- mybatis
}
