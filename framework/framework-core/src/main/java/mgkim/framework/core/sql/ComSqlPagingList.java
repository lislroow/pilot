package mgkim.framework.core.sql;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.type.IntegerTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import mgkim.framework.core.dto.KCmmVO;
import mgkim.framework.core.dto.KInPageVO;
import mgkim.framework.core.dto.KOutPageVO;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.type.KType.SqlType;
import mgkim.framework.core.util.KSqlUtil;
import mgkim.framework.core.util.KStringUtil;

public class ComSqlPagingList {
	
	private static final Logger log = LoggerFactory.getLogger(ComSqlPagingList.class);

	private ComSqlPagingCount comSqlPagingCount;
	
	protected Field proxyMappedStatement;
	protected Field proxyDelegate;
	
	private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
	private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
	private static final ReflectorFactory DEFAULT_REFLECTOR_FACTORY = new DefaultReflectorFactory();
	
	public ComSqlPagingList(Field proxyMappedStatement, Field proxyDelegate) {
		comSqlPagingCount = new ComSqlPagingCount(proxyMappedStatement, proxyDelegate);
		this.proxyMappedStatement = proxyMappedStatement;
		this.proxyDelegate = proxyDelegate;
	}
	
	public PreparedStatement preparePaging(Invocation invocation) throws Throwable {
		// ?????? ??????
		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
		MetaObject sHandlerMetaObject = MetaObject.forObject(statementHandler, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);
		PreparedStatementHandler pstmtHandler = (PreparedStatementHandler) proxyDelegate.get(statementHandler);
		MappedStatement mappedStatement = (MappedStatement) proxyMappedStatement.get(pstmtHandler);
		BoundSql boundSql = statementHandler.getBoundSql();
		String sqlId = mappedStatement.getId();
		String sqlFile = mappedStatement.getResource();
		sqlFile = sqlFile.substring(sqlFile.lastIndexOf(java.io.File.separator)+1, sqlFile.length()-1);
		KInPageVO inPageVO = KContext.getT(AttrKey.IN_PAGE);

		// closable ??????
		DataSource datasource = null;
		java.sql.Connection connection = null;
		PreparedStatement pstmt = null;
		
		// ?????? ??????
		double elapsedTime = -1;
		KContext.set(AttrKey.SQL_ID, sqlId);
		KContext.set(AttrKey.SQL_FILE, sqlFile);
		
		// count-sql ??????
		KOutPageVO outPageVO = null;
		{
			Integer totalRecordCount = null;
			StopWatch stopWatch = new StopWatch(sqlId+"-count-sql."+KContext.getT(AttrKey.TXID));
			stopWatch.start();
			try {
				// `countSql2`: `{sqlid}-count` sql ??? ????????? ???????????????.
				totalRecordCount = comSqlPagingCount.countSql2(invocation);
				if (totalRecordCount == null) {
					// `countSql1`: `?????? count-sql`??? ???????????????.
					totalRecordCount = comSqlPagingCount.countSql1(invocation);
				}
			} catch(Exception e) {
				throw e;
			} finally {
				stopWatch.stop();
				elapsedTime = stopWatch.getTotalTimeSeconds();
			}
			outPageVO = new KOutPageVO.Builder()
					.pageindex(inPageVO.getPageindex())
					.rowunit(inPageVO.getRowunit())
					.pageunit(inPageVO.getPageunit())
					.rowcount(totalRecordCount)
					.build();
			KContext.set(AttrKey.OUT_PAGE, outPageVO);
		}
		
		// paging-sql pstmt ??????
		try {
			datasource = mappedStatement.getConfiguration().getEnvironment().getDataSource();
			connection = org.springframework.jdbc.datasource.DataSourceUtils.getConnection(datasource);
			DefaultParameterHandler parameterHandler = (DefaultParameterHandler)statementHandler.getParameterHandler();
			Object parameterObject = parameterHandler.getParameterObject();
			ParameterMapping _parameter = null;
			TypeHandler _typeHandler = null;
			org.apache.ibatis.session.Configuration configuration = mappedStatement.getConfiguration();
			
			// prepareStatment ??????
			{
				String sql = KSqlUtil.removeForeachIndex(boundSql);
				sql = String.format(KSqlUtil.PAGING_SQL, sql);
				String comment = String.format("/* (%s) %s::%s */", KContext.getT(AttrKey.TXID), sqlFile, (SqlType.PAGING_SQL.code() + " " + sqlId));
				sql = KSqlUtil.insertSqlComment(sql, comment);
				sql = KStringUtil.replaceEmptyLine(sql);
				sHandlerMetaObject.setValue("delegate.boundSql.sql", sql);
				
				pstmt = connection.prepareStatement(sql);
				
				int parameterIndex = 1;
				try {
					List<ParameterMapping> list = new ArrayList<ParameterMapping>();
					// ????????? ???????????? binding (`_rowcount`)
					_parameter = new ParameterMapping.Builder(configuration, "_rowcount", new IntegerTypeHandler()).javaType(java.lang.Integer.class).jdbcType(JdbcType.INTEGER).build();
					list.add(_parameter);
					_typeHandler = _parameter.getTypeHandler();
					_typeHandler.setParameter(pstmt, parameterIndex++, outPageVO.getRowcount(), _parameter.getJdbcType());
					// -- ????????? ???????????? binding (`_rowcount`)
					
					// origin ???????????? binding
					int startIndex = parameterIndex;
					list.addAll(boundSql.getParameterMappings());
					parameterIndex = KSqlUtil.bindParameterToPstmt(pstmt, parameterObject, boundSql, startIndex);
					// -- origin ???????????? binding
					
					// ?????????, ????????? ???????????? binding (BETWEEN `_startrow` AND `_endrow`)
					_parameter = new ParameterMapping.Builder(configuration, "_startrow", new IntegerTypeHandler()).javaType(java.lang.Integer.class).jdbcType(JdbcType.INTEGER).build();
					list.add(_parameter);
					_typeHandler = _parameter.getTypeHandler();
					_typeHandler.setParameter(pstmt, parameterIndex++, outPageVO.getStartrow(), _parameter.getJdbcType());
					
					_parameter = new ParameterMapping.Builder(configuration, "_endrow", new IntegerTypeHandler()).javaType(java.lang.Integer.class).jdbcType(JdbcType.INTEGER).build();
					list.add(_parameter);
					_typeHandler = _parameter.getTypeHandler();
					_typeHandler.setParameter(pstmt, parameterIndex++, outPageVO.getEndrow(), _parameter.getJdbcType());
					// -- ?????????, ????????? ???????????? binding (BETWEEN `_startrow` AND `_endrow`)
					
					sHandlerMetaObject.setValue("delegate.boundSql.parameterMappings", list);
				} catch (Exception e) {
					throw e;
				}
			}
			// -- prepareStatment ??????
			
			if (KCmmVO.class.isInstance(parameterObject)) {
				KCmmVO vo = (KCmmVO) parameterObject;
				vo.set_rowcount(outPageVO.getRowcount());
				vo.set_startrow(outPageVO.getStartrow());
				vo.set_endrow(outPageVO.getEndrow());
			} else if (Map.class.isInstance(parameterObject)) {
				Map map = (Map) parameterObject;
				map.put("_rowcount", outPageVO.getRowcount());
				map.put("_startrow", outPageVO.getStartrow());
				map.put("_endrow", outPageVO.getEndrow());
			} else {
				throw new KSysException(KMessage.E8102, KCmmVO.class.getName());
			}
		} catch (Exception e) {
			if (connection != null) {
				if (!org.springframework.jdbc.datasource.DataSourceUtils.isConnectionTransactional(connection, datasource)) {
					connection.close();
				}
			}
			throw e;
		}
		return pstmt;
	}
}
