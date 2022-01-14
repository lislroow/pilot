package mgkim.framework.core.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import mgkim.framework.core.util.KStringUtil;

public class KLogMarker {
	
	public static final Marker REQUEST = MarkerFactory.getMarker("[REQ]");
	public static final Marker RESPONSE = MarkerFactory.getMarker("[RES]");
	public static final Marker SQL = MarkerFactory.getMarker("[sql]");
	public static final Marker SQL_CMM = MarkerFactory.getMarker("[sql-cmm]");
	public static final Marker request = MarkerFactory.getMarker("[req]");
	public static final Marker response = MarkerFactory.getMarker("[res]");
	public static final Marker security = MarkerFactory.getMarker("[sec]");
	public static final Marker common = MarkerFactory.getMarker("[com]");

	public static final Marker getSqlMarker(String sqlFile) {
		if (KStringUtil.nvl(sqlFile).startsWith("Cmm")) {
			return KLogMarker.SQL_CMM;
		} else {
			return KLogMarker.SQL;
		}
	}
}
