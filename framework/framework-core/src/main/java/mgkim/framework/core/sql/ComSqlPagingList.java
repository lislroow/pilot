package mgkim.framework.core.sql;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.util.Map;

import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.type.IntegerTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.springframework.util.StopWatch;

import mgkim.framework.core.dto.KCmmVO;
import mgkim.framework.core.dto.KInPageVO;
import mgkim.framework.core.dto.KOutPageVO;
import mgkim.framework.core.env.KConfig;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
import mgkim.framework.core.logging.KLogApm;
import mgkim.framework.core.logging.KLogLayout;
import mgkim.framework.core.logging.KLogSql;
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
	
	public PreparedStatement preparePaging(Invocation invocation) throws Throwable {
		// 실행 준비
		StatementHandler sHandler = (StatementHandler) invocation.getTarget();
		PreparedStatementHandler pstmtHandler = (PreparedStatementHandler) proxyDelegate.get(sHandler);
		MappedStatement mappedStatement = (MappedStatement) proxyMappedStatement.get(pstmtHandler);
		BoundSql boundSql = sHandler.getBoundSql();
		String sqlId = mappedStatement.getId();
		String sqlFile = KSqlUtil.getRelativePath(mappedStatement.getResource());
		KInPageVO inPageVO = KContext.getT(AttrKey.IN_PAGE);
		// -- 실행 준비
		
		
		// 로깅 준비
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
			StopWatch stopWatch = new StopWatch(sqlId+"-count-sql."+KContext.getT(AttrKey.TXID));
			stopWatch.start();
			try {
				// `countSql2`: `{sqlid}-count` sql 을 찾아서 실행합니다.
				totalRecordCount = comSqlPagingCount.countSql2(invocation);
				if (totalRecordCount == null) {
					// `countSql1`: `공통 count-sql`을 실행합니다.
					totalRecordCount = comSqlPagingCount.countSql1(invocation);
				}
			} catch(Exception e) {
				throw e;
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
		String sql = null;
		PreparedStatement pstmt = null;
		java.sql.Connection connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
		DefaultParameterHandler parameterHandler = (DefaultParameterHandler)sHandler.getParameterHandler();
		Object parameterObject = parameterHandler.getParameterObject();
		ParameterMapping _parameter = null;
		TypeHandler _typeHandler = null;
		org.apache.ibatis.session.Configuration configuration = mappedStatement.getConfiguration();
		
		// prepareStatment 생성
		{
			sql = KSqlUtil.removeForeachIndex(boundSql);
			sql = String.format(KSqlUtil.PAGING_SQL, sql);
			sql = KSqlUtil.insertSqlId(sql, "(paging-sql) "+sqlId);
			pstmt = connection.prepareStatement(sql);
			
			int parameterIndex = 1;
			try {
				// 첫번째 파라미터 binding (`_rowcount`)
				_parameter = new ParameterMapping.Builder(configuration, "_rowcount", new IntegerTypeHandler()).javaType(java.lang.Integer.class).jdbcType(JdbcType.INTEGER).build();
				_typeHandler = _parameter.getTypeHandler();
				_typeHandler.setParameter(pstmt, parameterIndex++, outPageVO.getRowcount(), _parameter.getJdbcType());
				// -- 첫번째 파라미터 binding (`_rowcount`)
				
				// origin 파라미터 binding
				int startBindingIndex = parameterIndex;
				parameterIndex = KSqlUtil.bindParameterToPstmt(pstmt, parameterObject, boundSql, startBindingIndex);
				// -- origin 파라미터 binding
				
				// 두번째, 세번째 파라미터 binding (BETWEEN `_startrow` AND `_endrow`)
				_parameter = new ParameterMapping.Builder(configuration, "_startrow", new IntegerTypeHandler()).javaType(java.lang.Integer.class).jdbcType(JdbcType.INTEGER).build();
				_typeHandler = _parameter.getTypeHandler();
				_typeHandler.setParameter(pstmt, parameterIndex++, outPageVO.getStartrow(), _parameter.getJdbcType());
				
				_parameter = new ParameterMapping.Builder(configuration, "_endrow", new IntegerTypeHandler()).javaType(java.lang.Integer.class).jdbcType(JdbcType.INTEGER).build();
				_typeHandler = _parameter.getTypeHandler();
				_typeHandler.setParameter(pstmt, parameterIndex++, outPageVO.getEndrow(), _parameter.getJdbcType());
				// -- 두번째, 세번째 파라미터 binding (BETWEEN `_startrow` AND `_endrow`)
			} catch (Exception e) {
				throw e;
			}
		}
		// -- prepareStatment 생성
		
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
		return pstmt;
	}
}
