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
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import mgkim.framework.core.dto.KInPageVO;
import mgkim.framework.core.env.KConfig;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.logging.KLogMarker;
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
	
	private static final Logger log = LoggerFactory.getLogger(ComSqlInterceptor.class);

	private ComSqlPagingList comSqlPagingList;

	protected Field proxyMappedStatement;
	protected Field proxyDelegate;
	
	private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
	private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
	private static final ReflectorFactory DEFAULT_REFLECTOR_FACTORY = new DefaultReflectorFactory();

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
		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
		MetaObject sHandlerMetaObject = MetaObject.forObject(statementHandler, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
		PreparedStatementHandler pstmtHandler = (PreparedStatementHandler) proxyDelegate.get(statementHandler);
		MappedStatement mappedStatement = (MappedStatement) proxyMappedStatement.get(pstmtHandler);
		BoundSql boundSql = statementHandler.getBoundSql();
		String sqlId = mappedStatement.getId();
		//String sqlFile = KSqlUtil.getRelativePath(mappedStatement.getResource());
		String sqlFile = mappedStatement.getResource();
		sqlFile = sqlFile.substring(sqlFile.lastIndexOf(java.io.File.separator)+1, sqlFile.length()-1);
		Object parameterObject = statementHandler.getParameterHandler().getParameterObject();
		
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
		boolean isOnError = false;
		int resultCount = -1;
		String elapsed = null;
		TSqlType sqlType = TSqlType.ORIGIN_SQL;
		try {
			// paging 처리 및 sql 로깅
			{
				switch(execType) {
				case REQUEST:
					// parameterObject 공통 필드 설정
					{
						KDtoUtil.setSysValues(parameterObject);
					}
					
					// paging 여부 확인
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
							String sql = KSqlUtil.removeForeachIndex(boundSql);
							String comment = String.format("/* (%s) %s::%s */", KContext.getT(AttrKey.TXID), sqlFile, sqlId);
							sql = KSqlUtil.insertSqlComment(sql, comment);
							pstmt = connection.prepareStatement(sql);
							int startIndex = 1;
							KSqlUtil.bindParameterToPstmt(pstmt, parameterObject, boundSql, startIndex);
							sHandlerMetaObject.setValue("delegate.boundSql.sql", sql);
						}
						invocation.getArgs()[0] = pstmt;
					} else {
						pstmt = comSqlPagingList.preparePaging(invocation);
						invocation.getArgs()[0] = pstmt;
					}
					break;
				case SCHEDULE:
				case SYSTEM:
				default:
					// prepareStatment 생성
					{
						connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
						{
							String sql = KSqlUtil.removeForeachIndex(boundSql);
							String comment = String.format("/* %s::%s */", sqlFile, sqlId);
							sql = KSqlUtil.insertSqlComment(sql, comment);
							pstmt = connection.prepareStatement(sql);
							int startIndex = 1;
							KSqlUtil.bindParameterToPstmt(pstmt, parameterObject, boundSql, startIndex);
							sHandlerMetaObject.setValue("delegate.boundSql.sql", sql);
						}
						invocation.getArgs()[0] = pstmt;
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
				} finally {
					if (stopWatch != null) {
						stopWatch.stop();
						elapsed = String.format("%.3f", stopWatch.getTotalTimeSeconds());
					}
				}
			}
		} catch (Exception e) {
			isOnError = true;
			throw e;
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
			if (connection != null) {
				connection.close();
			}
			
			// param-sql 로깅
			switch(execType) {
			case REQUEST:
				paramSql = KSqlUtil.createParamSql(parameterObject, statementHandler, sqlType);
				if (resultObject instanceof List) {
					resultCount = ((List)resultObject).size();
				} else if (resultObject instanceof Integer) {
					resultCount = (Integer)resultObject;
				} else if (resultObject == null) {
					resultCount = 0;
				} else {
					resultCount = 1;
				}
				if (isVerboss) {
					log.info(KLogMarker.getSqlMarker(sqlFile), "\nparam = {}\n{}\n(rows={}, elapsed={})\nresult = {}", KStringUtil.toJson(parameterObject), paramSql, resultCount, elapsed, KStringUtil.toJson(resultObject));
				} else {
					log.info(KLogMarker.getSqlMarker(sqlFile), "\n{}\n(rows={}, elapsed={})", paramSql, resultCount, elapsed);
				}
				// sql 실행 테이블 분석
				if (true && KProfile.SYS == TSysType.LOC) {
					KSqlUtil.resolveTables(sqlFile, sqlId, paramSql);
				}
				break;
			case SCHEDULE:
			case SYSTEM:
				if (isOnError) {
					paramSql = KSqlUtil.createParamSql(parameterObject, statementHandler, sqlType);
					log.info(KLogMarker.getSqlMarker(sqlFile), "\nparam = {}\n{}", KStringUtil.toJson(parameterObject), paramSql);
				}
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