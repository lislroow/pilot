package mgkim.framework.core.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import mgkim.framework.core.util.KStringUtil;

public class KLogMarker {
	
	public static final Marker error = MarkerFactory.getMarker("error");
	public static final Marker sql = MarkerFactory.getMarker("sql");
	public static final Marker sql_cmm = MarkerFactory.getMarker("sql-cmm");
	public static final Marker sql_table = MarkerFactory.getMarker("sql-table");
	public static final Marker request = MarkerFactory.getMarker("request");
	public static final Marker response = MarkerFactory.getMarker("response");
	public static final Marker security = MarkerFactory.getMarker("security");

	public static final Marker aop = MarkerFactory.getMarker("aop");
	
	static {
		sql_cmm.add(sql);
		sql_table.add(sql);
	}
	
	
	public static final Marker getSqlMarker(String sqlFile) {
		if (KStringUtil.nvl(sqlFile).startsWith("Cmm")) {
			return KLogMarker.sql_cmm;
		} else {
			return KLogMarker.sql;
		}
	}
}
