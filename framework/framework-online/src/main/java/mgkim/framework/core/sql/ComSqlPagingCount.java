package mgkim.framework.core.sql;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import mgkim.framework.core.env.KConfig;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.logging.KLogSql;
import mgkim.framework.core.type.TSqlType;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.core.util.KSqlUtil;
import mgkim.framework.online.com.scheduler.ComSqlmapReloadScheduler;

public class ComSqlPagingCount {

	protected Field proxyMappedStatement;
	protected Field proxyDelegate;

	public ComSqlPagingCount(Field proxyMappedStatement, Field proxyDelegate) {
		this.proxyMappedStatement = proxyMappedStatement;
		this.proxyDelegate = proxyDelegate;
	}


	public Integer executeRemoveOrderBy(Invocation invocation) throws Exception {
		//Object resultObj = null;
		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
		PreparedStatementHandler preparedStatementHandler = (PreparedStatementHandler) proxyDelegate.get(statementHandler);
		MappedStatement mappedStatement = (MappedStatement) proxyMappedStatement.get(preparedStatementHandler);
		BoundSql boundSql = statementHandler.getBoundSql();
		Configuration configuration = mappedStatement.getConfiguration();
		String sqlId = mappedStatement.getId();
		String orignalSql = boundSql.getSql();
		//String sqlFile = KSqlUtil.getRelativePath(mappedStatement);
		Object paramObject = statementHandler.getParameterHandler().getParameterObject();

		Integer count = null;

		String countSql = String.format(KSqlUtil.COUNT_SQL, KSqlUtil.removeOrderBy(orignalSql));

		// 로깅 준비
		//boolean isDebugMode = KContext.isDebugMode();
		boolean isLoggableSql = KLogSql.isLoggableSql(sqlId);
		boolean isVerboss = KConfig.VERBOSS_ALL || KConfig.VERBOSS_SQL;
		{

		}
		// -- 로깅 준비

		// count-sql 로깅
		{
			if(isLoggableSql) {
				if(!isVerboss) {
				} else {
					KSqlUtil.createParamSql(paramObject, mappedStatement, TSqlType.COUNT1_SQL);
				}
			}
		} // -- count-sql 로깅

		// count-sql 실행
		{
			Connection connection = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try {
				connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
				pstmt = connection.prepareStatement(countSql);
				final BoundSql countBoundSql = new BoundSql(mappedStatement.getConfiguration(), countSql, boundSql.getParameterMappings(), paramObject);
				List<ParameterMapping> parameterMappings = countBoundSql.getParameterMappings();
				if(parameterMappings != null) {
					TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
					// 2021-08-06 MetaObject metaObject = parameterObj == null ? null : configuration.newMetaObject(parameterObj);
					for(int i=0; i<parameterMappings.size(); i++) {
						ParameterMapping parameterMapping = parameterMappings.get(i);
						if(parameterMapping.getMode() != ParameterMode.OUT) {
							Object value = null;
							String propertyName = parameterMapping.getProperty();
							PropertyTokenizer prop = new PropertyTokenizer(propertyName);
							if(paramObject == null) {
								value = null;
							} else if(typeHandlerRegistry.hasTypeHandler(paramObject.getClass())) {
								value = paramObject;
							} else if(boundSql.hasAdditionalParameter(propertyName)) {
								value = boundSql.getAdditionalParameter(propertyName);
							} else if(propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX) && boundSql.hasAdditionalParameter(prop.getName())) {
								value = boundSql.getAdditionalParameter(prop.getName());
								if(value != null) {
									value = configuration.newMetaObject(value).getValue(propertyName.substring(prop.getName().length()));
								}
							} else {
								value = KObjectUtil.getValue(paramObject, propertyName);
							}
							TypeHandler typeHandler = parameterMapping.getTypeHandler();
							JdbcType jdbcType = parameterMapping.getJdbcType();
							if(value == null && jdbcType == null) {
								jdbcType = configuration.getJdbcTypeForNull();
							}
							typeHandler.setParameter(pstmt, i+1, value, jdbcType);
						}
					}
				}
				rs = pstmt.executeQuery();
				if(rs.next()) {
					count = rs.getInt(1);
				}
			} catch(Exception ex) {
				throw ex;
			} finally {
				if(rs != null) {
					rs.close();
				}
				if(pstmt != null) {
					pstmt.close();
				}
				if(connection != null) {
					connection.close();
				}
			}
		}
		// -- count-sql 실행

