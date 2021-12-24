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
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;

import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KSqlContext;
import mgkim.framework.core.type.TSqlType;
import mgkim.framework.core.util.KSqlUtil;

public class ComSqlPagingCount {

	protected Field proxyMappedStatement;
	protected Field proxyDelegate;
	
	private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
	private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
	private static final ReflectorFactory DEFAULT_REFLECTOR_FACTORY = new DefaultReflectorFactory();
	
	public ComSqlPagingCount(Field proxyMappedStatement, Field proxyDelegate) {
		this.proxyMappedStatement = proxyMappedStatement;
		this.proxyDelegate = proxyDelegate;
	}
	
	public Integer countSql1(Invocation invocation) throws Exception {
		// count-sql 실행 준비
		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
		MetaObject sHandlerMetaObject = MetaObject.forObject(statementHandler, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
		PreparedStatementHandler pstmtHandler = (PreparedStatementHandler) proxyDelegate.get(statementHandler);
		MappedStatement mappedStatement = (MappedStatement) proxyMappedStatement.get(pstmtHandler);
		BoundSql boundSql = statementHandler.getBoundSql();
		String sqlId = mappedStatement.getId();
		Object parameterObject = statementHandler.getParameterHandler().getParameterObject();
		
		// closable 객체
		Connection connection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		// 반환값 준비
		Integer count = null;
		
		// (주의) 반드시 finally 에서 originSql 을 boundSql 에 set 할 것
		String originSql = boundSql.getSql();
		
		try {
			// count-sql 실행
			{
				connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
				
				// prepareStatment 생성
				{
					String sql = KSqlUtil.removeForeachIndex(boundSql);
					sql = String.format(KSqlUtil.COUNT_SQL, sql);
					sql = KSqlUtil.insertSqlId(sql, TSqlType.COUNT_SQL1.code() + " " + sqlId);
					pstmt = connection.prepareStatement(sql);
					int startIndex = 1;
					KSqlUtil.bindParameterToPstmt(pstmt, parameterObject, boundSql, startIndex);
					sHandlerMetaObject.setValue("delegate.boundSql.sql", sql);
				}
				
				try {
					rs = pstmt.executeQuery();
					if (rs.next()) {
						count = rs.getInt(1);
					}
				} catch(Exception ex) {
					KSqlUtil.createParamSql(parameterObject, statementHandler, TSqlType.COUNT_SQL1);
					throw ex;
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			// (주의) 반드시 finally 에서 originSql 을 boundSql 에 set 할 것
			sHandlerMetaObject.setValue("delegate.boundSql.sql", originSql);
			
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
		return count;
	}
	
	public Integer countSql2(Invocation invocation) throws Exception {
		// count-sql 실행 준비
		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
		MetaObject sHandlerMetaObject = MetaObject.forObject(statementHandler, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
		PreparedStatementHandler preparedStatementHandler = (PreparedStatementHandler) proxyDelegate.get(statementHandler);
		MappedStatement mappedStatement = (MappedStatement) proxyMappedStatement.get(preparedStatementHandler);
		BoundSql boundSql = statementHandler.getBoundSql();
		Configuration configuration = mappedStatement.getConfiguration();
		String sqlId = mappedStatement.getId();
		String sqlFile = null;
		Object parameterObject = statementHandler.getParameterHandler().getParameterObject();
		
		// closable 객체
		Connection connection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		// 반환값 준비
		Integer count = null;
		
		// (주의) 반드시 finally 에서 originSql 을 boundSql 에 set 할 것
		String originSql = boundSql.getSql();
		
		try {
			// count-sql 존재여부 확인
			MappedStatement countMappedStatement = null;
			{
				Set<MappedStatement> mappedStatementList = KSqlContext.MAPPED_STATEMENT_LIST;
				Iterator<MappedStatement> iter = mappedStatementList.iterator();
				while (iter.hasNext()) {
					Object obj = iter.next();
					if (obj instanceof MappedStatement) {
						MappedStatement ms = (MappedStatement)obj;
						if (ms.getId().equals(sqlId+"-count")) {
							countMappedStatement = ms;
							break;
						}
					}
				}
				
				if (countMappedStatement == null) {
					return count; // count-sql 이 존재하지 않을 경우 `null` 을 반환
				}
			}
			
			// 로깅 준비
			{
				sqlId = countMappedStatement.getId();
				sqlFile = KSqlUtil.getRelativePath(countMappedStatement.getResource());
				
				KContext.set(AttrKey.SQL_ID, sqlId);
				KContext.set(AttrKey.SQL_FILE, sqlFile);
			}
			
			// count-sql 실행
			{
				try {
					// (주의) 반드시 finally 에서 originSql 을 boundSql 에 set 할 것
					String countSql = countMappedStatement.getBoundSql(parameterObject).getSql();
					
					// (주의) 반드시 finally 에서 mappedStatement 을 mappedStatement 에 set 할 것
					sHandlerMetaObject.setValue("delegate.mappedStatement", countMappedStatement);
					
					List<ParameterMapping> parameterMappings = countMappedStatement.getBoundSql(parameterObject).getParameterMappings();
					connection = countMappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
					boundSql = new BoundSql(configuration, countSql, parameterMappings, parameterObject);
					
					// prepareStatment 생성
					{
						String sql = KSqlUtil.removeForeachIndex(boundSql);
						sql = KSqlUtil.insertSqlId(sql, TSqlType.COUNT_SQL2.code() + " " + sqlId);
						pstmt = connection.prepareStatement(sql);
						int startIndex = 1;
						KSqlUtil.bindParameterToPstmt(pstmt, parameterObject, boundSql, startIndex);
						sHandlerMetaObject.setValue("delegate.boundSql.sql", sql);
					}
					
					rs = pstmt.executeQuery();
					if (rs.next()) {
						count = rs.getInt(1);
					}
				} catch(Exception ex) {
					throw ex;
				} finally {
					KSqlUtil.createParamSql(parameterObject, statementHandler, TSqlType.COUNT_SQL2);
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			// (주의) 반드시 finally 에서 originSql 을 boundSql 에 set 할 것
			sHandlerMetaObject.setValue("delegate.boundSql.sql", originSql);
			
			// (주의) 반드시 finally 에서 mappedStatement 을 mappedStatement 에 set 할 것
			sHandlerMetaObject.setValue("delegate.mappedStatement", mappedStatement);
			
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
		return count;
	}
}
