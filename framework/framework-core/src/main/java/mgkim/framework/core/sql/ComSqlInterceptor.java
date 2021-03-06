package mgkim.framework.core.sql;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

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

import mgkim.framework.core.dto.KCmmVO;
import mgkim.framework.core.dto.KInPageVO;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.logging.KLogElastic;
import mgkim.framework.core.logging.KLogMarker;
import mgkim.framework.core.type.KType.ExecType;
import mgkim.framework.core.type.KType.SqlType;
import mgkim.framework.core.type.KType.SysType;
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
		// sql ?????? ??????
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
		
		// closable ??????
		DataSource datasource = null;
		Connection connection = null;
		PreparedStatement pstmt = null;
		
		// ????????? ??????
		Object resultObject = null;
		
		// ?????? ??????
		ExecType execType = KContext.getT(AttrKey.EXEC_TYPE);
		KContext.set(AttrKey.SQL_ID, sqlId);
		KContext.set(AttrKey.SQL_FILE, sqlFile);
		
		// paging ?????? ??? sql ??????
		String paramSql = null;
		boolean isPaging = false;
		
		// sql ??????
		boolean isOnError = false;
		int resultCount = -1;
		String elapsed = null;
		SqlType sqlType = SqlType.ORIGIN_SQL;
		try {
			// paging ?????? ??? sql ??????
			{
				switch(execType) {
				case REQUEST:
					// parameterObject ?????? ?????? ??????
					{
						if (KCmmVO.class.isInstance(parameterObject)) {
							KDtoUtil.setSysValues(parameterObject);
						} else if (java.util.Map.class.isInstance(parameterObject)) {
							KDtoUtil.putSysValues((Map)parameterObject);
						}
					}
					
					// paging ?????? ??????
					{
						KInPageVO inPageVO = KContext.getT(AttrKey.IN_PAGE);
						if (inPageVO == null) { // // com ???????????? ?????? sql ??? paging ?????? ???????????? ?????????
							isPaging = false;
						} else {
							isPaging = inPageVO.getPaging() && mappedStatement.getSqlCommandType() == SqlCommandType.SELECT;
						}
						
						if (isPaging) {
							sqlType = SqlType.PAGING_SQL;
						} else {
							sqlType = SqlType.ORIGIN_SQL;
						}
					}
					
					// prepareStatment ??????
					if (!isPaging) {
						datasource = mappedStatement.getConfiguration().getEnvironment().getDataSource();
						connection = org.springframework.jdbc.datasource.DataSourceUtils.getConnection(datasource);
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
					// prepareStatment ??????
					{
						datasource = mappedStatement.getConfiguration().getEnvironment().getDataSource();
						connection = org.springframework.jdbc.datasource.DataSourceUtils.getConnection(datasource);
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
			
			
			// sql ??????
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
			if (connection != null) {
				if (!org.springframework.jdbc.datasource.DataSourceUtils.isConnectionTransactional(connection, datasource)) {
					connection.close();
				}
			}
			// param-sql ??????
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
				
				//log.trace(KLogMarker.getSqlMarker(sqlFile), "\nparam = {}\n{}\n(elapsed={}, rows={})\nresult = {}", KStringUtil.toJson(parameterObject), paramSql, resultCount, elapsed, KStringUtil.toJson(resultObject));
				log.info(KLogMarker.getSqlMarker(sqlFile), "\n{}\n(elapsed={}, rows={})", paramSql, elapsed, resultCount);
				
				// sql ?????? ????????? ??????
				if (true && KProfile.SYS == SysType.LOC) {
					KSqlUtil.resolveTables(sqlFile, sqlId, paramSql);
				}
				KLogElastic.sqlLog(sqlId, sqlFile, paramSql, resultCount, elapsed);
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