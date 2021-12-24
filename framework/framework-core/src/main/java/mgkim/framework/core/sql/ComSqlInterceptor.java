package mgkim.framework.core.sql;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.util.StopWatch;

import mgkim.framework.core.dto.KInPageVO;
import mgkim.framework.core.env.KConfig;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.logging.KLogApm;
import mgkim.framework.core.logging.KLogLayout;
import mgkim.framework.core.logging.KLogSql;
import mgkim.framework.core.type.TExecType;
import mgkim.framework.core.type.TSqlType;
import mgkim.framework.core.type.TSysType;
import mgkim.framework.core.util.KDtoUtil;
import mgkim.framework.core.util.KSqlUtil;
import mgkim.framework.core.util.KStringUtil;

@Intercepts({
	@Signature(type = StatementHandler.class, method = "query", args = { Statement.class, ResultHandler.class })
	, @Signature(type = StatementHandler.class, method = "update", args = { Statement.class })
})
public class ComSqlInterceptor implements Interceptor {

	private ComSqlPagingList comSqlPagingList;

	protected Field proxyMappedStatement;
	protected Field proxyDelegate;

	public ComSqlInterceptor() {
		try {
			proxyMappedStatement = BaseStatementHandler.class.getDeclaredField("mappedStatement");
			proxyMappedStatement.setAccessible(true);
			proxyDelegate = RoutingStatementHandler.class.getDeclaredField("delegate");
			proxyDelegate.setAccessible(true);

			comSqlPagingList = new ComSqlPagingList(proxyMappedStatement, proxyDelegate);
		} catch(SecurityException e) {
			throw new RuntimeException(e);
		} catch(NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}


	public Object intercept(Invocation invocation) throws Throwable {
		// sql 실행 준비
		StatementHandler sHandler = (StatementHandler) invocation.getTarget();
		PreparedStatementHandler pstmtHandler = (PreparedStatementHandler) proxyDelegate.get(sHandler);
		MappedStatement mappedStatement = (MappedStatement) proxyMappedStatement.get(pstmtHandler);
		BoundSql boundSql = sHandler.getBoundSql();
		String sqlId = mappedStatement.getId();
		String sql = boundSql.getSql();
		String sqlFile = KSqlUtil.getRelativePath(mappedStatement.getResource());
		Object parameterObject = sHandler.getParameterHandler().getParameterObject();
		
		// closable 객체
		Connection connection = null;
		PreparedStatement pstmt = null;
		
		// 반환값 준비
		Object resultObject = null;
		
		// 로깅 준비
		TExecType execType = KContext.getT(AttrKey.EXEC_TYPE);
		boolean isLoggableSql = KLogSql.isLoggableSql(sqlId);
		boolean isComSql = KLogSql.isCmmSql(sqlId);
		boolean isVerboss = KConfig.VERBOSS_ALL || KConfig.VERBOSS_SQL;
		{
			KContext.set(AttrKey.SQL_ID, sqlId);
			KContext.set(AttrKey.SQL_FILE, sqlFile);
		}
		
		// paging 처리 및 sql 로깅
		String paramSql = null;
		boolean isPaging = false;
		
		// sql 실행
		int resultCount = -1;
		double elapsedTime = -1;
		
		try {
			// paging 처리 및 sql 로깅
			{
				switch(execType) {
				case REQUEST:
					// parameterObject 공통 필드 설정
					{
						KDtoUtil.setSysValues(parameterObject);
					}
					
					// parameterObject 로깅
					{
						if (isLoggableSql) {
							if (!isVerboss) {
							} else {
								KLogSql.info("{} `{}` {}{} `{}` `{}` {}`parameterObject` = {}", KConstant.LT_SQL_PARAM, sqlId, KLogLayout.LINE, KConstant.LT_SQL_PARAM, sqlFile, sqlId, KLogLayout.LINE, KStringUtil.toJson(parameterObject));
							}
						}
					}
					
					// paging 여부 확인
					TSqlType sqlType = null;
					{
						KInPageVO inPageVO = KContext.getT(AttrKey.IN_PAGE);
						if (inPageVO == null || isComSql) { // // com 패키지에 있는 sql 은 paging 처리 대상에서 제외함
							isPaging = false;
						} else {
							isPaging = inPageVO.getPaging() && mappedStatement.getSqlCommandType() == SqlCommandType.SELECT;
						}
						
						if (isPaging) {
							sqlType = TSqlType.PAGING_SQL;
						} else {
							sqlType = TSqlType.ORIGIN_SQL;
						}
					}
					
					// prepareStatment 생성
					if (!isPaging) {
						connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
						{
							sql = KSqlUtil.removeForeachIndex(boundSql);
							sql = KSqlUtil.insertSqlId(sql, sqlId);
							pstmt = connection.prepareStatement(sql);
							int startIndex = 1;
							KSqlUtil.bindParameterToPstmt(pstmt, parameterObject, boundSql, startIndex);
						}
						invocation.getArgs()[0] = pstmt;
					} else {
						pstmt = comSqlPagingList.preparePaging(invocation);
						invocation.getArgs()[0] = pstmt;
					}
					
					// param-sql 로깅
					if (!isComSql || isLoggableSql) {
						paramSql = KSqlUtil.createParamSql(parameterObject, mappedStatement, sqlType);
					}
					break;
				case SCHEDULE:
				case SYSTEM:
				default:
					// parameterObject 로깅
					{
						if (isLoggableSql) {
							KLogSql.info("{} `{}` {}{} `{}` `{}` {}{}", KConstant.LT_SQL_PARAM, sqlId, KLogLayout.LINE, KConstant.LT_SQL_PARAM, sqlFile, sqlId, KLogLayout.LINE, KStringUtil.toJson(parameterObject));
						}
					}
					
					// prepareStatment 생성
					{
						connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
						{
							sql = KSqlUtil.removeForeachIndex(boundSql);
							sql = KSqlUtil.insertSqlId(sql, sqlId);
							pstmt = connection.prepareStatement(sql);
							int startIndex = 1;
							KSqlUtil.bindParameterToPstmt(pstmt, parameterObject, boundSql, startIndex);
						}
						invocation.getArgs()[0] = pstmt;
					}
					
					// param-sql 로깅
					if (isLoggableSql) {
						paramSql = KSqlUtil.createParamSql(parameterObject, mappedStatement, TSqlType.ORIGIN_SQL);
					}
					break;
				}
			}
			
			
			// sql 실행
			{
				StopWatch stopWatch = null;
				switch(execType) {
				case REQUEST:
					stopWatch = new StopWatch(sqlId+"#"+KContext.getT(AttrKey.TXID));
					stopWatch.start();
					break;
				case SCHEDULE:
				case SYSTEM:
				default:
					break;
				}
				try {
					resultObject = invocation.proceed();
				} catch(Exception e) {
					switch(execType) {
					case REQUEST:
						break;
					case SCHEDULE:
					case SYSTEM:
					default:
						paramSql = KSqlUtil.createParamSql(parameterObject, mappedStatement, TSqlType.ORIGIN_SQL);
						break;
					}
					throw e;
				} finally {
					if (stopWatch != null) {
						stopWatch.stop();
						if (!isLoggableSql) {
							KLogApm.sql(stopWatch);
						}
						elapsedTime = stopWatch.getTotalTimeSeconds();
					}
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
			if (connection != null) {
				connection.close();
			}
		}
		
		
		// sql 결과 로깅
		{
			if (isLoggableSql) {
				if (resultObject instanceof List) {
					resultCount = ((List)resultObject).size();
				} else if (resultObject instanceof Integer) {
					resultCount = (Integer)resultObject;
				}
				
				switch(execType) {
				case REQUEST:
					if (!isVerboss) {
						KLogSql.info("{} `{}` {}{} `{}` `{}` {}`rows` = {},  `elapsed` = {} sec",                       KConstant.LT_SQL_RESULT, sqlId, KLogLayout.LINE, KConstant.LT_SQL_RESULT, sqlFile, sqlId, KLogLayout.LINE, resultCount, String.format("%.3f", elapsedTime));
					} else {
						KLogSql.info("{} `{}` {}{} `{}` `{}` {}`rows` = {},  `elapsed` = {} sec,{}`resultObject` = {}", KConstant.LT_SQL_RESULT, sqlId, KLogLayout.LINE, KConstant.LT_SQL_RESULT_VERBOSS, sqlFile, sqlId, KLogLayout.LINE, resultCount, String.format("%.3f", elapsedTime), KLogLayout.LINE, KStringUtil.toJson(resultObject));
					}
					break;
				case SCHEDULE:
				case SYSTEM:
					if (!isVerboss) {
						KLogSql.info("{} `{}` {}{} `{}` `{}` {}`rows` = {}",                        KConstant.LT_SQL_RESULT, sqlId, KLogLayout.LINE, KConstant.LT_SQL_RESULT, sqlFile, sqlId, KLogLayout.LINE, resultCount);
					} else {
						KLogSql.info("{} `{}` {}{} `{}` `{}` {}`rows` = {}, {}`resultObject` = {}", KConstant.LT_SQL_RESULT, sqlId, KLogLayout.LINE, KConstant.LT_SQL_RESULT_VERBOSS, sqlFile, sqlId, KLogLayout.LINE, resultCount, KLogLayout.LINE, KStringUtil.toJson(resultObject));
					}
				default:
					break;
				}
			}
		}
		
		
		// sql 실행 테이블 분석
		{
			switch(execType) {
			case REQUEST:
				if (KProfile.SYS == TSysType.LOC && !KStringUtil.isEmpty(paramSql)) {
					KSqlUtil.resolveTables(sqlFile, sqlId, paramSql);
				}
				break;
			case SCHEDULE:
			case SYSTEM:
			default:
				break;
			}
		}
		return resultObject;
	}
	
	@Override
	public Object plugin(Object target) {
		if (target instanceof StatementHandler || target instanceof ResultSetHandler) {
			return Plugin.wrap(target, this);
		} else {
			return target;
		}
	}
	
	@Override
	public void setProperties(Properties properties) {
	}
}