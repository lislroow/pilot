package mgkim.framework.online.api.adm.runtime.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.logging.KLogLayout;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.online.api.adm.runtime.vo.JavaEnvVariableVO;
import mgkim.framework.online.api.adm.runtime.vo.JdbcDatasourceVO;
import mgkim.framework.online.api.adm.runtime.vo.SpringBeansVO;
import mgkim.framework.online.api.adm.runtime.vo.SpringSecurityUriVO;
import mgkim.framework.online.api.adm.runtime.vo.SpringUri2VO;
import mgkim.framework.online.api.adm.runtime.vo.SpringUriVO;
import mgkim.framework.online.com.mgr.ComUriAuthorityMgr;
import mgkim.framework.online.com.mybatis.ComSqlSessionFactory;

@Service
public class RuntimeService {

	private static final Logger log = LoggerFactory.getLogger(RuntimeService.class);

	private ApplicationContext rootContext;
	//private ApplicationContext dispatcherContext;

	private RequestMappingHandlerMapping requestMapping;

	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	private Environment environment;

	@Autowired(required = false)
	//private JndiObjectFactoryBean jndiObjectFactoryBean;
	private BasicDataSource jndiObjectFactoryBean;

	@EventListener
	public void contextInit(ContextRefreshedEvent event) {
		ApplicationContext ctx = event.getApplicationContext();
		/*if (ctx.getParent() != null) {
			dispatcherContext = ctx;
			requestMapping = (RequestMappingHandlerMapping)dispatcherContext.getBean("requestMappingHandlerMapping");
		} else {
			rootContext = ctx;
		}*/
		requestMapping = (RequestMappingHandlerMapping)ctx.getBean("requestMappingHandlerMapping");
		rootContext = ctx;
	}

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	public void printJndi(String path, Context context) throws Exception {
		NamingEnumeration<NameClassPair> list = context.list(path);
		while (list.hasMore()) {
			String name = list.next().getName();
			String child = path.equals("") ? name : path + "/" + name;
			log.info(child);
			printJndi(path, context);
		}
	}

	public List<SpringBeansVO> getBeans() {
		List<SpringBeansVO> resultList = new ArrayList<SpringBeansVO>();
		resultList.addAll(resolveApplicationContext(rootContext));
		//resultList.addAll(resolveApplicationContext(dispatcherContext));
		Collections.sort(resultList);
		return resultList;
	}

	private List<SpringBeansVO> resolveApplicationContext(ApplicationContext applicationContext) {
		List<SpringBeansVO> resultList = new ArrayList<SpringBeansVO>();
		List<String> list = null;
		list = Arrays.asList(applicationContext.getBeanDefinitionNames());
		list.forEach((item) -> {
			SpringBeansVO vo = new SpringBeansVO();
			Object bean = applicationContext.getBean(item);
			if (ObjectUtils.isEmpty(bean)) {
				return;
			}
			List<Annotation> annotations = Arrays.asList(bean.getClass().getAnnotations());
			StringBuffer swaggerTags = new StringBuffer("");
			//annotations.forEach(annotation -> {
			//	if (annotation instanceof io.swagger.annotations.Api) {
			//		Api api = (Api) annotation;
			//		//swaggerTags.append(Arrays.asList(api.tags()).toString());
			//		Arrays.asList(api.tags()).forEach(tag -> {
			//			if (swaggerTags.toString().equals("")) {
			//				swaggerTags.append(tag);
			//			} else {
			//				swaggerTags.append(", "+ tag);
			//			}
			//		});
			//	}
			//});
			vo.setApplicationContextId(applicationContext.getId());
			vo.setBeanId(item);
			String beanClass = bean.getClass().getName();
			if (beanClass.contains(".$")) {
				beanClass = bean.getClass().getInterfaces()[0].getCanonicalName();
			}
			vo.setBeanClass(beanClass);
			vo.setSwaggerTags(swaggerTags.toString());
			resultList.add(vo);
			log.trace(item + " = " + bean.getClass().getCanonicalName() + " = " + annotations);
		});
		return resultList;
	}

