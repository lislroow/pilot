package mgkim.framework.core.sql;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
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
import mgkim.framework.core.util.KExceptionUtil;
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

	public Connection preparePaging(Invocation invocation) throws Throwable {
		// 실행 준비
		StatementHandler sHandler = (StatementHandler) invocation.getTarget();
		PreparedStatementHandler pstmtHandler = (PreparedStatementHandler) proxyDelegate.get(sHandler);
		MappedStatement mappedStatement = (MappedStatement) proxyMappedStatement.get(pstmtHandler);
		BoundSql boundSql = sHandler.getBoundSql();
		String paramSql = boundSql.getSql();
		Configuration configuration = mappedStatement.getConfiguration();
		String sqlId = mappedStatement.getId();
		//String orignalSql = boundSql.getSql();
		String sqlFile = KSqlUtil.getRelativePath(mappedStatement.getResource());
		Object paramObject = sHandler.getParameterHandler().getParameterObject();
		// -- 실행 준비


		// 반환값 준비
		Connection connection = null;
		// -- 반환값 준비


		// paging 여부 확인
		KInPageVO inPageVO = KContext.getT(AttrKey.IN_PAGE);
		{
			if (inPageVO == null) {
				return connection; // paging 대상이 아닐 경우 Connection 객체를 null 로 반환
			}

			boolean isSelectSqlID = sqlId.matches("^.+\\.select.+(List)$");
			boolean isSelectType = mappedStatement.getSqlCommandType() == SqlCommandType.SELECT;
			boolean isPagingYN = inPageVO.getPaging();

			boolean isPaging = isSelectSqlID && isSelectType && isPagingYN;

			if (!isPaging) {
				return connection; // paging 대상이 아닐 경우 Connection 객체를 null 로 반환
			}
		} // -- paging 여부 확인


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
		String pagingSql = String.format(KSqlUtil.PAGING_SQL, boundSql.getSql());
		connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
		PreparedStatement pstmt = connection.prepareStatement(pagingSql);
		DefaultParameterHandler parameterHandler = (DefaultParameterHandler)sHandler.getParameterHandler();
		TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
		Object parameterObject = parameterHandler.getParameterObject();
		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		List<ParameterMapping> pagingParameterMappings = new ArrayList<ParameterMapping>();
		ParameterMapping _parameter = null;
		TypeHandler _typeHandler = null;
		JdbcType _jdbcType = null;


		// 첫번째 파라미터 생성 (`_rowcount`)
		{
			_parameter = new ParameterMapping.Builder(configuration, "_rowcount", new IntegerTypeHandler())
					.javaType(java.lang.Integer.class)
					.jdbcType(JdbcType.INTEGER).build();
			pagingParameterMappings.add(_parameter);
			_typeHandler = _parameter.getTypeHandler();
			_jdbcType = _parameter.getJdbcType();
			if (_jdbcType == null) {
				_jdbcType = configuration.getJdbcTypeForNull();
			}
			try {
				_typeHandler.setParameter(pstmt, 1, outPageVO.getRowcount(), JdbcType.INTEGER);
			} catch(Exception e) {
				KLogSql.error(String.format(
								"%s `%s` error-message=%s%s%s", KConstant.LT_SQL_PAING, "(paging)"+sqlId, KExceptionUtil.getCauseMessage(e), KLogLayout.LINE, paramSql)
							, e);
				throw e;
			}
		}

		// 실제 binding 파라미터 생성
		{
			if (parameterMappings != null) {
				for (int i=0; i<parameterMappings.size(); i++) {
					ParameterMapping parameterMapping = parameterMappings.get(i);
					if (parameterMapping.getMode() != ParameterMode.OUT) {
						Object value;
						String propertyName = parameterMapping.getProperty();
						if (boundSql.hasAdditionalParameter(propertyName)) {
							value = boundSql.getAdditionalParameter(propertyName);
						} else if (parameterObject == null) {
							value = null;
						} else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
							value = parameterObject;
						} else {
							value = KObjectUtil.getValue(paramObject, propertyName);
						}
						TypeHandler typeHandler = parameterMapping.getTypeHandler();
						JdbcType jdbcType = parameterMapping.getJdbcType();
						if (value == null && jdbcType == null) {
							jdbcType = configuration.getJdbcTypeForNull();
						}
						try {
							typeHandler.setParameter(pstmt, i+2, value, jdbcType);
						} catch(Exception e) {
							KLogSql.error(String.format(
											"{} `{}` error-message={}{}{}", KConstant.LT_SQL_PAING, "(paging)"+sqlId, KExceptionUtil.getCauseMessage(e), KLogLayout.LINE, paramSql)
										, e);
							throw e;
						}
						pagingParameterMappings.add(parameterMapping);
					} else if (parameterMapping.getMode() == ParameterMode.OUT) {
						KLogSql.warn("페이징 sql의 statement 파라미터를 생성하는 중 parameterMapping의 mode 가 OUT 인 형태가 발견되었습니다.", parameterMapping.toString());
					}
				}
			}
		}

		// 두번째, 세번째 파라미터 생성 (BETWEEN `_startrow` AND `_endrow`)
		{
			_parameter = new ParameterMapping.Builder(configuration, "_startrow", new IntegerTypeHandler())
					.javaType(java.lang.Integer.class)
					.jdbcType(JdbcType.INTEGER).build();
			pagingParameterMappings.add(_parameter);
			_typeHandler = _parameter.getTypeHandler();
			_jdbcType = _parameter.getJdbcType();
			if (_jdbcType == null) {
				_jdbcType = configuration.getJdbcTypeForNull();
			}
			try {
				_typeHandler.setParameter(pstmt, pagingParameterMappings.size(), outPageVO.getStartrow(), JdbcType.INTEGER);
			} catch(Exception e) {
				KLogSql.error(String.format(
								"{} `{}` error-message={}{}{}", KConstant.LT_SQL_PAING, "(paging)"+sqlId, KExceptionUtil.getCauseMessage(e), KLogLayout.LINE, paramSql)
							, e);
				throw e;
			}

			_parameter = new ParameterMapping.Builder(configuration, "_endrow", new IntegerTypeHandler())
					.javaType(java.lang.Integer.class)
					.jdbcType(JdbcType.INTEGER).build();
			pagingParameterMappings.add(_parameter);
			_typeHandler = _parameter.getTypeHandler();
			_jdbcType = _parameter.getJdbcType();
			if (_jdbcType == null) {
				_jdbcType = configuration.getJdbcTypeForNull();
			}
			try {
				_typeHandler.setParameter(pstmt, pagingParameterMappings.size(), outPageVO.getEndrow(), JdbcType.INTEGER);
			} catch(Exception e) {
				KLogSql.error(String.format(
								"{} `{}` error-message={}{}{}", KConstant.LT_SQL_PAING, "(paging)"+sqlId, KExceptionUtil.getCauseMessage(e), KLogLayout.LINE, paramSql)
							, e);
				throw e;
			}
		}

		// 새로운 paing-sql로 `boundSql` 교체
		{
			BoundSql newBoundSql = new BoundSql(mappedStatement.getConfiguration(), pagingSql, pagingParameterMappings, boundSql.getParameterObject());
			MetaObject metaObject = SystemMetaObject.forObject(sHandler);
			for (ParameterMapping mapping : pagingParameterMappings) {
				String prop = mapping.getProperty();
				if (boundSql.hasAdditionalParameter(prop)) {
					newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
				}
			}
			metaObject.setValue("delegate.boundSql", newBoundSql);
		}

		// invocation 의 args에 새로운 `StatementHandler` 교체
		invocation.getArgs()[0] = pstmt;

		return connection; // paging 처리가 되었음을 반환
	}
}
