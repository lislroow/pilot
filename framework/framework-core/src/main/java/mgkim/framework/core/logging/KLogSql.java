package mgkim.framework.core.logging;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mgkim.framework.core.env.KConfig;
import mgkim.framework.core.env.KContext;
import mgkim.framework.core.env.KContext.AttrKey;
import mgkim.framework.core.type.TExecType;

public class KLogSql {

	private static final Logger log = LoggerFactory.getLogger(KLogSql.class);

	public static boolean isCmmSql(String sqlId) {
		List<String> cmmSqlList = KConfig.CMM_SQL;
		boolean matched = false;
		for (String cmmSql : cmmSqlList) {
			matched = sqlId.startsWith(cmmSql);
			if (matched) {
				break;
			}
		}
		return matched;
	}

	public static boolean isLoggableSql(String sqlId) {
		TExecType execType = KContext.getT(AttrKey.EXEC_TYPE);
		boolean loggable = KContext.getT(AttrKey.LOGGABLE);
		switch(execType) {
		case REQUEST:
		case SYSTEM:
			boolean isComSql = isCmmSql(sqlId);
			if (!isComSql || KConfig.DEBUG_COM) {
				return true;
			}
			break;
		case SCHEDULE:
		default:
			return loggable;
		}
		return false;
	}
}
