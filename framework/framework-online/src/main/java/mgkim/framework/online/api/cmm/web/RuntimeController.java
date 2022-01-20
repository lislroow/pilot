package mgkim.framework.online.api.cmm.web;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import mgkim.framework.core.dto.KOutDTO;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.online.com.mgr.ComUriAuthorityMgr;
import mgkim.framework.online.com.mybatis.ComSqlSessionFactory;

@Api( tags = { KConstant.SWG_SYSTEM_MANAGEMENT } )
@RestController
public class RuntimeController {

	private static final Logger log = LoggerFactory.getLogger(RuntimeController.class);
	
	@Autowired
	private ApplicationContext springContext;
	
	@Autowired
	@Qualifier(value = "requestMappingHandlerMapping")
	private RequestMappingHandlerMapping requestMapping;
	
	private NamedParameterJdbcTemplate namedJdbc;
	
	@Autowired
	private Environment environment;
	
	@Autowired(required = false)
	private BasicDataSource jndiObjectFactoryBean;
	
	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.namedJdbc = new NamedParameterJdbcTemplate(dataSource);
	}
	
	@ApiOperation(value = "(실행환경) spring-bean 목록 조회")
	@RequestMapping(value = "/api/adm/runtime/spring-beans", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<Map>> springBeans() throws Exception {
		KOutDTO<List<Map>> outDTO = new KOutDTO<List<Map>>();
		List<Map> listVO = new ArrayList<Map>();
		Arrays.stream(springContext.getBeanDefinitionNames())
		.forEach(beanName -> {
			Map vo = new HashMap();
			Object bean = springContext.getBean(beanName);
			String beanClass = bean.getClass().getName();
			if (beanClass.startsWith("springfox.")) {
				return;
			}
			if (beanClass.contains(".$")) {
				beanClass = bean.getClass().getInterfaces()[0].getCanonicalName();
			}
			vo.put("beanClass", beanClass);
			List<Annotation> annotations = Arrays.asList(bean.getClass().getAnnotations());
			StringBuffer swaggerTags = new StringBuffer("");
			annotations.forEach(annotation -> {
				if (annotation instanceof io.swagger.annotations.Api) {
					Api api = (Api) annotation;
					Arrays.asList(api.tags()).forEach(tag -> {
						if (swaggerTags.toString().equals("")) {
							swaggerTags.append(tag);
						} else {
							swaggerTags.append(", "+ tag);
						}
					});
				}
			});
			vo.put("applicationContextId", springContext.getId());
			vo.put("beanId", beanName);
			vo.put("swaggerTags", swaggerTags.toString());
			listVO.add(vo);
			log.trace(beanName + " = " + bean.getClass().getCanonicalName() + " = " + annotations);
		});
		outDTO.setBody(listVO);
		return outDTO;
	}
	
	@ApiOperation(value = "(실행환경) mybatis-mapper 목록 조회")
	@RequestMapping(value = "/api/adm/runtime/mybatis-mapper", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<String>> mybatisMapper() throws Exception {
		KOutDTO<List<String>> outDTO = new KOutDTO<List<String>>();

		List<String> listVO = new ArrayList<String>();
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
					}
					String sqlId = item.getId();
					listVO.add(sqlId);
					SqlSource sqlSource = item.getSqlSource();
					String sqlText = sqlSource.getBoundSql(null).getSql();
				} catch(NullPointerException e) {
					Object rootSqlNode = KObjectUtil.getValue(((SqlSource) item.getSqlSource()), "rootSqlNode");
					ArrayList ls = (ArrayList)KObjectUtil.getValue(rootSqlNode, "contents");
					StringBuffer sbuf = new StringBuffer();
					for (int k=0; k<ls.size(); k++) {
						if (ls.get(k) instanceof org.apache.ibatis.scripting.xmltags.StaticTextSqlNode) {
							StaticTextSqlNode node = (StaticTextSqlNode)ls.get(k);
							String text = KStringUtil.nvl(KObjectUtil.getValue(node, "text"));
							sbuf.append(text);
							sbuf.append(KConstant.LINE);
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
							sbuf.append(KConstant.LINE);
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
		Collections.sort(listVO);
		outDTO.setBody(listVO);
		return outDTO;
	}
	
	private List<MappedStatement> getSqlmapEntry() {
		List<MappedStatement> result = new ArrayList<MappedStatement>();
		ComSqlSessionFactory bean = springContext.getBean(ComSqlSessionFactory.class);
		SqlSessionFactory proxy = (SqlSessionFactory) Proxy.newProxyInstance(
				SqlSessionFactory.class.getClassLoader(),
				new Class[]{SqlSessionFactory.class},
				new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
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

	@ApiOperation(value = "(실행환경) mybatis-mapper-sqltext 조회")
	@RequestMapping(value = "/api/adm/runtime/mybatis-mapper-sqltext", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<byte[]> mybatisMapperSqltext() throws Exception {
		ResponseEntity<byte[]> resp = null;
		
		StringBuffer respText = new StringBuffer();
		Iterator<MappedStatement> iter = getSqlmapEntry().iterator();
		int seq = 1;
		String resourceFile = null;
		while (iter.hasNext()) {
			MappedStatement item = iter.next();
			try {
				String file = item.getResource();
				if (!file.equals(resourceFile)) {
					resourceFile = file;
					respText.append(String.format("##########")+KConstant.LINE);
					respText.append(String.format("### %s ###", resourceFile)+KConstant.LINE);
					respText.append(String.format("##########")+KConstant.LINE);
					respText.append(""+KConstant.LINE);
				}
				respText.append(String.format("[%04d] ", seq));
				String sqlId = item.getId();
				respText.append(String.format("SQL_ID=%s", sqlId));
				SqlSource sqlSource = item.getSqlSource();
				String sqlText = sqlSource.getBoundSql(null).getSql();
				respText.append(String.format("%sSQLTEXT=%s", KConstant.LINE, sqlText)+KConstant.LINE);
			} catch(NullPointerException e) {
				Object rootSqlNode = KObjectUtil.getValue(((SqlSource) item.getSqlSource()), "rootSqlNode");
				ArrayList ls = (ArrayList)KObjectUtil.getValue(rootSqlNode, "contents");
				StringBuffer sbuf = new StringBuffer();
				for (int k=0; k<ls.size(); k++) {
					if (ls.get(k) instanceof org.apache.ibatis.scripting.xmltags.StaticTextSqlNode) {
						StaticTextSqlNode node = (StaticTextSqlNode)ls.get(k);
						String text = KStringUtil.nvl(KObjectUtil.getValue(node, "text"));
						sbuf.append(text);
						sbuf.append(KConstant.LINE);
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
				respText.append(String.format("%sSQLTEXT=%s", KConstant.LINE, sbuf.toString())+KConstant.LINE);
			} catch(BuilderException e) {
				Object rootSqlNode = KObjectUtil.getValue(((SqlSource) item.getSqlSource()), "rootSqlNode");
				ArrayList ls = (ArrayList)KObjectUtil.getValue(rootSqlNode, "contents");
				StringBuffer buf = new StringBuffer();
				for (int k=0; k<ls.size(); k++) {
					if (ls.get(k) instanceof org.apache.ibatis.scripting.xmltags.StaticTextSqlNode) {
						StaticTextSqlNode node = (StaticTextSqlNode)ls.get(k);
						String text = KStringUtil.nvl(KObjectUtil.getValue(node, "text"));
						buf.append(text);
						buf.append(KConstant.LINE);
					} else if (ls.get(k) instanceof org.apache.ibatis.scripting.xmltags.ForEachSqlNode) {
						//ForEachSqlNode node = (ForEachSqlNode)ls.get(k);
						buf.append("  *** FOREACH SQL ***  ");
					} else if (ls.get(k) instanceof org.apache.ibatis.scripting.xmltags.IfSqlNode) {
						//IfSqlNode node = (IfSqlNode)ls.get(k);
						buf.append("  *** IF SQL ***  ");
					} else {
						buf.append("  *** OTHER SQL ***  ");
					}
				}
				respText.append(String.format("%sSQLTEXT=%s", KConstant.LINE, buf.toString())+KConstant.LINE);
			} catch(Exception e) {
				respText.append(""+KConstant.LINE+"[ ERROR ]");
			}
			seq++;
			respText.append(""+KConstant.LINE);
		}
		
		try {
			HttpHeaders headers = new HttpHeaders();
			//headers.setContentType(MediaType.TEXT_PLAIN);
			headers.set("Content-Type", "text/plain; charset=utf-8");
			resp = new ResponseEntity<byte[]>(respText.toString().getBytes(), headers, HttpStatus.OK);
		} catch(Exception e) {
			resp = new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return resp;
	}

	@ApiOperation(value = "(실행환경) spring-uri 목록 조회")
	@RequestMapping(value = "/api/adm/runtime/spring-uri", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<Map>> springUri() throws Exception {
		KOutDTO<List<Map>> outDTO = new KOutDTO<List<Map>>();
		
		List<Map> listVO = new ArrayList<Map>();
		Map<RequestMappingInfo, HandlerMethod> map = null;
		map = requestMapping.getHandlerMethods();
		map.forEach((key, value) -> {
			Map vo = new HashMap();
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
					Arrays.asList(annotations.value()).forEach(annotation -> {
						uri.append(annotation);
					});
					log.trace(key+" = "+annotations.value());
				}
			});
			StringBuffer roles = new StringBuffer("");
			vo.put("uri", uri.toString());
			vo.put("className", value.getBeanType().getSimpleName());
			vo.put("swaggerHttpMethod", swaggerHttpMehtod.toString());
			vo.put("swaggerValue", swaggerValue.toString());
			vo.put("securityRole", roles.toString());
			
			listVO.add(vo);
		});
		
		outDTO.setBody(listVO);
		return outDTO;
	}

	@ApiOperation(value = "(실행환경) spring-security-uri 목록 조회")
	@RequestMapping(value = "/api/adm/runtime/spring-security-uri", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<Map<String, Object>>> springSecurityUri() throws Exception {
		KOutDTO<List<Map<String, Object>>> outDTO = new KOutDTO<List<Map<String, Object>>>();

		List<Map<String, Object>> listVO = null;
		Map<String, String> param = new HashMap<String, String>();
		param.put("appCd", KProfile.APP_CD);
		List<Map<String, Object>> list = namedJdbc.queryForList(ComUriAuthorityMgr.CONFIG_SQL, param);
		listVO = list.stream().map(
				item -> item.entrySet()
					.stream()
					.collect(Collectors.toMap(e -> e.getKey().toLowerCase(), Map.Entry::getValue)))
				.collect(Collectors.toList());
		outDTO.setBody(listVO);
		return outDTO;
	}

	@ApiOperation(value = "(실행환경) java-env-variable 목록 조회")
	@RequestMapping(value = "/api/adm/runtime/java-env-variable", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<Map>> javaEnvVariable() throws Exception {
		KOutDTO<List<Map>> outDTO = new KOutDTO<List<Map>>();
		List<Map> listVO = new ArrayList<Map>();
		MutablePropertySources sources = ((org.springframework.core.env.AbstractEnvironment) environment).getPropertySources();
		sources.iterator().forEachRemaining(item -> {
			if (item instanceof SystemEnvironmentPropertySource) {
				SystemEnvironmentPropertySource sysEnv = (SystemEnvironmentPropertySource) item;
				sysEnv.getSource().forEach((key, value) -> {
					Map vo = new HashMap();
					vo.put("sourceType", "sys-env");
					vo.put("key", key);
					vo.put("value", value.toString());
					listVO.add(vo);
					log.trace(key + " = " + value);
				});
			} else if (item instanceof MapPropertySource) {
				MapPropertySource javaEnv = (MapPropertySource) item;
				javaEnv.getSource().forEach((key, value) -> {
					Map vo = new HashMap();
					vo.put("sourceType", "java-env");
					vo.put("key", key);
					vo.put("value", value.toString());
					listVO.add(vo);
					log.trace(key + " = " + value);
				});
			}
		});
		outDTO.setBody(listVO);
		return outDTO;
	}

	@ApiOperation(value = "(실행환경) jdbc-datasource 목록 조회")
	@RequestMapping(value = "/api/adm/runtime/jdbc-datasource", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<Map>> jdbcDatasource() throws Exception {
		KOutDTO<List<Map>> outDTO = new KOutDTO<List<Map>>();

		List<Map> listVO = new ArrayList<Map>();
		Map vo = null;
		
		BasicDataSource jndiObject = jndiObjectFactoryBean;

		vo = new HashMap();
		vo.put("key", "closed");
		vo.put("value", ""+KObjectUtil.getValue(jndiObject, "closed"));
		listVO.add(vo);

		vo = new HashMap();
		vo.put("key", "driverClassName");
		vo.put("value", ""+KObjectUtil.getValue(jndiObject, "driverClassName"));
		listVO.add(vo);

		vo = new HashMap();
		vo.put("key", "initialSize");
		vo.put("value", ""+KObjectUtil.getValue(jndiObject, "initialSize"));
		listVO.add(vo);

		vo = new HashMap();
		vo.put("key", "maxIdle");
		vo.put("value", ""+KObjectUtil.getValue(jndiObject, "maxIdle"));
		listVO.add(vo);

		vo = new HashMap();
		vo.put("key", "maxTotal");
		vo.put("value", ""+KObjectUtil.getValue(jndiObject, "maxTotal"));
		listVO.add(vo);

		vo = new HashMap();
		vo.put("key", "minIdle");
		vo.put("value", ""+KObjectUtil.getValue(jndiObject, "minIdle"));
		listVO.add(vo);

		vo = new HashMap();
		vo.put("key", "password");
		vo.put("value", ""+KObjectUtil.getValue(jndiObject, "password"));
		listVO.add(vo);

		vo = new HashMap();
		vo.put("key", "url");
		vo.put("value", ""+KObjectUtil.getValue(jndiObject, "url"));
		listVO.add(vo);

		vo = new HashMap();
		vo.put("key", "username");
		vo.put("value", ""+KObjectUtil.getValue(jndiObject, "username"));
		listVO.add(vo);

		vo = new HashMap();
		vo.put("key", "validationQuery");
		vo.put("value", ""+KObjectUtil.getValue(jndiObject, "validationQuery"));
		listVO.add(vo);

		vo = new HashMap();
		vo.put("key", "evictionPolicyClassName");
		vo.put("value", ""+KObjectUtil.getValue(jndiObject, "evictionPolicyClassName"));
		listVO.add(vo);
	
		outDTO.setBody(listVO);
		return outDTO;
	}
}
