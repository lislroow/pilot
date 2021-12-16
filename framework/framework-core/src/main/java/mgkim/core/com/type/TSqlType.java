package mgkim.core.com.type;

public enum TSqlType {

	ORIGINAL_SQL("origin-sql"),
	PAGING_SQL("paging-sql"),
	COUNT1_SQL("count1-sql"),
	COUNT2_SQL("count2-sql"),
	COUNT3_SQL("count3-sql");

	private final String code;

	public String code() {
		return code;
	}

	private TSqlType(String code) {
		this.code = code;
	}
}
