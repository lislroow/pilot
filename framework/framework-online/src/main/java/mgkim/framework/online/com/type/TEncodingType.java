package mgkim.framework.online.com.type;

public enum TEncodingType {
	EUCKR("euc-kr"), UTF8("utf-8"), ISO88591("iso-8859-1");

	private final String code;

	public String code() {
		return code;
	}

	private TEncodingType(String code) {
		this.code = code;
	}
}