	public List<SpringUriVO> getUri() throws Exception {
		List<SpringUriVO> resultList = new ArrayList<SpringUriVO>();
		Map<RequestMappingInfo, HandlerMethod> map = null;
		map = requestMapping.getHandlerMethods();
		map.forEach((key, value) -> {
			SpringUriVO vo = new SpringUriVO();
			StringBuffer uri = new StringBuffer("");
			RequestMapping classAnnotation = value.getBeanType().getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
			if (classAnnotation != null) {
				if (classAnnotation.value().length > 0) {
					uri.append(classAnnotation.value()[0]);
					if (classAnnotation.value().length >= 2) {
						log.warn("Class-Lavel의 @RequestMapping의 value 값이 2개 이상인 것이 있습니다. 해당 클래스를 확인해주세요. {}" + value.getClass().getSimpleName());
					}
				}
			}
			StringBuffer swaggerHttpMehtod = new StringBuffer("");
			StringBuffer swaggerValue = new StringBuffer("");
			List<Annotation> list = Arrays.asList(value.getMethod().getAnnotations());
			list.forEach(item -> {
				if (item instanceof org.springframework.web.bind.annotation.RequestMapping) {
					RequestMapping annotations = (RequestMapping)item;
					//uri.append(Arrays.asList(annotation.value()));
					Arrays.asList(annotations.value()).forEach(annotation -> {
						uri.append(annotation);
					});
					log.trace(key+" = "+annotations.value());
				}
				//else if (item instanceof io.swagger.annotations.ApiOperation) {
				//	ApiOperation annotation = (ApiOperation)item;
				//	swaggerHttpMehtod.append(annotation.httpMethod());
				//	swaggerValue.append(annotation.value());
				//}
			});
			StringBuffer roles = new StringBuffer("");
			vo.setUri(uri.toString());
			vo.setClassName(value.getBeanType().getSimpleName());
			vo.setSwaggerHttpMethod(swaggerHttpMehtod.toString());
			vo.setSwaggerValue(swaggerValue.toString());
			vo.setSecurityRole(roles.toString());

			resultList.add(vo);
		});
		Collections.sort(resultList);
		return resultList;
	}

	public List<SpringUri2VO> getUri2() throws Exception {
		List<SpringUri2VO> resultList = new ArrayList<SpringUri2VO>();
		Map<RequestMappingInfo, HandlerMethod> map = null;
		/*map = requestMapping.getHandlerMethods();
		List<Map<String, Object>> urlAndRolelist = namedParameterJdbcTemplate.queryForList(securedObjectDAO.getSqlRolesAndUrl(), new HashMap<String, String>());
		map.forEach((key, value) -> {
			SpringUri2VO vo = new SpringUri2VO();
			StringBuffer uri = new StringBuffer("");
			RequestMapping classAnnotation = value.getBeanType().getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
			if (classAnnotation != null) {
				if (classAnnotation.value().length > 0) {
					uri.append(classAnnotation.value()[0]);
					if (classAnnotation.value().length >= 2) {
						log.warn("Class-Lavel의 @RequestMapping의 value 값이 2개 이상인 것이 있습니다. 해당 클래스를 확인해주세요. {}" + value.getClass().getSimpleName());
					}
				}
			}
			StringBuffer swaggerHttpMehtod = new StringBuffer("");
			StringBuffer swaggerValue = new StringBuffer("");
			List<Annotation> list = Arrays.asList(value.getMethod().getAnnotations());
			list.forEach(item -> {
				if (item instanceof org.springframework.web.bind.annotation.RequestMapping) {
					RequestMapping annotations = (RequestMapping)item;
					//uri.append(Arrays.asList(annotation.value()));
					Arrays.asList(annotations.value()).forEach(annotation -> {
						uri.append(annotation);
					});
					log.trace(key+" = "+annotations.value());
				} else if (item instanceof io.swagger.annotations.ApiOperation) {
					ApiOperation annotation = (ApiOperation)item;
					swaggerHttpMehtod.append(annotation.httpMethod());
					swaggerValue.append(annotation.value());
				}
			});
			StringBuffer roles = new StringBuffer("");
			urlAndRolelist.forEach(urlAndRole -> {
				if (Pattern.matches(urlAndRole.get("url").toString(), uri.toString())) {
					if (roles.toString().equals("")) {
						roles.append(urlAndRole.get("comUriAuthorityMgr").toString() + " ("+urlAndRole.get("url").toString()+")");
					} else {
						roles.append(", "+ urlAndRole.get("comUriAuthorityMgr").toString() + " ("+urlAndRole.get("url").toString()+")");
					}
				}
			});
			if (uri.toString().startsWith("/api/sys/apm/") || uri.toString().startsWith("/swagger")) {
				return;
			}
			vo.setUri(uri.toString());
			vo.setSwaggerValue(swaggerValue.toString());
			resultList.add(vo);
		});*/
		Collections.sort(resultList);
		return resultList;
	}

