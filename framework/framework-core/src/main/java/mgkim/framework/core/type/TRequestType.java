package mgkim.framework.core.type;

public enum TRequestType {

	JSON("json"), FILE("file"), FORM_DATA("form-data");

	private final String code;

	public String code() {
		return code;
	}

	private TRequestType(String code) {
		this.code = code;
	}

	public static TRequestType get(String code) {
		TRequestType[] values = TRequestType.values();
		for (TRequestType item : values) {
			if (item.code().equals(code)) {
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
