package mgkim.framework.core.sql;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.type.IntegerTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.util.StopWatch;

import mgkim.framework.core.dto.KInPageVO;
import mgkim.framework.core.dto.KOutPageVO;
import mgkim.framework.core.env.KConfig;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.logging.KLogApm;
import mgkim.framework.core.logging.KLogLayout;
import mgkim.framework.core.logging.KLogSql;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.core.util.KSqlUtil;

public class ComSqlPagingList {

	private ComSqlPagingCount comSqlPagingCount;
	
	protected Field proxyMappedStatement;
	protected Field proxyDelegate;
	
	public ComSqlPagingList(Field proxyMappedStatement, Field proxyDelegate) {
		comSqlPagingCount = new ComSqlPagingCount(proxyMappedStatement, proxyDelegate);
		this.proxyMappedStatement = proxyMappedStatement;
		this.proxyDelegate = proxyDelegate;
	}
	
	public PreparedStatement preparePaging(Invocation invocation) throws Throwable {
		// 실행 준비
		StatementHandler sHandler = (StatementHandler) invocation.getTarget();
		PreparedStatementHandler pstmtHandler = (PreparedStatementHandler) proxyDelegate.get(sHandler);
		MappedStatement mappedStatement = (MappedStatement) proxyMappedStatement.get(pstmtHandler);
		BoundSql boundSql = sHandler.getBoundSql();
		String sqlId = mappedStatement.getId();
		//String orignalSql = boundSql.getSql();
		String sqlFile = KSqlUtil.getRelativePath(mappedStatement.getResource());
		Object paramObject = sHandler.getParameterHandler().getParameterObject();
		KInPageVO inPageVO = KContext.getT(AttrKey.IN_PAGE);
		// -- 실행 준비
		
		// 로깅 준비
		//TExecType execType = (TExecType) KContext.get(AttrKey.EXEC_TYPE);
		//boolean isDebugMode = KContext.isDebugMode();
		boolean isLogExclude = KLogSql.isLoggableSql(sqlId);
		boolean isVerboss = KConfig.VERBOSS_ALL || KConfig.VERBOSS_SQL;
		double elapsedTime = -1;
		{
			KContext.set(AttrKey.SQL_ID, sqlId);
			KContext.set(AttrKey.SQL_FILE, sqlFile);
		}
		// -- 로깅 준비
		
		
		// count-sql 실행
		KOutPageVO outPageVO = null;
		{
			Integer totalRecordCount = null;
			String countSqlId = sqlId;
			StopWatch stopWatch = new StopWatch(sqlId+"-count-sql."+KContext.getT(AttrKey.TXID));
			stopWatch.start();
			try {
				// `executeFindCountSql`: sqlmap 에 `{sqlid}-count` 규칙의 sqlid가 있으면 `공통 count-sql`을 실행하지 않습니다.
				totalRecordCount = comSqlPagingCount.executeFindCountSql(invocation);
				if (totalRecordCount != null) {
					countSqlId += "-count";
				} else {
					// `executeRemoveOrderBy`: `order by`문을 제거한 `공통 count-sql`을 실행합니다.
					totalRecordCount = comSqlPagingCount.executeRemoveOrderBy(invocation);
				}
			} catch(Exception e) {
				// `executeNoRemoveOrderBy`: `order by`문을 제거하지 않은 `공통 count-sql`을 실행합니다.
				try {
					totalRecordCount = comSqlPagingCount.executeNoRemoveOrderBy(invocation);
				} catch(Exception ex) {
					throw ex;
				}
			} finally {
				stopWatch.stop();
				if (!isLogExclude) {
					KLogApm.sql(stopWatch);
				}
				elapsedTime = stopWatch.getTotalTimeSeconds();
			}
			outPageVO = new KOutPageVO.Builder()
					.pageindex(inPageVO.getPageindex())
					.rowunit(inPageVO.getRowunit())
					.pageunit(inPageVO.getPageunit())
					.rowcount(totalRecordCount)
					.build();
			KContext.set(AttrKey.OUT_PAGE, outPageVO);
			
			// count-sql 결과 로깅
			if (!isVerboss) {
			} else {
				KLogSql.info("{} `{}` {}{} `{}` `{}` {}`rows` = {},  `elapsed` = {}(ms)", KConstant.LT_SQL_RESULT, sqlId, KLogLayout.LINE, KConstant.LT_SQL_RESULT, sqlFile, sqlId, KLogLayout.LINE, totalRecordCount, elapsedTime);
			}
		} // -- count-sql 실행
		
		
		// 페이징 파라미터 설정
		String pagingSql = null;
		PreparedStatement pstmt = null;
		java.sql.Connection connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
		DefaultParameterHandler parameterHandler = (DefaultParameterHandler)sHandler.getParameterHandler();
		TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
		Object parameterObject = parameterHandler.getParameterObject();
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		ParameterMapping _parameter = null;
		TypeHandler _typeHandler = null;
		JdbcType _jdbcType = null;
		org.apache.ibatis.session.Configuration configuration = mappedStatement.getConfiguration();
		
		// mybatis foreach 문
		{
			String originSql = KSqlUtil.removeForeachIndex(boundSql);
			pagingSql = String.format(KSqlUtil.PAGING_SQL, originSql);
			pagingSql = KSqlUtil.insertSqlId(pagingSql, "(paging-sql) "+sqlId);
			pstmt = connection.prepareStatement(pagingSql);
		}
		// -- mybatis foreach 문
		
		int parameterIndex = 1;
		
		{
			// 첫번째 파라미터 생성 (`_rowcount`)
			_parameter = new ParameterMapping.Builder(configuration, "_rowcount", new IntegerTypeHandler())
					.javaType(java.lang.Integer.class)
					.jdbcType(JdbcType.INTEGER).build();
			try {
				_typeHandler = _parameter.getTypeHandler();
				_typeHandler.setParameter(pstmt, parameterIndex, outPageVO.getRowcount(), _parameter.getJdbcType());
				parameterIndex++;
			} catch(Exception e) {
				throw e;
			}
			// -- 첫번째 파라미터 생성 (`_rowcount`)
			
			// 실제 binding 파라미터 생성
			if (parameterMappings != null) {
				for (int i=0; i<parameterMappings.size(); i++) {
					_parameter = parameterMappings.get(i);
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
						} else if (paramObject instanceof java.util.Map) {
							value = ((Map)paramObject).get(propertyName);
						} else {
							value = KObjectUtil.getValue(paramObject, propertyName);
						}
						_typeHandler = _parameter.getTypeHandler();
						JdbcType jdbcType = _parameter.getJdbcType();
						if (value == null && jdbcType == null) {
							jdbcType = configuration.getJdbcTypeForNull();
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
			// -- 실제 binding 파라미터 생성
			
			// 두번째, 세번째 파라미터 생성 (BETWEEN `_startrow` AND `_endrow`)
			_parameter = new ParameterMapping.Builder(configuration, "_startrow", new IntegerTypeHandler())
					.javaType(java.lang.Integer.class)
					.jdbcType(JdbcType.INTEGER).build();
			_typeHandler = _parameter.getTypeHandler();
			try {
				_typeHandler.setParameter(pstmt, parameterIndex, outPageVO.getStartrow(), _parameter.getJdbcType());
				parameterIndex++;
			} catch(Exception e) {
				throw e;
			}
			
			_parameter = new ParameterMapping.Builder(configuration, "_endrow", new IntegerTypeHandler())
					.javaType(java.lang.Integer.class)
					.jdbcType(JdbcType.INTEGER).build();
			_typeHandler = _parameter.getTypeHandler();
			try {
				_typeHandler.setParameter(pstmt, parameterIndex, outPageVO.getEndrow(), _parameter.getJdbcType());
				parameterIndex++;
			} catch(Exception e) {
				throw e;
			}
			// -- 두번째, 세번째 파라미터 생성 (BETWEEN `_startrow` AND `_endrow`)
		}
		
		// invocation 의 args에 새로운 `StatementHandler` 교체
		//invocation.getArgs()[0] = pstmt;
		return pstmt;
	}
}
