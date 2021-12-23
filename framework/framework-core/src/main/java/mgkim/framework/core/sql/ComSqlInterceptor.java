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

import mgkim.framework.core.dto.KCmmVO;
import mgkim.framework.core.dto.KInPageVO;
import mgkim.framework.core.dto.KOutPageVO;
import mgkim.framework.core.env.KConfig;
import mgkim.framework.core.env.KConstant;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.env.KProfile;
import mgkim.framework.core.exception.KMessage;
import mgkim.framework.core.exception.KSysException;
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
		String originSql = boundSql.getSql();
		String sqlFile = KSqlUtil.getRelativePath(mappedStatement.getResource());
		Object parameterObject = sHandler.getParameterHandler().getParameterObject();
		// -- sql 실행 준비

		// 반환값 준비
		Object resultObject = null;
		// -- 반환값 준비

		// 로깅 준비
		TExecType execType = KContext.getT(AttrKey.EXEC_TYPE);
		//boolean isDebugMode = KContext.isDebugMode();
		boolean isLoggableSql = KLogSql.isLoggableSql(sqlId);
		boolean isComSql = KLogSql.isCmmSql(sqlId);
		boolean isVerboss = KConfig.VERBOSS_ALL || KConfig.VERBOSS_SQL;
		{
			KContext.set(AttrKey.SQL_ID, sqlId);
			KContext.set(AttrKey.SQL_FILE, sqlFile);
		}
		// -- 로깅 준비

		// paging 처리 및 sql 로깅
		Connection connection = null;
		String paramSql = null;
		boolean isPaging = false;
		{
			switch(execType) {
			case REQUEST:
				// parameterObject 공통 필드 설정
				{
					KDtoUtil.setSysValues(parameterObject);
				} // -- parameterObject 공통 필드 설정
				
				// parameterObject 로깅
				{
					if (isLoggableSql) {
						if (!isVerboss) {
						} else {
							KLogSql.info("{} `{}` {}{} `{}` `{}` {}`parameterObject` = {}", KConstant.LT_SQL_PARAM, sqlId, KLogLayout.LINE, KConstant.LT_SQL_PARAM, sqlFile, sqlId, KLogLayout.LINE, KStringUtil.toJson(parameterObject));
						}
					}
				} // -- parameterObject 로깅
				
				// paging 여부 확인
				TSqlType sqlType = null;
				{
					KInPageVO inPageVO = KContext.getT(AttrKey.IN_PAGE);
					if (inPageVO == null || isComSql) { // // com 패키지에 있는 sql 은 paging 처리 대상에서 제외함
						isPaging = false;
					} else {
						boolean isSelectType = mappedStatement.getSqlCommandType() == SqlCommandType.SELECT;
						boolean isPagingYN = inPageVO.getPaging();
						// sqlId 로 페이징 여부 체크
						//boolean isSelectSqlId = sqlId.matches("^.+\\.select.+(List)$");
						//isPaging = isSelectSqlId && isSelectType && isPagingYN;
						isPaging = isSelectType && isPagingYN;
					}
					
					if (isPaging) {
						sqlType = TSqlType.PAGING_SQL;
					} else {
						sqlType = TSqlType.ORIGIN_SQL;
					}
				} // -- paging 여부 확인
				
				
				// paging 처리
				{
					//Connection pagingConnection = null; 
					if (isPaging) {
						PreparedStatement pstmt = comSqlPagingList.preparePaging(invocation);
						invocation.getArgs()[0] = pstmt;
					}
					
					// 파라미터 처리 후에는 반드시 아래 설정이 필요함
					if (isPaging) {
						KOutPageVO outPageVO = KContext.getT(AttrKey.OUT_PAGE);
						if (KCmmVO.class.isInstance(parameterObject)) {
							KCmmVO vo = (KCmmVO) parameterObject;
							vo.set_rowcount(outPageVO.getRowcount());
							vo.set_startrow(outPageVO.getStartrow());
							vo.set_endrow(outPageVO.getEndrow());
						} else {
							throw new KSysException(KMessage.E8102, KCmmVO.class.getName());
						}
					}
				}
				// -- paging 처리

				if (!isComSql || isLoggableSql) {
					paramSql = KSqlUtil.createParamSql(parameterObject, mappedStatement, sqlType);
				}

				// -- origin-sql 생성 및 로깅
				break;
			case SCHEDULE:
			case SYSTEM:
			default:
				// parameterObject 로깅
				{
					if (isLoggableSql) {
						KLogSql.info("{} `{}` {}{} `{}` `{}` {}{}", KConstant.LT_SQL_PARAM, sqlId, KLogLayout.LINE, KConstant.LT_SQL_PARAM, sqlFile, sqlId, KLogLayout.LINE, KStringUtil.toJson(parameterObject));
					}
				} // -- parameterObject 로깅

				// origin-sql 생성 및 로깅
				if (isLoggableSql) {
					paramSql = KSqlUtil.createParamSql(parameterObject, mappedStatement, TSqlType.ORIGIN_SQL);
				}
				// -- origin-sql 생성 및 로깅
				break;
			}
		}
		// -- paging 처리 및 sql 로깅
		
		
		// (페이징이 아닐 경우) 새로운 orignal-sql로 `boundSql` 교체
		{
			if (!isPaging) {
				connection = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
				PreparedStatement pstmt = null;
				//DefaultParameterHandler parameterHandler = (DefaultParameterHandler)sHandler.getParameterHandler();
				//TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
				//List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
				//TypeHandler _typeHandler = null;
				
				// mybatis foreach 문
				{
					originSql = KSqlUtil.removeForeachIndex(boundSql);
					originSql = KSqlUtil.insertSqlId(originSql, sqlId);
					pstmt = connection.prepareStatement(originSql);
				}
				// -- mybatis foreach 문
				
				// origin 파라미터 binding
				int startBindingIndex = 1;
				KSqlUtil.bindParameterToPstmt(pstmt, parameterObject, boundSql, startBindingIndex);
				// -- origin 파라미터 binding
				
				invocation.getArgs()[0] = pstmt;
			}
		}
		
		// sql 실행
		int resultCount = -1;
		double elapsedTime = -1;
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
				// paging 으로 처리되었을 경우에는 connection 이 null 이 아니며, 반드시 `connection.close()` 를 해야합니다.
				// paging 처리 과정에는 새로운 connection 객체를 얻고 preparedStatement 가 생성됩니다.
				if (invocation.getArgs().length > 0 && invocation.getArgs()[0] instanceof java.sql.PreparedStatement) {
					java.sql.PreparedStatement stmt = (java.sql.PreparedStatement) invocation.getArgs()[0];
					if (stmt != null) {
						stmt.close();
					}
				}
				if (connection != null) {
					connection.close();
				}
			}
		} // -- sql 실행
		
		
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
		} // -- sql 결과 로깅


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
		} // -- sql 실행 테이블 분석
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