package mgkim.framework.core.type;

public enum TApiType {

	API("api"), OPENAPI("openapi"), INTERAPI("interapi"), PUBLIC("public"), API2("api-2"), API3("api-3"), UNKNOWN("unknown");

	private final String code;

	public String code() {
		return code;
	}

	private TApiType(String code) {
		this.code = code;
	}

	public static TApiType get(String code) {
		TApiType[] values = TApiType.values();
		for(TApiType item : values) {
			if(item.code().equals(code)) {
				return item;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return code;
	}
}
