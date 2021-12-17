package mgkim.framework.online.com.sql;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.util.StopWatch;

import mgkim.framework.online.com.dto.KCmmVO;
import mgkim.framework.online.com.dto.KOutPageVO;
import mgkim.framework.online.com.env.KConfig;
import mgkim.framework.online.com.env.KConstant;
import mgkim.framework.online.com.env.KContext;
import mgkim.framework.online.com.env.KProfile;
import mgkim.framework.online.com.env.KContext.AttrKey;
import mgkim.framework.online.com.exception.KMessage;
import mgkim.framework.online.com.exception.KSysException;
import mgkim.framework.online.com.logging.KLogApm;
import mgkim.framework.online.com.logging.KLogLayout;
import mgkim.framework.online.com.logging.KLogSql;
import mgkim.framework.online.com.type.TExecType;
import mgkim.framework.online.com.type.TSqlType;
import mgkim.framework.online.com.type.TSysType;
import mgkim.framework.online.com.util.KDtoUtil;
import mgkim.framework.online.com.util.KSqlUtil;
import mgkim.framework.online.com.util.KStringUtil;

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
		//BoundSql boundSql = statementHandler.getBoundSql();
		//Configuration configuration = mappedStatement.getConfiguration();
		String sqlId = mappedStatement.getId();
		//String orignalSql = boundSql.getSql();
		String sqlFile = KSqlUtil.getRelativePath(mappedStatement.getResource());
		Object paramObject = sHandler.getParameterHandler().getParameterObject();
		// -- sql 실행 준비

		// 반환값 준비
		Object resultObject = null;
		// -- 반환값 준비

		// 로깅 준비
		TExecType execType = KContext.getT(AttrKey.EXEC_TYPE);
		//boolean isDebugMode = KContext.isDebugMode();
		boolean isLoggableSql = KLogSql.isLoggableSql(sqlId);
		boolean isComSql = KLogSql.isComSql(sqlId);
		boolean isVerboss = KConfig.VERBOSS_ALL || KConfig.VERBOSS_SQL;
		{
			KContext.set(AttrKey.SQL_ID, sqlId);
			KContext.set(AttrKey.SQL_FILE, sqlFile);
		}
		// -- 로깅 준비

		// paging 처리 및 sql 로깅
		Connection connection = null;
		String paramSql = null;
		{
			switch(execType) {
			case REQUEST:
				// paramObject 공통 필드 설정
				{
					KDtoUtil.setSysValues(paramObject);
				} // -- paramObject 공통 필드 설정

				// paramObject 로깅
				{
					if(isLoggableSql) {
						if(!isVerboss) {
						} else {
							KLogSql.info("{} `{}` {}{} `{}` `{}` {}`paramObject` = {}", KConstant.LT_SQL_PARAM, sqlId, KLogLayout.LINE, KConstant.LT_SQL_PARAM, sqlFile, sqlId, KLogLayout.LINE, KStringUtil.toJson(paramObject));
						}
					}
				} // -- paramObject 로깅

				// paging 처리
				TSqlType sqlType = null;
				{
					if(!isComSql) { // com 패키지에 있는 sql 은 paging 처리 대상에서 제외함
						connection = comSqlPagingList.preparePaging(invocation);
					}

					boolean isPaging;
					if(connection == null) {  // `null` 이면 paging 처리 대상이 아님
						isPaging = false;
						sqlType = TSqlType.ORIGINAL_SQL;
					} else {
						isPaging = true;
						sqlType = TSqlType.PAGING_SQL;
					}

					// 파라미터 처리 후에는 반드시 아래 설정이 필요함
					if(isPaging) {
						KOutPageVO outPageVO = KContext.getT(AttrKey.OUT_PAGE);
						if(KCmmVO.class.isInstance(paramObject)) {
							KCmmVO vo = (KCmmVO) paramObject;
							vo.set_rowcount(outPageVO.getRowcount());
							vo.set_startrow(outPageVO.getStartrow());
							vo.set_endrow(outPageVO.getEndrow());
						} else {
							throw new KSysException(KMessage.E8102, KCmmVO.class.getName());
						}
					}
				}
				// -- paging 처리

				if(!isComSql || isLoggableSql) {
					paramSql = KSqlUtil.createParamSql(paramObject, mappedStatement, sqlType);
				}

				// -- original-sql 생성 및 로깅
				break;
			case SCHEDULE:
			case SYSTEM:
			default:
				// paramObject 로깅
				{
					if(isLoggableSql) {
						KLogSql.info("{} `{}` {}{} `{}` `{}` {}{}", KConstant.LT_SQL_PARAM, sqlId, KLogLayout.LINE, KConstant.LT_SQL_PARAM, sqlFile, sqlId, KLogLayout.LINE, KStringUtil.toJson(paramObject));
					}
				} // -- paramObject 로깅

				// original-sql 생성 및 로깅
				if(isLoggableSql) {
					paramSql = KSqlUtil.createParamSql(paramObject, mappedStatement, TSqlType.ORIGINAL_SQL);
				}
				// -- original-sql 생성 및 로깅
				break;
			}
		}
		// -- paging 처리 및 sql 로깅

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
					paramSql = KSqlUtil.createParamSql(paramObject, mappedStatement, TSqlType.ORIGINAL_SQL);
					break;
				}
				throw e;
			} finally {
				if(stopWatch != null) {
					stopWatch.stop();
					if(!isLoggableSql) {
						KLogApm.sql(stopWatch);
					}
					elapsedTime = stopWatch.getTotalTimeSeconds();
				}
				// paging 으로 처리되었을 경우에는 connection 이 null 이 아니며, 반드시 `connection.close()` 를 해야합니다.
				// paging 처리 과정에는 새로운 connection 객체를 얻고 preparedStatement 가 생성됩니다.
				if(invocation.getArgs().length > 0 && invocation.getArgs()[0] instanceof java.sql.PreparedStatement) {
					java.sql.PreparedStatement stmt = (java.sql.PreparedStatement) invocation.getArgs()[0];
					if(stmt != null) {
						stmt.close();
					}
				}
				if(connection != null) {
					connection.close();
				}
			}
		} // -- sql 실행


		// sql 결과 로깅
		{
			if(!isComSql || isLoggableSql) {
				if(resultObject instanceof List) {
					resultCount = ((List)resultObject).size();
				} else if(resultObject instanceof Integer) {
					resultCount = (Integer)resultObject;
				}

				switch(execType) {
				case REQUEST:
					if(!isVerboss) {
						KLogSql.info("{} `{}` {}{} `{}` `{}` {}`rows` = {},  `elapsed` = {} sec",                       KConstant.LT_SQL_RESULT, sqlId, KLogLayout.LINE, KConstant.LT_SQL_RESULT, sqlFile, sqlId, KLogLayout.LINE, resultCount, String.format("%.3f", elapsedTime));
					} else {
						KLogSql.info("{} `{}` {}{} `{}` `{}` {}`rows` = {},  `elapsed` = {} sec,{}`resultObject` = {}", KConstant.LT_SQL_RESULT, sqlId, KLogLayout.LINE, KConstant.LT_SQL_RESULT_VERBOSS, sqlFile, sqlId, KLogLayout.LINE, resultCount, String.format("%.3f", elapsedTime), KLogLayout.LINE, KStringUtil.toJson(resultObject));
					}
					break;
				case SCHEDULE:
				case SYSTEM:
					if(!isVerboss) {
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
				if(KProfile.SYS == TSysType.LOC && !KStringUtil.isEmpty(paramSql)) {
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
		if(target instanceof StatementHandler || target instanceof ResultSetHandler) {
			return Plugin.wrap(target, this);
		} else {
			return target;
		}
	}

	@Override
	public void setProperties(Properties properties) {
	}
}