package mgkim.framework.core.sql;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import mgkim.framework.core.env.KConfig;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KSqlContext;
import mgkim.framework.core.logging.KLogSql;
import mgkim.framework.core.type.TSqlType;
import mgkim.framework.core.util.KObjectUtil;
import mgkim.framework.core.util.KSqlUtil;

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
		TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
		Object parameterObject = statementHandler.getParameterHandler().getParameterObject();

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
			if (isLoggableSql) {
				if (!isVerboss) {
				} else {
					KSqlUtil.createParamSql(parameterObject, mappedStatement, TSqlType.COUNT1_SQL);
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
				countSql = KSqlUtil.insertSqlId(countSql, "(count-sql1) "+sqlId);
				pstmt = connection.prepareStatement(countSql);
				List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
				ParameterMapping _parameter = null;
				TypeHandler _typeHandler = null;
				int parameterIndex = 1;
				
				// 실제 binding 파라미터 생성
				if (parameterMappings != null) {
					for (int i=0; i<parameterMappings.size(); i++) {
						_parameter = parameterMappings.get(i);
						if (_parameter.getMode() == ParameterMode.IN) {
							Object value;
							String propertyName = _parameter.getProperty();
							PropertyTokenizer prop = new PropertyTokenizer(propertyName);
							if (parameterObject == null) {
								value = null;
							} else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
								value = parameterObject;
							} else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX) && boundSql.hasAdditionalParameter(prop.getName())) {
								value = boundSql.getAdditionalParameter(prop.getName());
								//if (value == null) {
								//	value = configuration.newMetaObject(value).getValue(propertyName.substring(prop.getName().length()));
								//}
							} else if (parameterObject instanceof java.util.Map) {
								value = ((Map)parameterObject).get(propertyName);
							} else {
								value = KObjectUtil.getValue(parameterObject, propertyName);
							}
							_typeHandler = _parameter.getTypeHandler();
							try {
								_typeHandler.setParameter(pstmt, parameterIndex, value, _parameter.getJdbcType());
								parameterIndex++;
							} catch(Exception e) {
								throw e;
							}
						} else if (_parameter.getMode() == ParameterMode.OUT) {
							KLogSql.warn("statement 파라미터를 생성하는 중 ParameterMode.OUT 가 발견되었습니다.", _parameter.toString());
						}
					}
				}
				// -- 실제 binding 파라미터 생성
				
				rs = pstmt.executeQuery();
				if (rs.next()) {
					count = rs.getInt(1);
				}
			} catch(Exception ex) {
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
			if (isLoggableSql) {
				if (!isVerboss) {
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
				countSql = KSqlUtil.insertSqlId(countSql, "(count-sql2) "+sqlId);
				pstmt = connection.prepareStatement(countSql);
				MetaObject metaObject = SystemMetaObject.forObject(sHandler);
				ParameterHandler parameterHandler = (ParameterHandler) metaObject.getValue("delegate.parameterHandler");
				parameterHandler.setParameters(pstmt);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					count = rs.getInt(1);
				}
			} catch(Exception ex) {
				if (!isVerboss) {
					KSqlUtil.createParamSql(paramObject, mappedStatement, TSqlType.COUNT2_SQL);
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

	public Integer executeFindCountSql(Invocation invocation) throws Exception {
		// count-sql 실행 준비
		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
		PreparedStatementHandler preparedStatementHandler = (PreparedStatementHandler) proxyDelegate.get(statementHandler);
		MappedStatement mappedStatement = (MappedStatement) proxyMappedStatement.get(preparedStatementHandler);
		Configuration configuration = mappedStatement.getConfiguration();
		String sqlId = mappedStatement.getId();
		String sqlFile = KSqlUtil.getRelativePath(mappedStatement.getResource());
		TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
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
					KSqlUtil.createParamSql(parameterObject, mappedStatement, TSqlType.COUNT3_SQL);
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
				countSql = KSqlUtil.insertSqlId(countSql, "(count-sql3) "+sqlId);
				List<ParameterMapping> parameterMappings = mappedStatement.getBoundSql(parameterObject).getParameterMappings();
				connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
				pstmt = connection.prepareStatement(countSql);
				final BoundSql boundSql = new BoundSql(configuration, countSql, parameterMappings, parameterObject);
				ParameterMapping _parameter = null;
				TypeHandler _typeHandler = null;
				int parameterIndex = 1;
				
				// 실제 binding 파라미터 생성
				if (parameterMappings != null) {
					for (int i=0; i<parameterMappings.size(); i++) {
						_parameter = parameterMappings.get(i);
						if (_parameter.getMode() == ParameterMode.IN) {
							Object value;
							String propertyName = _parameter.getProperty();
							PropertyTokenizer prop = new PropertyTokenizer(propertyName);
							if (parameterObject == null) {
								value = null;
							} else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
								value = parameterObject;
							} else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX) && boundSql.hasAdditionalParameter(prop.getName())) {
								value = boundSql.getAdditionalParameter(prop.getName());
								//if (value == null) {
								//	value = configuration.newMetaObject(value).getValue(propertyName.substring(prop.getName().length()));
								//}
							} else if (parameterObject instanceof java.util.Map) {
								value = ((Map)parameterObject).get(propertyName);
							} else {
								value = KObjectUtil.getValue(parameterObject, propertyName);
							}
							_typeHandler = _parameter.getTypeHandler();
							try {
								_typeHandler.setParameter(pstmt, parameterIndex, value, _parameter.getJdbcType());
								parameterIndex++;
							} catch(Exception e) {
								throw e;
							}
						} else if (_parameter.getMode() == ParameterMode.OUT) {
							KLogSql.warn("statement 파라미터를 생성하는 중 ParameterMode.OUT 가 발견되었습니다.", _parameter.toString());
						}
					}
				}
				// -- 실제 binding 파라미터 생성
				
				rs = pstmt.executeQuery();
				if (rs.next()) {
					count = rs.getInt(1);
				}
			} catch(Exception ex) {
				if (!isVerboss) {
					KSqlUtil.createParamSql(parameterObject, mappedStatement, TSqlType.COUNT3_SQL);
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