	private List<MappedStatement> getSqlmapEntry() {
		List<MappedStatement> result = new ArrayList<MappedStatement>();

		ComSqlSessionFactory bean = rootContext.getBean(ComSqlSessionFactory.class);
		SqlSessionFactory proxy = (SqlSessionFactory) Proxy.newProxyInstance(
				SqlSessionFactory.class.getClassLoader(),
				new Class[]{SqlSessionFactory.class},
				new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						// log.debug("method.getName() : " + method.getName());
						return method.invoke(bean.getObject(), args);
					}
				});

		Set<MappedStatement> list = null;
		try {
			list = proxy.getConfiguration().getMappedStatements().stream().collect(Collectors.toSet());
		} catch(Exception e) {
			e.printStackTrace();
		}
		Iterator<MappedStatement> iter = list.iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof MappedStatement) {
				result.add((MappedStatement)obj);
			}
		}
		result.sort((Comparator<? super MappedStatement>) (MappedStatement m1, MappedStatement m2) -> m1.getId().compareTo(m2.getId()));

		return result;

	}
	public List<String> mapperList() {
		List<String> result = new ArrayList<String>();
		int seq = 1;
		String resourceFile = null;
		Iterator<MappedStatement> iter = getSqlmapEntry().iterator();

		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof MappedStatement) {
				MappedStatement item = (MappedStatement)obj;
				try {
					String file = item.getResource();
					if (!file.equals(resourceFile)) {
						resourceFile = file;
						//System.err.println(String.format("##########"));
						//System.err.println(String.format("### %s ###", resourceFile));
						//System.err.println(String.format("##########"));
						//System.err.println("");
					}
					//System.err.print(String.format("[%04d] ", seq));
					String sqlId = item.getId();
					//System.err.print(String.format("SQL_ID=%s", sqlId));
					result.add(sqlId);
					SqlSource sqlSource = item.getSqlSource();
					String sqlText = sqlSource.getBoundSql(null).getSql();
					//System.err.println(String.format("%sSQLTEXT=%s", KLogSysLayout.lineSeparator, sqlText));
				} catch(NullPointerException e) {
					Object rootSqlNode = KObjectUtil.getValue(((SqlSource) item.getSqlSource()), "rootSqlNode");
					ArrayList ls = (ArrayList)KObjectUtil.getValue(rootSqlNode, "contents");
					StringBuffer sbuf = new StringBuffer();
					for (int k=0; k<ls.size(); k++) {
						if (ls.get(k) instanceof org.apache.ibatis.scripting.xmltags.StaticTextSqlNode) {
							StaticTextSqlNode node = (StaticTextSqlNode)ls.get(k);
							String text = KStringUtil.nvl(KObjectUtil.getValue(node, "text"));
							sbuf.append(text);
							sbuf.append(KLogLayout.LINE);
						} else if (ls.get(k) instanceof org.apache.ibatis.scripting.xmltags.ForEachSqlNode) {
							//ForEachSqlNode node = (ForEachSqlNode)ls.get(k);
							sbuf.append("  *** FOREACHE SQL ***  ");
						} else if (ls.get(k) instanceof org.apache.ibatis.scripting.xmltags.IfSqlNode) {
							//IfSqlNode node = (IfSqlNode)ls.get(k);
							sbuf.append("  *** IF SQL ***  ");
						} else {
							sbuf.append("  *** OTHER SQL ***  ");
						}
					}
					//System.err.println(String.format("%sSQLTEXT=%s", KLogSysLayout.lineSeparator, sbuf.toString()));
				} catch(BuilderException e) {
					Object rootSqlNode = KObjectUtil.getValue(((SqlSource) item.getSqlSource()), "rootSqlNode");
					ArrayList ls = (ArrayList)KObjectUtil.getValue(rootSqlNode, "contents");
					StringBuffer sbuf = new StringBuffer();
					for (int k=0; k<ls.size(); k++) {
						if (ls.get(k) instanceof org.apache.ibatis.scripting.xmltags.StaticTextSqlNode) {
							StaticTextSqlNode node = (StaticTextSqlNode)ls.get(k);
							String text = KStringUtil.nvl(KObjectUtil.getValue(node, "text"));
							sbuf.append(text);
							sbuf.append(KLogLayout.LINE);
						} else if (ls.get(k) instanceof org.apache.ibatis.scripting.xmltags.ForEachSqlNode) {
							//ForEachSqlNode node = (ForEachSqlNode)ls.get(k);
							sbuf.append("  *** FOREACH SQL ***  ");
						} else if (ls.get(k) instanceof org.apache.ibatis.scripting.xmltags.IfSqlNode) {
							//IfSqlNode node = (IfSqlNode)ls.get(k);
							sbuf.append("  *** IF SQL ***  ");
						} else {
							sbuf.append("  *** OTHER SQL ***  ");
						}
					}
					//System.err.println(String.format("%sSQLTEXT=%s", KLogSysLayout.lineSeparator, sbuf.toString()));
				} catch(Exception e) {
					//System.err.println("");
					e.printStackTrace();
				}
				seq++;
			}
			//System.err.println("");
		}
		Collections.sort(result);
		return result;
	}

	public StringBuffer mapperSqltext() {
		StringBuffer result = new StringBuffer();
		Iterator<MappedStatement> iter = getSqlmapEntry().iterator();
		int seq = 1;
		String resourceFile = null;
		while (iter.hasNext()) {
			MappedStatement item = iter.next();
			try {
				String file = item.getResource();
				if (!file.equals(resourceFile)) {
					resourceFile = file;
					result.append(String.format("##########")+KLogLayout.LINE);
					result.append(String.format("### %s ###", resourceFile)+KLogLayout.LINE);
					result.append(String.format("##########")+KLogLayout.LINE);
					result.append(""+KLogLayout.LINE);
				}
				result.append(String.format("[%04d] ", seq));
				String sqlId = item.getId();
				result.append(String.format("SQL_ID=%s", sqlId));
				SqlSource sqlSource = item.getSqlSource();
				String sqlText = sqlSource.getBoundSql(null).getSql();
				result.append(String.format("%sSQLTEXT=%s", KLogLayout.LINE, sqlText)+KLogLayout.LINE);
			} catch(NullPointerException e) {
				Object rootSqlNode = KObjectUtil.getValue(((SqlSource) item.getSqlSource()), "rootSqlNode");
				ArrayList ls = (ArrayList)KObjectUtil.getValue(rootSqlNode, "contents");
				StringBuffer sbuf = new StringBuffer();
				for (int k=0; k<ls.size(); k++) {
					if (ls.get(k) instanceof org.apache.ibatis.scripting.xmltags.StaticTextSqlNode) {
						StaticTextSqlNode node = (StaticTextSqlNode)ls.get(k);
						String text = KStringUtil.nvl(KObjectUtil.getValue(node, "text"));
						sbuf.append(text);
						sbuf.append(KLogLayout.LINE);
					} else if (ls.get(k) instanceof org.apache.ibatis.scripting.xmltags.ForEachSqlNode) {
						//ForEachSqlNode node = (ForEachSqlNode)ls.get(k);
						sbuf.append("  *** FOREACHE SQL ***  ");
					} else if (ls.get(k) instanceof org.apache.ibatis.scripting.xmltags.IfSqlNode) {
						//IfSqlNode node = (IfSqlNode)ls.get(k);
						sbuf.append("  *** IF SQL ***  ");
					} else {
						sbuf.append("  *** OTHER SQL ***  ");
					}
				}
				result.append(String.format("%sSQLTEXT=%s", KLogLayout.LINE, sbuf.toString())+KLogLayout.LINE);
			} catch(BuilderException e) {
				Object rootSqlNode = KObjectUtil.getValue(((SqlSource) item.getSqlSource()), "rootSqlNode");
				ArrayList ls = (ArrayList)KObjectUtil.getValue(rootSqlNode, "contents");
				StringBuffer sbuf = new StringBuffer();
				for (int k=0; k<ls.size(); k++) {
					if (ls.get(k) instanceof org.apache.ibatis.scripting.xmltags.StaticTextSqlNode) {
						StaticTextSqlNode node = (StaticTextSqlNode)ls.get(k);
						String text = KStringUtil.nvl(KObjectUtil.getValue(node, "text"));
						sbuf.append(text);
						sbuf.append(KLogLayout.LINE);
					} else if (ls.get(k) instanceof org.apache.ibatis.scripting.xmltags.ForEachSqlNode) {
						//ForEachSqlNode node = (ForEachSqlNode)ls.get(k);
						sbuf.append("  *** FOREACH SQL ***  ");
					} else if (ls.get(k) instanceof org.apache.ibatis.scripting.xmltags.IfSqlNode) {
						//IfSqlNode node = (IfSqlNode)ls.get(k);
						sbuf.append("  *** IF SQL ***  ");
					} else {
						sbuf.append("  *** OTHER SQL ***  ");
					}
				}
				result.append(String.format("%sSQLTEXT=%s", KLogLayout.LINE, sbuf.toString())+KLogLayout.LINE);
			} catch(Exception e) {
				result.append(""+KLogLayout.LINE+"[ ERROR ]");
			}
			seq++;
			result.append(""+KLogLayout.LINE);
		}
		return result;
	}

	public List<SpringSecurityUriVO> getRoleAndUri() {
		List<SpringSecurityUriVO> resultList = new ArrayList<SpringSecurityUriVO>();
		Map<String, String> param = new HashMap<String, String>();
		param.put("siteTpcd", KProfile.SITE_TPCD);
		List<Map<String, Object>> list = namedParameterJdbcTemplate.queryForList(ComUriAuthorityMgr.CONFIG_SQL, param);
		list.forEach(item -> {
			SpringSecurityUriVO vo = new SpringSecurityUriVO();
			vo.setUri(item.get("URI").toString());
			vo.setSpringSecurityRole(item.get("AUTHORITY").toString());
			resultList.add(vo);
		});
		Collections.sort(resultList);
		return resultList;
	}

	public List<JavaEnvVariableVO> getEnv() {
		List<JavaEnvVariableVO> resultList = new ArrayList<JavaEnvVariableVO>();
		MutablePropertySources sources = ((org.springframework.core.env.AbstractEnvironment) environment).getPropertySources();
		sources.iterator().forEachRemaining(item -> {
			if (item instanceof SystemEnvironmentPropertySource) {
				SystemEnvironmentPropertySource sysEnv = (SystemEnvironmentPropertySource) item;
				sysEnv.getSource().forEach((key, value) -> {
					JavaEnvVariableVO vo = new JavaEnvVariableVO();
					vo.setSourceType("sys-env");
					vo.setKey(key);
					vo.setValue(value.toString());
					resultList.add(vo);
					log.trace(key + " = " + value);
				});
			} else if (item instanceof MapPropertySource) {
				MapPropertySource javaEnv = (MapPropertySource) item;
				javaEnv.getSource().forEach((key, value) -> {
					JavaEnvVariableVO vo = new JavaEnvVariableVO();
					vo.setSourceType("java-env");
					vo.setKey(key);
					vo.setValue(value.toString());
					resultList.add(vo);
					log.trace(key + " = " + value);
				});
			}
		});

		//Properties properties = System.getProperties();
		//properties.forEach((key, value) -> {
		//	JavaEnvVariableVO vo = new JavaEnvVariableVO();
		//	vo.setSourceType("properties");
		//	vo.setKey(key+"");
		//	vo.setValue(value+"");
		//	resultList.add(vo);
		//	log.trace(key + " = " + value);
		//});


		// context
		JavaEnvVariableVO vo = null;
		vo = new JavaEnvVariableVO();
		vo.setSourceType("spring");
		vo.setKey("rootContext.configLocations");
		//vo.setValue(Arrays.asList(((org.springframework.web.context.support.AnnotationConfigWebApplicationContext) rootContext).getConfigLocations())+"");
		//resultList.add(vo);

		vo = new JavaEnvVariableVO();
		vo.setSourceType("spring");
		vo.setKey("dispacherContext.configLocations");
		//vo.setValue(Arrays.asList(((org.springframework.web.context.support.AnnotationConfigWebApplicationContext) dispatcherContext).getConfigLocations())+"");
		//resultList.add(vo);

		Collections.sort(resultList);
		return resultList;
	}

	public List<JdbcDatasourceVO> getDataSource() {
		List<JdbcDatasourceVO> resultList = new ArrayList<JdbcDatasourceVO>();
		JdbcDatasourceVO vo = null;
		/*if (!(jndiObjectFactoryBean.getObject() instanceof org.apache.tomcat.dbcp.dbcp2.BasicDataSource)) {
			log.warn("jndi의 dataSource 객체가 BasicDataSource 타입이 아닙니다.");
			return resultList;
		}*/

		//BasicDataSource dataSource = (BasicDataSource) jndiObjectFactoryBean.getObject();
		BasicDataSource jndiObject = jndiObjectFactoryBean;
		//Object jndiObject = jndiObjectFactoryBean.getObject();
		//vo = new JdbcDatasourceVO();
		//vo.setKey("jndiName");
		//vo.setValue(""+jndiObjectFactoryBean.getJndiName());
		//resultList.add(vo);

		vo = new JdbcDatasourceVO();
		vo.setKey("closed");
		vo.setValue(""+KObjectUtil.getValue(jndiObject, "closed"));
		resultList.add(vo);

		vo = new JdbcDatasourceVO();
		vo.setKey("driverClassName");
		vo.setValue(""+KObjectUtil.getValue(jndiObject, "driverClassName"));
		resultList.add(vo);

		vo = new JdbcDatasourceVO();
		vo.setKey("initialSize");
		vo.setValue(""+KObjectUtil.getValue(jndiObject, "initialSize"));
		resultList.add(vo);

		vo = new JdbcDatasourceVO();
		vo.setKey("maxIdle");
		vo.setValue(""+KObjectUtil.getValue(jndiObject, "maxIdle"));
		resultList.add(vo);

		vo = new JdbcDatasourceVO();
		vo.setKey("maxTotal");
		vo.setValue(""+KObjectUtil.getValue(jndiObject, "maxTotal"));
		resultList.add(vo);

		vo = new JdbcDatasourceVO();
		vo.setKey("minIdle");
		vo.setValue(""+KObjectUtil.getValue(jndiObject, "minIdle"));
		resultList.add(vo);

		vo = new JdbcDatasourceVO();
		vo.setKey("password");
		vo.setValue(""+KObjectUtil.getValue(jndiObject, "password"));
		resultList.add(vo);

		vo = new JdbcDatasourceVO();
		vo.setKey("url");
		vo.setValue(""+KObjectUtil.getValue(jndiObject, "url"));
		resultList.add(vo);

		vo = new JdbcDatasourceVO();
		vo.setKey("username");
		vo.setValue(""+KObjectUtil.getValue(jndiObject, "username"));
		resultList.add(vo);

		vo = new JdbcDatasourceVO();
		vo.setKey("validationQuery");
		vo.setValue(""+KObjectUtil.getValue(jndiObject, "validationQuery"));
		resultList.add(vo);

		vo = new JdbcDatasourceVO();
		vo.setKey("evictionPolicyClassName");
		vo.setValue(""+KObjectUtil.getValue(jndiObject, "evictionPolicyClassName"));
		resultList.add(vo);

		Collections.sort(resultList);
		return resultList;
	}
}
