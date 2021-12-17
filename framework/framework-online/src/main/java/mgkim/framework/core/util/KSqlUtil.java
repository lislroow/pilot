package mgkim.framework.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;

import mgkim.framework.core.dto.KCmmVO;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSqlException;
import mgkim.framework.core.logging.KLogCommon;
import mgkim.framework.core.logging.KLogLayout;
import mgkim.framework.core.logging.KLogSql;
import mgkim.framework.core.type.TSqlType;

public class KSqlUtil {

	public static final String COUNT_SQL = "SELECT COUNT(*) FROM (\n\t%s\n) TB";
	public static final String PAGING_SQL = "SELECT * FROM ( SELECT rownum rn, (?+1)-rownum rnum, TB.* FROM (\n\t%s\n) TB ) WHERE rn BETWEEN ? AND ?";

	public static final int PAGING_RECORD_COUNT_PER_PAGE = 10;
	public static final int PAGING_PAGE_SIZE = 10;


	private static final String PARAM_CHAR = "\\?";
	private static final String PARAM_TEMP_CHAR = "\\Ξ"; // SQL 파라미터의 값에 '?' 문자가 있을 수 있으므로 prepareStatement의 파라미터 문자인 '?'을  'Ξ' 로 치환

	public static final String TABLE_PATTERN = "[A-Z]{4}[0-9]{3}[A-Z]{2}";

	public static String getRelativePath(String resourcePath) {
		String path = new java.io.File(resourcePath.replaceAll("file \\[(.*)\\]", "$1")).getAbsolutePath()
				.replace(new java.io.File(KConstant.PATH_WEBINF_CLASSES).getAbsolutePath(), "")
				.replaceAll("\\\\", "/").substring(1);
		final String injar = ".jar!/";
		if(path.indexOf(injar) > 0) {
			path = path.substring(path.indexOf(injar)+injar.length());
			path = "(jar) " + path;
		}
		return path;
	}

	public static String createParamSql(Object paramObject, MappedStatement mappedStatement, TSqlType paramSqlType) throws Exception {
		// 준비
		BoundSql boundSql = mappedStatement.getBoundSql(paramObject);
		String paramSql = boundSql.getSql();
		Configuration configuration = mappedStatement.getConfiguration();
		String sqlId = mappedStatement.getId();
		String sqlFile = KSqlUtil.getRelativePath(mappedStatement.getResource());
		// -- 준비

		// param-sql 생성
		{
			try {
				paramSql = paramSql.replaceAll(PARAM_CHAR, PARAM_TEMP_CHAR);
				if(paramObject == null) {
					// null 타입
					paramSql = paramSql.replaceAll(PARAM_TEMP_CHAR, "''");
				} else if(paramObject instanceof Map) {
					// Map 타입
					List<Object> entryParamsValueList = getParameters(configuration, boundSql, paramObject);
					List<ParameterMapping> entryParamKeyList = boundSql.getParameterMappings();
					if(entryParamsValueList.size() != entryParamKeyList.size()) {
						throw new KSqlException(KMessage.E8007, sqlId);
					}
					for(int i=0; i<entryParamKeyList.size(); i++) {
						Object value = entryParamsValueList.get(i);
						if(value == null) {
							continue;
						}
						if(String.class.isInstance(value)) {
							// `value` 에 `정규식에서 사용되는 특수문자`를 제거 합니다.
							String quoteStr = Matcher.quoteReplacement(KStringUtil.nvl(value));
							quoteStr = String.format("'%s'", quoteStr);
							paramSql = Pattern.compile(PARAM_TEMP_CHAR).matcher(paramSql).replaceFirst(quoteStr);
						} else {
							paramSql = paramSql.replaceFirst(PARAM_TEMP_CHAR, KStringUtil.nvl(value));
						}
					}
				} else if(paramObject instanceof String) {
					// String 타입
					String val = String.format("'%s'", paramObject);

					// `value` 에 `정규식에서 사용되는 특수문자`를 제거 합니다.
					String quoteStr = Matcher.quoteReplacement(val);

					paramSql = Pattern.compile(PARAM_TEMP_CHAR).matcher(paramSql).replaceAll(quoteStr);
				} else {
					// VO 타입
					List<ParameterMapping> entryParamKeyList = boundSql.getParameterMappings();
					for(ParameterMapping mapping : entryParamKeyList) {
						String propKey = mapping.getProperty();
						String val = KObjectUtil.getSqlParamByFieldName(paramObject, propKey);

						// `value` 에 `정규식에서 사용되는 특수문자`를 제거 합니다.
						String quoteStr = Matcher.quoteReplacement(val);

						// `value` 에 "$" 혹은 "\" 문자가 제거된 값으로 replace를 합니다.
						paramSql = Pattern.compile(PARAM_TEMP_CHAR).matcher(paramSql).replaceFirst(quoteStr);
					}
				}
				paramSql = paramSql.replaceAll(PARAM_TEMP_CHAR, PARAM_CHAR);
			} catch(NullPointerException e) {
				KLogSql.error(String.format("param-sql을 생성하는 중 오류가 발생했습니다. `%s`", sqlId), e);
			} catch(Exception e) {
				KLogSql.error(String.format("param-sql을 생성하는 중 오류가 발생했습니다. `%s`", sqlId), e);
			}
		} // -- param-sql 생성


		// param-sql 로깅
		{
			paramSql = paramSql.replaceAll("\n\t\t", "\n");
			switch(paramSqlType) {
			case ORIGINAL_SQL:
				KLogSql.warn("{} `{}` {}{} `{}` `{}`{}{}", KConstant.LT_SQL, sqlId, KLogLayout.LINE, KConstant.LT_SQL, sqlFile, sqlId, KLogLayout.LINE, paramSql);
				break;
			case PAGING_SQL:
				KCmmVO vo = (KCmmVO) paramObject;
				paramSql = paramSql.replaceAll("\n", "\n\t");
				paramSql = String.format(KSqlUtil.PAGING_SQL, paramSql);
				paramSql = paramSql.replaceFirst("\\?", vo.get_rowcount()+"")
						.replaceFirst("\\?", vo.get_startrow()+"")
						.replaceFirst("\\?", vo.get_endrow()+"");
				KLogSql.warn("{} `{}` {}{} `{}` `{}`{}{}", KConstant.LT_SQL_PAING, sqlId, KLogLayout.LINE, KConstant.LT_SQL_PAING, sqlFile, sqlId, KLogLayout.LINE, paramSql);
				break;
			case COUNT1_SQL:
				paramSql = paramSql.replaceAll("\n", "\n\t");
				paramSql = String.format(KSqlUtil.COUNT_SQL, paramSql);
				KLogSql.warn("{} `{}` {}{} `{}` `{}`{}{}", KConstant.LT_SQL_COUNT1, sqlId, KLogLayout.LINE, KConstant.LT_SQL_COUNT1, sqlFile, sqlId, KLogLayout.LINE, paramSql);
				break;
			case COUNT2_SQL:
				paramSql = paramSql.replaceAll("\n", "\n\t");
				paramSql = String.format(KSqlUtil.COUNT_SQL, paramSql);
				KLogSql.warn("{} `{}` {}{} `{}` `{}`{}{}", KConstant.LT_SQL_COUNT2, sqlId, KLogLayout.LINE, KConstant.LT_SQL_COUNT2, sqlFile, sqlId, KLogLayout.LINE, paramSql);
				break;
			case COUNT3_SQL:
				KLogSql.warn("{} `{}` {}{} `{}` `{}`{}{}", KConstant.LT_SQL_COUNT3, sqlId, KLogLayout.LINE, KConstant.LT_SQL_COUNT3, sqlFile, sqlId, KLogLayout.LINE, paramSql);
				break;
			}
		}
		// -- param-sql 로깅

		// param-sql 저장
		{
			// 예외 발생 시 시스템 로깅 및 응답에 포함시키기 위해 저장함
			KContext.set(AttrKey.SQL_TEXT, KStringUtil.replaceWhiteSpace(paramSql));
		}
		// -- param-sql 저장

		return paramSql;
	}