		return count;
	}


	public Integer executeNoRemoveOrderBy(Invocation invocation) throws Exception {
		// count-sql 실행 준비
		StatementHandler sHandler = (StatementHandler) invocation.getTarget();
		PreparedStatementHandler pstmtHandler = (PreparedStatementHandler) proxyDelegate.get(sHandler);
		MappedStatement mappedStatement = (MappedStatement) proxyMappedStatement.get(pstmtHandler);
		BoundSql boundSql = sHandler.getBoundSql();
		//Configuration configuration = mappedStatement.getConfiguration();
		String sqlId = mappedStatement.getId();
		String orignalSql = boundSql.getSql();
		//String sqlFile = KSqlUtil.getRelativePath(mappedStatement);
		Object paramObject = sHandler.getParameterHandler().getParameterObject();
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
			if(isLoggableSql) {
				if(!isVerboss) {
				} else {
					KSqlUtil.createParamSql(paramObject, mappedStatement, TSqlType.COUNT2_SQL);
				}
			}
		} // -- count-sql 로깅

		// count-sql 실행
		{
			Connection connection = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try {
				connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
				String countSql = String.format(KSqlUtil.COUNT_SQL, orignalSql);
				pstmt = connection.prepareStatement(countSql);
				MetaObject metaObject = SystemMetaObject.forObject(sHandler);
				ParameterHandler parameterHandler = (ParameterHandler) metaObject.getValue("delegate.parameterHandler");
				parameterHandler.setParameters(pstmt);
				rs = pstmt.executeQuery();
				if(rs.next()) {
					count = rs.getInt(1);
				}
			} catch(Exception ex) {
				if(!isVerboss) {
					KSqlUtil.createParamSql(paramObject, mappedStatement, TSqlType.COUNT2_SQL);
				} else {
				}
				throw ex;
			} finally {
				if(rs != null) {
					rs.close();
				}
				if(pstmt != null) {
					pstmt.close();
				}
				if(connection != null) {
					connection.close();
				}
			}
		}
		// -- count-sql 실행

		return count;
	}

	public Integer executeFindCountSql(Invocation invocation) throws Exception {
		// count-sql 실행 준비
		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
		PreparedStatementHandler preparedStatementHandler = (PreparedStatementHandler) proxyDelegate.get(statementHandler);
		MappedStatement mappedStatement = (MappedStatement) proxyMappedStatement.get(preparedStatementHandler);
		//BoundSql boundSql = statementHandler.getBoundSql();
		Configuration configuration = mappedStatement.getConfiguration();
		String sqlId = mappedStatement.getId();
		//String orignalSql = boundSql.getSql();
		String sqlFile = KSqlUtil.getRelativePath(mappedStatement.getResource());
		Object paramObject = statementHandler.getParameterHandler().getParameterObject();
		// -- count-sql 실행 준비

		// 반환값 준비
		Integer count = null;
		// -- 반환값 준비

		// count-sql 존재여부 확인
		MappedStatement countMappedStatement = null;
		{
			Set<MappedStatement> mappedStatementList = ComSqlmapReloadScheduler.getMappedStatements();
			Iterator<MappedStatement> iter = mappedStatementList.iterator();
			while(iter.hasNext()) {
				Object obj = iter.next();
				if(obj instanceof MappedStatement) {
					MappedStatement ms = (MappedStatement)obj;
					if(ms.getId().equals(sqlId+"-count")) {
						countMappedStatement = ms;
						break;
					}
				}
			}

			if(countMappedStatement == null) {
				return count; // count-sql 이 존재하지 않을 경우 `null` 을 반환
			}
		}
		// -- count-sql 존재여부 확인

		// 로깅 준비
		//boolean isDebugMode = KContext.isDebugMode();
		boolean isLoggableSql = KLogSql.isLoggableSql(sqlId);
		boolean isVerboss = KConfig.VERBOSS_ALL || KConfig.VERBOSS_SQL;
		{
			sqlId = countMappedStatement.getId();
			sqlFile = KSqlUtil.getRelativePath(countMappedStatement.getResource());

			KContext.set(AttrKey.SQL_ID, sqlId);
			KContext.set(AttrKey.SQL_FILE, sqlFile);
		}
		// -- 로깅 준비

		// count-sql 로깅
		{
			if(isLoggableSql) {
				if(!isVerboss) {
				} else {
					KSqlUtil.createParamSql(paramObject, countMappedStatement, TSqlType.COUNT3_SQL);
				}
			}
		} // -- count-sql 로깅


		// count-sql 실행
		{
			Connection connection = null;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try {
				BoundSql countBoundSql = countMappedStatement.getBoundSql(paramObject);
				String countSql = countBoundSql.getSql();
				List<ParameterMapping> parameterMappings = countMappedStatement.getBoundSql(paramObject).getParameterMappings();
				connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
				pstmt = connection.prepareStatement(countSql);
				final BoundSql newBoundSql = new BoundSql(configuration, countSql, parameterMappings, paramObject);
				if(parameterMappings != null) {
					TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
					//MetaObject metaObject = paramObject == null ? null : configuration.newMetaObject(paramObject);
					for(int i=0; i<parameterMappings.size(); i++) {
						ParameterMapping parameterMapping = parameterMappings.get(i);
						if(parameterMapping.getMode() != ParameterMode.OUT) {
							Object value = null;
							String propertyName = parameterMapping.getProperty();
							PropertyTokenizer prop = new PropertyTokenizer(propertyName);
							if(paramObject == null) {
								value = null;
							} else if(typeHandlerRegistry.hasTypeHandler(paramObject.getClass())) {
								value = paramObject;
							} else if(newBoundSql.hasAdditionalParameter(propertyName)) {
								value = newBoundSql.getAdditionalParameter(propertyName);
							} else if(propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX) && newBoundSql.hasAdditionalParameter(prop.getName())) {
								value = newBoundSql.getAdditionalParameter(prop.getName());
								if(value != null) {
									value = configuration.newMetaObject(value).getValue(propertyName.substring(prop.getName().length()));
								}
							} else {
								value = KObjectUtil.getValue(paramObject, propertyName);
							}
							TypeHandler typeHandler = parameterMapping.getTypeHandler();
							JdbcType jdbcType = parameterMapping.getJdbcType();
							if(value == null && jdbcType == null) {
								jdbcType = configuration.getJdbcTypeForNull();
							}
							typeHandler.setParameter(pstmt, i+1, value, jdbcType);
						}
					}
				}
				rs = pstmt.executeQuery();
				if(rs.next()) {
					count = rs.getInt(1);
				}
			} catch(Exception ex) {
				if(!isVerboss) {
					KSqlUtil.createParamSql(paramObject, countMappedStatement, TSqlType.COUNT3_SQL);
				} else {
				}
				throw ex;
			} finally {
				if(rs != null) {
					rs.close();
				}
				if(pstmt != null) {
					pstmt.close();
				}
				if(connection != null) {
					connection.close();
				}
			}
		}
		// -- count-sql 실행

		return count;
	}


}
