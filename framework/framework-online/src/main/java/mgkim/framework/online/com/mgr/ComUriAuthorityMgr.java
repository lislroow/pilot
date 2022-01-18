package mgkim.framework.online.com.mgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import mgkim.framework.core.annotation.KBean;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.exception.KExceptionHandler;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.core.util.KStringUtil;

@KBean(name = "url-authority 매핑")
public class ComUriAuthorityMgr implements FilterInvocationSecurityMetadataSource {
	
	private static final Logger log = LoggerFactory.getLogger(ComUriAuthorityMgr.class);

	final String BEAN_NAME = KObjectUtil.name(ComUriAuthorityMgr.class);

	public static final String CONFIG_SQL = ""
			+ "	SELECT /* mgkim.framework.online.com.mgr.ComUriAuthorityMgr.CONFIG_SQL */  "
			+ "			  A.URI_VAL   AS URI                                        "
			+ "			, C.ROLE_ID   AS AUTHORITY                                  "
			+ "	FROM	  MGCB301TM A /* MGCB_uri기본정보 */                        "
			+ "			, MGCB241TR B /* MGCB_uri권한그룹매핑 */                    "
			+ "			, MGCB221TR C /* MGCB_권한그룹설정 */                       "
			+ "	WHERE   A.APP_CD = :appCd                                         "
			+ "		AND A.APP_CD = B.APP_CD                                   "
			+ "		AND A.APP_CD = C.APP_CD                                   "
			+ "		AND A.USE_YN = 'Y'                                              "
			+ "		AND A.URI_ID = B.URI_ID                                         "
			+ "		AND B.RGRP_ID = C.RGRP_ID                                       "
			+ "	ORDER BY URI_PTRN_YN ASC                                            "
			+ "";

	private Map<RequestMatcher, List<ConfigAttribute>> uriAuthorityMap;

	@Autowired(required = true)
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String RESOURCE = "uri";
	private static final String AUTH_ID = "authority";

	public LinkedHashMap<RequestMatcher, List<ConfigAttribute>> init() throws KSysException {

		List<Map<String, Object>> resultList = null;
		try {
			KContext.resetSql();
			KContext.set(AttrKey.SQL_FILE, "mgkim.framework.core.session.SysUriAuthorityMgr.CONFIG_SQL");
			KContext.set(AttrKey.SQL_TEXT, CONFIG_SQL);
			Map<String, String> paramMap = new HashMap<String, String>();
			paramMap.put("appCd", KProfile.APP_CD);
			resultList = jdbcTemplate.queryForList(CONFIG_SQL, paramMap);
		} catch(Exception e) {
			KExceptionHandler.resolve(e);
			return null;
		}

		LinkedHashMap<RequestMatcher, List<ConfigAttribute>> result = new LinkedHashMap<RequestMatcher, List<ConfigAttribute>>();
		Iterator<Map<String, Object>> itr = resultList.iterator();
		String preUrl = null;
		try {
			while (itr.hasNext()) {
				Map<String, Object> row = itr.next();
				String url = (String) row.get(RESOURCE);
				String authority = KStringUtil.nvl(row.get(AUTH_ID));
				RegexRequestMatcher matcher = new RegexRequestMatcher(url, null);

				List<ConfigAttribute> authList = new LinkedList<ConfigAttribute>();
				if (url.equals(preUrl)) {
					List<ConfigAttribute> preAuthList = result.get(matcher);
					if (preAuthList == null) {
						preAuthList = new ArrayList<ConfigAttribute>();
					} else {
						preAuthList.iterator().forEachRemaining(authId -> {
							authList.add(authId);
						});
					}
				}
				authList.add(new SecurityConfig(authority));
				result.put(matcher, authList);
				preUrl = url;
			}
		} catch(Exception e) {
			throw new KSysException(KMessage.E5201, e);
		}
		return result;
	}

	@Override
	public Collection<ConfigAttribute> getAllConfigAttributes() {
		LinkedHashMap<RequestMatcher, List<ConfigAttribute>> loadedMap = null;
		try {
			loadedMap = init();
		} catch(KSysException e) {
			log.error(e.getText(), e);
			return null;
		}
		uriAuthorityMap = loadedMap;
		if (uriAuthorityMap == null) {
			log.warn(KMessage.get(KMessage.E5202));
			return null;
		}
		Set<ConfigAttribute> allAttributes = new HashSet<ConfigAttribute>();
		for (Map.Entry<RequestMatcher, List<ConfigAttribute>> entry : uriAuthorityMap.entrySet()) {
			allAttributes.addAll(entry.getValue());
		}
		return allAttributes;
	}

	public String getRequiredAuthority(HttpServletRequest request) {
		String result = null;
		List<String> requiredList = new ArrayList<String>();
		for (Map.Entry<RequestMatcher, List<ConfigAttribute>> entry : uriAuthorityMap.entrySet()) {
			if (entry.getKey().matches(request)) {
				List<ConfigAttribute> list =  (List<ConfigAttribute>) entry.getValue();
				int cnt = list.size();
				for (int i=0; i<cnt; i++) {
					ConfigAttribute a = list.get(i);
					if (!requiredList.contains(a.getAttribute())) {
						requiredList.add(a.getAttribute());
					}
				}
			}
		}
		result = String.join(", ", requiredList);
		return result;
	}

	@Override
	public List<ConfigAttribute> getAttributes(Object object) {
		final HttpServletRequest request = ((FilterInvocation) object).getRequest();
		List<ConfigAttribute> attrList = new LinkedList<ConfigAttribute>();
		for (Map.Entry<RequestMatcher, List<ConfigAttribute>> entry : uriAuthorityMap.entrySet()) {
			if (entry.getKey().matches(request)) {
				attrList.addAll(entry.getValue());
			}
		}
		return attrList;
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return FilterInvocation.class.isAssignableFrom(clazz);
	}

	public void reload() throws Exception {
		log.warn("************ VERY SECURED PROCESS [시작] ************");
		LinkedHashMap<RequestMatcher, List<ConfigAttribute>> reloadedMap = init();
		Iterator<Entry<RequestMatcher, List<ConfigAttribute>>> iterator = reloadedMap.entrySet().iterator();
		log.warn("`uri-authority` 정보를 다시 적재합니다. 적재 전 건수={}", uriAuthorityMap.size());
		long start = System.currentTimeMillis();
		uriAuthorityMap.clear();
		while (iterator.hasNext()) {
			Entry<RequestMatcher, List<ConfigAttribute>> entry = iterator.next();
			uriAuthorityMap.put(entry.getKey(), entry.getValue());
		}
		long finished = System.currentTimeMillis();
		log.warn("`uri-authority` 정보 적재를 마쳤습니다. 적재 후 건수={}, 적재 소요시간={} 밀리초", uriAuthorityMap.size(), (finished - start));
		log.warn("************ VERY SECURED PROCESS [종료] ************");
	}

}