	public static List<Object> getParameters(Configuration configuration, BoundSql boundSql, Object mehtodParam) {
		List<Object> paramList = new ArrayList<Object>();
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		if(parameterMappings != null) {
			for(int i=0; i<parameterMappings.size(); i++) {
				ParameterMapping parameterMapping = parameterMappings.get(i);
				if(parameterMapping.getMode() != ParameterMode.OUT) {
					Object value = null;
					String propertyName = parameterMapping.getProperty();
					if(boundSql.hasAdditionalParameter(propertyName)) {
						value = boundSql.getAdditionalParameter(propertyName);
					} else {
						MetaObject metaObject = configuration.newMetaObject(mehtodParam);
						value = metaObject.getValue(propertyName);
					}
					paramList.add(value);
				}
			}
		}
		return paramList;
	}


	public static String removeOrderBy(String sql) {
		String ORDER_BY_REGEX = "(.*order\\s*by[\\w|\\s|,|\\.|\\/|\\*|가-힣]*)";
		Pattern p = Pattern.compile(ORDER_BY_REGEX, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(sql);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String str = m.group(0);
			if(Pattern.compile("partition\\s*by", Pattern.CASE_INSENSITIVE).matcher(str).find()
					|| Pattern.compile("row_number\\s*\\({1}\\s*\\){1}+", Pattern.CASE_INSENSITIVE).matcher(str).find()
					|| Pattern.compile("(group|over|keep)\\s*\\({1}", Pattern.CASE_INSENSITIVE).matcher(str).find()) {
				m.appendReplacement(sb, "$1");
			} else {
				m.appendReplacement(sb, "");
			}
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public static void resolveTables(String sqlFile, String sqlId, String sqlText) {
		String referer = KContext.getT(AttrKey.REFERER);
		String uri = KContext.getT(AttrKey.URI);
		if(!KStringUtil.isEmpty(referer)) {
			referer = referer.substring(referer.lastIndexOf("/")+1, referer.length());
			referer = referer.substring(0, referer.indexOf("?") == -1 ? referer.length() : referer.indexOf("?")); // GET 방식의 referer도 존재함
		}
		if(KStringUtil.isEmpty(referer)) {
			return;
		}
		String sqlTables = null;
		Matcher m = Pattern.compile("("+TABLE_PATTERN+")", Pattern.CASE_INSENSITIVE).matcher(sqlText);
		List<String> tableNames = new ArrayList<String>();
		while(m.find()) {
			String str = m.group(0);
			if(!tableNames.contains(str)) {
				tableNames.add(str);
			}
		}
		sqlTables = tableNames.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", "");
		String log = String.format("%s|%s|%s|%s|%s", referer, uri, sqlFile, sqlId, sqlTables);
		KLogCommon.table(referer, log);
	}
}
