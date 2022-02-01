package mgkim.framework.core.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.util.KSqlUtil;

public class KLogElastic {

	private static final Logger SYS_RESPONSE = LoggerFactory.getLogger("sys-response");
	private static final Logger SYS_SQL = LoggerFactory.getLogger("sys-sql");
	
	public static void responseLog(String resultCode, String resultMessage) {
		KLogMDC.put(AttrKey.RESULT_CODE, resultCode);
		KLogMDC.put(AttrKey.RESULT_MESSAGE, resultMessage);
		SYS_RESPONSE.info("[{}] {}", resultCode, resultMessage);
		KLogMDC.remove(AttrKey.RESULT_CODE);
		KLogMDC.remove(AttrKey.RESULT_MESSAGE);
	}
	
	public static void sqlLog(String sqlId, String sqlFile, String sqlText, int sqlCount, String sqlElapsed) {
		MDC.put("sqlId", sqlId);
		MDC.put("sqlFile", sqlFile);
		MDC.put("sqlText", sqlText);
		MDC.put("sqlCount", sqlCount+"");
		MDC.put("sqlElapsed", sqlElapsed);
		MDC.put("sqlTables", KSqlUtil.resolveTables(sqlText));
		SYS_SQL.info("");
		MDC.remove("sqlId");
		MDC.remove("sqlFile");
		MDC.remove("sqlText");
		MDC.remove("sqlCount");
		MDC.remove("sqlElapsed");
		MDC.remove("sqlTables");
	}
}
