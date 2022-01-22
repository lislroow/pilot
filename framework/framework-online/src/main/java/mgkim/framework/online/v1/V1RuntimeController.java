package mgkim.framework.online.v1;

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
import java.util.function.Function;
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
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
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
import mgkim.framework.core.mgr.ComScheduleMgr;
import mgkim.framework.core.stereo.KScheduler;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.core.util.KStringUtil;
import mgkim.framework.online.com.mgr.ComUriAuthorityMgr;
import mgkim.framework.online.com.mybatis.KSqlSessionFactory;

@Api( tags = { KConstant.SWG_V1 } )
@RestController
public class V1RuntimeController {

	private static final Logger log = LoggerFactory.getLogger(V1RuntimeController.class);
	
	@Autowired
	private ApplicationContext springContext;
	
	@ApiOperation(value = "(실행환경) spring-bean 목록 조회")
	@RequestMapping(value = "/v1/runtime/spring-beans", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<Map<String, String>>> springBeans() throws Exception {
		KOutDTO<List<Map<String, String>>> outDTO = new KOutDTO<List<Map<String, String>>>();
		List<Map<String, String>> outBody = new ArrayList<Map<String, String>>();
		outBody = Arrays.stream(springContext.getBeanDefinitionNames())
			.filter(beanName -> !springContext.getBean(beanName).getClass().getName().startsWith("springfox."))
			.collect(ArrayList<Map<String, String>>::new,
					(list, beanName) -> {
						Map<String, String> map = new HashMap<String, String>();
						
						Object bean = springContext.getBean(beanName);
						String beanClass = bean.getClass().getName();
						if (beanClass.contains(".$")) {
							beanClass = bean.getClass().getInterfaces()[0].getCanonicalName(); 
						}
						map.put("beanClass", beanClass);
						String swaggerTags = null;
						swaggerTags = Arrays.asList(bean.getClass().getAnnotations()).stream()
							.filter(anno -> anno instanceof io.swagger.annotations.Api)
							.map(anno -> String.join(",", ((Api)anno).tags()))
							.collect(Collectors.joining(","));
						map.put("beanId", beanName);
						map.put("swaggerTags", swaggerTags);
						list.add(map);
					},
					ArrayList<Map<String, String>>::addAll
				);
		Collections.sort(outBody, Comparator.comparing(map -> map.get("beanClass")));
		outDTO.setBody(outBody);
		return outDTO;
	}
	
	@ApiOperation(value = "(실행환경) mybatis-mapper 목록 조회")
	@RequestMapping(value = "/v1/runtime/mybatis-mapper", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<String>> mybatisMapper() throws Exception {
		KOutDTO<List<String>> outDTO = new KOutDTO<List<String>>();

		List<String> outBody = new ArrayList<String>();
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
					outBody.add(sqlId);
					SqlSource sqlSource = item.getSqlSource();
					String sqlText = sqlSource.getBoundSql(null).getSql();
				} catch (NullPointerException e) {
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
				} catch (BuilderException e) {
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
				} catch (Exception e) {
					//System.err.println("");
					e.printStackTrace();
				}
				seq++;
			}
			//System.err.println("");
		}
		Collections.sort(outBody);
		outDTO.setBody(outBody);
		return outDTO;
	}
	
	private List<MappedStatement> getSqlmapEntry() {
		List<MappedStatement> result = new ArrayList<MappedStatement>();
		KSqlSessionFactory bean = springContext.getBean(KSqlSessionFactory.class);
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
		} catch (Exception e) {
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
	@RequestMapping(value = "/v1/runtime/mybatis-mapper-sqltext", method = RequestMethod.GET)
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
			} catch (NullPointerException e) {
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
			} catch (BuilderException e) {
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
			} catch (Exception e) {
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
		} catch (Exception e) {
			resp = new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return resp;
	}

	@ApiOperation(value = "(실행환경) spring-uri 목록 조회")
	@RequestMapping(value = "/v1/runtime/spring-uri", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<Map<String, String>>> springUri() throws Exception {
		KOutDTO<List<Map<String, String>>> outDTO = new KOutDTO<List<Map<String, String>>>();
		RequestMappingHandlerMapping requestMapping = springContext.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
		List<Map<String, String>> outBody = new ArrayList<Map<String, String>>();
		Map<RequestMappingInfo, HandlerMethod> mapping = requestMapping.getHandlerMethods();
		outBody = mapping.entrySet().stream()
			.filter(item -> !item.getValue().getBeanType().getName().startsWith("springfox."))
			.collect(ArrayList<Map<String, String>>::new,
					(list, entry) -> {
						HandlerMethod handleMethod = entry.getValue();
						Map<String, String> map = new HashMap<String, String>();
						
						// uri
						String _uri = Arrays.asList(handleMethod.getMethod().getAnnotations())
							.stream()
							.filter(anno -> anno instanceof org.springframework.web.bind.annotation.RequestMapping)
							.map(anno -> String.join(",", ((RequestMapping)anno).value()))
							.collect(Collectors.joining(","));
						if ("".equals(_uri)) {
							return;
						}
						map.put("uri", _uri);
						map.put("className", handleMethod.getBeanType().getSimpleName());
						list.add(map);
					},
					ArrayList<Map<String, String>>::addAll
				);
		Collections.sort(outBody, Comparator.comparing(map -> map.get("uri")));
		outDTO.setBody(outBody);
		return outDTO;
	}
	
	@ApiOperation(value = "(실행환경) spring-security-uri 목록 조회")
	@RequestMapping(value = "/v1/runtime/spring-security-uri", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<Map<String, Object>>> springSecurityUri() throws Exception {
		KOutDTO<List<Map<String, Object>>> outDTO = new KOutDTO<List<Map<String, Object>>>();
		DataSource dataSource = springContext.getBean(DataSource.class);
		NamedParameterJdbcTemplate namedJdbc = new NamedParameterJdbcTemplate(dataSource);
		Map<String, String> param = new HashMap<String, String>();
		param.put("appCd", KProfile.APP_CD);
		List<Map<String, Object>> list = namedJdbc.queryForList(ComUriAuthorityMgr.CONFIG_SQL, param);
		
		List<Map<String, Object>> outBody = null;
		outBody = list.stream().map(item -> item.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toLowerCase(), e -> e.getValue())))
					.collect(Collectors.toList());
		Collections.sort(outBody, Comparator.comparing(map -> KStringUtil.nvl(map.get("uri"))));
		outDTO.setBody(outBody);
		return outDTO;
	}
	
	@ApiOperation(value = "(실행환경) properties 목록 조회")
	@RequestMapping(value = "/v1/runtime/properties", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<Map<String, Object>>> javaEnvVariable() throws Exception {
		KOutDTO<List<Map<String, Object>>> outDTO = new KOutDTO<List<Map<String, Object>>>();
		
		org.springframework.core.env.Environment environment = springContext.getBean(org.springframework.core.env.Environment.class);
		
		MutablePropertySources sources = ((AbstractEnvironment) environment).getPropertySources();
		List<Map<String, Object>> outBody = null;
		outBody = (sources.stream()
					.filter(ps -> ps instanceof EnumerablePropertySource)
					.collect(Collectors.toMap(e -> e.getClass().getSimpleName(), Function.identity())))
				.entrySet().stream()
				.collect(ArrayList<Map<String, Object>>::new,
					(list, prop) -> {
						Object clazz = prop.getKey();
						List<Map<String, Object>> smap = null;
						smap = Arrays.stream(((EnumerablePropertySource) prop.getValue()).getPropertyNames())
								.filter(sprop -> !"java.class.path".equals(sprop))
								.collect(ArrayList<Map<String, Object>>::new,
									(slist, sprop) -> {
										Map<String, Object> map = new HashMap<String, Object>();
										map.put("beanClass", clazz);
										map.put("key", sprop);
										map.put("value", environment.getProperty(sprop));
										list.add(map);
									},
									ArrayList::addAll);
						list.addAll(smap);
					},
					ArrayList::addAll);
		outDTO.setBody(outBody);
		return outDTO;
	}
	
	@ApiOperation(value = "(실행환경) jdbc-datasource 목록 조회")
	@RequestMapping(value = "/v1/runtime/jdbc-datasource", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<Map<String, String>>> jdbcDatasource() throws Exception {
		KOutDTO<List<Map<String, String>>> outDTO = new KOutDTO<List<Map<String, String>>>();
		BasicDataSource dataSource = springContext.getBean(BasicDataSource.class);
		if (dataSource != null) {
			List<Map<String, String>> outBody = null;
			outBody = Arrays.stream(BasicDataSource.class.getDeclaredFields())
					.filter(field -> !"connectionPool".equals(field.getName()))
					.collect(ArrayList<Map<String, String>>::new,
						(list, field) -> {
							Map<String, String> map = new HashMap<String, String>();
							map.put("key", field.getName());
							map.put("value", KStringUtil.nvl(KObjectUtil.getValue(dataSource, field.getName())));
							list.add(map);
						},
						ArrayList::addAll);
			outDTO.setBody(outBody);
		}
		return outDTO;
	}
	
	@ApiOperation(value = "(scheduler) 스케줄러 현황")
	@RequestMapping(value = "/v1/scheduler", method = RequestMethod.GET)
	public @ResponseBody KOutDTO<List<Map<String, String>>> scheduler() throws Exception {
		KOutDTO<List<Map<String, String>>> outDTO = new KOutDTO<List<Map<String, String>>>();
		
		ComScheduleMgr comScheduleMgr = springContext.getBean(ComScheduleMgr.class);
		if (comScheduleMgr != null) {
			List<KScheduler> scheduleList = comScheduleMgr.getScheduleList();
			if (scheduleList == null) {
				return outDTO;
			}
			List<Map<String, String>> outBody = null;
			outBody = scheduleList.stream()
					.collect(ArrayList<Map<String, String>>::new, 
							(list, item) -> {
								Map map = new HashMap();
								map.put("clazz", item.getClass().getTypeName());
								map.put("name", KObjectUtil.name(item.getClass()));
								map.put("interval", KObjectUtil.interval(item.getClass()));
								map.put("managed", KObjectUtil.manage(item.getClass()));
								//map.put("enabled", item.enabled);
								map.put("running", item.isRunning());
								map.put("uptime", item.uptime());
								map.put("lastStartedTime", item.getLastStartedTime());
								map.put("lastStoppedTime", item.getLastStoppedTime());
								map.put("lastExecutedTime", item.getLastExecutedTime());
								list.add(map);
							}, 
							ArrayList::addAll);
			outDTO.setBody(outBody);
		}
		return outDTO;
	}
	
}
