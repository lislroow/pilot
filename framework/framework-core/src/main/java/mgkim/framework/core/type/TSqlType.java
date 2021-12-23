package mgkim.framework.core.type;

public enum TSqlType {

	ORIGIN_SQL("origin-sql"),
	PAGING_SQL("paging-sql"),
	COUNT_SQL1("count-sql1"),
	COUNT_SQL2("count-sql2"),
	;

	private final String code;

	public String code() {
		return code;
	}

	private TSqlType(String code) {
		this.code = code;
	}
}
