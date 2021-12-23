package mgkim.framework.core.util;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

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


	public static final String PARAM_CHAR = "\\?";
	public static final String PARAM_TEMP_CHAR = "\\Ξ"; // SQL 파라미터의 값에 '?' 문자가 있을 수 있으므로 prepareStatement의 파라미터 문자인 '?'을  'Ξ' 로 치환

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
	
	public static String removeForeachIndex(BoundSql boundSql) throws Exception {
		String sql = boundSql.getSql();
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		// mybatis foreach 문
		{
			if (parameterMappings != null) {
				sql = sql.replaceAll(KSqlUtil.PARAM_CHAR, KSqlUtil.PARAM_TEMP_CHAR);
				for (ParameterMapping _p : parameterMappings) {
					String propertyName = _p.getProperty();
					Matcher matcher = Pattern.compile("__frch_index_(\\d+)").matcher(propertyName);
					if (matcher.find()) {
						String value = matcher.replaceFirst("$1");
						sql = Pattern.compile(KSqlUtil.PARAM_TEMP_CHAR).matcher(sql).replaceFirst(value);
					} else {
						sql = Pattern.compile(KSqlUtil.PARAM_TEMP_CHAR).matcher(sql).replaceFirst(KSqlUtil.PARAM_CHAR);
					}
				}
			}
		}
		// -- mybatis foreach 문
		return sql;
	}
	
	public static int bindParameterToPstmt(PreparedStatement pstmt, Object parameterObject, BoundSql boundSql, int startBindingIndex) throws Exception {
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		TypeHandler _typeHandler = null;
		
		int parameterIndex = startBindingIndex;
		
		// origin 파라미터 binding
		if (parameterMappings != null) {
			for (ParameterMapping _parameter : parameterMappings) {
				if (_parameter.getMode() == ParameterMode.IN) {
					Object value;
					String propertyName = _parameter.getProperty();
					
					if (Pattern.matches("__frch_index_\\d+", propertyName)) {
						continue;
					}
					
					if (parameterObject == null) {
						value = null;
					} else if (boundSql.hasAdditionalParameter(propertyName)) { // propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX) && 
						value = boundSql.getAdditionalParameter(propertyName);
					} else if (parameterObject instanceof java.util.Map) {
						value = ((Map)parameterObject).get(propertyName);
					} else {
						value = KObjectUtil.getValue(parameterObject, propertyName);
					}
					_typeHandler = _parameter.getTypeHandler();
					JdbcType jdbcType = _parameter.getJdbcType();
					if (value == null && jdbcType == null) {
						jdbcType = JdbcType.VARCHAR;
					}
					try {
						_typeHandler.setParameter(pstmt, parameterIndex, value, jdbcType);
						parameterIndex++;
					} catch(Exception e) {
						throw e;
					}
				}
			}
		}
		// -- origin 파라미터 binding
		
		return parameterIndex;
	}
	
	public static String createParamSql(Object parameterObject, MappedStatement mappedStatement, TSqlType paramSqlType) throws Exception {
		// 준비
		BoundSql boundSql = mappedStatement.getBoundSql(parameterObject);
		String paramSql = boundSql.getSql();
		String sqlId = mappedStatement.getId();
		String sqlFile = KSqlUtil.getRelativePath(mappedStatement.getResource());
		// -- 준비
		
		// param-sql 생성
		{
			try {
				paramSql = paramSql.replaceAll(PARAM_CHAR, PARAM_TEMP_CHAR);
				if (parameterObject == null) {
					paramSql = paramSql.replaceAll(PARAM_TEMP_CHAR, "null");
				} else if (parameterObject instanceof String) {
					String value = String.format("'%s'", parameterObject);
					// `value` 에 `정규식에서 사용되는 특수문자`를 제거 합니다.
					String quoteStr = Matcher.quoteReplacement(value);
					paramSql = Pattern.compile(PARAM_TEMP_CHAR).matcher(paramSql).replaceAll(quoteStr);
				} else if (parameterObject instanceof Map) {
					// Map 타입
					Map map = ((Map)parameterObject); 
					List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
					for (int i=0; i<parameterMappings.size(); i++) {
						ParameterMapping _parameter = parameterMappings.get(i);
						String propertyName = _parameter.getProperty();
						Object value = null;
						
						if (boundSql.hasAdditionalParameter(propertyName)) {
							// foreach 내부 index 변수는 Integer 형으로 반환됨
							value = boundSql.getAdditionalParameter(propertyName);
						} else {
							value = map.get(propertyName);
						}
						
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
				} else {
					// VO 타입
					List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
					for (int i=0; i<parameterMappings.size(); i++) {
						ParameterMapping _parameter = parameterMappings.get(i);
						String propertyName = _parameter.getProperty();
						String value = null;
						if (boundSql.hasAdditionalParameter(propertyName)) {
							// foreach 내부 index 변수는 Integer 형으로 반환됨
							Object obj = boundSql.getAdditionalParameter(propertyName);
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
			case COUNT_SQL1:
				paramSql = KSqlUtil.insertSqlId(paramSql, TSqlType.COUNT_SQL1.code() + " " + sqlId);
				KLogSql.warn("{} `{}` {}{} `{}` `{}`{}{}", KConstant.LT_SQL_COUNT1, sqlId, KLogLayout.LINE, KConstant.LT_SQL_COUNT1, sqlFile, sqlId, KLogLayout.LINE, paramSql);
				break;
			case COUNT_SQL2:
				paramSql = paramSql.replaceAll("\n", "\n\t");
				paramSql = String.format(KSqlUtil.COUNT_SQL, paramSql);
				paramSql = KSqlUtil.insertSqlId(paramSql, TSqlType.COUNT_SQL2.code() + " " + sqlId);
				KLogSql.warn("{} `{}` {}{} `{}` `{}`{}{}", KConstant.LT_SQL_COUNT2, sqlId, KLogLayout.LINE, KConstant.LT_SQL_COUNT2, sqlFile, sqlId, KLogLayout.LINE, paramSql);
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
