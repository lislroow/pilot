package mgkim.framework.core.sql;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.Configuration;

import mgkim.framework.core.env.KConfig;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KSqlContext;
import mgkim.framework.core.logging.KLogSql;
import mgkim.framework.core.type.TSqlType;
import mgkim.framework.core.util.KSqlUtil;

public class ComSqlPagingCount {

	protected Field proxyMappedStatement;
	protected Field proxyDelegate;
	
	public ComSqlPagingCount(Field proxyMappedStatement, Field proxyDelegate) {
		this.proxyMappedStatement = proxyMappedStatement;
		this.proxyDelegate = proxyDelegate;
	}
	
	public Integer countSql1(Invocation invocation) throws Exception {
		// count-sql 실행 준비
		StatementHandler sHandler = (StatementHandler) invocation.getTarget();
		PreparedStatementHandler pstmtHandler = (PreparedStatementHandler) proxyDelegate.get(sHandler);
		MappedStatement mappedStatement = (MappedStatement) proxyMappedStatement.get(pstmtHandler);
		BoundSql boundSql = sHandler.getBoundSql();
		//Configuration configuration = mappedStatement.getConfiguration();
		String sqlId = mappedStatement.getId();
		//String sqlFile = KSqlUtil.getRelativePath(mappedStatement);
		Object parameterObject = sHandler.getParameterHandler().getParameterObject();
		// -- count-sql 실행 준비

		// 반환값 준비
		Integer count = null;
		// -- 반환값 준비

		// 로깅 준비
		//boolean isDebugMode = KContext.isDebugMode();
		boolean isLoggableSql = KLogSql.isLoggableSql(sqlId);
		boolean isVerboss = KConfig.VERBOSS_ALL || KConfig.VERBOSS_SQL;
		{

		}
		// -- 로깅 준비

		// count-sql 로깅
		{
			if (isLoggableSql) {
				if (!isVerboss) {
				} else {
					KSqlUtil.createParamSql(parameterObject, mappedStatement, TSqlType.COUNT_SQL1);
				}
			}
		} // -- count-sql 로깅

		// count-sql 실행
		{
			Connection connection = null;
			String countSql = null;
			PreparedStatement pstmt = null;
			connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
			//List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
			//TypeHandler _typeHandler = null;
			
			// mybatis foreach 문
			{
				String originSql = KSqlUtil.removeForeachIndex(boundSql);
				countSql = String.format(KSqlUtil.COUNT_SQL, originSql);
				countSql = KSqlUtil.insertSqlId(countSql, TSqlType.COUNT_SQL1.code() + " " + sqlId);
				pstmt = connection.prepareStatement(countSql);
			}
			// -- mybatis foreach 문
			
			// origin 파라미터 binding
			int startBindingIndex = 1;
			KSqlUtil.bindParameterToPstmt(pstmt, parameterObject, boundSql, startBindingIndex);
			// -- origin 파라미터 binding
			
			ResultSet rs = null;
			try {
				rs = pstmt.executeQuery();
				if (rs.next()) {
					count = rs.getInt(1);
				}
			} catch(Exception ex) {
				if (!isVerboss) {
					KSqlUtil.createParamSql(parameterObject, mappedStatement, TSqlType.COUNT_SQL1);
				} else {
				}
				throw ex;
			} finally {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (connection != null) {
					connection.close();
				}
			}
		}
		// -- count-sql 실행

		return count;
	}

	public Integer countSql2(Invocation invocation) throws Exception {
		// count-sql 실행 준비
		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
		PreparedStatementHandler preparedStatementHandler = (PreparedStatementHandler) proxyDelegate.get(statementHandler);
		MappedStatement mappedStatement = (MappedStatement) proxyMappedStatement.get(preparedStatementHandler);
		Configuration configuration = mappedStatement.getConfiguration();
		String sqlId = mappedStatement.getId();
		String sqlFile = KSqlUtil.getRelativePath(mappedStatement.getResource());
		//TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
		Object parameterObject = statementHandler.getParameterHandler().getParameterObject();
		// -- count-sql 실행 준비
		
		// 반환값 준비
		Integer count = null;
		// -- 반환값 준비
		
		// count-sql 존재여부 확인
		{
			Set<MappedStatement> mappedStatementList = KSqlContext.MAPPED_STATEMENT_LIST;
			Iterator<MappedStatement> iter = mappedStatementList.iterator();
			boolean isFound = false;
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof MappedStatement) {
					MappedStatement ms = (MappedStatement)obj;
					if (ms.getId().equals(sqlId+"-count")) {
						isFound = true;
						mappedStatement = ms;
						break;
					}
				}
			}
			
			if (!isFound) {
				return count; // count-sql 이 존재하지 않을 경우 `null` 을 반환
			}
		}
		// -- count-sql 존재여부 확인
		
		// 로깅 준비
		//boolean isDebugMode = KContext.isDebugMode();
		boolean isLoggableSql = KLogSql.isLoggableSql(sqlId);
		boolean isVerboss = KConfig.VERBOSS_ALL || KConfig.VERBOSS_SQL;
		{
			sqlId = mappedStatement.getId();
			sqlFile = KSqlUtil.getRelativePath(mappedStatement.getResource());
			
			KContext.set(AttrKey.SQL_ID, sqlId);
			KContext.set(AttrKey.SQL_FILE, sqlFile);
		}
		// -- 로깅 준비
		
		// count-sql 로깅
		{
			if (isLoggableSql) {
				if (!isVerboss) {
				} else {
					KSqlUtil.createParamSql(parameterObject, mappedStatement, TSqlType.COUNT_SQL2);
				}
			}
		} // -- count-sql 로깅
		
		// count-sql 실행
		{
			Connection connection = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try {
				String countSql = mappedStatement.getBoundSql(parameterObject).getSql();
				List<ParameterMapping> parameterMappings = mappedStatement.getBoundSql(parameterObject).getParameterMappings();
				connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
				BoundSql boundSql = new BoundSql(configuration, countSql, parameterMappings, parameterObject);
				
				// mybatis foreach 문
				{
					countSql = KSqlUtil.removeForeachIndex(boundSql);
					countSql = KSqlUtil.insertSqlId(countSql, TSqlType.COUNT_SQL2.code() + " " + sqlId);
					pstmt = connection.prepareStatement(countSql);
				}
				// -- mybatis foreach 문
				
				// origin 파라미터 binding
				int startBindingIndex = 1;
				KSqlUtil.bindParameterToPstmt(pstmt, parameterObject, boundSql, startBindingIndex);
				// -- origin 파라미터 binding
				
				rs = pstmt.executeQuery();
				if (rs.next()) {
					count = rs.getInt(1);
				}
			} catch(Exception ex) {
				if (!isVerboss) {
					KSqlUtil.createParamSql(parameterObject, mappedStatement, TSqlType.COUNT_SQL2);
				} else {
				}
				throw ex;
			} finally {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (connection != null) {
					connection.close();
				}
			}
		}
		// -- count-sql 실행
		
		return count;
	}
}
