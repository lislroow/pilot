package mgkim.framework.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;

import mgkim.framework.core.dto.KCmmVO;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.logging.KLogCommon;
import mgkim.framework.core.logging.KLogLayout;
import mgkim.framework.core.logging.KLogSql;
import mgkim.framework.core.type.TSqlType;

public class KSqlUtil {

	public static final String COUNT_SQL = "SELECT COUNT(*) \n  FROM (\n  %s\n) TB";
	public static final String PAGING_SQL = "SELECT * \n  FROM (SELECT rownum rn, (?+1)-rownum rnum, TB.* FROM (\n    %s\n  ) TB\n) WHERE rn BETWEEN ? AND ?";

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
		if (path.indexOf(injar) > 0) {
			path = path.substring(path.indexOf(injar)+injar.length());
			path = "(jar) " + path;
		}
		return path;
	}

	public static String createParamSql(Object parameterObject, MappedStatement mappedStatement, TSqlType paramSqlType) throws Exception {
		// 준비
		TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
		BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
		String paramSql = boundSql.getSql();
		Configuration configuration = mappedStatement.getConfiguration();
		String sqlId = mappedStatement.getId();
		String sqlFile = KSqlUtil.getRelativePath(mappedStatement.getResource());
		// -- 준비
		
		// param-sql 생성
		{
			try {
				paramSql = paramSql.replaceAll(PARAM_CHAR, PARAM_TEMP_CHAR);
				if (parameterObject == null) {
					// null 타입
					paramSql = paramSql.replaceAll(PARAM_TEMP_CHAR, "''");
				} else if (parameterObject instanceof Map) {
					// Map 타입
					Map map = ((Map)parameterObject); 
					List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
					for (int i=0; i<parameterMappings.size(); i++) {
						ParameterMapping _parameter = parameterMappings.get(i);
						Object value = map.get(_parameter.getProperty());
						if (value == null) { 
							paramSql = Pattern.compile(PARAM_TEMP_CHAR).matcher(paramSql).replaceFirst("null");
						} else if (String.class.isInstance(value)) {
							// `value` 에 `정규식에서 사용되는 특수문자`를 제거 합니다.
							String quoteStr = Matcher.quoteReplacement(KStringUtil.nvl(value));
							quoteStr = String.format("'%s'", quoteStr);
							paramSql = Pattern.compile(PARAM_TEMP_CHAR).matcher(paramSql).replaceFirst(quoteStr);
						} else {
							paramSql = Pattern.compile(PARAM_TEMP_CHAR).matcher(paramSql).replaceFirst(KStringUtil.nvl(value));
						}
					}
				} else if (parameterObject instanceof String) {
					// String 타입
					String value = String.format("'%s'", parameterObject);

					// `value` 에 `정규식에서 사용되는 특수문자`를 제거 합니다.
					String quoteStr = Matcher.quoteReplacement(value);

					paramSql = Pattern.compile(PARAM_TEMP_CHAR).matcher(paramSql).replaceAll(quoteStr);
				} else {
					// VO 타입
					List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
					for (int i=0; i<parameterMappings.size(); i++) {
						ParameterMapping _parameter = parameterMappings.get(i);
						String propertyName = _parameter.getProperty();
						String value = null;
						if (boundSql.hasAdditionalParameter(propertyName)) {
							Object obj = (String) boundSql.getAdditionalParameter(propertyName);
							if (obj instanceof String) {
								value = String.format("'%s'", KStringUtil.nvl(obj));
							} else {
								value = KStringUtil.nvl(obj);
							}
						} else {
							value = KObjectUtil.getSqlParamByFieldName(parameterObject, propertyName);
						}
						
						if (value == null) {
							value = "";
						}

						// `value` 에 `정규식에서 사용되는 특수문자`를 제거 합니다.
						String quoteStr = Matcher.quoteReplacement(value);

						// `value` 에 "$" 혹은 "\" 문자가 제거된 값으로 replace를 합니다.
						paramSql = Pattern.compile(PARAM_TEMP_CHAR).matcher(paramSql).replaceFirst(quoteStr);
					}
				}
				paramSql = paramSql.replaceAll(PARAM_TEMP_CHAR, PARAM_CHAR);
			} catch(Exception e) {
				KLogSql.error(String.format("param-sql을 생성하는 중 오류가 발생했습니다. `%s`", sqlId), e);
			}
		} // -- param-sql 생성
		
		
		// param-sql 로깅
		{
			paramSql = paramSql.replaceAll("\n    ", "\n");
			switch(paramSqlType) {
			case ORIGIN_SQL:
				paramSql = KSqlUtil.insertSqlId(paramSql, "(origin-sql) "+sqlId);
				KLogSql.warn("{} `{}` {}{} `{}` `{}`{}{}", KConstant.LT_SQL, sqlId, KLogLayout.LINE, KConstant.LT_SQL, sqlFile, sqlId, KLogLayout.LINE, paramSql);
				break;
			case PAGING_SQL:
				KCmmVO vo = (KCmmVO) parameterObject;
				paramSql = paramSql.replaceAll("\n", "\n\t");
				paramSql = String.format(KSqlUtil.PAGING_SQL, paramSql);
				paramSql = KSqlUtil.insertSqlId(paramSql, "(paging-sql) "+sqlId);
				paramSql = paramSql.replaceFirst("\\?", vo.get_rowcount()+"")
						.replaceFirst("\\?", vo.get_startrow()+"")
						.replaceFirst("\\?", vo.get_endrow()+"");
				KLogSql.warn("{} `{}` {}{} `{}` `{}`{}{}", KConstant.LT_SQL_PAING, sqlId, KLogLayout.LINE, KConstant.LT_SQL_PAING, sqlFile, sqlId, KLogLayout.LINE, paramSql);
				break;
			case COUNT1_SQL:
				paramSql = paramSql.replaceAll("\n", "\n\t");
				paramSql = String.format(KSqlUtil.COUNT_SQL, paramSql);
				paramSql = KSqlUtil.insertSqlId(paramSql, "(count-sql1) "+sqlId);
				KLogSql.warn("{} `{}` {}{} `{}` `{}`{}{}", KConstant.LT_SQL_COUNT1, sqlId, KLogLayout.LINE, KConstant.LT_SQL_COUNT1, sqlFile, sqlId, KLogLayout.LINE, paramSql);
				break;
			case COUNT2_SQL:
				paramSql = paramSql.replaceAll("\n", "\n\t");
				paramSql = String.format(KSqlUtil.COUNT_SQL, paramSql);
				paramSql = KSqlUtil.insertSqlId(paramSql, "(count-sql2) "+sqlId);
				KLogSql.warn("{} `{}` {}{} `{}` `{}`{}{}", KConstant.LT_SQL_COUNT2, sqlId, KLogLayout.LINE, KConstant.LT_SQL_COUNT2, sqlFile, sqlId, KLogLayout.LINE, paramSql);
				break;
			case COUNT3_SQL:
				paramSql = KSqlUtil.insertSqlId(paramSql, "(count-sql3) "+sqlId);
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
	
	public static String insertSqlId(String sql, String sqlId) {
		sqlId = String.format("$1 /* %s */", sqlId);
		sql = sql.replaceFirst("^\\s*([0-9a-zA-Zㄱ-힣_]*)", sqlId);
		return sql;
	}
	
	public static String removeOrderBy(String sql) {
		String ORDER_BY_REGEX = "(.*order\\s*by[\\w|\\s|,|\\.|\\/|\\*|가-힣]*)";
		Pattern p = Pattern.compile(ORDER_BY_REGEX, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(sql);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			String str = m.group(0);
			if (Pattern.compile("partition\\s*by", Pattern.CASE_INSENSITIVE).matcher(str).find()
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
		if (!KStringUtil.isEmpty(referer)) {
			referer = referer.substring(referer.lastIndexOf("/")+1, referer.length());
			referer = referer.substring(0, referer.indexOf("?") == -1 ? referer.length() : referer.indexOf("?")); // GET 방식의 referer도 존재함
		}
		if (KStringUtil.isEmpty(referer)) {
			return;
		}
		String sqlTables = null;
		Matcher m = Pattern.compile("("+TABLE_PATTERN+")", Pattern.CASE_INSENSITIVE).matcher(sqlText);
		List<String> tableNames = new ArrayList<String>();
		while (m.find()) {
			String str = m.group(0);
			if (!tableNames.contains(str)) {
				tableNames.add(str);
			}
		}
		sqlTables = tableNames.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", "");
		String log = String.format("%s|%s|%s|%s|%s", referer, uri, sqlFile, sqlId, sqlTables);
		KLogCommon.table(referer, log);
	}
}
