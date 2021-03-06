package mgkim.framework.core.sql;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KSqlContext;
import mgkim.framework.core.logging.KLogMarker;
import mgkim.framework.core.type.KType.SqlType;
import mgkim.framework.core.util.KSqlUtil;

public class ComSqlPagingCount {
	
	private static final Logger log = LoggerFactory.getLogger(ComSqlPagingCount.class);

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
		// count-sql ?????? ??????
		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
		MetaObject sHandlerMetaObject = MetaObject.forObject(statementHandler, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
		PreparedStatementHandler pstmtHandler = (PreparedStatementHandler) proxyDelegate.get(statementHandler);
		MappedStatement mappedStatement = (MappedStatement) proxyMappedStatement.get(pstmtHandler);
		BoundSql boundSql = statementHandler.getBoundSql();
		String sqlId = mappedStatement.getId();
		String sqlFile = mappedStatement.getResource();
		sqlFile = sqlFile.substring(sqlFile.lastIndexOf(java.io.File.separator)+1, sqlFile.length()-1);
		Object parameterObject = statementHandler.getParameterHandler().getParameterObject();
		
		// closable ??????
		DataSource datasource = null;
		Connection connection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		// ????????? ??????
		Integer count = null;
		
		// (??????) ????????? finally ?????? originSql ??? boundSql ??? set ??? ???
		String originSql = boundSql.getSql();
		
		try {
			// count-sql ??????
			{
				datasource = mappedStatement.getConfiguration().getEnvironment().getDataSource();
				connection = org.springframework.jdbc.datasource.DataSourceUtils.getConnection(datasource);
				// prepareStatment ??????
				{
					String sql = KSqlUtil.removeForeachIndex(boundSql);
					sql = String.format(KSqlUtil.COUNT_SQL, sql);
					String comment = String.format("/* (%s) %s::%s */", KContext.getT(AttrKey.TXID), sqlFile, (SqlType.COUNT_SQL1.code() + " " + sqlId));
					sql = KSqlUtil.insertSqlComment(sql, comment);
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
					String countSql = KSqlUtil.createParamSql(parameterObject, statementHandler, SqlType.COUNT_SQL1);
					log.error("{}", countSql);
					throw ex;
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			// (??????) ????????? finally ?????? originSql ??? boundSql ??? set ??? ???
			sHandlerMetaObject.setValue("delegate.boundSql.sql", originSql);
			
			if (connection != null) {
				if (!org.springframework.jdbc.datasource.DataSourceUtils.isConnectionTransactional(connection, datasource)) {
					connection.close();
				}
			}
		}
		return count;
	}
	
	public Integer countSql2(Invocation invocation) throws Exception {
		// count-sql ?????? ??????
		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
		MetaObject sHandlerMetaObject = MetaObject.forObject(statementHandler, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
		PreparedStatementHandler preparedStatementHandler = (PreparedStatementHandler) proxyDelegate.get(statementHandler);
		MappedStatement mappedStatement = (MappedStatement) proxyMappedStatement.get(preparedStatementHandler);
		BoundSql boundSql = statementHandler.getBoundSql();
		Configuration configuration = mappedStatement.getConfiguration();
		String sqlId = mappedStatement.getId();
		String sqlFile = null;
		Object parameterObject = statementHandler.getParameterHandler().getParameterObject();
		
		// closable ??????
		DataSource datasource = null;
		Connection connection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		// ????????? ??????
		Integer count = null;
		
		// (??????) ????????? finally ?????? originSql ??? boundSql ??? set ??? ???
		String originSql = boundSql.getSql();
		
		try {
			// count-sql ???????????? ??????
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
					return count; // count-sql ??? ???????????? ?????? ?????? `null` ??? ??????
				}
			}
			
			// ?????? ??????
			{
				sqlId = countMappedStatement.getId();
				sqlFile = countMappedStatement.getResource();
				sqlFile = sqlFile.substring(sqlFile.lastIndexOf(java.io.File.separator)+1, sqlFile.length()-1);
				
				KContext.set(AttrKey.SQL_ID, sqlId);
				KContext.set(AttrKey.SQL_FILE, sqlFile);
			}
			
			// count-sql ??????
			{
				try {
					// (??????) ????????? finally ?????? originSql ??? boundSql ??? set ??? ???
					String countSql = countMappedStatement.getBoundSql(parameterObject).getSql();
					
					// (??????) ????????? finally ?????? mappedStatement ??? mappedStatement ??? set ??? ???
					sHandlerMetaObject.setValue("delegate.mappedStatement", countMappedStatement);
					
					List<ParameterMapping> parameterMappings = countMappedStatement.getBoundSql(parameterObject).getParameterMappings();
					datasource = mappedStatement.getConfiguration().getEnvironment().getDataSource();
					connection = org.springframework.jdbc.datasource.DataSourceUtils.getConnection(datasource);
					boundSql = new BoundSql(configuration, countSql, parameterMappings, parameterObject);
					
					// prepareStatment ??????
					{
						String sql = KSqlUtil.removeForeachIndex(boundSql);
						String comment = String.format("/* (%s) %s::%s */", KContext.getT(AttrKey.TXID), sqlFile, (SqlType.COUNT_SQL2.code() + " " + sqlId));
						sql = KSqlUtil.insertSqlComment(sql, comment);
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
					String countSql = KSqlUtil.createParamSql(parameterObject, statementHandler, SqlType.COUNT_SQL2);
					log.error(KLogMarker.getSqlMarker(sqlFile), "\n{}", countSql);
					throw ex;
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			// (??????) ????????? finally ?????? originSql ??? boundSql ??? set ??? ???
			sHandlerMetaObject.setValue("delegate.boundSql.sql", originSql);
			
			// (??????) ????????? finally ?????? mappedStatement ??? mappedStatement ??? set ??? ???
			sHandlerMetaObject.setValue("delegate.mappedStatement", mappedStatement);
			
			if (connection != null) {
				if (!org.springframework.jdbc.datasource.DataSourceUtils.isConnectionTransactional(connection, datasource)) {
					connection.close();
				}
			}
		}
		return count;
	}
}
