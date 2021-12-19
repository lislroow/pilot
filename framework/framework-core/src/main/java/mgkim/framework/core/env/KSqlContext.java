package mgkim.framework.core.env;

import java.util.Iterator;
import java.util.Set;

import org.apache.ibatis.mapping.MappedStatement;

public class KSqlContext {
	
	public static Set<MappedStatement> MAPPED_STATEMENT_LIST = null;
	
	public static String getSqlFile(String sqlId) {
		MappedStatement mappedStatement = null;
		Iterator<MappedStatement> iter = MAPPED_STATEMENT_LIST.iterator();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (obj instanceof MappedStatement) {
				MappedStatement ms = (MappedStatement)obj;
				if (ms.getId().equals(sqlId)) {
					mappedStatement = ms;
					break;
				}
			}
		}
		
		if (mappedStatement == null) {
			return null;
		}
		
		return new java.io.File(mappedStatement.getResource().replaceAll("file \\[(.*)\\]", "$1")).getAbsolutePath()
			.replace(new java.io.File(KConstant.PATH_WEBINF_CLASSES).getAbsolutePath(), "")
			.replaceAll("\\\\", "/").substring(1);
	}
}
